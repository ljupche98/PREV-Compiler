/**
 * @author sliva
 */
package compiler.phases.frames;

import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;
import compiler.data.type.*;
import compiler.data.layout.*;
import compiler.phases.seman.*;
import java.util.*;

/**
 * Computing function frames and accesses.
 * 
 * @author sliva
 */
public class FrmEvaluator extends AbsFullVisitor<Object, FrmEvaluator.Context> {

	/**
	 * The context {@link FrmEvaluator} uses while computing function frames and
	 * variable accesses.
	 * 
	 * @author sliva
	 */
	protected abstract class Context {
	}

	/**
	 * Functional context, i.e., used when traversing function and building a new
	 * frame, parameter acceses and variable acceses.
	 * 
	 * @author sliva
	 */
	private class FunContext extends Context {
		public int depth = 0;
		public long locsSize = 0;
		public long argsSize = 0;
		public long parsSize = new SemPtrType(new SemVoidType()).size();
	}

	/**
	 * Record context, i.e., used when traversing record definition and computing
	 * record component acceses.
	 * 
	 * @author sliva
	 */
	private class RecContext extends Context {
		public long compsSize = 0;
	}

	/**
	1 ... look for funDecls
	2 ... within a function, find MAX(SIZE(ARGS) + SL) for every fun. call.
	3 ... within a function, find SUM(varDecls).
	4 ... add parameters of a function.

	**/

	private static AbsSource src;

	private static int level = 0;

	private static Stack<Integer> state = new Stack<Integer>();
	private static FunContext cxt;

	@Override
	public Object visit(AbsArgs args, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			for (AbsExpr arg : args.args())
				arg.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsArrExpr arrExpr, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			arrExpr.array.accept(this, visArg);
			arrExpr.index.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsArrType arrType, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			arrType.len.accept(this, visArg);
			arrType.elemType.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsAssignStmt assignStmt, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			assignStmt.dst.accept(this, visArg);
			assignStmt.src.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsAtomExpr atomExpr, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsAtomType atomType, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsBinExpr binExpr, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			binExpr.fstExpr.accept(this, visArg);
			binExpr.sndExpr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsBlockExpr blockExpr, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			blockExpr.decls.accept(this, visArg);
			blockExpr.stmts.accept(this, visArg);
			blockExpr.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsCastExpr castExpr, FrmEvaluator.Context visArg) {
		castExpr.type.accept(this, visArg);
		castExpr.expr.accept(this, visArg);
		return null;
	}

