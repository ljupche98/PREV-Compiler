/**
 * @author sliva
 */
package compiler.phases.synan;

import compiler.common.report.*;
import compiler.data.symbol.*;
import compiler.data.dertree.*;
import compiler.phases.*;
import compiler.phases.lexan.*;

/**
 * Syntax analysis.
 * 
 * @author sliva
 */
public class SynAn extends Phase {

	/** The derivation tree of the program being compiled. */
	public static DerTree derTree = null;

	/** The lexical analyzer used by this syntax analyzer. */
	private final LexAn lexAn;

	/**
	 * Constructs a new phase of syntax analysis.
	 */
	public SynAn() {
		super("synan");
		lexAn = new LexAn();
	}

	@Override
	public void close() {
		lexAn.close();
		super.close();
	}

	/**
	 * The parser.
	 * 
	 * This method constructs a derivation tree of the program in the source file.
	 * It calls method {@link #parseSource()} that starts a recursive descent parser
	 * implementation of an LL(1) parsing algorithm.
	 */
	public void parser() {
		currSymb = lexAn.lexer();
		derTree = parseSource();
		if (currSymb.token != Symbol.Term.EOF)
			throw new Report.Error(currSymb, "Unexpected '" + currSymb + "' at the end of a program.");
	}

	/** The lookahead buffer (of length 1). */
	private Symbol currSymb = null;

	/**
	 * Appends the current symbol in the lookahead buffer to a derivation tree node
	 * (typically the node of the derivation tree that is currently being expanded
	 * by the parser) and replaces the current symbol (just added) with the next
	 * input symbol.
	 * 
	 * @param node The node of the derivation tree currently being expanded by the
	 *             parser.
	 */
	private void add(DerNode node) {
		if (currSymb == null)
			throw new Report.InternalError();
		node.add(new DerLeaf(currSymb));
		currSymb = lexAn.lexer();
	}

	/**
	 * If the current symbol is the expected terminal, appends the current symbol in
	 * the lookahead buffer to a derivation tree node (typically the node of the
	 * derivation tree that is currently being expanded by the parser) and replaces
	 * the current symbol (just added) with the next input symbol. Otherwise,
	 * produces the error message.
	 * 
	 * @param node     The node of the derivation tree currently being expanded by
	 *                 the parser.
	 * @param token    The expected terminal.
	 * @param errorMsg The error message.
	 */
	private void add(DerNode node, Symbol.Term token, String errorMsg) {
		if (currSymb == null)
			throw new Report.InternalError();
		if (currSymb.token == token) {
			node.add(new DerLeaf(currSymb));
			currSymb = lexAn.lexer();
		} else
			throw new Report.Error(currSymb, errorMsg);
	}

