/**
 * @author sliva
 */
package compiler.phases.ralloc;

import java.util.*;
import compiler.common.report.*;
import compiler.data.layout.*;
import compiler.data.asmcode.*;
import compiler.phases.*;
import compiler.phases.livean.*;
import compiler.phases.asmcode.*;

class Pair implements Comparable<Pair> {
	int f;
	int s;

	public Pair(int x, int y) {
		f = x;
		s = y;
	}

	@Override
	public int compareTo(Pair x) {
		if (f > x.f) return 1;
		if (f < x.f) return -1;
		if (s > x.s) return 1;
		if (s < x.s) return -1;
		return 0;
	}
}

/**
 * Register allocation phase.
 * 
 * @author sliva
 */
public class RAlloc extends Phase {

	int it = 0;
	long tempSize = 0;
	Vector<Integer> deg = new Vector<Integer>();
	Vector<Boolean> elim = new Vector<Boolean>();
	PriorityQueue<Pair> pq = new PriorityQueue<Pair>();
	TreeMap<Temp, Integer> mapTemps = new TreeMap<Temp, Integer>();
	TreeMap<Integer, Temp> invMapTemps = new TreeMap<Integer, Temp>();
	TreeMap<Integer, Boolean> edgeMap = new TreeMap<Integer, Boolean>();

	TreeSet<Integer> spill = new TreeSet<Integer>();
	Vector<Integer> actSpill = new Vector<Integer>();
	Stack<Integer> stack = new Stack<Integer>();
	Vector<Vector<Integer>> graph = new Vector<Vector<Integer>>();

	Vector<Integer> color = new Vector<Integer>();


	public int numOfRegs = 8;

	public RAlloc() {
		super("ralloc");
	}

	public void add(Temp x) {
		if (mapTemps.containsKey(x)) return;

		mapTemps.put(x, it);
		invMapTemps.put(it++, x);
	}

	public void add(int x, int y) {
		if (x == y || edgeMap.containsKey(x * it + y)) return;

		edgeMap.put(x * it + y, true);
		graph.get(x).add(y);
		add(y, x);
	}

	public void init(Code code) {
		it = 0;
		tempSize = 0;
		deg.clear();
		elim.clear();
		mapTemps.clear();
		invMapTemps.clear();
		edgeMap.clear();
		spill.clear();
		actSpill.clear();
		stack.clear();
		graph.clear();
		color.clear();

		LiveAn livean = new LiveAn();
		livean.chunkLiveness(code);

		for (int i = 0; i < code.instrs.size(); i++) {
			for (Temp x : code.instrs.get(i).uses()) add(x);
			for (Temp x : code.instrs.get(i).defs()) add(x);
			for (Temp x : code.instrs.get(i).in()) add(x);
			for (Temp x : code.instrs.get(i).out()) add(x);
		}

		for (int i = 0; i < it; i++) deg.add(0);
		for (int i = 0; i < it; i++) elim.add(false);
		for (int i = 0; i < it; i++) graph.add(new Vector<Integer>());
		for (int i = 0; i < it; i++) color.add(-1);
	}

	public void build(Code code) {
		for (int i = 0; i < code.instrs.size(); i++) {
			for (Temp x : code.instrs.get(i).in())
				for (Temp y : code.instrs.get(i).in())
					add(mapTemps.get(x), mapTemps.get(y));

			for (Temp x : code.instrs.get(i).out())
				for (Temp y : code.instrs.get(i).out())
					add(mapTemps.get(x), mapTemps.get(y));
		}

		for (int i = 0; i < it; i++) deg.set(i, graph.get(i).size());
		for (int i = 0; i < it; i++) pq.add(new Pair(deg.get(i), i));
	}

	boolean simplify() {
		while (!pq.isEmpty()) {
			Pair p = pq.poll();
			if (p.f != deg.get(p.s) || elim.get(p.s)) continue;
			if (p.f >= numOfRegs) {
				pq.add(p);
				break;
			}

			int u = p.s;
			for (Integer v : graph.get(u))
				if (!elim.get(v)) {
					deg.set(u, deg.get(u) - 1);
					deg.set(v, deg.get(v) - 1);
					pq.add(new Pair(deg.get(v), v));
				}

		///	System.out.printf("REM: %d\n", u);
			stack.add(u);
			elim.set(u, true);
		}

		return pq.isEmpty();
	}

	public void color() {
		while (!stack.empty()) {
			int u = stack.pop();
			if (color.get(u) >= 0) throw new Report.Error("An already colored node found on stack");

			Vector<Boolean> col = new Vector<Boolean>();
			for (int i = 0; i < numOfRegs; i++) col.add(false);
			for (Integer v : graph.get(u))
				if (color.get(v) >= 0)
					col.set(color.get(v), true);

			int cur = -2;
			for (int i = numOfRegs - 1; i >= 0; i--)
				if (!col.get(i)) cur = i;

			color.set(u, cur);

			if (cur == -2) {
				if (!spill.contains(u)) throw new Report.Error("A non candidate for spill detected as an actual spill: " + u);
			}
		}
	}

	/**
	Loads the last added temp from the stack.
	**/
	public Temp load(Vector<AsmInstr> instrs, Code code) {
		Temp x = new Temp();
		Vector<Temp> uses = new Vector<Temp>();
		Vector<Temp> defs = new Vector<Temp>();
		uses.add(x);
		defs.add(x);

		instrs.add(new AsmOPER("ADD `d0, $253, " + Long.toString(tempSize + code.frame.locsSize + 2 * 8), null, defs, null));
		instrs.add(new AsmOPER("LDO `d0, `s0, 0", uses, defs, null));

		return x;
	}

