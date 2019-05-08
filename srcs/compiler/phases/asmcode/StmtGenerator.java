/**
 * @author sliva
 */
package compiler.phases.asmcode;

import java.util.*;
import compiler.data.imcode.*;
import compiler.data.imcode.visitor.*;
import compiler.data.layout.*;
import compiler.data.asmcode.*;
import compiler.common.report.*;

/**
 * @author sliva
 */
public class StmtGenerator implements ImcVisitor<Vector<AsmInstr>, Object> {
	/**
		ImcExpr: pushes Temp to itemp && pushes Vector<AsmInstr> to instr.
		ImcStmt: pushes Vector<AsmInstr> to instr.
	**/

	Stack<Temp> itemp = new Stack<Temp>();
	Stack<Vector<AsmInstr>> instr = new Stack<Vector<AsmInstr>>();

	public Vector<AsmInstr> visit(ImcBINOP binOp, Object visArg) {
		binOp.fstExpr.accept(this, visArg);
		Temp s0 = itemp.pop();
		Vector<AsmInstr> is0 = instr.pop();

		binOp.sndExpr.accept(this, visArg);
		Temp s1 = itemp.pop();
		Vector<AsmInstr> is1 = instr.pop();

		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();
		cinstr.addAll(is0);
		cinstr.addAll(is1);

		Temp d0 = new Temp();

		Vector<Temp> uses = new Vector<Temp>();
		uses.add(s0);
		uses.add(s1);

		Vector<Temp> defs = new Vector<Temp>();
		defs.add(d0);

		switch (binOp.oper) {
			case IOR: {
				cinstr.add(new AsmOPER("OR `d0, `s0, `s1", uses, defs, null));
				break;
			}

			case XOR: {
				cinstr.add(new AsmOPER("XOR `d0, `s0, `s1", uses, defs, null));
				break;
			}

			case AND: {
				cinstr.add(new AsmOPER("AND `d0, `s0, `s1", uses, defs, null));
				break;
			}

			case EQU: {
				cinstr.add(new AsmOPER("CMP `d0, `s0, `s1", uses, defs, null));
				cinstr.add(new AsmOPER("ZSZ `d0, `s0, 1", defs, defs, null));
				break;

			/**
				Temp _t = new Temp();

				Vector<Temp> _defs = new Vector<Temp>();
				_defs.add(_t);

				cinstr.add(new AsmOPER("CMP `d0, `s0, `s1", uses, _defs, null));
				cinstr.add(new AsmOPER("NOR `d0, `s0, 0", _defs, defs, null));

				break;
			**/
			}

			case NEQ: {
				cinstr.add(new AsmOPER("CMP `d0, `s0, `s1", uses, defs, null));
				cinstr.add(new AsmOPER("ZSNZ `d0, `s0, 1", defs, defs, null));
				break;
			}

			case LTH: {
				cinstr.add(new AsmOPER("CMP `d0, `s0, `s1", uses, defs, null));
				cinstr.add(new AsmOPER("ZSN `d0, `s0, 1", defs, defs, null));
				break;
			}

			case GTH: {
				cinstr.add(new AsmOPER("CMP `d0, `s0, `s1", uses, defs, null));
				cinstr.add(new AsmOPER("ZSP `d0, `s0, 1", defs, defs, null));
				break;
			}

			case LEQ: {
				cinstr.add(new AsmOPER("CMP `d0, `s0, `s1", uses, defs, null));
				cinstr.add(new AsmOPER("ZSNP `d0, `s0, 1", defs, defs, null));
				break;
			}

			case GEQ: {
				cinstr.add(new AsmOPER("CMP `d0, `s0, `s1", uses, defs, null));
				cinstr.add(new AsmOPER("ZSNN `d0, `s0, 1", defs, defs, null));
				break;
			}

			case ADD: {
				cinstr.add(new AsmOPER("ADD `d0, `s0, `s1", uses, defs, null));
				break;
			}

			case SUB: {
				cinstr.add(new AsmOPER("SUB `d0, `s0, `s1", uses, defs, null));
				break;
			}

			case MUL:{
				cinstr.add(new AsmOPER("MUL `d0, `s0, `s1", uses, defs, null));
				break;
			}

			case DIV: {
				cinstr.add(new AsmOPER("DIV `d0, `s0, `s1", uses, defs, null));
				break;
			}

			case MOD: {
				cinstr.add(new AsmOPER("DIV `d0, `s0, `s1", uses, defs, null));
				cinstr.add(new AsmOPER("OR `d0, rR, 0", null, defs, null));
				break;
			}

			default: throw new Report.Error("Unknown binary operator");
		}

		/** __TODO: RETURN **/

		itemp.push(d0);
		instr.push(cinstr);

		return null;
	}

