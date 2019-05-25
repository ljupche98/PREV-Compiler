/**
 * @author sliva
 */
package compiler.phases.chunks;

import java.util.*;
import compiler.common.report.*;
import compiler.data.layout.*;
import compiler.data.imcode.*;
import compiler.data.imcode.visitor.*;

/**
 * @author sliva
 */
public class StmtCanonizer implements ImcVisitor<Vector<ImcStmt>, Object> {

	public boolean skip = false;
	public static Stack<ImcExpr> iexpr = new Stack<ImcExpr>();
	public static Stack<Vector<ImcStmt>> istmt = new Stack<Vector<ImcStmt>>();

	public Vector<ImcStmt> visit(ImcBINOP binOp, Object visArg) {
		binOp.fstExpr.accept(this, visArg);
		Temp fst = new Temp();
		ImcExpr fstExpr = iexpr.pop();
		Vector<ImcStmt> fstStmt = istmt.pop();

		binOp.sndExpr.accept(this, visArg);
		Temp snd = new Temp();
		ImcExpr sndExpr = iexpr.pop();
		Vector<ImcStmt> sndStmt = istmt.pop();

		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.addAll(fstStmt);
		stmt.add(new ImcMOVE(new ImcTEMP(fst), fstExpr));
		stmt.addAll(sndStmt);
		stmt.add(new ImcMOVE(new ImcTEMP(snd), sndExpr));

		iexpr.push(new ImcBINOP(binOp.oper, new ImcTEMP(fst), new ImcTEMP(snd)));
		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcCALL call, Object visArg) {
		Vector<ImcExpr> args = new Vector<ImcExpr>();
		Vector<ImcStmt> stmt = new Vector<ImcStmt>();

		for (int i = 0; i < call.args().size(); i++) {
			Vector<ImcStmt> aStmt = new Vector<ImcStmt>();

			if (i == 0) {
				Temp temp = new Temp();
				aStmt.add(new ImcMOVE(new ImcTEMP(temp), call.args().get(i)));
				args.add(new ImcTEMP(temp));
				stmt.addAll(aStmt);
				continue;
			}

			ImcExpr expr = call.args().get(i);
			expr.accept(this, visArg);
			Temp argv = new Temp();
			ImcExpr argExpr = iexpr.pop();
			Vector<ImcStmt> argStmt = istmt.pop();

			aStmt.addAll(argStmt);
			aStmt.add(new ImcMOVE(new ImcTEMP(argv), argExpr));

			args.add(new ImcTEMP(argv));
			stmt.addAll(aStmt);
		}

		iexpr.push(new ImcCALL(call.label, args));
		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcCJUMP cjump, Object visArg) {
		cjump.cond.accept(this, visArg);
		Temp temp = new Temp();
		ImcExpr cExpr = iexpr.pop();
		Vector<ImcStmt> cStmt = istmt.pop();

		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.addAll(cStmt);
		stmt.add(new ImcMOVE(new ImcTEMP(temp), cExpr));

		Label fLabel = new Label();
		stmt.add(new ImcCJUMP(new ImcTEMP(temp), cjump.posLabel, fLabel));
		stmt.add(new ImcLABEL(fLabel));
		stmt.add(new ImcJUMP(cjump.negLabel));

		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcCONST constant, Object visArg) {
		iexpr.push(new ImcCONST(constant.value));
		istmt.push(new Vector<ImcStmt>());

		return null;
	}

