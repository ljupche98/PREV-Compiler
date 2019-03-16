package compiler.phases.lexan;

import java.io.*;
import compiler.common.report.*;
import compiler.data.symbol.*;
import compiler.phases.*;

/**
 * Lexical analysis.
 * 
 * @author sliva
 */
public class LexAn extends Phase {

	/** The name of the source file. */
	private final String srcFileName;

	/** The source file reader. */
	private final BufferedReader srcFile;

	private int state;
	private int curChar;
	private int prevChar;
	private String lexeme;

	private String in;

	private final String[] keywords = {"none", "true", "false", "null", "arr", "bool", "char", "del", "do",
	"else", "end", "fun", "if", "int", "new", "ptr", "rec", "then", "typ", "var", "void", "where", "while"};
	private final Symbol.Term[] keywordTerms = {Symbol.Term.VOIDCONST, Symbol.Term.BOOLCONST, Symbol.Term.BOOLCONST, Symbol.Term.PTRCONST,
	Symbol.Term.ARR, Symbol.Term.BOOL, Symbol.Term.CHAR, Symbol.Term.DEL, Symbol.Term.DO, Symbol.Term.ELSE, Symbol.Term.END,
	Symbol.Term.FUN, Symbol.Term.IF, Symbol.Term.INT, Symbol.Term.NEW, Symbol.Term.PTR, Symbol.Term.REC, Symbol.Term.THEN,
	Symbol.Term.TYP, Symbol.Term.VAR, Symbol.Term.VOID, Symbol.Term.WHERE, Symbol.Term.WHILE};

	private final char[] oneCharOperators = {'|', '^', '&', '+', '-', '*', '/', '%', '$', '@', '.', ',', ':', ';', '[', ']', '(', ')', '{', '}'};
	private final Symbol.Term[] oneCharOperatorTerms = {Symbol.Term.IOR, Symbol.Term.XOR, Symbol.Term.AND, Symbol.Term.ADD, Symbol.Term.SUB,
	Symbol.Term.MUL, Symbol.Term.DIV, Symbol.Term.MOD, Symbol.Term.ADDR, Symbol.Term.DATA, Symbol.Term.DOT, Symbol.Term.COMMA, Symbol.Term.COLON,
	Symbol.Term.SEMIC, Symbol.Term.LBRACKET, Symbol.Term.RBRACKET, Symbol.Term.LPARENTHESIS, Symbol.Term.RPARENTHESIS, Symbol.Term.LBRACE, Symbol.Term.RBRACE};
 
	private final char[] twoCharOperators = {'!', '=', '<', '>'};
	private final Symbol.Term[] twoCharOperatorTermsDef = {Symbol.Term.NOT, Symbol.Term.ASSIGN, Symbol.Term.LTH, Symbol.Term.GTH};
	private final Symbol.Term[] twoCharOperatorTermsPos = {Symbol.Term.NEQ, Symbol.Term.EQU, Symbol.Term.LEQ, Symbol.Term.GEQ};


	/**
	 * Constructs a new phase of lexical analysis.
	 */
	public LexAn() {
		super("lexan");

		srcFileName = compiler.Main.cmdLineArgValue("--src-file-name");
		try {
			srcFile = new BufferedReader(new FileReader(srcFileName));
		} catch (IOException ___) {
			throw new Report.Error("Cannot open source file '" + srcFileName + "'.");
		}

		state = 0;
		curChar = 0;
		prevChar = -1;
		lexeme = new String("");
		in = new String("");
	}

	@Override
	public void close() {
		try {
			srcFile.close();
		} catch (IOException ___) {
			Report.warning("Cannot close source file '" + this.srcFileName + "'.");
		}

		super.close();
	}

	/**
	 * The lexer.
	 * 
	 * This method returns the next symbol from the source file. To perform the
	 * lexical analysis of the entire source file, this method must be called until
	 * it returns EOF. This method calls {@link #lexify()}, logs its result if
	 * requested, and returns it.
	 * 
	 * @return The next symbol from the source file or EOF if no symbol is available
	 *         any more.
	 */
	public Symbol lexer() {
		state = 0;
		lexeme = new String("");

		Symbol symb = lexify();
		if (symb.token != Symbol.Term.EOF)
			symb.log(logger);
		return symb;
	}

	public int getNextChar() {
		int ret = -1;

		if (prevChar != -1) {
			ret = prevChar;
			prevChar = -1;
		} else {
			try {
				ret = srcFile.read();
			} catch (IOException e) {
				e.printStackTrace();
			}

			in += (char) ret;
		}

		return ret;
	}

	public boolean eq(int x, char y) {
		return x == (int) y;
	}

	public void processWhiteSpace() {
		while (eq(curChar, ' ') || eq(curChar, '\t') || eq(curChar, '\n') || eq(curChar, '\r')) curChar = getNext();
		prevChar = curChar;
	}

	public void processComments() {
		if (eq(curChar, '#'))
			while (!eq(curChar, '\n'))
				curChar = getNextChar();
	}

	public int getNext() {
		curChar = getNextChar();
		if (state != 0) return curChar;
		if (state == 0) {
			processWhiteSpace();
			processComments();
		}
		return getNextChar();
	}

	public boolean inRange(int x, int l, int r) {
		return l <= x && x <= r;
	}

