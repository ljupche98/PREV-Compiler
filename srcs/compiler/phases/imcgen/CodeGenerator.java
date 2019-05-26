/**
 * @author sliva
 */
package compiler.phases.imcgen;

import java.util.*;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;
import compiler.data.layout.*;
import compiler.data.type.*;
import compiler.data.imcode.*;
import compiler.phases.frames.*;
import compiler.phases.seman.*;
import compiler.common.report.*;

/**
 * Intermediate code generator.
 * 
 * This is a plain full visitor
 * 
 * @author sliva
 */
public class CodeGenerator extends AbsFullVisitor<Object, Stack<Frame>> {

	/**
	States 1, 2 and 3 properly modify the stack 'istack'.

	0 ... Maps function declarations to its parent's declaration or null if the function is on depth 1 (= does not have parent)
	1 ... []ADDR. Maps strings to labels. Get addresses of variable accesses. (except for arrays and record components)	access
	2 ... []EXPR. Constructs ImcExpr from AbsExpr.										iexpr
	3 ... []STMT. Constructs statements.
	**/

	private static HashMap<AbsAtomExpr, AbsAccess> stringAccess = new HashMap<AbsAtomExpr, AbsAccess>();
	private static HashMap<AbsExpr, Access> access = new HashMap<AbsExpr, Access>();
	private static HashMap<AbsExpr, ImcExpr> iexpr = new HashMap<AbsExpr, ImcExpr>();


	/** Maps function declarations to its parent's declaration or null if the function is on depth 1 (== does not have parent) **/
	private static HashMap<AbsDecl, AbsDecl> parent = new HashMap<AbsDecl, AbsDecl>();
	private static Stack<AbsDecl> fstack = new Stack<AbsDecl>();


	private static Stack<ImcInstr> istack = new Stack<ImcInstr>();

	private static AbsSource src;
	private static Stack<Integer> state = new Stack<Integer>();


	private static int level;
	private static Stack<Frame> frstack = new Stack<Frame>();


