/**
 * @author sliva
 */
package compiler.phases.abstr;

import java.util.*;
import compiler.common.report.*;
import compiler.data.dertree.*;
import compiler.data.dertree.visitor.*;
import compiler.data.abstree.*;

/**
 * Transforms a derivation tree to an abstract syntax tree.
 * 
 * @author sliva
 */
public class AbsTreeConstructor implements DerVisitor<AbsTree, AbsTree> {

	@Override
	public AbsTree visit(DerLeaf leaf, AbsTree visArg) {
		throw new Report.InternalError();
	}

	@Override
	public AbsTree visit(DerNode node, AbsTree visArg) {
		switch (node.label) {
			case Source: {
				AbsDecls decls = (AbsDecls) node.subtree(0).accept(this, null);
				return new AbsSource(decls, decls);
			}
	
			case Declarations: {
				Vector<AbsDecl> allDecls = new Vector<AbsDecl>();
				AbsDecl decl = (AbsDecl) node.subtree(0).accept(this, null);
				allDecls.add(decl);
				AbsDecls decls = (AbsDecls) node.subtree(1).accept(this, null);
				if (decls != null)
					allDecls.addAll(decls.decls());
				return new AbsDecls(new Location(decl, decls == null ? decl : decls), allDecls);	
			}
	
			case DeclarationsP: {
				if (node.numSubtrees() == 0)
					return null;
				Vector<AbsDecl> allDecls = new Vector<AbsDecl>();
				AbsDecl decl = (AbsDecl) node.subtree(0).accept(this, null);
				allDecls.add(decl);
				AbsDecls decls = (AbsDecls) node.subtree(1).accept(this, null);
				if (decls != null)
					allDecls.addAll(decls.decls());
				return new AbsDecls(new Location(decl, decls == null ? decl : decls), allDecls);
			}
	
			case Declaration: {
				switch (((DerLeaf) node.subtree(0)).symb.token) {
					case VAR: return new AbsVarDecl(new Location(node.subtree(0), node.subtree(4)),
									((DerLeaf) node.subtree(1)).symb.lexeme,
									(AbsType) node.subtree(3).accept(this, null));
					case TYP: return new AbsTypDecl(new Location(node.subtree(0), node.subtree(4)),
									((DerLeaf) node.subtree(1)).symb.lexeme,
									(AbsType) node.subtree(3).accept(this, null));
					case FUN: {
						AbsExpr exp = (AbsExpr) node.subtree(7).accept(this, null);

						if (exp == null)
							return new AbsFunDecl(new Location(node, node),
										((DerLeaf) node.subtree(1)).symb.lexeme,
										(AbsParDecls) node.subtree(3).accept(this, null),
										(AbsType) node.subtree(6).accept(this, null));

						return new AbsFunDef(new Location(node, node),
										((DerLeaf) node.subtree(1)).symb.lexeme,
										(AbsParDecls) node.subtree(3).accept(this, null),
										(AbsType) node.subtree(6).accept(this, null),
										exp);
					}
				}
			}

			case Statements: {
				AbsStmt stmt = (AbsStmt) node.subtree(0).accept(this, null);
				Vector<AbsStmt> allStmts = new Vector<AbsStmt>();
				allStmts.add(stmt);

				AbsStmts stmts = (AbsStmts) node.subtree(2).accept(this, null);
				if (stmts != null) allStmts.addAll(stmts.stmts());

				return new AbsStmts(new Location(stmt, stmts == null ? stmt : stmts), allStmts);
			}

			case StatementsP: {
				if (node.numSubtrees() == 0) return null;

				AbsStmt stmt = (AbsStmt) node.subtree(0).accept(this, null);
				Vector<AbsStmt> allStmts = new Vector<AbsStmt>();
				allStmts.add(stmt);

				AbsStmts stmts = (AbsStmts) node.subtree(2).accept(this, null);
				if (stmts != null) allStmts.addAll(stmts.stmts());

				return new AbsStmts(new Location(stmt, stmts == null ? stmt : stmts), allStmts);
			}

			case Statement: {
				if (node.numSubtrees() == 2) {
					AbsExpr first = (AbsExpr) node.subtree(0).accept(this, null);
					AbsExpr second = (AbsExpr) node.subtree(1).accept(this, null);

					/// Expression ;
					if (second == null) return new AbsExprStmt(new Location(node, node), first);

					/// Expression = Expression ;
					return new AbsAssignStmt(new Location(node, node), first, second);
				}

				if (node.numSubtrees() == 5) {
					AbsExpr cond = (AbsExpr) node.subtree(1).accept(this, null);
					AbsStmts stmts = (AbsStmts) node.subtree(3).accept(this, null);

					return new AbsWhileStmt(new Location(node, node), cond, stmts);
				}

				if (node.numSubtrees() == 6) {
					AbsExpr cond = (AbsExpr) node.subtree(1).accept(this, null);
					AbsStmts thenStmt = (AbsStmts) node.subtree(3).accept(this, null);
					AbsStmts elseStmt = (AbsStmts) node.subtree(4).accept(this, null);

					if (elseStmt == null) elseStmt = new AbsStmts(new Location(0, 0), new Vector<AbsStmt>());

					return new AbsIfStmt(new Location(node, node), cond, thenStmt, elseStmt);
				}

				return null;
			}

			case StatementP: {
				if (node.numSubtrees() == 0) return null;

				return (AbsExpr) node.subtree(1).accept(this, null);
			}

			case WhereOpt: {
				if (node.numSubtrees() == 0) return null;

				return node.subtree(1).accept(this, null);
			}

			case ElseOpt: {
				if (node.numSubtrees() == 0) return null;

				return node.subtree(1).accept(this, null);
			}

			case ArgumentsOpt: {
				if (node.numSubtrees() == 0) return null;

				return node.subtree(1).accept(this, null);
			}

			case ArgumentsOptP: {
				if (node.numSubtrees() == 1) return new AbsArgs(new Location(0, 0), new Vector<AbsExpr>());

				return node.subtree(0).accept(this, null);
			}

			case Arguments: {
				Vector<AbsExpr> allArgs = new Vector<AbsExpr>();
				AbsExpr arg = (AbsExpr) node.subtree(0).accept(this, null);
				allArgs.add(arg);

				AbsArgs args = (AbsArgs) node.subtree(1).accept(this, null);
				if (args != null) allArgs.addAll(args.args());

				return new AbsArgs(new Location(arg, args == null ? arg : args), allArgs);
			}

			case ArgumentsP: {
				if (node.numSubtrees() == 0) return null;

				Vector<AbsExpr> allArgs = new Vector<AbsExpr>();
				AbsExpr arg = (AbsExpr) node.subtree(0).accept(this, null);
				allArgs.add(arg);

				AbsArgs args = (AbsArgs) node.subtree(1).accept(this, null);
				if (args != null) allArgs.addAll(args.args());

				return new AbsArgs(new Location(arg, args == null ? arg : args), allArgs);
			}

			case Expression: {
				return node.subtree(0).accept(this, null);
			}

			case ORXORExpression: {
				AbsExpr first = (AbsExpr) node.subtree(0).accept(this, null);
				return node.subtree(1).accept(this, first);
			}

			case ORXORExpressionP: {
				if (node.numSubtrees() == 0) return visArg;

				AbsExpr second = (AbsExpr) node.subtree(1).accept(this, visArg);
				
				AbsBinExpr.Oper oper = null;
				switch (((DerLeaf) node.subtree(0)).symb.token) {
					case IOR:
						oper = AbsBinExpr.Oper.IOR;
						break;
					case XOR:
						oper = AbsBinExpr.Oper.XOR;
						break;
				}

				AbsBinExpr merge = new AbsBinExpr(new Location(visArg, second), oper, (AbsExpr) visArg, second);
				return node.subtree(2).accept(this, merge);
			}


			case ANDExpression: {
				AbsExpr first = (AbsExpr) node.subtree(0).accept(this, null);
				return node.subtree(1).accept(this, first);
			}

			case ANDExpressionP: {
				if (node.numSubtrees() == 0) return visArg;

				AbsExpr second = (AbsExpr) node.subtree(1).accept(this, visArg);
				
				AbsBinExpr.Oper oper = null;
				switch (((DerLeaf) node.subtree(0)).symb.token) {
					case AND:
						oper = AbsBinExpr.Oper.AND;
						break;
				}

				AbsBinExpr merge = new AbsBinExpr(new Location(visArg, second), oper, (AbsExpr) visArg, second);
				return node.subtree(2).accept(this, merge);
			}

			case RelationalExpression: {
				AbsExpr first = (AbsExpr) node.subtree(0).accept(this, null);
				return node.subtree(1).accept(this, first);
			}

			case RelationalExpressionP: {
				if (node.numSubtrees() == 0) return visArg;
				
				AbsBinExpr.Oper oper = null;
				switch (((DerLeaf) node.subtree(0)).symb.token) {
					case EQU:
						oper = AbsBinExpr.Oper.EQU;
						break;
					case NEQ:
						oper = AbsBinExpr.Oper.NEQ;
						break;
					case GEQ:
						oper = AbsBinExpr.Oper.GEQ;
						break;
					case LEQ:
						oper = AbsBinExpr.Oper.LEQ;
						break;
					case GTH:
						oper = AbsBinExpr.Oper.GTH;
						break;
					case LTH:
						oper = AbsBinExpr.Oper.LTH;
						break;
				}

				AbsExpr second = (AbsExpr) node.subtree(1).accept(this, visArg);
				return new AbsBinExpr(new Location(visArg, second), oper, (AbsExpr) visArg, second);
			}

			case ADDSUBExpression: {
				AbsExpr first = (AbsExpr) node.subtree(0).accept(this, null);
				return node.subtree(1).accept(this, first);
			}

			case ADDSUBExpressionP: {
				if (node.numSubtrees() == 0) return visArg;

				AbsExpr second = (AbsExpr) node.subtree(1).accept(this, visArg);
				
				AbsBinExpr.Oper oper = null;
				switch (((DerLeaf) node.subtree(0)).symb.token) {
					case ADD:
						oper = AbsBinExpr.Oper.ADD;
						break;
					case SUB:
						oper = AbsBinExpr.Oper.SUB;
						break;
				}

				AbsBinExpr merge = new AbsBinExpr(new Location(visArg, second), oper, (AbsExpr) visArg, second);
				return node.subtree(2).accept(this, merge);
			}

			case MULDIVExpression: {
				AbsExpr first = (AbsExpr) node.subtree(0).accept(this, null);
				return node.subtree(1).accept(this, first);
			}

			case MULDIVExpressionP: {
				if (node.numSubtrees() == 0) return visArg;

				AbsExpr second = (AbsExpr) node.subtree(1).accept(this, visArg);
				
				AbsBinExpr.Oper oper = null;
				switch (((DerLeaf) node.subtree(0)).symb.token) {
					case MUL:
						oper = AbsBinExpr.Oper.MUL;
						break;
					case DIV:
						oper = AbsBinExpr.Oper.DIV;
						break;
					case MOD:
						oper = AbsBinExpr.Oper.MOD;
						break;
				}

				AbsBinExpr merge = new AbsBinExpr(new Location(visArg, second), oper, (AbsExpr) visArg, second);
				return node.subtree(2).accept(this, merge);
			}

			case PrefixExpression: {
				if (node.numSubtrees() == 1) return node.subtree(0).accept(this, null);

				if (node.numSubtrees() == 2) {
					AbsUnExpr.Oper oper = null;
					switch (((DerLeaf) node.subtree(0)).symb.token) {
						case ADD:
							oper = AbsUnExpr.Oper.ADD;
							break;
						case SUB:
							oper = AbsUnExpr.Oper.SUB;
							break;
						case NOT:
							oper = AbsUnExpr.Oper.NOT;
							break;
						case DATA:
							oper = AbsUnExpr.Oper.DATA;
							break;
						case ADDR:
							oper = AbsUnExpr.Oper.ADDR;
							break;			
					}

					AbsExpr next = (AbsExpr) node.subtree(1).accept(this, null);
					return new AbsUnExpr(new Location(node, node), oper, next);
				}

				if (node.numSubtrees() == 3) {
					AbsExpr expr = (AbsExpr) node.subtree(1).accept(this, null);
					AbsType type = (AbsType) node.subtree(2).accept(this, null);

					/// ( Expr )
					if (type == null)
						return expr;

					/// ( Expr : Type )
					return new AbsCastExpr(new Location(node, node), expr, type);
				}

				if (node.numSubtrees() == 4) {
					switch (((DerLeaf) node.subtree(0)).symb.token) {
						case NEW: return new AbsNewExpr(new Location(node, node), (AbsType) node.subtree(2).accept(this, null));
						case DEL: return new AbsDelExpr(new Location(node, node), (AbsExpr) node.subtree(2).accept(this, null));		
					}

					return null;
				}

				return null;
			}

			case PrefixExpressionP: {
				/// ( Expr )
				if (node.numSubtrees() == 1) return null;

				/// returns Type of typecasting expression
				return node.subtree(1).accept(this, null);
			}

			case PostfixExpression: {
				AbsExpr first = (AbsExpr) node.subtree(0).accept(this, null);
				AbsExpr second = (AbsExpr) node.subtree(1).accept(this, first);

				return node.subtree(1).accept(this, first);
			}

			case PostfixExpressionP: {
				if (node.numSubtrees() == 0) return visArg;

				if (node.numSubtrees() == 3) {
					AbsVarName comp = new AbsVarName(new Location(node.subtree(1), node.subtree(1)), ((DerLeaf) node.subtree(1)).symb.lexeme);
					AbsRecExpr recExpr = new AbsRecExpr(new Location(visArg, comp), (AbsExpr) visArg, comp);
					return node.subtree(2).accept(this, recExpr);
				}

				if (node.numSubtrees() == 4) {	
					AbsExpr idx = (AbsExpr) node.subtree(1).accept(this, null);
					AbsArrExpr ret = new AbsArrExpr(new Location(visArg, node.subtree(2)), (AbsExpr) visArg, idx);
					return node.subtree(3).accept(this, ret);
				}

				return null;
			}

			case AtomicExpression: {
				if (node.numSubtrees() == 1) {
					switch (((DerLeaf) node.subtree(0)).symb.token) {
						case INTCONST: return new AbsAtomExpr(new Location(node, node), AbsAtomExpr.Type.INT, ((DerLeaf) node.subtree(0)).symb.lexeme);
						case VOIDCONST: return new AbsAtomExpr(new Location(node, node), AbsAtomExpr.Type.VOID, ((DerLeaf) node.subtree(0)).symb.lexeme);
						case CHARCONST: return new AbsAtomExpr(new Location(node, node), AbsAtomExpr.Type.CHAR, ((DerLeaf) node.subtree(0)).symb.lexeme);
						case BOOLCONST: return new AbsAtomExpr(new Location(node, node), AbsAtomExpr.Type.BOOL, ((DerLeaf) node.subtree(0)).symb.lexeme);
						case PTRCONST: return new AbsAtomExpr(new Location(node, node), AbsAtomExpr.Type.PTR, ((DerLeaf) node.subtree(0)).symb.lexeme);
						case STRCONST: return new AbsAtomExpr(new Location(node, node), AbsAtomExpr.Type.STR, ((DerLeaf) node.subtree(0)).symb.lexeme);
					}
				}

				if (node.numSubtrees() == 2) {
					AbsArgs args = (AbsArgs) node.subtree(1).accept(this, null);

					/// Identifier
					if (args == null) return new AbsVarName(new Location(node, node), ((DerLeaf) node.subtree(0)).symb.lexeme);

					/// Identifier ( Argumments )
					return new AbsFunName(new Location(node, node), ((DerLeaf) node.subtree(0)).symb.lexeme, args);
				}

				if (node.numSubtrees() == 6) {
					AbsDecls decls = (AbsDecls) node.subtree(4).accept(this, null);
					AbsStmts stmts = (AbsStmts) node.subtree(1).accept(this, null);
					AbsExpr expr = (AbsExpr) node.subtree(3).accept(this, null);

					boolean have = decls == null ? false : true;
					if (decls == null) decls = (AbsDecls) new AbsDecls(new Location(0, 0), new Vector<AbsDecl>());

					return new AbsBlockExpr(new Location(node.subtree(1), have ? node.subtree(4) : node.subtree(3)), decls, stmts, expr);
				}

				return null;
			}

			case Type: {
				/// Base case
				if (node.numSubtrees() == 1) {
					switch (((DerLeaf) node.subtree(0)).symb.token) {
						case INT: return new AbsAtomType(new Location(node, node), AbsAtomType.Type.INT);
						case CHAR: return new AbsAtomType(new Location(node, node), AbsAtomType.Type.CHAR);
						case BOOL: return new AbsAtomType(new Location(node, node), AbsAtomType.Type.BOOL);
						case VOID: return new AbsAtomType(new Location(node, node), AbsAtomType.Type.VOID);
						case IDENTIFIER: return new AbsTypName(new Location(node, node), ((DerLeaf) node.subtree(0)).symb.lexeme);
					}
				}

				/// PTR Type
				if (node.numSubtrees() == 2) {
					AbsType next = (AbsType) node.subtree(1).accept(this, null);
					return new AbsPtrType(new Location(node, next), next);
				}

				/// ( Type )
				if (node.numSubtrees() == 3) return node.subtree(1).accept(this, null);

				/// REC ( Parameters )
				if (node.numSubtrees() == 4) {
					AbsParDecls parDecls = (AbsParDecls) node.subtree(2).accept(this, null);

					Vector<AbsParDecl> pars = parDecls.parDecls();
					Vector<AbsCompDecl> compDecls = new Vector<AbsCompDecl>();
					for (AbsParDecl par : pars) {
						compDecls.add(new AbsCompDecl(new Location((Locatable) par), ((AbsDecl) par).name, ((AbsDecl) par).type));
					}

					return new AbsRecType(new Location(node, node),
							new AbsCompDecls(new Location(compDecls.get(0), compDecls.get(compDecls.size() - 1)), compDecls));
				}

				/// ARR [ Expr ] Type
				if (node.numSubtrees() == 5) {
					return new AbsArrType(new Location(node, node),
								(AbsExpr) node.subtree(2).accept(this, null),
								(AbsType) node.subtree(4).accept(this, null));
				}

				return null;
			}

			case ParametersOpt: {
				if (node.numSubtrees() == 0) return new AbsParDecls(new Location(0, 0), new Vector<AbsParDecl>());

				return node.subtree(0).accept(this, null);
			}

			case Parameters: {
				String name = (String) ((DerLeaf) node.subtree(0)).symb.lexeme;
				AbsType type = (AbsType) node.subtree(2).accept(this, null);
				AbsParDecl decl = new AbsParDecl(new Location(node.subtree(0), node.subtree(2)), name, type);

				Vector<AbsParDecl> allDecls = new Vector<AbsParDecl>();
				allDecls.add(decl);

				AbsParDecls parDecls = (AbsParDecls) node.subtree(3).accept(this, null);
				if (parDecls != null)
					allDecls.addAll(parDecls.parDecls());

				return new AbsParDecls(new Location(node.subtree(0), parDecls == null ? decl : parDecls), allDecls);
			}

			case ParametersP: {
				if (node.numSubtrees() == 0) return null;

				String name = (String) ((DerLeaf) node.subtree(1)).symb.lexeme;
				AbsType type = (AbsType) node.subtree(3).accept(this, null);
				AbsParDecl decl = new AbsParDecl(new Location(node.subtree(1), node.subtree(3)), name, type);

				Vector<AbsParDecl> allDecls = new Vector<AbsParDecl>();
				allDecls.add(decl);

				AbsParDecls parDecls = (AbsParDecls) node.subtree(4).accept(this, null);
				if (parDecls != null)
					allDecls.addAll(parDecls.parDecls());

				return new AbsParDecls(new Location(node.subtree(1), parDecls == null ? decl : parDecls), allDecls);
			}

			case FunctionBodyOpt: {
				if (node.numSubtrees() == 0) return null;

				/// = Expr
				return node.subtree(1).accept(this, null);				
			}
		}

		throw new Report.Error("Cannot construct AST.");
	}
}