	public void warning(String err, Location p) {
		throw new Report.Error(err + p);
	}

	public int getLine(int it, int len) {
		it -= len;

		int ret = 0;
		while (it >= 0) {
			ret += in.charAt(it) == '\n' ? 1 : 0;
			it -= 1;
		}
		return 1 + ret;
	}

	public int getColumn(int it, int len) {
		it -= len;

		int ret = 0;
		while (it >= 0 && ( !eq((int) in.charAt(it), '\n'))) {
			ret += eq((int) in.charAt(it), '\t') ? 8 : 1;
			it -= 1;
		}
		return 1 + ret;
	}

	public Location getLocation(int a, int b) {
		return new Location(getLine(in.length() - 1 - (prevChar != -1 ? 1 : 0), lexeme.length()), getColumn(in.length() - 1 - (prevChar != -1 ? 1 : 0), lexeme.length()) + a,
				    getLine(in.length() - 1 - (prevChar != -1 ? 1 : 0), 1), 	       	  getColumn(in.length() - 1 - (prevChar != -1 ? 1 : 0), 1) + b);
	}

	public boolean processCharConst() {
		if (eq(curChar, '\'')) {
			state = 1;

			lexeme += (char) curChar;
			curChar = getNext();

			if (inRange(curChar, 32, 126)) {
				lexeme += (char) curChar;

				if (eq(curChar, '\\')) {
					curChar = getNext();
					lexeme += (char) curChar;
				}
				
				curChar = getNext();

				if (eq(curChar, '\'')) {
					lexeme += (char) curChar;
					return true;
				}

				warning("Expected ' at ", getLocation(1, 0));
			}

			warning("The character used must be in ASCII range between 32 and 126 ", getLocation(0, 0)); 
		}

		return false;
	}

	public boolean processStringConst() {
		if (eq(curChar, '"')) {
			state = 1;

			lexeme += (char) curChar;
			curChar = getNext();

			boolean escape = false;
			while (!eq(curChar, '"') || escape) {
				if (escape) escape = false;
				else {
					if (!inRange(curChar, 32, 126)) warning("The character used must be in ASCII range between 32 and 126 ", getLocation(0, 0));
					escape = curChar == '\\';
				}

				lexeme += (char) curChar;
				curChar = getNext();
			}

			lexeme += (char) curChar;
			return true;			
		}

		return false;
	}

	public boolean processNumericConst() {
		if (inRange(curChar, (int) '0', (int) '9')) {
			state = 1;

			while (inRange(curChar, (int) '0', (int) '9')) {
				lexeme += (char) curChar;
				curChar = getNext();
			}

			prevChar = curChar;

			return true;
		}

		return false;
	}

	public boolean processIdentifier() {
		if ('a' <= curChar && curChar <= 'z' || 'A' <= curChar && curChar <= 'Z' || curChar == '_') {
			state = 1;

			while ('a' <= curChar && curChar <= 'z' || 'A' <= curChar && curChar <= 'Z' || curChar == '_' || '0' <= curChar && curChar <= '9') {
				lexeme += (char) curChar;
				curChar = getNext();
			}

			prevChar = curChar;

			return true;
		}
		return false;
	}

	/**
	 * Performs the lexical analysis of the source file.
	 * 
	 * This method returns the next symbol from the source file. To perform the
	 * lexical analysis of the entire source file, this method must be called until
	 * it returns EOF.
	 * 
	 * @return The next symbol from the source file or EOF if no symbol is available
	 *         any more.
	 */
	private Symbol lexify() {
		curChar = getNext();
		
		if (curChar == -1) {
			return new Symbol(Symbol.Term.EOF, lexeme, getLocation(0, 0));
		}

		if (processCharConst()) {
			return new Symbol(Symbol.Term.CHARCONST, lexeme, getLocation(0, 0));
		}

		if (processStringConst()) {
			return new Symbol(Symbol.Term.STRCONST, lexeme, getLocation(0, 0));
		}

		if (processNumericConst()) {
			return new Symbol(Symbol.Term.INTCONST, lexeme, getLocation(0, 0));
		}

		if (processIdentifier()) {
			for (int i = 0; i < keywords.length; i++)
				if (lexeme.equals(keywords[i]))
					return new Symbol(keywordTerms[i], lexeme, getLocation(0, 0));
			return new Symbol(Symbol.Term.IDENTIFIER, lexeme, getLocation(0, 0));
		}

		lexeme += (char) curChar;

		for (int i = 0; i < oneCharOperators.length; i++)
			if (curChar == oneCharOperators[i])
				return new Symbol(oneCharOperatorTerms[i], lexeme, getLocation(0, 0));

		for (int i = 0; i < twoCharOperators.length; i++)
			if (curChar == twoCharOperators[i]) {
				curChar = getNext();

				if (curChar != '=') {
					prevChar = curChar;
					return new Symbol(twoCharOperatorTermsDef[i], lexeme, getLocation(0, 0));
				}

				lexeme += (char) curChar;
				return new Symbol(twoCharOperatorTermsPos[i], lexeme, getLocation(0, 0));
			}

		warning("Unacceptable character detected", getLocation(0, 0));

		/// this return statement never executes
		return new Symbol(Symbol.Term.EOF, lexeme, getLocation(0, 0));
	}

}
