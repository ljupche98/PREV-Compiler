/**
 * @author sliva
 */
package compiler.phases.seman;

import compiler.common.report.*;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;

/**
 * Name resolving: the result is stored in {@link SemAn#declaredAt}.
 * 
 * @author sliva
 */
public class NameResolver extends AbsFullVisitor<Object, Object> {

	/** Symbol table. */
	private final SymbTable symbTable = new SymbTable();

	@Override
	public Object visit(AbsArgs args, Object visArg) {
		for (AbsExpr arg : args.args())
			arg.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsArrExpr arrExpr, Object visArg) {
		arrExpr.array.accept(this, visArg);
		arrExpr.index.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsArrType arrType, Object visArg) {
		arrType.len.accept(this, visArg);
		arrType.elemType.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsAssignStmt assignStmt, Object visArg) {
		assignStmt.dst.accept(this, visArg);
		assignStmt.src.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsAtomExpr atomExpr, Object visArg) {
		return null;
	}

	@Override
	public Object visit(AbsAtomType atomType, Object visArg) {
		return null;
	}

	@Override
	public Object visit(AbsBinExpr binExpr, Object visArg) {
		binExpr.fstExpr.accept(this, visArg);
		binExpr.sndExpr.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsBlockExpr blockExpr, Object visArg) {
		symbTable.newScope();
		blockExpr.decls.accept(this, visArg);
		blockExpr.stmts.accept(this, visArg);
		blockExpr.expr.accept(this, visArg);
		symbTable.oldScope();
		return null;
	}

	@Override
	public Object visit(AbsCastExpr castExpr, Object visArg) {
		castExpr.type.accept(this, visArg);
		castExpr.expr.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsCompDecl compDecl, Object visArg) {
		compDecl.type.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsCompDecls compDecls, Object visArg) {
		for (AbsCompDecl compDecl : compDecls.compDecls())
			compDecl.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsDecls decls, Object visArg) {
		for (AbsDecl decl : decls.decls())
			decl.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsDelExpr delExpr, Object visArg) {
		delExpr.expr.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsExprStmt exprStmt, Object visArg) {
		exprStmt.expr.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsFunDecl funDecl, Object visArg) {
		try {
			symbTable.ins(funDecl.name, funDecl);
		} catch (Exception e) {
			throw new Report.Error(funDecl.name + " is already defined");
		}

		funDecl.parDecls.accept(this, null);
		funDecl.type.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsFunDef funDef, Object visArg) {
		try {
			symbTable.ins(funDef.name, funDef);
		} catch (Exception e) {
			throw new Report.Error(funDef.name + " is already defined");
		}

		funDef.parDecls.accept(this, null);
		funDef.type.accept(this, visArg);

		symbTable.newScope();
		funDef.parDecls.accept(this, true);
		funDef.value.accept(this, visArg);
		symbTable.oldScope();

		return null;
	}

	@Override
	public Object visit(AbsFunName funName, Object visArg) {
		try {
			SemAn.declaredAt.put(funName, symbTable.fnd(funName.name));
		} catch (Exception e) {
			throw new Report.Error(funName.name + " has not been defined");
		}

		funName.args.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsIfStmt ifStmt, Object visArg) {
		ifStmt.cond.accept(this, visArg);
		ifStmt.thenStmts.accept(this, visArg);
		ifStmt.elseStmts.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsNewExpr newExpr, Object visArg) {
		newExpr.type.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsParDecl parDecl, Object visArg) {
		if (visArg != null) { /// visArg = true iff a new scope has been created
			try {
				symbTable.ins(parDecl.name, parDecl);
			} catch (Exception e) {
				throw new Report.Error(parDecl.name + " is already defined");
			}
		} else { /// else find the type declaration
			parDecl.type.accept(this, visArg);
		}

		return null;
	}

	@Override
	public Object visit(AbsParDecls parDecls, Object visArg) {
		for (AbsParDecl parDecl : parDecls.parDecls())
			parDecl.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsPtrType ptrType, Object visArg) {
		ptrType.ptdType.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsRecExpr recExpr, Object visArg) {
		recExpr.record.accept(this, visArg);
	///	ecExpr.comp.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsRecType recType, Object visArg) {
		recType.compDecls.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsSource source, Object visArg) {
		source.decls.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsStmts stmts, Object visArg) {
		for (AbsStmt stmt : stmts.stmts())
			stmt.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsTypDecl typDecl, Object visArg) {
		try {
			symbTable.ins(typDecl.name, typDecl);
		} catch (Exception e) {
			throw new Report.Error(typDecl.name + " is already defined");
		}

		typDecl.type.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsTypName typName, Object visArg) {
		try {
			SemAn.declaredAt.put(typName, symbTable.fnd(typName.name));
		} catch (Exception e) {
			throw new Report.Error(typName.name + " has not been defined");
		}

		return null;
	}

	@Override
	public Object visit(AbsUnExpr unExpr, Object visArg) {
		unExpr.subExpr.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsVarDecl varDecl, Object visArg) {
		try {
			symbTable.ins(varDecl.name, varDecl);
		} catch (Exception e) {
			throw new Report.Error(varDecl.name + " is already defined");
		}

		varDecl.type.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsVarName varName, Object visArg) {
		try {
			SemAn.declaredAt.put(varName, symbTable.fnd(varName.name));
		} catch (Exception e) {
			throw new Report.Error(varName.name + " has not been defined");
		}

		return null;
	}

	@Override
	public Object visit(AbsWhileStmt whileStmt, Object visArg) {
		whileStmt.cond.accept(this, visArg);
		whileStmt.stmts.accept(this, visArg);
		return null;
	}
}
