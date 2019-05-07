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
 * Type resolving: the result is stored in {@link SemAn#declaresType},
 * {@link SemAn#isType}, and {@link SemAn#ofType}.
 * 
 * @author sliva
 */
public class TypeResolver extends AbsFullVisitor<SemType, Object> {

	/** Symbol tables of individual record types. */
	private final HashMap<SemRecType, SymbTable> symbTables = new HashMap<SemRecType, SymbTable>();

	/**
	0: Type declarations. SemAn.declaresType
	1: Type expressions. SemAn.isType
		2: Calculate expression. An integer is expected. Is used for the array length. Result is stored in exprVaue.
		3: Record expression. Go through compDecls and add them to recDecl.
	4: Value expressions. SemAn.isOfType
	5: Assert parameter declarations are not of type void.
	
	**/

	private boolean lock = false;
	public TypeResolver() {}
	public TypeResolver(boolean l) {
		this.lock = l;
	}

	private static long exprValue = 0;
	private static Vector<SemType> recDecl = new Vector<SemType>();

	@Override
	public SemType visit(AbsArgs args, Object visArg) {
		switch ((int) visArg) {

			default:
				for (AbsExpr arg : args.args())
					arg.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsArrExpr arrExpr, Object visArg) {
		switch ((int) visArg) {
			case 2:
				throw new Report.Error("Cannot evaluate expression " + arrExpr);

			case 4: {
				SemType array = arrExpr.array.accept(this, 4);
				SemType index = arrExpr.index.accept(this, 4);
				if (!(array instanceof SemArrType)) throw new Report.Error("Array type is expected at " + arrExpr);
				if (!index.matches(new SemIntType())) throw new Report.Error("Expression of type int is expected when accessing array element at " + arrExpr);

				SemType ret = ((SemArrType) array).elemType;
				if (!lock) SemAn.isOfType.put(arrExpr, ret);
				return ret;
			}
			
			default:
				arrExpr.array.accept(this, visArg);
				arrExpr.index.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsArrType arrType, Object visArg) {
		switch ((int) visArg) {
			case 1: {
				long fv = TypeResolver.exprValue;

				TypeResolver.exprValue = 0;
				arrType.len.accept(this, 2);
				if (TypeResolver.exprValue <= 0) throw new Report.Error("A positive number is expected for length of an array at " + arrType);

				SemType type = arrType.elemType.accept(this, 1);
				if (type instanceof SemVoidType) throw new Report.Error("Array at " + arrType + " cannot be of type void");

				SemArrType ret = new SemArrType(TypeResolver.exprValue, type);
				if (!lock) SemAn.isType.put(arrType, ret);

				TypeResolver.exprValue = fv;
				return ret;
			}

			case 4: return SemAn.isType.get(arrType);

			default:
				arrType.len.accept(this, visArg);
				arrType.elemType.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsAssignStmt assignStmt, Object visArg) {
		switch ((int) visArg) {
			case 2:
				throw new Report.Error("Cannot evaluate expression");

			case 4: {
				SemType dst = assignStmt.dst.accept(this, 4);
				SemType src = assignStmt.src.accept(this, 4);
				SemType reqInt = new SemIntType();
				SemType reqChar = new SemCharType();
				SemType reqBool = new SemBoolType();
				if (!dst.matches(src)) throw new Report.Error("Expressions at " + assignStmt + " in assign statement must be of same type");
				if (!dst.matches(reqInt) && !dst.matches(reqChar) && !dst.matches(reqBool) && !(dst instanceof SemPtrType)) throw new Report.Error("Expected int, char, bool or pointer type expressions in assign statement at " + assignStmt);

				SemType ret = new SemVoidType();
			///	SemAn.isOfType.put(assignStmt, ret);
				return ret;
			}

			default:
				assignStmt.dst.accept(this, visArg);
				assignStmt.src.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsAtomExpr atomExpr, Object visArg) {
		switch ((int) visArg) {
			case 2: {
				if (TypeResolver.exprValue < 0) return null;
				if (atomExpr.type == AbsAtomExpr.Type.INT) {
					TypeResolver.exprValue += Integer.valueOf(atomExpr.expr);
					return null;
				}

				throw new Report.Error("Cannot evaluate expression at " + atomExpr);
			}

			case 4: {
				SemType ret;
				switch (atomExpr.type) {
					case INT:
						ret = new SemIntType();
						if (!lock) SemAn.isOfType.put(atomExpr, ret);
						return ret;

					case CHAR:
						ret = new SemCharType();
						if (!lock) SemAn.isOfType.put(atomExpr, ret);
						return ret;

					case BOOL:
						ret = new SemBoolType();
						if (!lock) SemAn.isOfType.put(atomExpr, ret);
						return ret;

					case VOID:
						ret = new SemVoidType();
						if (!lock) SemAn.isOfType.put(atomExpr, ret);
						return ret;

					case PTR:
						ret = new SemPtrType(new SemVoidType());
						if (!lock) SemAn.isOfType.put(atomExpr, ret);
						return ret;

					case STR:
						ret = new SemPtrType(new SemCharType());
						if (!lock) SemAn.isOfType.put(atomExpr, ret);
						return ret;

					default: throw new Report.Error("Cannot determine type of atomic expression at " + atomExpr);
				}
			}

			default:
				return null;
		}
	}

	@Override
	public SemType visit(AbsAtomType atomType, Object visArg) {
		switch ((int) visArg) {
			case 0:
			case 1: 
			case 4: {
				switch (atomType.type) {
					case INT: return new SemIntType();
					case CHAR: return new SemCharType();
					case BOOL: return new SemBoolType();
					case VOID: return new SemVoidType();

					default: throw new Report.Error("Cannot type resolve an atom type at " + atomType);
				}
			}

			default:
				return null;
		}
	}

	@Override
	public SemType visit(AbsBinExpr binExpr, Object visArg) {
		switch ((int) visArg) {
			case 2: {
				long fv = TypeResolver.exprValue;
				TypeResolver.exprValue = 0;
				binExpr.fstExpr.accept(this, 2);

				long sv = TypeResolver.exprValue;
				TypeResolver.exprValue = 0;
				binExpr.sndExpr.accept(this, 2);

				long tv = TypeResolver.exprValue;

				switch (binExpr.oper) {
					case ADD: TypeResolver.exprValue = fv + sv + tv; break;
					case SUB: TypeResolver.exprValue = fv + sv - tv; break;
					case MUL: TypeResolver.exprValue = fv + sv * tv; break;
					case DIV: TypeResolver.exprValue = fv + sv / tv; break;
					case MOD: TypeResolver.exprValue = fv + sv % tv; break;

					default: throw new Report.Error("Cannot evaluate expression at " + binExpr);
				}

				return null;
			}

			case 4: {
				SemType f = binExpr.fstExpr.accept(this, 4);
				SemType s = binExpr.sndExpr.accept(this, 4);
				switch (binExpr.oper) {
					case IOR:
					case XOR:
					case AND:
						SemType req = (SemType) new SemBoolType();
						if (!f.matches(req)) throw new Report.Error("Expression of type boolean is expected as first operand of a &, |, ^ logical expression at " + binExpr);
						if (!s.matches(req)) throw new Report.Error("Expression of type boolean is expected as second operand of a &, |, ^ logical expression at " + binExpr);

						if (!lock) SemAn.isOfType.put(binExpr, req);
						return req;

					case ADD:
					case SUB:
					case MUL:
					case DIV:
					case MOD: {
						SemType reqInt = (SemType) new SemIntType();
						SemType reqChar = (SemType) new SemCharType();
						if (!f.matches(s)) throw new Report.Error("Expressions of same types are expected in a +, -, *, /, % arithmetic expression at " + binExpr);
						if (!f.matches(reqInt) && !f.matches(reqChar)) throw new Report.Error("Expressions of type int or char are expected for +, -, *, /, % arithmetic expression at " + binExpr);

						if (!lock) SemAn.isOfType.put(binExpr, reqInt);
						return reqInt;
					}

					case EQU:
					case NEQ: {
						SemType reqInt = (SemType) new SemIntType();
						SemType reqChar = (SemType) new SemCharType();
						SemType reqBool = (SemType) new SemBoolType();
						if (!f.matches(s)) throw new Report.Error("Expressions of same types are expected in a ==, != comparison expression at " + binExpr + " " + f + " " + s);
						if (!f.matches(reqInt) && !f.matches(reqChar) && !f.matches(reqBool) && !(f instanceof SemPtrType)) throw new Report.Error("Expressions of type int, char, bool or pointer are expected for a ==, != comparison expression at " + binExpr);

						if (!lock) SemAn.isOfType.put(binExpr, reqBool);
						return reqBool;
					}

					case LTH:
					case GTH:
					case LEQ:
					case GEQ: {
						SemType reqInt = (SemType) new SemIntType();
						SemType reqChar = (SemType) new SemCharType();
						SemType reqBool = (SemType) new SemBoolType();
						if (!f.matches(s)) throw new Report.Error("Expressions of same types are expected in a >, <, >=, <= relational expression at " + binExpr);
						if (!f.matches(reqInt) && !f.matches(reqChar) && !(f instanceof SemPtrType)) throw new Report.Error("Expressions of type int, char or pointer are expected for a >, <, >=, <= relational expression at " + binExpr);

						if (!lock) SemAn.isOfType.put(binExpr, reqBool);
						return reqBool;
					}


					default: throw new Report.Error("Cannot determine type of binary expression at " + binExpr);
				}
			}

			default:
				binExpr.fstExpr.accept(this, visArg);
				binExpr.sndExpr.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsBlockExpr blockExpr, Object visArg) {
		switch ((int) visArg) {
			case 2: {
				blockExpr.expr.accept(this, 2);
				return null;
			}

			case 4: {
				blockExpr.decls.accept(this, 4);
				blockExpr.stmts.accept(this, 4);

				SemType ret = blockExpr.expr.accept(this, 4);
				if (!lock) SemAn.isOfType.put(blockExpr, ret);
				return ret;
			}

			default:
				blockExpr.decls.accept(this, visArg);
				blockExpr.stmts.accept(this, visArg);
				blockExpr.expr.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsCastExpr castExpr, Object visArg) {
		switch ((int) visArg) {
			case 2:
				throw new Report.Error("Cannot evaluate expression at " + castExpr);

			case 4: {
				SemType type = castExpr.type.accept(this, 1); /// !!!IMPORTANT: visArg = 1
				SemType expr = castExpr.expr.accept(this, 4);

				SemType reqInt = new SemIntType();
				SemType reqChar = new SemCharType();
				if (!expr.matches(reqInt) && !expr.matches(reqChar) && !(expr instanceof SemPtrType)) throw new Report.Error("Expected int, char or pointer type as expression in cast expression at " + castExpr);
				if (!type.matches(reqInt) && !type.matches(reqChar) && !(type instanceof SemPtrType)) throw new Report.Error("Expected int, char or pointer type as type in cast expression at " + castExpr);

				SemType ret = type;
				if (!lock) SemAn.isOfType.put(castExpr, ret);
				return ret;
			}

			default:
				castExpr.type.accept(this, visArg);
				castExpr.expr.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsCompDecl compDecl, Object visArg) {
		switch ((int) visArg) {
			case 3:
				return compDecl.type.accept(this, 1);

			case 4:
				return compDecl.type.accept(this, 4);

			default:
				compDecl.type.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsCompDecls compDecls, Object visArg) {
		switch ((int) visArg) {
			case 3: {
				TypeResolver.recDecl = new Vector<SemType>();
				for (AbsCompDecl compDecl : compDecls.compDecls()) {
					SemType type = compDecl.accept(this, 3);
					if (type instanceof SemVoidType) throw new Report.Error("Component of a record cannot be of type void at " + compDecl);

					recDecl.add(compDecl.accept(this, 3));
				}

				return null;
			}

			default:
				for (AbsCompDecl compDecl : compDecls.compDecls())
					compDecl.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsDecls decls, Object visArg) {
		switch ((int) visArg) {

			default:
				for (AbsDecl decl : decls.decls())
					decl.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsDelExpr delExpr, Object visArg) {
		switch ((int) visArg) {
			case 2:
				throw new Report.Error("Cannot evaluate expression");

			case 4: {
				SemType expr = delExpr.expr.accept(this, 4);
				if (!(expr instanceof SemPtrType)) throw new Report.Error("Expression of pointer type is expected in del(EXPR) expression at " + delExpr);
				if (expr.matches(new SemPtrType(new SemVoidType()))) throw new Report.Error("Expression must not point to void in del(EXPR) expression at " + delExpr);

				SemType ret = new SemVoidType();
				if (!lock) SemAn.isOfType.put(delExpr, ret);
				return ret;	
			}

			default:
				delExpr.expr.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsExprStmt exprStmt, Object visArg) {
		switch ((int) visArg) {

			default:
				exprStmt.expr.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsFunDecl funDecl, Object visArg) {
		switch ((int) visArg) {
			case 4: {
				funDecl.parDecls.accept(this, 4);
				SemType type = funDecl.type.accept(this, 4);
			///	SemAn.isOfType.put(funDecl, type);
				return type;
			}

			case 5: {
				funDecl.parDecls.accept(this, 5);
				funDecl.type.accept(this, 5);
				SemType type = funDecl.type.accept(this, 1);
				if (!type.matches(new SemVoidType()) && !type.matches(new SemIntType()) && !type.matches(new SemCharType()) && !type.matches(new SemBoolType()) && !(type instanceof SemPtrType))
					throw new Report.Error("Return type of a function must be of void, int, char, bool or pointer type");
				return null;
			}

			default:
				funDecl.parDecls.accept(this, visArg);
				funDecl.type.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsFunDef funDef, Object visArg) {
		switch ((int) visArg) {
			case 4: {
				funDef.parDecls.accept(this, 4);
				SemType type = funDef.type.accept(this, 4);
				SemType anst = funDef.value.accept(this, 4);
				if (!type.matches(anst)) throw new Report.Error("Type returned by function must be of same type as defined");
			///	SemAn.isOfType.put(funDef, type);
				return type;
			}

			default:
				funDef.parDecls.accept(this, visArg);
				funDef.type.accept(this, visArg);
				funDef.value.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsFunName funName, Object visArg) {
		switch ((int) visArg) {
			case 2:
				throw new Report.Error("Cannot evaluate expression");

			case 4: {
				AbsDecl fx = SemAn.declaredAt.get(funName);
				if (!(fx instanceof AbsFunDecl)) throw new Report.Error("Variable " + funName.name + " is not a function");

				AbsFunDecl decl = (AbsFunDecl) SemAn.declaredAt.get(funName);
				Vector<AbsParDecl> decls = decl.parDecls.parDecls();
				Vector<AbsExpr> args = funName.args.args();

				if (decls.size() != args.size()) throw new Report.Error("Number of parameters mismatch when calling function " + funName.name);

				for (int i = 0; i < decls.size(); i++) {
					SemType need = decls.get(i).accept(this, 4);
					SemType have = args.get(i).accept(this, 4);

					if (!(args.get(i) instanceof AbsAtomExpr)) {
						SemAn.isOfType.put(args.get(i), have);
					}

					if (!need.matches(have)) throw new Report.Error("Parameter mismatch. Expected " + need + " instead of " + have + ".");
					if (!need.matches(new SemIntType()) && !need.matches(new SemCharType()) && !need.matches(new SemBoolType())
						&& !(need instanceof SemPtrType)) throw new Report.Error("Expected int, char, bool or pointer as a parameter of a function " + funName.name);
				}

				SemType type = decl.type.accept(this, 4);
				if (!type.matches(new SemIntType()) && !type.matches(new SemCharType()) && !type.matches(new SemBoolType()) &&
					!type.matches(new SemVoidType()) && !(type instanceof SemPtrType))
					throw new Report.Error("Function " + funName.name + " must return int, char, bool or pointer");

				if (!lock) SemAn.isOfType.put(funName, type);
				return type;
			}

			default:
				funName.args.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsIfStmt ifStmt, Object visArg) {
		switch ((int) visArg) {
			case 4: {
				SemType cond = ifStmt.cond.accept(this, 4);
				SemType thenStmts = ifStmt.thenStmts.accept(this, 4);
				SemType elseStmts = ifStmt.elseStmts.accept(this, 4);
				SemType reqBool = new SemBoolType();
				SemType reqVoid = new SemVoidType();
				if (!cond.matches(reqBool)) throw new Report.Error("Condition expression in if statement must be a bool expression at " + ifStmt);
				if (!thenStmts.matches(reqVoid)) throw new Report.Error("Expected statements in then statement in if expression at " + ifStmt);
				if (elseStmts != null && !elseStmts.matches(reqVoid)) throw new Report.Error("Expected statements in else statement in if expression at " + ifStmt);

				SemType ret = new SemVoidType();
				return ret;
			}

			default:
				ifStmt.cond.accept(this, visArg);
				ifStmt.thenStmts.accept(this, visArg);
				ifStmt.elseStmts.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsNewExpr newExpr, Object visArg) {
		switch ((int) visArg) {
			case 2:
				throw new Report.Error("Cannot evaluate expression");

			case 4: {
				SemType type = newExpr.type.accept(this, 1); /// !!!IMPORTANT: visArg = 1, so that type is resolved.
				if (type instanceof SemVoidType) throw new Report.Error("Type void cannot be used for new(TYPE) expression at " + newExpr);

				SemType ret = new SemPtrType(type);
				if (!lock) SemAn.isOfType.put(newExpr, ret);
				return ret;
			}

			default:
				newExpr.type.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsParDecl parDecl, Object visArg) {
		switch ((int) visArg) {
			case 4: {
				SemType type = parDecl.type.accept(this, 4);
				return type;
			}

			case 5: {
				SemType type = parDecl.type.accept(this, 4);
				if (type.matches(new SemVoidType())) throw new Report.Error("Parameter of a function cannot be of type void at " + parDecl);
				if (!type.matches(new SemIntType()) && !type.matches(new SemCharType()) && !type.matches(new SemBoolType()) && !(type instanceof SemPtrType)) throw new Report.Error("Parameters of a function must be of int, char, bool or pointer type");
				return type;
			}

			default:
				parDecl.type.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsParDecls parDecls, Object visArg) {
		switch ((int) visArg) {

			default:
				for (AbsParDecl parDecl : parDecls.parDecls())
					parDecl.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsPtrType ptrType, Object visArg) {
		switch ((int) visArg) {
			case 1:
			case 4: {
				SemType type = ptrType.ptdType.accept(this, 1);
				SemPtrType ret = new SemPtrType(type);
				if (!lock) SemAn.isType.put(ptrType, ret);
				return ret;
			}

			default:
				ptrType.ptdType.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsRecExpr recExpr, Object visArg) {
		switch ((int) visArg) {
			case 2:
				throw new Report.Error("Cannot evaluate expression");

			case 4: {
				SemType record = recExpr.record.accept(this, 4);
				if (!(record instanceof SemRecType)) throw new Report.Error("Record expression is expected at " + recExpr);

				AbsDecl decl;
				SymbTable tab = symbTables.get((SemRecType) record);
				try {
					decl = tab.fnd(recExpr.comp.name);
				} catch (Exception e) {
					throw new Report.Error("Cannot find component name " + recExpr.comp.name);
				}

				SemType ret = decl.accept(this, 4);
				if (!lock) SemAn.declaredAt.put(recExpr.comp, decl);
				if (!lock) SemAn.isOfType.put(recExpr, ret);
				return ret;
			}

			default:
				recExpr.record.accept(this, visArg);
				recExpr.comp.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsRecType recType, Object visArg) {
		switch ((int) visArg) {
			case 1: {
				Vector<SemType> fv = TypeResolver.recDecl;

				recType.compDecls.accept(this, 3);	
				SemRecType type = new SemRecType(TypeResolver.recDecl);
				if (!lock) SemAn.isType.put(recType, type);

				for (AbsCompDecl decl : recType.compDecls.compDecls())
					SemAn.compOf.put(decl, recType);

				SymbTable tab = new SymbTable();
				for (AbsCompDecl decl : recType.compDecls.compDecls())
					try {
						tab.ins(decl.name, decl);
					} catch (Exception e) {}
				symbTables.put(type, tab);

				TypeResolver.recDecl = fv;
				return type;
			}

			case 4: return SemAn.isType.get(recType);

			default:
				recType.compDecls.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsSource source, Object visArg) {
		source.decls.accept(this, 6);

		source.decls.accept(this, 0);
		source.decls.accept(this, 1);
		source.decls.accept(this, 4);
		source.decls.accept(this, 5);
		return null;
	}

	@Override
	public SemType visit(AbsStmts stmts, Object visArg) {
		switch ((int) visArg) {
			case 4: {
				for (AbsStmt stmt : stmts.stmts())
					stmt.accept(this, visArg);
				return new SemVoidType();
			}

			default:
				for (AbsStmt stmt : stmts.stmts())
					stmt.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsTypDecl typDecl, Object visArg) {
		switch ((int) visArg) {
			case 0: {
				SemNamedType td = new SemNamedType(typDecl.name);
				td.define(typDecl.type.accept(this, 0));
				if (!lock) SemAn.declaresType.put(typDecl, td);
				return td;
			}

			case 1:
			case 4: {
				SemType type = typDecl.type.accept(this, visArg);
			///	SemAn.isType.put(typDecl.type, type);
				return type;
			}

			default:
				typDecl.type.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsTypName typName, Object visArg) {
		switch ((int) visArg) {
			case 0:
			case 1: {
				AbsDecl decl = SemAn.declaredAt.get(typName);
				return decl.accept(this, visArg);
			}

			case 4: {
				AbsDecl decl = SemAn.declaredAt.get(typName);
				return decl.accept(this, 4);
			}

			default:
				return null;
		}
	}

	@Override
	public SemType visit(AbsUnExpr unExpr, Object visArg) {
		switch ((int) visArg) {
			case 2: {
				long fv = TypeResolver.exprValue;
				TypeResolver.exprValue = 0;
				unExpr.subExpr.accept(this, 2);

				switch (unExpr.oper) {
					case ADD: TypeResolver.exprValue = fv + TypeResolver.exprValue; break;
					case SUB: TypeResolver.exprValue = fv - TypeResolver.exprValue; break;

					default: throw new Report.Error("Cannot evaluate expression at " + unExpr);
				}

				return null;
			}

			case 4: {
				SemType expr = unExpr.subExpr.accept(this, 4);

				switch (unExpr.oper) {
					case ADD:
					case SUB: {
						SemType req = (SemType) new SemIntType();
						if (!expr.matches(req)) throw new Report.Error("Expression of type int is required for +- unary expression at " + unExpr);

						if (!lock) SemAn.isOfType.put(unExpr, req);
						return req;
					}

					case NOT: {
						SemType req = (SemType) new SemBoolType();
						if (!expr.matches(req)) throw new Report.Error("Expression of type bool is required for ! unary expression at " + unExpr);

						if (!lock) SemAn.isOfType.put(unExpr, req);
						return req;
					}

					case ADDR: {
						SemType req = (SemType) new SemVoidType();
						if (expr.matches(req)) throw new Report.Error("Expression of void type cannot be used for $ unary expression at " + unExpr);

						SemType ret = (SemType) new SemPtrType(expr);
						if (!lock) SemAn.isOfType.put(unExpr, ret);
						return ret;
					}

					case DATA: {
						SemType req = (SemType) new SemPtrType(new SemVoidType());
						if (!(expr instanceof SemPtrType)) throw new Report.Error("Expression of pointer type must be used for @ unary exression at " + unExpr);
						if (expr.matches(req)) throw new Report.Error("Expression of pointer to void type cannot be used for @ unary expression at " + unExpr);

						SemType ret = ((SemPtrType) expr).ptdType;
						if (!lock) SemAn.isOfType.put(unExpr, ret);
						if (!lock) SemAn.isOfType.put(unExpr.subExpr, expr);
						return ret;
					}

					default: throw new Report.Error("Cannot determine type of unary expression at " + unExpr);
				}
			}

			default:
				unExpr.subExpr.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsVarDecl varDecl, Object visArg) {
		switch ((int) visArg) {
			case 4: {
				SemType type = varDecl.type.accept(this, 4);
				if (type.matches(new SemVoidType())) throw new Report.Error("Variable cannot be of type void at " + varDecl);
				return type;
			}

			default:
				varDecl.type.accept(this, visArg);
				return null;
		}
	}

	@Override
	public SemType visit(AbsVarName varName, Object visArg) {
		switch ((int) visArg) {
			case 4: {
				AbsDecl decl = SemAn.declaredAt.get(varName);
				return decl.accept(this, 4);
			}

			default:
				return null;
		}
	}

	@Override
	public SemType visit(AbsWhileStmt whileStmt, Object visArg) {
		switch ((int) visArg) {
			case 4: {
				SemType cond = whileStmt.cond.accept(this, 4);
				SemType stmts = whileStmt.stmts.accept(this, 4);
				SemType reqBool = new SemBoolType();
				SemType reqVoid = new SemVoidType();
				if (!cond.matches(reqBool)) throw new Report.Error("Expected bool expression as condition of while statement at " + whileStmt);
				if (!stmts.matches(reqVoid)) throw new Report.Error("Expected statements in then statement of while statement at " + whileStmt);

				SemType ret = new SemVoidType();
				return ret;
			}

			default:
				whileStmt.cond.accept(this, visArg);
				whileStmt.stmts.accept(this, visArg);
				return null;
		}
	}
}