    	@Override
	public Object visit(AbsArgs args, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			for (AbsExpr arg : args.args())
				arg.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsArrExpr arrExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 1: {
				SemType type = SemAn.isOfType.get(arrExpr);

				state.push(1);
				arrExpr.array.accept(this, visArg);
				state.pop();
				ImcExpr addr1 = (ImcExpr) istack.pop();

				state.push(2);
				arrExpr.index.accept(this, visArg);
				state.pop();
				ImcExpr addr2 = (ImcExpr) istack.pop();

				ImcBINOP binop = new ImcBINOP(ImcBINOP.Oper.ADD,
								addr1,
								new ImcBINOP(ImcBINOP.Oper.MUL,
										addr2,
										new ImcCONST(type.size())
									)
							);

				istack.push(binop);
				ImcGen.exprImCode.put(arrExpr, binop);

				return null;
			}

			case 2: {
				state.push(1);
				arrExpr.accept(this, visArg);
				state.pop();

				ImcExpr expr = (ImcExpr) istack.pop();
				ImcMEM mem = new ImcMEM(expr);
				istack.push(mem);
				ImcGen.exprImCode.put(arrExpr, mem);
				return null;
			}

			default:
			arrExpr.array.accept(this, visArg);
			arrExpr.index.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsArrType arrType, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			arrType.len.accept(this, visArg);
			arrType.elemType.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsAssignStmt assignStmt, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 3: {
				state.push(1);
				assignStmt.dst.accept(this, visArg);
				state.pop();
				ImcExpr dst = (ImcExpr) istack.pop();

				state.push(2);
				assignStmt.src.accept(this, visArg);
				state.pop();
				ImcExpr src = (ImcExpr) istack.pop();

				ImcMOVE node = new ImcMOVE(new ImcMEM(dst), src);
				ImcGen.stmtImCode.put(assignStmt, node);
				istack.push(node);
				return null;
			}

			default:
			assignStmt.dst.accept(this, visArg);
			assignStmt.src.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsAtomExpr atomExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 1: {
				switch (atomExpr.type) {
					case STR: {
						AbsAccess acs = new AbsAccess((atomExpr.expr.length() + 1) * (new SemCharType()).size(), new Label(), atomExpr.expr);
					///	stringAccess.put(atomExpr, acs);
						access.put(atomExpr, acs);
	
						ImcNAME node = new ImcNAME(acs.label);
						istack.push(node);
						ImcGen.exprImCode.put(atomExpr, node);
						return null;
					}

					default: return null;
				}
			}

			case 2: {
				switch (atomExpr.type) {
					case INT: {
						ImcCONST node = new ImcCONST(Long.valueOf(atomExpr.expr));
						istack.push(node);
						ImcGen.exprImCode.put(atomExpr, node);
						return null;
					}

					case CHAR: {
						if (atomExpr.expr.length() != 3) throw new Report.Error("Character cannot be converted to ASCII code");
						else {
							ImcCONST node = new ImcCONST(Long.valueOf(atomExpr.expr.charAt(1)));
							istack.push(node);
							ImcGen.exprImCode.put(atomExpr, node);
						}

						return null;
					}

					case BOOL: {
						ImcCONST node = new ImcCONST(atomExpr.expr.equals("true") ? 1 : 0);
						istack.push(node);
						ImcGen.exprImCode.put(atomExpr, node);
						return null;
					}

					case PTR:
					case VOID: {
						ImcCONST node = new ImcCONST(0);
						istack.push(node);
						ImcGen.exprImCode.put(atomExpr, node);
						return null;
					}

					case STR: {
						state.push(1);
						atomExpr.accept(this, visArg);
						state.pop();
						return null;
					/**
						AbsAccess acs = (AbsAccess) access.get(atomExpr);
						ImcNAME node = new ImcNAME(acs.label);
						istack.push(node);
						ImcGen.exprImCode.put(atomExpr, node);
					**/
					}

					default: return null;
				}
			}


			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsAtomType atomType, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsBinExpr binExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 2: {
				state.push(2);
				binExpr.fstExpr.accept(this, visArg);
				state.pop();
				ImcExpr fstExpr = (ImcExpr) istack.pop();

				state.push(2);
				binExpr.sndExpr.accept(this, visArg);
				state.pop();
				ImcExpr sndExpr = (ImcExpr) istack.pop();

				ImcBINOP.Oper oper = ImcBINOP.Oper.IOR;
				switch (binExpr.oper) {
					case IOR: oper = ImcBINOP.Oper.IOR; break;
					case XOR: oper = ImcBINOP.Oper.XOR; break;
					case AND: oper = ImcBINOP.Oper.AND; break;
					case EQU: oper = ImcBINOP.Oper.EQU; break;
					case NEQ: oper = ImcBINOP.Oper.NEQ; break;
					case LTH: oper = ImcBINOP.Oper.LTH; break;
					case GTH: oper = ImcBINOP.Oper.GTH; break;
					case LEQ: oper = ImcBINOP.Oper.LEQ; break;
					case GEQ: oper = ImcBINOP.Oper.GEQ; break;
					case ADD: oper = ImcBINOP.Oper.ADD; break;
					case SUB: oper = ImcBINOP.Oper.SUB; break;
					case MUL: oper = ImcBINOP.Oper.MUL; break;
					case DIV: oper = ImcBINOP.Oper.DIV; break;
					case MOD: oper = ImcBINOP.Oper.MOD; break;
					default: break;
				}

				ImcBINOP node = new ImcBINOP(oper, fstExpr, sndExpr);
				istack.push(node);
				ImcGen.exprImCode.put(binExpr, node);
				return null;
			}

			default:
			binExpr.fstExpr.accept(this, visArg);
			binExpr.sndExpr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsBlockExpr blockExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 2: {
				state.push(3);
				blockExpr.decls.accept(this, visArg);
				state.pop();

				state.push(3);
				blockExpr.stmts.accept(this, visArg);
				state.pop();
				ImcStmt stmts = (ImcStmt) istack.pop();

				state.push(2);
				blockExpr.expr.accept(this, visArg);
				state.pop();
				ImcExpr expr = (ImcExpr) istack.pop();

				ImcSEXPR node = new ImcSEXPR(stmts, expr);
				istack.push(node);
				ImcGen.exprImCode.put(blockExpr, node);
				return null;
			}

			default:
			blockExpr.decls.accept(this, visArg);
			blockExpr.stmts.accept(this, visArg);
			blockExpr.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsCastExpr castExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 2: {
				state.push(2);
				castExpr.expr.accept(this, visArg);
				state.pop();
				ImcExpr expr = (ImcExpr) istack.pop();
		
				SemType type = SemAn.isOfType.get(castExpr);
				if (type.matches(new SemCharType())) {
					ImcBINOP node = new ImcBINOP(ImcBINOP.Oper.MOD, expr, new ImcCONST(256));
					istack.push(node);
					ImcGen.exprImCode.put(castExpr, node);
					return null;
				}

				istack.push(expr);
				ImcGen.exprImCode.put(castExpr, expr);
				return null;
			}

			default:
			castExpr.type.accept(this, visArg);
			castExpr.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsCompDecl compDecl, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			compDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsCompDecls compDecls, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			for (AbsCompDecl compDecl : compDecls.compDecls())
				compDecl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsDecls decls, Stack<Frame> visArg) {
		switch (state.peek()) {
			
			default:
			for (AbsDecl decl : decls.decls())
				decl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsDelExpr delExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 2: {
				Vector<ImcExpr> args = new Vector<ImcExpr>();
				args.add(new ImcCONST(0));			/// static link of a global function. should never be used.

				state.push(2);
				delExpr.expr.accept(this, visArg);
				state.pop();
				ImcExpr expr = (ImcExpr) istack.pop();
				args.add(expr);

				ImcCALL node = new ImcCALL(new Label("del"), args);
				ImcGen.exprImCode.put(delExpr, node);
				istack.push(node);
				return null;
			}

			default:
			delExpr.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsExprStmt exprStmt, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 3: {
				state.push(2);
				exprStmt.expr.accept(this, visArg);
				state.pop();
				ImcExpr expr = (ImcExpr) istack.pop();

				ImcESTMT node = new ImcESTMT(expr);
				istack.push(node);
				ImcGen.stmtImCode.put(exprStmt, node);
				return null;
			}

			default:
			exprStmt.expr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsFunDecl funDecl, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 0: {
				++level;
				parent.put(funDecl, fstack.peek());
				--level;
				return null;
			}

			default:
			++level;
			funDecl.parDecls.accept(this, visArg);
			funDecl.type.accept(this, visArg);
			--level;
			return null;
		}
	}

