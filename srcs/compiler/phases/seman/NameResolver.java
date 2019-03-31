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

	/**
	symbTableType is almost equal to symbTable with the only difference that I am also checking the node type.
	Prevents exceptions that occur when a variable has been defined, but is later called as a function or vice versa.
	**/

	/** Symbol table. */
	private final SymbTable symbTable = new SymbTable();
	private final SymbTable symbTableType = new SymbTable();

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
		symbTableType.newScope();
		blockExpr.decls.accept(this, 0);
		blockExpr.stmts.accept(this, 0);
		blockExpr.expr.accept(this, 0);
		symbTableType.oldScope();
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
			decl.accept(this, (int) visArg | (1 << 0));
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
		if ((((int) visArg) & (1 << 0)) != 0) {
			/// first visit => we should just add it to the symbol table.
			try {
				symbTable.ins(funDecl.name, funDecl);
				symbTableType.ins(funDecl.name + "FUN", funDecl);
			} catch (Exception e) {
				throw new Report.Error("Function " + funDecl.name + " is already defined");
			}
		} else {
			/// second visit => we should process the declaration.
			funDecl.parDecls.accept(this, (int) visArg | (1 << 1));
			funDecl.type.accept(this, visArg);
		}

		return null;
	}

	@Override
	public Object visit(AbsFunDef funDef, Object visArg) {
		if ((((int) visArg) & (1 << 0)) != 0) {
			/// first visit => we should just add it to the symbol table.
			try {
				symbTable.ins(funDef.name, funDef);
				symbTableType.ins(funDef.name + "FUN", funDef);
			} catch (Exception e) {
				throw new Report.Error("Function " + funDef.name + " is already defined");
			}
		} else {
			/// second visit => we should process the declaration.
			funDef.parDecls.accept(this, (int) visArg | (1 << 1));
			funDef.type.accept(this, visArg);

			symbTable.newScope();
			symbTableType.newScope();
			funDef.parDecls.accept(this, visArg);
			funDef.value.accept(this, visArg);
			symbTableType.oldScope();
			symbTable.oldScope();
		}

		return null;
	}

	@Override
	public Object visit(AbsFunName funName, Object visArg) {
		try {
			symbTableType.fnd(funName.name + "FUN");
			SemAn.declaredAt.put(funName, symbTable.fnd(funName.name));
		} catch (Exception e) {
			throw new Report.Error("Function " + funName.name + " has not been defined");
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
		if ((((int) visArg) & (1 << 1)) != 0) {
			/// first visit => just check the types.
			parDecl.type.accept(this, visArg);
		} else {
			/// second visit => add parameters into the new scope.
			try {
				symbTable.ins(parDecl.name, parDecl);
				symbTableType.ins(parDecl.name + "VAR", parDecl);
			} catch (Exception e) {
				throw new Report.Error("Variable " + parDecl.name + " is already defined");
			}
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
	///	recExpr.comp.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsRecType recType, Object visArg) {
		recType.compDecls.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsSource source, Object visArg) {
		source.decls.accept(this, 0);
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
		if ((((int) visArg) & (1 << 0)) != 0) {
			/// first visit => we should just add it to the symbol table.
			try {
				symbTable.ins(typDecl.name, typDecl);
				symbTableType.ins(typDecl.name + "TYP", typDecl);
			} catch (Exception e) {
				throw new Report.Error("Type " + typDecl.name + " is already defined");
			}
		} else {
			/// second visit => we should process the declaration.
			typDecl.type.accept(this, visArg);
		}

		return null;
	}

	@Override
	public Object visit(AbsTypName typName, Object visArg) {
		try {
			symbTableType.fnd(typName.name + "TYP");
			SemAn.declaredAt.put(typName, symbTable.fnd(typName.name));
		} catch (Exception e) {
			throw new Report.Error("Type " + typName.name + " has not been defined");
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
		if ((((int) visArg) & (1 << 0)) != 0) {
			/// first visit => we should just add it to the symbol table.
			try {
				symbTable.ins(varDecl.name, varDecl);
				symbTableType.ins(varDecl.name + "VAR", varDecl);
			} catch (Exception e) {
				throw new Report.Error("Variable " + varDecl.name + " has already been defined");
			}
		} else {
			/// second visit => we should process the declaration.
			varDecl.type.accept(this, visArg);
		}

		return null;
	}

	@Override
	public Object visit(AbsVarName varName, Object visArg) {
		try {
			symbTableType.fnd(varName.name + "VAR");
			SemAn.declaredAt.put(varName, symbTable.fnd(varName.name));
		} catch (Exception e) {
			throw new Report.Error("Variable " + varName.name + " has not been defined");
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