	public Vector<ImcStmt> visit(ImcESTMT eStmt, Object visArg) {
		eStmt.expr.accept(this, visArg);
		ImcExpr exExpr = iexpr.pop();
		Vector<ImcStmt> exStmt = istmt.pop();

		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.addAll(exStmt);

		/// execute the expression in case it modifies the environment.
		stmt.add(new ImcMOVE(new ImcTEMP(new Temp()), exExpr));

		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcJUMP jump, Object visArg) {
		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.add(new ImcJUMP(jump.label));

		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcLABEL label, Object visArg) {
		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.add(new ImcLABEL(label.label));

		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcMEM mem, Object visArg) {
		mem.addr.accept(this, visArg);
		Temp temp = new Temp();
		ImcExpr addrExpr = iexpr.pop();
		Vector<ImcStmt> addrStmt = istmt.pop();

		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.addAll(addrStmt);
		stmt.add(new ImcMOVE(new ImcTEMP(temp), addrExpr));

		iexpr.push(new ImcMEM(new ImcTEMP(temp)));
		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcMOVE move, Object visArg) {
		if (move.dst instanceof ImcMEM) {
			boolean prevSkip = skip;
			skip = true;
			move.dst.accept(this, visArg);
			skip = prevSkip;
			Temp dst = new Temp();
			ImcExpr dstExpr = iexpr.pop();
			Vector<ImcStmt> dstStmt = istmt.pop();

			move.src.accept(this, visArg);
			Temp src = new Temp();
			ImcExpr srcExpr = iexpr.pop();
			Vector<ImcStmt> srcStmt = istmt.pop();
	
			Vector<ImcStmt> stmt = new Vector<ImcStmt>();
			stmt.addAll(dstStmt);
			stmt.addAll(srcStmt);
			stmt.add(new ImcMOVE(new ImcTEMP(src), srcExpr));
	
			stmt.add(new ImcMOVE(dstExpr, new ImcTEMP(src)));

			istmt.push(stmt);

			return null;
		}

		move.dst.accept(this, visArg);
		Temp dst = new Temp();
		ImcExpr dstExpr = iexpr.pop();
		Vector<ImcStmt> dstStmt = istmt.pop();

		move.src.accept(this, visArg);
		Temp src = new Temp();
		ImcExpr srcExpr = iexpr.pop();
		Vector<ImcStmt> srcStmt = istmt.pop();

		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.addAll(dstStmt);
		stmt.add(new ImcMOVE(new ImcTEMP(dst), dstExpr));
		stmt.addAll(srcStmt);
		stmt.add(new ImcMOVE(new ImcTEMP(src), srcExpr));

		stmt.add(new ImcMOVE(new ImcTEMP(dst), new ImcTEMP(src)));

		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcNAME name, Object visArg) {
		iexpr.push(new ImcNAME(name.label));
		istmt.push(new Vector<ImcStmt>());

		return null;
	}

	public Vector<ImcStmt> visit(ImcSEXPR sExpr, Object visArg) {
		sExpr.stmt.accept(this, visArg);
		Vector<ImcStmt> sStmt = istmt.pop();

		sExpr.expr.accept(this, visArg);
		Temp temp = new Temp();
		ImcExpr sexprExpr = iexpr.pop();
		Vector<ImcStmt> sexprStmt = istmt.pop();

		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.addAll(sStmt);
		stmt.addAll(sexprStmt);
		stmt.add(new ImcMOVE(new ImcTEMP(temp), sexprExpr));

		iexpr.push(new ImcTEMP(temp));
		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcSTMTS stmts, Object visArg) {
		Vector<ImcStmt> stmt = new Vector<ImcStmt>();

		for (ImcStmt cStmt : stmts.stmts()) {
			cStmt.accept(this, visArg);
			Vector<ImcStmt> nStmt = istmt.pop();
			stmt.addAll(nStmt);
		}

		istmt.push(stmt);

		return null;
	}

	public Vector<ImcStmt> visit(ImcTEMP temp, Object visArg) {
		iexpr.push(new ImcTEMP(temp.temp));
		istmt.push(new Vector<ImcStmt>());

		return null;
	}

	public Vector<ImcStmt> visit(ImcUNOP unOp, Object visArg) {
		unOp.subExpr.accept(this, visArg);
		Temp temp = new Temp();
		ImcExpr subExpr = iexpr.pop();
		Vector<ImcStmt> subStmt = istmt.pop();

		Vector<ImcStmt> stmt = new Vector<ImcStmt>();
		stmt.addAll(subStmt);
		stmt.add(new ImcMOVE(new ImcTEMP(temp), subExpr));

		iexpr.push(new ImcUNOP(unOp.oper, new ImcTEMP(temp)));
		istmt.push(stmt);

		return null;
	}
    
}