	@Override
	public Object visit(AbsFunDef funDef, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 0: {
				++level;
				parent.put(funDef, fstack.peek());
				fstack.push(funDef);
				funDef.value.accept(this, visArg);
				fstack.pop();
				--level;
				return null;
			}

			case 3: {
				++level;
				state.push(2);
				frstack.push(Frames.frames.get(funDef));
				funDef.value.accept(this, visArg);
				frstack.pop();
				state.pop();
				--level;
				return null;
			}

			default:
			++level;
			funDef.parDecls.accept(this, visArg);
			funDef.type.accept(this, visArg);
			funDef.value.accept(this, visArg);
			--level;
			return null;
		}
	}

	public ImcTEMP getFP() {
		return new ImcTEMP(frstack.peek().FP);
	}

	public ImcExpr getSP() {
		return new ImcBINOP(ImcBINOP.Oper.ADD, getFP(), new ImcCONST(frstack.peek().size));
	}

	public ImcExpr getSL(int lvl, int dlvl) {
		if (lvl == 1) return new ImcCONST(0);

		ImcExpr SL = getFP();

		for (int i = 0; i < dlvl; i++)
			SL = new ImcMEM(SL);

		return SL;
	}

	@Override
	public Object visit(AbsFunName funName, Stack<Frame> visArg) {
		switch (state.peek()) {	
			case 2: {
				/** Construct the static link. **/
			///	System.out.println(funName.name + " " + frstack.peek().depth + " ... " + Frames.frames.get((AbsFunDecl) SemAn.declaredAt.get(funName)).depth);
				int dlvl = frstack.peek().depth - Frames.frames.get((AbsFunDecl) SemAn.declaredAt.get(funName)).depth + 1;

				Vector<ImcExpr> args = new Vector<ImcExpr>();
				args.add(getSL(Frames.frames.get((AbsFunDecl) SemAn.declaredAt.get(funName)).depth, dlvl));

				for (AbsExpr arg : funName.args.args()) {
					state.push(2);
					arg.accept(this, visArg);
					state.pop();
					ImcExpr aexpr = (ImcExpr) istack.pop();

					ImcGen.exprImCode.put(arg, aexpr);
					args.add(aexpr);
				}

				ImcCALL node = new ImcCALL(Frames.frames.get((AbsFunDecl) SemAn.declaredAt.get(funName)).label, args);
				ImcGen.exprImCode.put(funName, node);
				istack.push(node);
				return null;
			}

			default:
			funName.args.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsIfStmt ifStmt, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 3: {
				Vector<ImcStmt> stmts = new Vector<ImcStmt>();

				state.push(2);
				ifStmt.cond.accept(this, visArg);
				state.pop();
				ImcExpr expr = (ImcExpr) istack.pop();	

				state.push(3);
				ifStmt.thenStmts.accept(this, visArg);
				state.pop();
				ImcStmt thenStmts = (ImcStmt) istack.pop();

				if (ifStmt.elseStmts.stmts().size() == 0) {
					// if expr then stmt end;

					Label beg = new Label();
					Label end = new Label();

					stmts.add(new ImcCJUMP(expr, beg, end));
					stmts.add(new ImcLABEL(beg));
					stmts.add(thenStmts);
					stmts.add(new ImcLABEL(end));
				} else {
					state.push(3);
					ifStmt.elseStmts.accept(this, visArg);
					state.pop();
					ImcStmt elseStmts = (ImcStmt) istack.pop();

					Label beg = new Label();
					Label mid = new Label();
					Label end = new Label();

					stmts.add(new ImcCJUMP(expr, beg, mid));
					stmts.add(new ImcLABEL(beg));
					stmts.add(thenStmts);
					stmts.add(new ImcJUMP(end));
					stmts.add(new ImcLABEL(mid));
					stmts.add(elseStmts);
					stmts.add(new ImcLABEL(end));
				}

				ImcSTMTS node = new ImcSTMTS(stmts);
				istack.push(node);
				ImcGen.stmtImCode.put(ifStmt, node);
				return null;
			}

			default:
			ifStmt.cond.accept(this, visArg);
			ifStmt.thenStmts.accept(this, visArg);
			ifStmt.elseStmts.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsNewExpr newExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 2: {
				Vector<ImcExpr> args = new Vector<ImcExpr>();
				args.add(new ImcCONST(0));	/// static link of a global function. should never be used.

				SemType argType = newExpr.type.accept(new TypeResolver(true), 1);
				ImcCONST sz = new ImcCONST(argType.size());
				args.add(sz);

				ImcCALL node = new ImcCALL(new Label("new"), args);
				ImcGen.exprImCode.put(newExpr, node);
				istack.push(node);
				return null;
			}

			default:
			newExpr.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsParDecl parDecl, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			parDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsParDecls parDecls, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			for (AbsParDecl parDecl : parDecls.parDecls())
				parDecl.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsPtrType ptrType, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			ptrType.ptdType.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsRecExpr recExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 1: {
				state.push(1);
				recExpr.record.accept(this, visArg);
				state.pop();

				AbsCompDecl compDecl = (AbsCompDecl) SemAn.declaredAt.get(recExpr.comp);
				AbsRecType recDecl = SemAn.compOf.get(compDecl);
				SemRecType recType = (SemRecType) SemAn.isType.get(recDecl);

				long offset = 0;
				for (int i = 0; i < recDecl.compDecls.compDecls().size(); i++) {
					if (recDecl.compDecls.compDecl(i).name.equals(recExpr.comp.name)) {
						break;
					}

					offset += recType.compType(i).size();
				}

				ImcExpr prev = (ImcExpr) istack.pop();
				ImcBINOP binop = new ImcBINOP(ImcBINOP.Oper.ADD, prev, new ImcCONST(offset));	

				istack.push(binop);
				ImcGen.exprImCode.put(recExpr, binop);

				return null;
			}

			case 2: {
				state.push(1);
				recExpr.accept(this, visArg);
				state.pop();

				ImcExpr expr = (ImcExpr) istack.pop();
				ImcMEM mem = new ImcMEM(expr);
				istack.push(mem);
				ImcGen.exprImCode.put(recExpr, mem);
				return null;
			}

			default:
			recExpr.record.accept(this, visArg);
		///	recExpr.comp.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsRecType recType, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			recType.compDecls.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsSource source, Stack<Frame> visArg) {
		src = source;

		state.push(0);
		fstack.push(null);
		source.decls.accept(this, visArg);
		fstack.pop();
		state.pop();

		//state.push(1);
		//source.decls.accept(this, visArg);
		//state.pop();

		//state.push(2);
		//source.decls.accept(this, visArg);
		//state.pop();

		state.push(3);
		source.decls.accept(this, visArg);
		state.pop();

		return null;
	}

	@Override
	public Object visit(AbsStmts stmts, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 3: {
				Vector<ImcStmt> stmtv = new Vector<ImcStmt>();

				for (AbsStmt stmt : stmts.stmts()) {
					state.push(3);
					stmt.accept(this, visArg);
					state.pop();

					stmtv.add((ImcStmt) istack.pop());
				}

				ImcSTMTS node = new ImcSTMTS(stmtv);
				istack.push(node);
				return null;
			}

			default:
			for (AbsStmt stmt : stmts.stmts())
				stmt.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsTypDecl typDecl, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			typDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsTypName typName, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsUnExpr unExpr, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 1: {
				switch (unExpr.oper) {
					case ADDR: {
						state.push(1);
						unExpr.subExpr.accept(this, visArg);
						state.pop();

					///	access.put(unExpr, access.get(unExpr.subExpr));

						ImcExpr addr = (ImcExpr) istack.peek();
						ImcMEM node = new ImcMEM(addr);
						ImcGen.exprImCode.put(unExpr, node);
						istack.push(node);
						return node;
					}

					case DATA: {
						state.push(2);
						unExpr.subExpr.accept(this, visArg);
						state.pop();

					///	access.put(unExpr, access.get(unExpr.subExpr));				

						ImcMEM ptr = (ImcMEM) istack.peek();
						ImcGen.exprImCode.put(unExpr, ptr);
						istack.push(ptr);
						return ptr;
					}

					default: return null;
				}
			}

			case 2: {

				switch (unExpr.oper) {
					case ADD: {
						state.push(2);
						unExpr.subExpr.accept(this, visArg);
						state.pop();

						ImcGen.exprImCode.put(unExpr, (ImcExpr) istack.peek());
						return null;
					}

					case SUB: {
						state.push(2);
						unExpr.subExpr.accept(this, visArg);
						state.pop();

						ImcExpr subExpr = (ImcExpr) istack.pop();
						ImcUNOP node = new ImcUNOP(ImcUNOP.Oper.NEG, subExpr);
						istack.push(node);
						ImcGen.exprImCode.put(unExpr, node);
						return null;
					}

					case NOT: {
						state.push(2);
						unExpr.subExpr.accept(this, visArg);
						state.pop();
	
						ImcExpr subExpr = (ImcExpr) istack.pop();
						ImcUNOP node = new ImcUNOP(ImcUNOP.Oper.NOT, subExpr);
						istack.push(node);
						ImcGen.exprImCode.put(unExpr, node);
						return null;
					}

					case ADDR: {
						state.push(1);
						unExpr.subExpr.accept(this, visArg);
						state.pop();

					///	access.put(unExpr, access.get(unExpr.subExpr));

						ImcExpr addr = (ImcExpr) istack.peek();
						ImcMEM node = new ImcMEM(addr);
						ImcGen.exprImCode.put(unExpr, node);
						istack.push(node);
						return node;
					}

					case DATA: {
						state.push(2);
						unExpr.subExpr.accept(this, visArg);
						state.pop();

					///	access.put(unExpr, access.get(unExpr.subExpr));				

						ImcMEM ptr = new ImcMEM((ImcExpr) istack.peek());
						ImcGen.exprImCode.put(unExpr, ptr);
						istack.push(ptr);
						return ptr;
					}

					default: return null;
				}
			}

			default:
			unExpr.subExpr.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsVarDecl varDecl, Stack<Frame> visArg) {
		switch (state.peek()) {
			default:
			varDecl.type.accept(this, visArg);
			return null;
		}
	}

	@Override
	public Object visit(AbsVarName varName, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 1: {
				AbsDecl decl = SemAn.declaredAt.get(varName);

				if (decl instanceof AbsVarDecl) {
					Access acs = Frames.accesses.get((AbsVarDecl) decl);
					access.put(varName, acs);

					if (acs instanceof AbsAccess) {
						ImcNAME node = new ImcNAME(((AbsAccess) acs).label);
						istack.push(node);
						ImcGen.exprImCode.put(varName, node);
						return null;
					} else {
						int dlvl = frstack.peek().depth - ((RelAccess) acs).depth;
						long offset = ((RelAccess) acs).offset;

						ImcTEMP temp = getFP();
						if (dlvl < 0) throw new Report.Error("A lower level function cannot access high level function's variable (" + varName.name + ", " + varName + ")");
						else {
							ImcBINOP binop;

							if (dlvl == 0) binop = new ImcBINOP(ImcBINOP.Oper.ADD, temp, new ImcCONST(offset));
							else {
								ImcMEM mem = new ImcMEM(temp);

								for (int i = 1; i < dlvl; i++)
									mem = new ImcMEM(mem);

								binop = new ImcBINOP(ImcBINOP.Oper.ADD, mem, new ImcCONST(offset));
							}

							istack.push(binop);
							ImcGen.exprImCode.put(varName, binop);
						}

						return null;
					}
				} else throw new Report.Error("Declaration at " + varName + " that " + varName.name + " is pointing to is not a variable declaration");
			}

			case 2: {
				AbsDecl decl = SemAn.declaredAt.get(varName);

				if (decl instanceof AbsVarDecl) {
					Access acs = Frames.accesses.get((AbsVarDecl) decl);
					access.put(varName, acs);

					state.push(1);
					varName.accept(this, visArg);
					state.pop();
					ImcExpr expr = (ImcExpr) istack.pop();

					ImcMEM node = new ImcMEM(expr);
					istack.push(node);
					ImcGen.exprImCode.put(varName, node);
					return null;

					/**
					if (acs instanceof AbsAccess) {
						ImcNAME node = new ImcNAME(((AbsAccess) acs).label);
						ImcMEM mem = new ImcMEM(node);
						istack.push(mem);
						ImcGen.exprImCode.put(varName, mem);
						return null;
					} else {
						int dlvl = frstack.peek().depth - frstack.peek().depth;
						long offset = frstack.peek().offset;


					}
						ImcTEMP temp = getFP();
						if (dlvl < 0) throw new Report.Error("A lower level function cannot access high level function's variable (" + varName.name + ", " + varName + ")");
						else {
							ImcBINOP binop;

							if (dlvl == 0) binop = new ImcBINOP(ImcBINOP.Oper.ADD, temp, new ImcCONST(offset));
							else {
								ImcMEM mem = new ImcMEM(temp);

								for (int i = 1; i < dlvl; i++)
									mem = new ImcMEM(mem);

								binop = new ImcBINOP(ImcBINOP.Oper.ADD, mem, new ImcCONST(offset));
							}

							ImcMEM mem = new ImcMEM(binop);
							istack.push(mem);
							ImcGen.exprImCode.put(varName, mem);
						}
					**/
				} else throw new Report.Error("Declaration at " + varName + " that " + varName.name + " is pointing to is not a variable declaration");
			}

			default:
			return null;
		}
	}

	@Override
	public Object visit(AbsWhileStmt whileStmt, Stack<Frame> visArg) {
		switch (state.peek()) {
			case 3: {
				Vector<ImcStmt> stmts = new Vector<ImcStmt>();

				state.push(2);
				whileStmt.cond.accept(this, visArg);
				state.pop();
				ImcExpr expr = (ImcExpr) istack.pop();

				state.push(3);
				whileStmt.stmts.accept(this, visArg);
				state.pop();
				ImcStmt execStmt = (ImcStmt) istack.pop();

				Label beg = new Label();
				Label mid = new Label();
				Label end = new Label();

				stmts.add(new ImcLABEL(beg));
				stmts.add(new ImcCJUMP(expr, mid, end));
				stmts.add(new ImcLABEL(mid));
				stmts.add(execStmt);
				stmts.add(new ImcJUMP(beg));
				stmts.add(new ImcLABEL(end));

				ImcSTMTS node = new ImcSTMTS(stmts);
				istack.push(node);
				ImcGen.stmtImCode.put(whileStmt, node);
				return null;
			}

			default:
			whileStmt.cond.accept(this, visArg);
			whileStmt.stmts.accept(this, visArg);
			return null;
		}
	}

}