	public Vector<AsmInstr> visit(ImcCALL call, Object visArg) {
		Vector<Label> jumps = new Vector<Label>();
		jumps.add(call.label);

		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();

		long offset = 0;
		for (ImcExpr expr : call.args()) {
			offset += 8;

			expr.accept(this, visArg);
			Temp s0 = itemp.pop();
			Vector<AsmInstr> is0 = instr.pop();

			Vector<Temp> uses = new Vector<Temp>();
			uses.add(s0);

			cinstr.addAll(is0);
			cinstr.add(new AsmOPER("STO `d0, $254, " + offset, uses, null, null));		
		}

		Temp d0 = new Temp();
		Vector<Temp> defs = new Vector<Temp>();
		defs.add(d0);

		/// __TODO: Hard coded. Address of next instruction is written in $0.
		cinstr.add(new AsmOPER("LDA `d0, " + call.label.name, null, defs, null));
		cinstr.add(new AsmOPER("GO $0, `s0, 0", defs, defs, jumps));

		/// __TODO: Returned temp should be RV Temp.
		itemp.push(new Temp());
		instr.push(cinstr);

		return null;
	}

	public Vector<AsmInstr> visit(ImcCJUMP cjump, Object visArg) {
		Vector<Label> jumps = new Vector<Label>();
		jumps.add(cjump.negLabel);
		jumps.add(cjump.posLabel);

		cjump.cond.accept(this, visArg);
		Temp s0 = itemp.pop();
		Vector<AsmInstr> is0 = instr.pop();

		Vector<Temp> uses = new Vector<Temp>();
		uses.add(s0);

		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();
		cinstr.addAll(is0);
		cinstr.add(new AsmOPER("BNZ `s0, " + cjump.posLabel.name, uses, null, jumps));

		instr.add(cinstr);

		return instr.peek();
	}

	public Vector<AsmInstr> visit(ImcCONST constant, Object visArg) {
		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();

		Temp t = new Temp();
		Vector<Temp> defs = new Vector<Temp>();
		defs.add(t);

		cinstr.add(new AsmOPER("SETL  `d0, " + (0x000000000000FFFFL & Math.abs(constant.value)), null, defs, null));
		cinstr.add(new AsmOPER("INCML `d0, " + (0x00000000FFFF0000L & Math.abs(constant.value)), null, defs, null));
		cinstr.add(new AsmOPER("INCMH `d0, " + (0x0000FFFF00000000L & Math.abs(constant.value)), null, defs, null));
		cinstr.add(new AsmOPER("INCH  `d0, " + (0xFFFF000000000000L & Math.abs(constant.value)), null, defs, null));

		if (constant.value < 0) {
			cinstr.add(new AsmOPER("NEG `d0, 0, `s0", defs, defs, null));
		}

		itemp.push(t);
		instr.push(cinstr);

		return null;
	}

	public Vector<AsmInstr> visit(ImcESTMT eStmt, Object visArg) {
		throw new Report.Error("ESTMT is not allowed past ImcGen phase");
	}

	public Vector<AsmInstr> visit(ImcJUMP jump, Object visArg) {
		Vector<Label> jumps = new Vector<Label>();
		jumps.add(jump.label);

		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();

		/// __TODO: next instruction
		Temp d0 = new Temp();
		Vector<Temp> defs = new Vector<Temp>();
		defs.add(d0);

		cinstr.add(new AsmOPER("LDA `d0, " + jump.label.name, null, defs, null));
		cinstr.add(new AsmOPER("GO `d0, `s0, 0", defs, defs, jumps));

		instr.add(cinstr);

		return instr.peek();
	}