	private DerNode parseSource() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Source);

		switch (cur) {
			case TYP:
			case VAR:
			case FUN:
				node.add(parseDeclarations());
				break;
			default:
				throw new Report.Error("Cannot parse Source");
		}
		
		return node;
	}

	private DerNode parseDeclarations() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Declarations);

		switch (cur) {
			case TYP:
			case VAR:
			case FUN:
				node.add(parseDeclaration());
				node.add(parseDeclarationsP());
				break;
			default:
				throw new Report.Error("Cannot parse Declarations");
		}

		return node;
	}

	private DerNode parseDeclarationsP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.DeclarationsP);

		switch (cur) {
			case TYP:
			case VAR:
			case FUN:
				node.add(parseDeclaration());
				node.add(parseDeclarationsP());
				break;
			case RBRACE:
			case EOF:
				break;
			default:
				throw new Report.Error("Cannot parse DeclarationsP");
		}

		return node;
	}

	private DerNode parseDeclaration() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Declaration);

		switch (cur) {
			case TYP:
				add(node, Symbol.Term.TYP, "Expected TYP when parsing Declaration");
				add(node, Symbol.Term.IDENTIFIER, "Expected IDENTIFIER when parsing Declaration");
				add(node, Symbol.Term.COLON, "Expected COLON when parsing Declaration");
				node.add(parseType());
				add(node, Symbol.Term.SEMIC, "Expected SEMIC when parsing Declaration");
				break;
			case VAR:
				add(node, Symbol.Term.VAR, "Expected VAR when parsing Declaration");
				add(node, Symbol.Term.IDENTIFIER, "Expected IDENTIFIER when parsing Declaration");
				add(node, Symbol.Term.COLON, "Expected COLON when parsing Declaration");
				node.add(parseType());
				add(node, Symbol.Term.SEMIC, "Expected SEMIC when parsing Declaration");
				break;
			case FUN:
				add(node, Symbol.Term.FUN, "Expected FUN when parsing Declaration");
				add(node, Symbol.Term.IDENTIFIER, "Expected IDENTIFER when parsing Declaration");
				add(node, Symbol.Term.LPARENTHESIS, "Expected LPARENTHESIS when parsing Declaration");
				node.add(parseParametersOpt());
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing Declaration");
				add(node, Symbol.Term.COLON, "Expected COLON when parsing Declaration");
				node.add(parseType());
				node.add(parseFunctionBodyOpt());
				add(node, Symbol.Term.SEMIC, "Expected SEMIC when parsing Declaration");
				break;
			default:
				throw new Report.Error("Cannot parse Declaration");
		}

		return node;
	}
	
	private DerNode parseStatement() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Statement);
		
		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseExpression());
				node.add(parseStatementP());
				break;
			case IF:
				add(node, Symbol.Term.IF, "Expected IF when parsing Statement");
				node.add(parseExpression());
				add(node, Symbol.Term.THEN, "Expected THEN when parsing Statement");
				node.add(parseStatements());
				node.add(parseElseOpt());
				add(node, Symbol.Term.END, "Expected END when parsing Statement");
				break;
			case WHILE:
				add(node, Symbol.Term.WHILE, "Expected WHILE when parsing Statement");
				node.add(parseExpression());
				add(node, Symbol.Term.DO, "Expected DO when parsing Statement");
				node.add(parseStatements());
				add(node, Symbol.Term.END, "Expected END when parsing Statement");
				break;
			default:
				throw new Report.Error("Cannot parse Statement");
		}
		
		return node;
	}
	
	private DerNode parseStatementP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.StatementP);

		switch (cur) {
			case SEMIC:
				break;
			case ASSIGN:
				add(node, Symbol.Term.ASSIGN, "Expected ASSIGN when parsing StatementP");
				node.add(parseExpression());
				break;
			default:
				throw new Report.Error("Cannot parse StatementP");
		}
		
		return node;
	}

	private DerNode parseExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Expression);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseORXORExpression());
				break;
			default:
				throw new Report.Error("Cannot parse Expression");
		}

		return node;
	}

	private DerNode parseORXORExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ORXORExpression);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseANDExpression());
				node.add(parseORXORExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse ORXORExpression");
		}

		return node;
	}

	private DerNode parseORXORExpressionP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ORXORExpressionP);

		switch (cur) {
			case COLON:
			case SEMIC:
			case RPARENTHESIS:
			case ASSIGN:
			case THEN:
			case DO:
			case RBRACKET:
			case RBRACE:
			case COMMA:
			case WHERE:
				break;
			case IOR:
				add(node, Symbol.Term.IOR, "Expected IOR when parsing ORXORExpressionP");
				node.add(parseANDExpression());
				node.add(parseORXORExpressionP());
				break;
			case XOR:
				add(node, Symbol.Term.XOR, "Expected XOR when parsing ORXORExpressionP");
				node.add(parseANDExpression());
				node.add(parseORXORExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse ORXORExpressionP");
		}

		return node;
	}

	private DerNode parseANDExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ANDExpression);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseRelationalExpression());
				node.add(parseANDExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse ANDExpression");
		}

		return node;
	}

	private DerNode parseANDExpressionP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ANDExpressionP);

		switch (cur) {
			case COLON:
			case SEMIC:
			case RPARENTHESIS:
			case ASSIGN:
			case THEN:
			case DO:
			case RBRACKET:
			case RBRACE:
			case COMMA:
			case WHERE:
			case IOR:
			case XOR:
				break;
			case AND:
				add(node, Symbol.Term.AND, "Expected AND when parsing ANDExpressionP");
				node.add(parseRelationalExpression());
				node.add(parseANDExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse ANDExpressionP");
		}

		return node;
	}

	private DerNode parseRelationalExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.RelationalExpression);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseADDSUBExpression());
				node.add(parseRelationalExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse RelationalExpression");
		}

		return node;
	}

	private DerNode parseRelationalExpressionP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.RelationalExpressionP);

		switch (cur) {
			case COLON:
			case SEMIC:
			case RPARENTHESIS:
			case ASSIGN:
			case THEN:
			case DO:
			case RBRACKET:
			case RBRACE:
			case COMMA:
			case WHERE:
			case IOR:
			case XOR:
			case AND:
				break;
			case EQU:
				add(node, Symbol.Term.EQU, "Expected AND when parsing RelationalExpressionP");
				node.add(parseADDSUBExpression());
				break;
			case NEQ:
				add(node, Symbol.Term.NEQ, "Expected NEQ when parsing RelationalExpressionP");
				node.add(parseADDSUBExpression());
				break;
			case GEQ:
				add(node, Symbol.Term.GEQ, "Expected GEQ when parsing RelationalExpressionP");
				node.add(parseADDSUBExpression());
				break;
			case LEQ:
				add(node, Symbol.Term.LEQ, "Expected LEQ when parsing RelationalExpressionP");
				node.add(parseADDSUBExpression());
				break;
			case GTH:
				add(node, Symbol.Term.GTH, "Expected GTH when parsing RelationalExpressionP");
				node.add(parseADDSUBExpression());
				break;
			case LTH:
				add(node, Symbol.Term.LTH, "Expected LTH when parsing RelationalExpressionP");
				node.add(parseADDSUBExpression());
				break;
			default:
				throw new Report.Error("Cannot parse RelationalExpressionP");
		}

		return node;
	}

	private DerNode parseADDSUBExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ADDSUBExpression);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseMULDIVExpression());
				node.add(parseADDSUBExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse ADDSUBExpression");
		}

		return node;
	}

	private DerNode parseADDSUBExpressionP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ADDSUBExpressionP);

		switch (cur) {
			case COLON:
			case SEMIC:
			case RPARENTHESIS:
			case ASSIGN:
			case THEN:
			case DO:
			case RBRACKET:
			case RBRACE:
			case COMMA:
			case WHERE:
			case IOR:
			case XOR:
			case AND:
			case EQU:
			case NEQ:
			case GEQ:
			case LEQ:
			case GTH:
			case LTH:
				break;
			case ADD:
				add(node, Symbol.Term.ADD, "Expected ADD when parsing ADDSUBExpressionP");
				node.add(parseMULDIVExpression());
				node.add(parseADDSUBExpressionP());
				break;
			case SUB:
				add(node, Symbol.Term.SUB, "Expected SUB when parsing ADDSUBExpressionP");
				node.add(parseMULDIVExpression());
				node.add(parseADDSUBExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse ADDSUBExpressionP");
		}

		return node;
	}

	private DerNode parseMULDIVExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.MULDIVExpression);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parsePrefixExpression());
				node.add(parseMULDIVExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse MULDIVExpression");
		}

		return node;
	}

	private DerNode parseMULDIVExpressionP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.MULDIVExpressionP);

		switch (cur) {
			case COLON:
			case SEMIC:
			case RPARENTHESIS:
			case ASSIGN:
			case THEN:
			case DO:
			case RBRACKET:
			case RBRACE:
			case COMMA:
			case WHERE:
			case IOR:
			case XOR:
			case AND:
			case EQU:
			case NEQ:
			case GEQ:
			case LEQ:
			case GTH:
			case LTH:
			case ADD:
			case SUB:
				break;
			case MUL:
				add(node, Symbol.Term.MUL, "Expected MUL when parsing MULDIVExpressionP");
				node.add(parsePrefixExpression());
				node.add(parseMULDIVExpressionP());
				break;
			case DIV:
				add(node, Symbol.Term.DIV, "Expected DIV when parsing MULDIVExpressionP");
				node.add(parsePrefixExpression());
				node.add(parseMULDIVExpressionP());
				break;
			case MOD:
				add(node, Symbol.Term.MOD, "Expected MOD when parsing MULDIVExpressionP");
				node.add(parsePrefixExpression());
				node.add(parseMULDIVExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse MULDIVExpressionP");
		}

		return node;
	}

	private DerNode parsePrefixExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.PrefixExpression);

		switch (cur) {
			case IDENTIFIER:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parsePostfixExpression());
				break;
			case LPARENTHESIS:
				add(node, Symbol.Term.LPARENTHESIS, "Expected LPARENTHESIS when parsing PrefixExpression");
				node.add(parseExpression());
				node.add(parsePrefixExpressionP());
				break;
			case ADD:
				add(node, Symbol.Term.ADD, "Expected ADD when parsing PrefixExpression");
				node.add(parsePrefixExpression());
				break;
			case SUB:
				add(node, Symbol.Term.SUB, "Expected SUB when parsing PrefixExpression");
				node.add(parsePrefixExpression());
				break;
			case NOT:
				add(node, Symbol.Term.NOT, "Expected NOT when parsing PrefixExpression");
				node.add(parsePrefixExpression());
				break;
			case NEW:
				add(node, Symbol.Term.NEW, "Expected NEW when parsing PrefixExpression");
				add(node, Symbol.Term.LPARENTHESIS, "Expected LPARENTHESIS when parsing PrefixExpression");
				node.add(parseType());
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing PrefixExpression");
				break;
			case DEL:
				add(node, Symbol.Term.DEL, "Expected DEL when parsing PrefixExpression");
				add(node, Symbol.Term.LPARENTHESIS, "Expected LPARENTHESIS when parsing PrefixExpression");
				node.add(parseExpression());
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing PrefixExpression");
				break;
			case DATA:
				add(node, Symbol.Term.DATA, "Expected DATA when parsing PrefixExpression");
				node.add(parsePrefixExpression());
				break;
			case ADDR:
				add(node, Symbol.Term.ADDR, "Expected ADDR when parsing PrefixExpression");
				node.add(parsePrefixExpression());
				break;
			default:
				throw new Report.Error("Cannot parse PrefixExpression");
		}

		return node;
	}

	private DerNode parsePrefixExpressionP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.PrefixExpressionP);

		switch (cur) {
			case COLON:
				add(node, Symbol.Term.COLON, "Expected COLON when parsing PrefixExpressionP");
				node.add(parseType());
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing PrefixExpressionP");
				break;
			case RPARENTHESIS:
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing PrefixExpressionP");
				break;
			default:
				throw new Report.Error("Cannot parse PrefixExpressionP");
		}

		return node;
	}

	private DerNode parsePostfixExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.PostfixExpression);

		switch (cur) {
			case IDENTIFIER:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseAtomicExpression());
				node.add(parsePostfixExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse PostfixExpression");
		}

		return node;
	}

	private DerNode parsePostfixExpressionP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.PostfixExpressionP);

		switch (cur) {
			case COLON:
			case SEMIC:
			case RPARENTHESIS:
			case ASSIGN:
			case THEN:
			case DO:
			case IOR:
			case XOR:
			case AND:
			case EQU:
			case NEQ:
			case GEQ:
			case LEQ:
			case GTH:
			case LTH:
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
			case RBRACKET:
			case RBRACE:
			case COMMA:
			case WHERE:
				break;
			case LBRACKET:
				add(node, Symbol.Term.LBRACKET, "Expected LBRACKET when parsing PostfixExpressionP");
				node.add(parseExpression());
				add(node, Symbol.Term.RBRACKET, "Expected RBRACKET when parsing PostfixExpressionP");
				node.add(parsePostfixExpressionP());
				break;
			case DOT:
				add(node, Symbol.Term.DOT, "Expected DOT when parsing PostfixExpressionP");
				add(node, Symbol.Term.IDENTIFIER, "Expected IDENTIFIER when parsing PostfixExpressionP");
				node.add(parsePostfixExpressionP());
				break;
			default:
				throw new Report.Error("Cannot parse PostfixExpressionP");
		}

		return node;
	}

	private DerNode parseAtomicExpression() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.AtomicExpression);

		switch (cur) {
			case IDENTIFIER:
				add(node, Symbol.Term.IDENTIFIER, "Expected IDENTIFIER when parsing AtomicExpression");
				node.add(parseArgumentsOpt());
				break;
			case INTCONST:
				add(node, Symbol.Term.INTCONST, "Expected INTCONST when parsing AtomicExpression");
				break;
			case VOIDCONST:
				add(node, Symbol.Term.VOIDCONST, "Expected VOIDCONST when parsing AtomicExpression");
				break;
			case CHARCONST:
				add(node, Symbol.Term.CHARCONST, "Expected CHARCONST when parsing AtomicExpression");
				break;
			case BOOLCONST:
				add(node, Symbol.Term.BOOLCONST, "Expected BOOLCONST when parsing AtomicExpression");
				break;
			case PTRCONST:
				add(node, Symbol.Term.PTRCONST, "Expected PTRCONST when parsing AtomicExpression");
				break;
			case STRCONST:
				add(node, Symbol.Term.STRCONST, "Expected STRCONST when parsing AtomicExpression");
				break;
			case LBRACE:
				add(node, Symbol.Term.LBRACE, "Expected LBRACE when parsing AtomicExpression");
				node.add(parseStatements());
				add(node, Symbol.Term.COLON, "Expected COLON  when parsing AtomicExpression");
				node.add(parseExpression());
				node.add(parseWhereOpt());
				add(node, Symbol.Term.RBRACE, "Expected RBRACE when parsing AtomicExpression");
				break;
			default:
				throw new Report.Error("Cannot parse AtomicExpression");
		}

		return node;
	}

	private DerNode parseType() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Type);

		switch (cur) {
			case IDENTIFIER:
				add(node, Symbol.Term.IDENTIFIER, "Expected IDENTIFER when parsing Type");
				break;
			case LPARENTHESIS:
				add(node, Symbol.Term.LPARENTHESIS, "Expected LPARENTHESIS when parsing Type");
				node.add(parseType());
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing Type");
				break;
			case INT:
				add(node, Symbol.Term.INT, "Expected INT when parsing Type");
				break;
			case VOID:
				add(node, Symbol.Term.VOID, "Expected VOID when parsing Type");
				break;
			case CHAR:
				add(node, Symbol.Term.CHAR, "Expected CHAR when parsing Type");
				break;
			case BOOL:
				add(node, Symbol.Term.BOOL, "Expected BOOL when parsing Type");
				break;
			case ARR:
				add(node, Symbol.Term.ARR, "Expected ARR when parsing Type");
				add(node, Symbol.Term.LBRACKET, "Expected LBRACKET when parsing Type");
				node.add(parseExpression());
				add(node, Symbol.Term.RBRACKET, "Expected RBRACKET when parsing Type");
				node.add(parseType());
				break;
			case PTR:
				add(node, Symbol.Term.PTR, "Expected PTR when parsing Type");
				node.add(parseType());
				break;
			case REC:
				add(node, Symbol.Term.REC, "Expected PTR when parsing REC");
				add(node, Symbol.Term.LPARENTHESIS, "Expected LPARENTHESIS when parsing Type");
				node.add(parseParameters());
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing Type");
				break;
			default:
				throw new Report.Error("Cannot parse Type");
		}

		return node;
	}

	private DerNode parseFunctionBodyOpt() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.FunctionBodyOpt);

		switch (cur) {
			case SEMIC:
				break;
			case ASSIGN:
				add(node, Symbol.Term.ASSIGN, "Expected ASSIGN when parsing FunctionBodyOpt");
				node.add(parseExpression());
				break;
			default:
				throw new Report.Error("Cannot parse FunctionBodyOpt");
		}

		return node;
	}

	private DerNode parseParametersOpt() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ParametersOpt);

		switch (cur) {
			case IDENTIFIER:
				node.add(parseParameters());
				break;
			case RPARENTHESIS:
				break;
			default:
				throw new Report.Error("Cannot parse ParametersOpt");
		}

		return node;
	}

	private DerNode parseParameters() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Parameters);

		switch (cur) {
			case IDENTIFIER:
				add(node, Symbol.Term.IDENTIFIER, "Expected IDENTIFIER when parsing Parameters");
				add(node, Symbol.Term.COLON, "Expected COLON when parsing Parameters");
				node.add(parseType());
				node.add(parseParametersP());
				break;
			default:
				throw new Report.Error("Cannot parse Parameters");
		}

		return node;
	}

	private DerNode parseParametersP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ParametersP);

		switch (cur) {
			case RPARENTHESIS:
				break;
			case COMMA:
				add(node, Symbol.Term.COMMA, "Expected COMMA when parsing ParametersP");
				add(node, Symbol.Term.IDENTIFIER, "Expected IDENTIFIER when parsing ParametersP");
				add(node, Symbol.Term.COLON, "Expected COLON when parsing ParametersP");
				node.add(parseType());
				node.add(parseParametersP());
				break;
			default:
				throw new Report.Error("Cannot parse ParametersP");
		}

		return node;
	}

	private DerNode parseStatements() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Statements);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case IF:
			case WHILE:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseStatement());
				add(node, Symbol.Term.SEMIC, "Expected SEMIC when parsing Statements");
				node.add(parseStatementsP());
				break;
			default:
				throw new Report.Error("Cannot parse Statements");
		}

		return node;
	}

	private DerNode parseStatementsP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.StatementsP);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case IF:
			case WHILE:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseStatement());
				add(node, Symbol.Term.SEMIC, "Expected SEMIC when parsing StatementsP");
				node.add(parseStatementsP());
				break;
			case COLON:
			case END:
			case ELSE:
				break;
			default:
				throw new Report.Error("Cannot parse StatementsP");
		}

		return node;
	}

	private DerNode parseWhereOpt() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.WhereOpt);

		switch (cur) {
			case RBRACE:
				break;
			case WHERE:
				add(node, Symbol.Term.WHERE, "Expected WHERE when parsing WhereOpt");
				node.add(parseDeclarations());
				break;
			default:
				throw new Report.Error("Cannot parse WhereOpt");
		}

		return node;
	}

	private DerNode parseArgumentsOpt() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ArgumentsOpt);

		switch (cur) {
			case COLON:
			case SEMIC:
			case RPARENTHESIS:
			case ASSIGN:
			case THEN:
			case DO:
			case IOR:
			case XOR:
			case AND:
			case EQU:
			case NEQ:
			case GEQ:
			case LEQ:
			case GTH:
			case LTH:
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
			case LBRACKET:
			case RBRACKET:
			case DOT:
			case RBRACE:
			case COMMA:
			case WHERE:
				break;
			case LPARENTHESIS:
				add(node, Symbol.Term.LPARENTHESIS, "Expected LPARENTHESIS when parsing ArgumentsOpt");
				node.add(parseArgumentsOptP());
				break;
			default:
				throw new Report.Error("Cannot parse ArgumentsOpt");
		}

		return node;
	}

	private DerNode parseArgumentsOptP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ArgumentsOptP);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseArguments());
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing ArgumentsOptP");
				break;
			case RPARENTHESIS:
				add(node, Symbol.Term.RPARENTHESIS, "Expected RPARENTHESIS when parsing ArgumentsOptP");
				break;
			default:
				throw new Report.Error("Cannot parse ArgumentsOptP");
		}

		return node;
	}

	private DerNode parseArguments() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.Arguments);

		switch (cur) {
			case IDENTIFIER:
			case LPARENTHESIS:
			case ADD:
			case SUB:
			case NOT:
			case NEW:
			case DEL:
			case DATA:
			case ADDR:
			case INTCONST:
			case VOIDCONST:
			case CHARCONST:
			case BOOLCONST:
			case PTRCONST:
			case STRCONST:
			case LBRACE:
				node.add(parseExpression());
				node.add(parseArgumentsP());
				break;
			default:
				throw new Report.Error("Cannot parse Arguments");
		}

		return node;
	}

	private DerNode parseArgumentsP() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ArgumentsP);

		switch (cur) {
			case RPARENTHESIS:
				break;
			case COMMA:
				add(node, Symbol.Term.COMMA, "Expected COMMA when parsing ArgumentsP");
				node.add(parseExpression());
				node.add(parseArgumentsP());
				break;
			default:
				throw new Report.Error("Cannot parse ArgumentsP");
		}

		return node;
	}

	private DerNode parseElseOpt() {
		Symbol.Term cur = currSymb.token;
		DerNode node = new DerNode(DerNode.Nont.ElseOpt);

		switch (cur) {
			case END:
				break;
			case ELSE:
				add(node, Symbol.Term.ELSE, "Expected ELSE when parsing ElseOpt");
				node.add(parseStatements());
				break;
			default:
				throw new Report.Error("Cannot parse ElseOpt");
		}

		return node;
	}
}