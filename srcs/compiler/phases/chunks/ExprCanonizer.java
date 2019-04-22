/**
 * @author sliva
 */
package compiler.phases.chunks;

import java.util.*;
import compiler.data.layout.*;
import compiler.data.imcode.*;
import compiler.data.imcode.visitor.*;

/**
 * @author sliva
 */
public class ExprCanonizer implements ImcVisitor<ImcExpr, Vector<ImcStmt>> {

	public default ImcExpr visit(ImcBINOP binOp, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcCALL call, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcCJUMP cjump, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcCONST constant, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcESTMT eStmt, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcJUMP jump, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcLABEL label, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcMEM mem, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcMOVE move, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcNAME name, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcSEXPR sExpr, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcSTMTS stmts, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcTEMP temp, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

	public default ImcExpr visit(ImcUNOP unOp, Vector<ImcStmt> visArg) {
		throw new Report.InternalError();
	}

}
