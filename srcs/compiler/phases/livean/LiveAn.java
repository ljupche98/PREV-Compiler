/**
 * @author sliva
 */
package compiler.phases.livean;

import java.util.*;
import compiler.data.asmcode.*;
import compiler.data.layout.*;
import compiler.phases.*;
import compiler.phases.asmcode.*;

/**
 * @author sliva
 */
public class LiveAn extends Phase {

	public LiveAn() {
		super("livean");
	}

	public void merge(HashSet<Temp> u, Vector<Temp> v) {
		for (Temp w : v) u.add(w);
	}

	public HashSet getDifference(HashSet<Temp> u, Vector<Temp> v) {
		for (Temp w : v) u.remove(w);
		return u;
	}

	public boolean eq(HashSet<Temp> u, HashSet<Temp> v) {
		HashSet<Temp> nu = new HashSet(u);
		nu.addAll(v);
		return u.size() == nu.size();
	}

	public void chunkLiveness(Code code) {
		Vector<AsmLABEL> labels = new Vector<AsmLABEL>();
		for (AsmInstr instr : code.instrs) if (instr instanceof AsmLABEL) labels.add((AsmLABEL) instr);

		boolean change = true;
		while (change) {
			change = false;

			for (int i = 0; i < code.instrs.size(); i++) {
				AsmInstr instr = code.instrs.get(i);
				int ins = instr.in().size(), outs = instr.out().size();

				instr.addInTemps(new HashSet(instr.uses()));
				instr.addInTemps(getDifference(instr.out(), instr.defs()));

				if (i + 1 < code.instrs.size())
					instr.addOutTemp(code.instrs.get(i + 1).in());

				for (int j = 0; j < instr.jumps().size(); j++)
					for (AsmLABEL label : labels)
						if (label.label.name.equals(instr.jumps().get(j).name))
							instr.addOutTemp(label.out());

				change |= ins != instr.in().size() || outs != instr.out().size();
			}
		}
	}

	public void chunksLiveness() {
		for (Code code : AsmGen.codes) {
			chunkLiveness(code);
		}
	}

	public void log() {
		if (logger == null)
			return;
		for (Code code : AsmGen.codes) {
			{
				logger.begElement("code");
				logger.addAttribute("entrylabel", code.entryLabel.name);
				logger.addAttribute("exitlabel", code.exitLabel.name);
				logger.addAttribute("tempsize", Long.toString(code.tempSize));
				code.frame.log(logger);
				logger.begElement("instructions");
				for (AsmInstr instr : code.instrs) {
					logger.begElement("instruction");
					logger.addAttribute("code", instr.toString());
					logger.begElement("temps");
					logger.addAttribute("name", "use");
					for (Temp temp : instr.uses()) {
						logger.begElement("temp");
						logger.addAttribute("name", temp.toString());
						logger.endElement();
					}
					logger.endElement();
					logger.begElement("temps");
					logger.addAttribute("name", "def");
					for (Temp temp : instr.defs()) {
						logger.begElement("temp");
						logger.addAttribute("name", temp.toString());
						logger.endElement();
					}
					logger.endElement();
					logger.begElement("temps");
					logger.addAttribute("name", "in");
					for (Temp temp : instr.in()) {
						logger.begElement("temp");
						logger.addAttribute("name", temp.toString());
						logger.endElement();
					}
					logger.endElement();
					logger.begElement("temps");
					logger.addAttribute("name", "out");
					for (Temp temp : instr.out()) {
						logger.begElement("temp");
						logger.addAttribute("name", temp.toString());
						logger.endElement();
					}
					logger.endElement();
					logger.endElement();
				}
				logger.endElement();
				logger.endElement();
			}
		}
	}

}