	@Override
	public Object visit(AbsCompDecl compDecl, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			compDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsCompDecls compDecls, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			for (AbsCompDecl compDecl : compDecls.compDecls())
				compDecl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsDecls decls, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			for (AbsDecl decl : decls.decls())
				decl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsDelExpr delExpr, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			case 2: {
				long curSize = (new SemPtrType(new SemVoidType())).size(); /// size of SL.
				curSize += (new SemIntType()).size();			   /// size of argument.
				cxt.argsSize = Math.max(cxt.argsSize, curSize);
				return null;
			}

			default:
			delExpr.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsExprStmt exprStmt, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			exprStmt.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsFunDecl funDecl, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			case 1: {
				FunContext oldFC = cxt;
				cxt = new FunContext();
				cxt.depth = ++level;

				state.push(4);
				funDecl.parDecls.accept(this, visArg);
				state.pop();

				Frames.frames.put(funDecl, new Frame(new Label(funDecl.name), cxt.depth, cxt.locsSize, cxt.argsSize));

				--level;
				cxt = oldFC;
				return null;
			}

			case 3: return null;

			default:
			++level;
			funDecl.parDecls.accept(this, visArg);
			funDecl.type.accept(this, visArg);
			--level;
			return null;
		}
	}

	@Override
	public Object visit(AbsFunDef funDef, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			case 1: {
				FunContext oldFC = cxt;
				cxt = new FunContext();
				cxt.depth = ++level;

				state.push(2);
				funDef.value.accept(this, visArg);
				state.pop();

				state.push(3);
				funDef.value.accept(this, visArg);
				state.pop();

				state.push(4);
				funDef.parDecls.accept(this, visArg);
				state.pop();

				Frames.frames.put(funDef, new Frame(level == 1 ? new Label(funDef.name) : new Label(), cxt.depth, cxt.locsSize, cxt.argsSize));

				funDef.value.accept(this, visArg);

				--level;
				cxt = oldFC;
				return null;
			}

			case 3: return null; /// prevents adding size of declarations of variables of nested functions.

			default:
			++level;
			funDef.parDecls.accept(this, visArg);
			funDef.type.accept(this, visArg);
			funDef.value.accept(this, visArg);
			--level;
			return null;
		}
	}

	@Override
	public Object visit(AbsFunName funName, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			case 2: {
				long curSize = (new SemPtrType(new SemVoidType())).size(); /// size of SL.

				for (AbsExpr expr : funName.args.args()) {
					SemType exprType = SemAn.isOfType.get(expr);
					curSize += exprType.size();
				}

				AbsDecl funDecl = SemAn.declaredAt.get(funName);
				SemType retType = funDecl.type.accept(new TypeResolver(true), 1);
				if (!(retType instanceof SemVoidType)) curSize = Math.max(curSize, retType.size()); /// size of return type.

				cxt.argsSize = Math.max(cxt.argsSize, curSize);
				return null;
			}

			default:
			funName.args.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsIfStmt ifStmt, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			ifStmt.cond.accept(this, visArg);
			ifStmt.thenStmts.accept(this, visArg);
			ifStmt.elseStmts.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsNewExpr newExpr, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			case 2: {
				long curSize = (new SemPtrType(new SemVoidType())).size(); /// size of SL.
				curSize += (new SemIntType()).size();			   /// size of argument.
				cxt.argsSize = Math.max(cxt.argsSize, curSize);
				return null;
			}

			default:
			newExpr.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsParDecl parDecl, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			parDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsParDecls parDecls, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			case 4: {
				long cum = 0;

				for (int i = 0; i < parDecls.parDecls().size(); i++) {
					SemType type = parDecls.parDecl(i).type.accept(new TypeResolver(true), 1);

					Frames.accesses.put(parDecls.parDecl(i), new RelAccess(type.size(), cum + type.size(), level));

					cum = cum + type.size();
				}

				return null;
			}
			
			default:
			for (AbsParDecl parDecl : parDecls.parDecls())
				parDecl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsPtrType ptrType, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			ptrType.ptdType.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsRecExpr recExpr, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			recExpr.record.accept(this, visArg);
			recExpr.comp.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsRecType recType, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			recType.compDecls.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsSource source, FrmEvaluator.Context visArg) {
		src = source;
		state.push(1);
		source.decls.accept(this, visArg);
		state.pop();
		return null;
	}

	@Override
	public Object visit(AbsStmts stmts, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			for (AbsStmt stmt : stmts.stmts())
				stmt.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsTypDecl typDecl, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			typDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsTypName typName, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsUnExpr unExpr, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			unExpr.subExpr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsVarDecl varDecl, FrmEvaluator.Context visArg) {
		if (level == 0) {
			/// Global variable declaration.

			SemType type = varDecl.type.accept(new TypeResolver(true), 1);
			Frames.accesses.put(varDecl, new AbsAccess(type.size(), new Label(varDecl.name)));
		} else {
			switch (state.peek()) {
				case 3: {
					SemType type = varDecl.type.accept(new TypeResolver(true), 1);

					cxt.locsSize += type.size();
					Frames.accesses.put(varDecl, new RelAccess(type.size(), -cxt.locsSize, level));
					return null;
				}

				default:
				varDecl.type.accept(this, visArg);
				return null;
			}
		}

		return null;
	}

	@Override
	public Object visit(AbsVarName varName, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsWhileStmt whileStmt, FrmEvaluator.Context visArg) {
		switch (state.peek()) {
			default:
			whileStmt.cond.accept(this, visArg);
			whileStmt.stmts.accept(this, visArg);
			return null;
		}
	}

}
