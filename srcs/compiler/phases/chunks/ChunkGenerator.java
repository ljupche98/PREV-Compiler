/**
 * @author sliva
 */
package compiler.phases.chunks;

import java.util.*;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;
import compiler.data.type.*;
import compiler.data.layout.*;
import compiler.data.imcode.*;
import compiler.data.chunk.*;
import compiler.phases.frames.*;
import compiler.phases.imcgen.*;

/**
 * @author sliva
 *
 */
public class ChunkGenerator extends AbsFullVisitor<Object, Object> {

	/**
	1 ... Chunks.dataChunks. Global variables & string constants.
	2 ... Chunks.codeChunks. Canonize functions' code.
	**/

	private static AbsSource src;
	private static Stack<Integer> state = new Stack<Integer>();

	@Override
	public Object visit(AbsArgs args, Object visArg) {
		switch (state.peek()) {
			default:
			for (AbsExpr arg : args.args())
				arg.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsArrExpr arrExpr, Object visArg) {
		switch (state.peek()) {
			default:
			arrExpr.array.accept(this, visArg);
			arrExpr.index.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsArrType arrType, Object visArg) {
		switch (state.peek()) {
			default:
			arrType.len.accept(this, visArg);
			arrType.elemType.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsAssignStmt assignStmt, Object visArg) {
		switch (state.peek()) {
			default:
			assignStmt.dst.accept(this, visArg);
			assignStmt.src.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsAtomExpr atomExpr, Object visArg) {
		switch (state.peek()) {
			case 1: {
				if (atomExpr.type == AbsAtomExpr.Type.STR) {
					String init = atomExpr.expr.substring(1, atomExpr.expr.length() - 1);
					ImcNAME imc = (ImcNAME) ImcGen.exprImCode.get(atomExpr);

					Chunks.dataChunks.add(new DataChunk(new AbsAccess((init.length() + 1) * (new SemCharType()).size(), imc.label, init)));
				}

				return null;
			}

			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsAtomType atomType, Object visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsBinExpr binExpr, Object visArg) {
		switch (state.peek()) {
			default:
			binExpr.fstExpr.accept(this, visArg);
			binExpr.sndExpr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsBlockExpr blockExpr, Object visArg) {
		switch (state.peek()) {
			default:
			blockExpr.decls.accept(this, visArg);
			blockExpr.stmts.accept(this, visArg);
			blockExpr.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsCastExpr castExpr, Object visArg) {
		switch (state.peek()) {
			default:
			castExpr.type.accept(this, visArg);
			castExpr.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsCompDecl compDecl, Object visArg) {
		switch (state.peek()) {
			default:
			compDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsCompDecls compDecls, Object visArg) {
		switch (state.peek()) {
			default:
			for (AbsCompDecl compDecl : compDecls.compDecls())
				compDecl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsDecls decls, Object visArg) {
		switch (state.peek()) {
			default:
			for (AbsDecl decl : decls.decls())
				decl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsDelExpr delExpr, Object visArg) {
		switch (state.peek()) {
			default:
			delExpr.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsExprStmt exprStmt, Object visArg) {
		switch (state.peek()) {
			default:
			exprStmt.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsFunDecl funDecl, Object visArg) {
		switch (state.peek()) {
			default:
			funDecl.parDecls.accept(this, visArg);
			funDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsFunDef funDef, Object visArg) {
		switch (state.peek()) {
			case 2: {
				funDef.value.accept(this, visArg);

				ImcGen.exprImCode.get(funDef.value).accept(new StmtCanonizer(), null);
				Temp temp = new Temp();
				ImcExpr fExpr = StmtCanonizer.iexpr.pop();
				Vector<ImcStmt> fStmt = StmtCanonizer.istmt.pop();

				Vector<ImcStmt> stmt = new Vector<ImcStmt>();
				stmt.addAll(fStmt);
				stmt.add(new ImcMOVE(new ImcTEMP(temp), fExpr));

				Chunks.codeChunks.add(new CodeChunk(Frames.frames.get(funDef), stmt, new Label(), new Label()));

				return null;
			}

			default:
			funDef.parDecls.accept(this, visArg);
			funDef.type.accept(this, visArg);
			funDef.value.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsFunName funName, Object visArg) {
		switch (state.peek()) {
			default:
			funName.args.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsIfStmt ifStmt, Object visArg) {
		switch (state.peek()) {
			default:
			ifStmt.cond.accept(this, visArg);
			ifStmt.thenStmts.accept(this, visArg);
			ifStmt.elseStmts.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsNewExpr newExpr, Object visArg) {
		switch (state.peek()) {
			default:
			newExpr.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsParDecl parDecl, Object visArg) {
		switch (state.peek()) {
			default:
			parDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsParDecls parDecls, Object visArg) {
		switch (state.peek()) {
			default:
			for (AbsParDecl parDecl : parDecls.parDecls())
				parDecl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsPtrType ptrType, Object visArg) {
		switch (state.peek()) {
			default:
			ptrType.ptdType.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsRecExpr recExpr, Object visArg) {
		switch (state.peek()) {
			default:
			recExpr.record.accept(this, visArg);
			recExpr.comp.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsRecType recType, Object visArg) {
		switch (state.peek()) {
			default:
			recType.compDecls.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsSource source, Object visArg) {
		src = source;

		state.push(1);
		source.decls.accept(this, visArg);
		state.pop();

		state.push(2);
		source.decls.accept(this, visArg);
		state.pop();

		return null;
	}

	@Override
	public Object visit(AbsStmts stmts, Object visArg) {
		switch (state.peek()) {
			default:
			for (AbsStmt stmt : stmts.stmts())
				stmt.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsTypDecl typDecl, Object visArg) {
		switch (state.peek()) {
			default:
			typDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsTypName typName, Object visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsUnExpr unExpr, Object visArg) {
		switch (state.peek()) {
			default:
			unExpr.subExpr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsVarDecl varDecl, Object visArg) {
		switch (state.peek()) {
			case 1: {
				Access acs = Frames.accesses.get(varDecl);

				if (acs instanceof AbsAccess) {
					Chunks.dataChunks.add(new DataChunk((AbsAccess) acs));
				}

				return null;
			}

			default:
			varDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsVarName varName, Object visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsWhileStmt whileStmt, Object visArg) {
		switch (state.peek()) {
			default:
			whileStmt.cond.accept(this, visArg);
			whileStmt.stmts.accept(this, visArg);
			return null;
		}
	}
}