	public Vector<AsmInstr> visit(ImcLABEL label, Object visArg) {
		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();
		cinstr.add(new AsmLABEL(label.label));
		instr.push(cinstr);

		return instr.peek();
	}

	public Vector<AsmInstr> visit(ImcMEM mem, Object visArg) {
		mem.addr.accept(this, visArg);
		Temp s0 = new Temp();
		Vector<AsmInstr> is0 = new Vector<AsmInstr>();

		Vector<Temp> uses = new Vector<Temp>();
		uses.add(s0);		

		Temp d0 = new Temp();
		Vector<Temp> defs = new Vector<Temp>();
		defs.add(d0);

		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();
		cinstr.addAll(is0);
		cinstr.add(new AsmOPER("LDO `d0, `s0, 0", uses, defs, null));

		itemp.add(d0);
		instr.add(cinstr);

		return null;
	}

	public Vector<AsmInstr> visit(ImcMOVE move, Object visArg) {
		move.dst.accept(this, visArg);
		Temp dst = itemp.pop();
		Vector<AsmInstr> idst = instr.pop();

		move.src.accept(this, visArg);
		Temp src = itemp.pop();
		Vector<AsmInstr> isrc = instr.pop();

		Vector<Temp> uses = new Vector<Temp>();
		Vector<Temp> defs = new Vector<Temp>();

		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();
		cinstr.addAll(idst);
		cinstr.addAll(isrc);

		if (move.dst instanceof ImcMEM) {
			uses.add(dst);
			uses.add(src);
			cinstr.add(new AsmOPER("STO `s0, `s1, 0", uses, null, null));
		} else {
			defs.add(dst);
			uses.add(src);
			cinstr.add(new AsmOPER("OR `d0, `s0, 0", uses, defs, null));
		}

		instr.push(cinstr);

		return instr.peek();
	}

	public Vector<AsmInstr> visit(ImcNAME name, Object visArg) {
		Temp d0 = new Temp();
		Vector<Temp> defs = new Vector<Temp>();
		defs.add(d0);

		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();
		cinstr.add(new AsmOPER("LDA `d0, " + name.label.name, null, defs, null));

		itemp.push(d0);
		instr.push(cinstr);

		return null;		
	}

	public Vector<AsmInstr> visit(ImcSEXPR sExpr, Object visArg) {
		throw new Report.Error("SEXPR is not allowed past ImcGen phase");
	}

	public Vector<AsmInstr> visit(ImcSTMTS stmts, Object visArg) {
		throw new Report.Error("STMTS is not allowed past ImcGen phase");
	}

	public Vector<AsmInstr> visit(ImcTEMP temp, Object visArg) {
		itemp.push(temp.temp);
		instr.push(new Vector<AsmInstr>());

		return null;
	}

	public Vector<AsmInstr> visit(ImcUNOP unOp, Object visArg) {
		unOp.subExpr.accept(this, visArg);
		Temp subTemp = itemp.pop();
		Vector<AsmInstr> subInstr = instr.pop();

		Temp d0 = new Temp();
		Vector<Temp> defs = new Vector<Temp>();
		defs.add(d0);

		Vector<Temp> uses = new Vector<Temp>();
		uses.add(subTemp);

		Vector<AsmInstr> cinstr = new Vector<AsmInstr>();
		cinstr.addAll(subInstr);

		switch (unOp.oper) {
			case NEG: {
				cinstr.add(new AsmOPER("NEG `d0, 0, `s0", uses, defs, null));
				break;
			}

			case NOT: {
				cinstr.add(new AsmOPER("NEG `d0, 1, `s0", uses, defs, null));
				break;
			}

			default: throw new Report.Error("Unknown unary operator");

		}

		itemp.push(d0);
		instr.push(cinstr);

		return null;
	}
}