	/**
	Stores the x as value of the last temp from the stack.
	**/
	public void store(Code code, Vector<AsmInstr> instrs, Temp x) {
		Temp y = new Temp();
		Vector<Temp> uses = new Vector<Temp>();
		Vector<Temp> defs = new Vector<Temp>();
		uses.add(x); uses.add(y);
		defs.add(y);

		instrs.add(new AsmOPER("ADD `d0, $253, " + Long.toString(tempSize + code.frame.locsSize + 2 * 8), null, defs, null));
		instrs.add(new AsmOPER("STO `s0, `s1, 0", uses, null, null));
	}

	public void replace(Code code, Temp x) {
		tempSize += 8;

		Vector<AsmInstr> ninstr = new Vector<AsmInstr>();
		for (int i = 0; i < code.instrs.size(); i++) {
			if (code.instrs.get(i) instanceof AsmLABEL) {
				ninstr.add(new AsmLABEL(((AsmLABEL) code.instrs.get(i)).label));
				continue;
			}

			AsmOPER instr = (AsmOPER) code.instrs.get(i);
			Vector<Temp> uses = instr.uses();
			Vector<Temp> defs = instr.defs();
			Vector<Label> jumps = instr.jumps();

			boolean ld = false;
			for (int j = 0; j < instr.uses().size(); j++) ld |= instr.uses().get(j) == x;
			if (ld) {
				Temp nx = load(ninstr, code);
				for (int j = 0; j < uses.size(); j++) if (uses.get(j) == x) uses.set(j, nx);
			}

			boolean st = false;
			for (int j = 0; j < instr.defs().size(); j++) st |= instr.defs().get(j) == x;
			if (st) {
				Temp nx = new Temp();
				for (int j = 0; j < defs.size(); j++) if (defs.get(j) == x) defs.set(j, nx);
			}

			ninstr.add(new AsmOPER(instr.instr, uses, defs, jumps));

			if (st) {
				store(code, ninstr, defs.get(0));
			}
		}

		code.instrs.clear();
		for (AsmInstr instr : ninstr) code.instrs.add(instr);
	}

	public Code registerAllocation(Code code) {
		init(code);
		build(code);

		while (!simplify()) {
			int u = pq.peek().s;
			for (Integer v : graph.get(u))
				if (!elim.get(v)) {
					deg.set(u, deg.get(u) - 1);
					deg.set(v, deg.get(v) - 1);
					pq.add(new Pair(deg.get(v), v));
				}

		///	System.out.printf("SPILL: %d\n", u);
			stack.add(u);
			spill.add(u);
			elim.set(u, true);
		}

		boolean empty = true;
		for (int i = 0; i < it; i++) empty &= deg.get(i) == 0;
		for (int i = 0; i < it; i++) empty &= elim.get(i);
	///	for (int i = 0; i < it; i++) System.out.printf("%d ", deg.get(i)); System.out.printf("\n");

		if (!empty) throw new Report.Error("Simplify phase finished but graph is not empty");

		/**
			col[i]:
				>=  0: actual color
				== -1: not visited
				== -2: must be spilled
		**/

		color();

		boolean done = true;
		for (int i = 0; i < it; i++) done &= color.get(i) >= 0;
		if (done) {
			for (int i = 0; i < it; i++)
				for (Integer v : graph.get(i))
					if (color.get(i) == color.get(v))
						throw new Report.Error("Neighbours " + Integer.toString(i) + " and " + Integer.toString(v) + " cannot be colored with the same color: " + Integer.toString(color.get(i)));

			HashMap<Temp, Integer> ans = new HashMap<Temp, Integer>();
			for (int i = 0; i < it; i++) ans.put(invMapTemps.get(i), color.get(i));
			return new Code(code.frame, code.entryLabel, code.exitLabel, code.instrs, ans, code.tempSize);
		}

		for (int i = 0; i < it; i++)
			if (color.get(i) < 0) {
				if (color.get(i) == -1) throw new Report.Error("An unvisited / uncolored node detected");

				replace(code, invMapTemps.get(i));
			}

		Code ncode = new Code(code.frame, code.entryLabel, code.exitLabel, code.instrs, null, code.tempSize + tempSize);
		return registerAllocation(ncode);

		/**
		for (int i = 0; i < it; i++) System.out.printf("%d ", deg.get(i)); System.out.printf("\n");

		for (int i = 0; i < it; i++) {
			System.out.printf("%d: ", i);
			for (Integer x : graph.get(i)) System.out.printf("%d ", x);
			System.out.printf("\n");
		}
		
		for (Map.Entry<Temp, Integer> e : mapTemps.entrySet()) {
			System.out.printf("%s -> %d\n", e.getKey(), e.getValue());
		}
		**/
	}

	/**
	 * Computes the mapping of temporary variables to registers for each function.
	 * If necessary, the code of each function is modified.
	 */
	public void tempsToRegs() {
		for (int i = 0; i < AsmGen.codes.size(); i++)
			AsmGen.codes.set(i, registerAllocation(AsmGen.codes.get(i)));
	}

	public void log() {
		if (logger == null)
			return;
		for (Code code : AsmGen.codes) {
			logger.begElement("code");
			logger.addAttribute("entrylabel", code.entryLabel.name);
			logger.addAttribute("exitlabel", code.exitLabel.name);
			logger.addAttribute("tempsize", Long.toString(code.tempSize));
			code.frame.log(logger);
			logger.begElement("instructions");
			for (AsmInstr instr : code.instrs) {
				logger.begElement("instruction");
				logger.addAttribute("code", instr.toString(code.regs));
				logger.endElement();
			}
			logger.endElement();
			logger.endElement();
		}
	}

}
