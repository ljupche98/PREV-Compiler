/**
 * @author sliva
 */
package compiler.phases.seman;

import java.util.*;
import compiler.common.report.*;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;
import compiler.data.type.*;
import compiler.data.type.property.*;

/**
 * Determines which value expression can denote an address.
 * 
 * @author sliva
 */
public class AddrResolver extends AbsFullVisitor<Boolean, Object> {
	@Override
	public Boolean visit(AbsArgs args, Object visArg) {
		for (AbsExpr arg : args.args())
			arg.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsArrExpr arrExpr, Object visArg) {
		Boolean array = arrExpr.array.accept(this, visArg);
		if (array == null) throw new Report.Error("Cannot address resolve something that is not an array");
		if (array) SemAn.isAddr.put(arrExpr, true);
		arrExpr.index.accept(this, visArg);
		return true;
	}

	@Override
	public Boolean visit(AbsArrType arrType, Object visArg) {
		arrType.len.accept(this, visArg);
		arrType.elemType.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsAssignStmt assignStmt, Object visArg) {
		assignStmt.dst.accept(this, visArg);
		assignStmt.src.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsAtomExpr atomExpr, Object visArg) {
		return null;
	}

	@Override
	public Boolean visit(AbsAtomType atomType, Object visArg) {
		return null;
	}

	@Override
	public Boolean visit(AbsBinExpr binExpr, Object visArg) {
		binExpr.fstExpr.accept(this, visArg);
		binExpr.sndExpr.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsBlockExpr blockExpr, Object visArg) {
		blockExpr.decls.accept(this, visArg);
		blockExpr.stmts.accept(this, visArg);
		blockExpr.expr.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsCastExpr castExpr, Object visArg) {
		castExpr.type.accept(this, visArg);
		castExpr.expr.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsCompDecl compDecl, Object visArg) {
		compDecl.type.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsCompDecls compDecls, Object visArg) {
		for (AbsCompDecl compDecl : compDecls.compDecls())
			compDecl.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsDecls decls, Object visArg) {
		for (AbsDecl decl : decls.decls())
			decl.accept(this, visArg);
		return true;
	}

	@Override
	public Boolean visit(AbsDelExpr delExpr, Object visArg) {
		delExpr.expr.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsExprStmt exprStmt, Object visArg) {
		exprStmt.expr.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsFunDecl funDecl, Object visArg) {
		funDecl.parDecls.accept(this, visArg);
		funDecl.type.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsFunDef funDef, Object visArg) {
		funDef.parDecls.accept(this, visArg);
		funDef.type.accept(this, visArg);
		funDef.value.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsFunName funName, Object visArg) {
		funName.args.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsIfStmt ifStmt, Object visArg) {
		ifStmt.cond.accept(this, visArg);
		ifStmt.thenStmts.accept(this, visArg);
		ifStmt.elseStmts.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsNewExpr newExpr, Object visArg) {
		newExpr.type.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsParDecl parDecl, Object visArg) {
		return true;
	}

	@Override
	public Boolean visit(AbsParDecls parDecls, Object visArg) {
		for (AbsParDecl parDecl : parDecls.parDecls())
			parDecl.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsPtrType ptrType, Object visArg) {
		ptrType.ptdType.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsRecExpr recExpr, Object visArg) {
		Boolean record = recExpr.record.accept(this, visArg);
		if (record == null) throw new Report.Error("Cannot address resolve something that is not a record variable");
		if (record) SemAn.isAddr.put(recExpr, true);
	///	recExpr.comp.accept(this, visArg);
		return true;
	}

	@Override
	public Boolean visit(AbsRecType recType, Object visArg) {
		recType.compDecls.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsSource source, Object visArg) {
		source.decls.accept(this, 0);
		source.decls.accept(this, 1);
		return null;
	}

	@Override
	public Boolean visit(AbsStmts stmts, Object visArg) {
		for (AbsStmt stmt : stmts.stmts())
			stmt.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsTypDecl typDecl, Object visArg) {
		return null;
	}

	@Override
	public Boolean visit(AbsTypName typName, Object visArg) {
		return null;
	}

	@Override
	public Boolean visit(AbsUnExpr unExpr, Object visArg) {
		if (unExpr.oper == AbsUnExpr.Oper.DATA) {
			SemType type = SemAn.isOfType.get(unExpr.subExpr);
			if (type instanceof SemPtrType) {
				SemAn.isAddr.put(unExpr, true);
			}
		}

		unExpr.subExpr.accept(this, visArg);
		return null;
	}

	@Override
	public Boolean visit(AbsVarDecl varDecl, Object visArg) {
		return true;
	}

	@Override
	public Boolean visit(AbsVarName varName, Object visArg) {
		AbsDecl decl = SemAn.declaredAt.get(varName);
		Boolean ret = decl.accept(this, visArg);
		if (ret == null) throw new Report.Error("Cannot address resolve something that is not a variable");
		///if (ret) SemAn.isAddr.put(varName, true);
		return ret;
	}

	@Override
	public Boolean visit(AbsWhileStmt whileStmt, Object visArg) {
		whileStmt.cond.accept(this, visArg);
		whileStmt.stmts.accept(this, visArg);
		return null;
	}
}
