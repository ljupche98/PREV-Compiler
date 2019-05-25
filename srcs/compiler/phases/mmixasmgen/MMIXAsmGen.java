/**
 * @author sliva
 */
package compiler.phases.mmixasmgen;

import java.io.*;
import java.util.*;
import compiler.phases.*;
import compiler.data.*;
import compiler.data.asmcode.*;
import compiler.data.layout.*;
import compiler.data.abstree.*;
import compiler.data.abstree.visitor.*;
import compiler.data.type.*;
import compiler.data.asmcode.*;
import compiler.common.report.*;
import compiler.phases.lexan.*;
import compiler.phases.synan.*;
import compiler.phases.abstr.*;
import compiler.phases.asmcode.*;
import compiler.phases.seman.*;
import compiler.phases.frames.*;
import compiler.phases.imcgen.*;
import compiler.phases.chunks.*;
import compiler.phases.livean.*;
import compiler.phases.ralloc.*;

/**
 * @author sliva
 */
public class MMIXAsmGen extends Phase {

	/**
		#10000000 -> Static data from source code.
		#20000000 -> Output data buffer.
		#30000000 -> STD library function definitions.
		#40000000 -> Function ASM code.
	**/

	public int numOfRegs = 8;
	public final String format = "%-16s\t%s\t%s\n";
	public static PrintWriter file;

	public MMIXAsmGen() {
		super("mmixasmgen");

		try {
			file = new PrintWriter("code.mms", "UTF-8");
		} catch (Exception e) {
			System.out.println("PrintWriter err");
		}
	}

	public void close() {
		file.close();
	}

	public void initPutChar() {
		file.printf(format, "", "GREG", "@");
		file.printf(format, "_putChar", "LDO", "$0,$254,8");
		file.printf(format, "", "LDA", "$1,OutData");
		file.printf(format, "", "OR", "$255,$1,0");
		file.printf(format, "", "STB", "$0,$1,0");
		file.printf(format, "", "ADD", "$1,$1,1");
		file.printf(format, "", "SETL", "$0,0");
		file.printf(format, "", "STB", "$0,$1,0");
		file.printf(format, "", "TRAP", "0,Fputs,StdOut");
		file.printf(format, "", "POP", Integer.toString(numOfRegs) + ",0");
		file.printf("\n");
	}

	public void initPutInt() {
		file.printf(format, "", "GREG", "@");
		file.printf(format, "_putInt", "LDO", "$0,$254,8");
		file.printf(format, "", "LDA", "$1,OutData");
		file.printf(format, "", "OR", "$255,$1,0");

		file.printf(format, "", "OR", "$2,$0,0");
		file.printf(format, "", "SETL", "$3,0");
		file.printf(format, "PutIntDigCntBeg", "BNP", "$2,PutIntDigCntEnd");
		file.printf(format, "", "DIV", "$2,$2,0");
		file.printf(format, "", "ADD", "$3,$3,1");
		file.printf(format, "", "JMP", "PutIntDigCntBeg");
		file.printf(format, "PutIntDigCntEnd", "OR", "$0,$0,$0");

		file.printf(format, "PutIntBeg", "BNP", "$0,PutIntEnd");
		file.printf(format, "", "DIV", "$0,$0,10");
		file.printf(format, "", "GET", "$2,rR");
		file.printf(format, "", "ADD", "$2,$2,48");
		file.printf(format, "", "STB", "$2,$1,0");
		file.printf(format, "", "ADD", "$1,$1,1");
		file.printf(format, "", "JMP", "PutIntBeg");
		file.printf(format, "PutIntEnd", "SETL", "$0,0");
		file.printf(format, "", "STB", "$0,$1,0");
		file.printf(format, "", "TRAP", "0,Fputs,StdOut");
		file.printf(format, "", "POP", Integer.toString(numOfRegs) + ",0");
		file.printf("\n");
	}

	public void initSTDLibrary() {
		file.printf(format, "", "LOC", "#30000000");
		initPutChar();
	///	initPutInt();
	}

	public void initOutData() {
		file.printf(format, "", "LOC", "#20000000");
		file.printf(format, "", "GREG", "@");
		file.printf(format, "OutData", "BYTE", "0");
		file.printf("\n");
	}

	public void initRegisters() {
		file.printf(format, "", "LOC", "#0");
		file.printf(format, "", "GREG", "0");
		file.printf(format, "", "GREG", "0");
		file.printf(format, "", "GREG", "0");
		file.printf("\n");
	}

	public void init() {
		initRegisters();
		initOutData();
		initSTDLibrary();
	}

	public void generateData() {
		file.printf(format, "", "LOC", "#10000000");
		for (int i = 0; i < Chunks.dataChunks.size(); i++) {
			file.printf(format, "", "GREG", "@");

			if (Chunks.dataChunks.get(i).init == null) {	/// global variable
				for (int j = 0; j < Chunks.dataChunks.get(i).size / 8; j++)
					file.printf(format, j == 0 ? Chunks.dataChunks.get(i).label.name : "", "OCTA", "0");
			} else {					/// string constant
				for (int j = 1; j < Chunks.dataChunks.get(i).init.length() - 1; j++)
					file.printf(format, j == 1 ? Chunks.dataChunks.get(i).label.name : "", "OCTA", Integer.toString((int) Chunks.dataChunks.get(i).init.charAt(j)));

				file.printf(format, "", "OCTA", "0");
			}
		}
		file.printf("\n");
	}

	public void generatePrologue(Code code) {
		file.printf(format, code.frame.label.name, "SETL",  "$0," + Math.abs(0x000000000000FFFFL & code.frame.locsSize));
		file.printf(format, "", "INCML", "$0," + Math.abs(0x00000000FFFF0000L & code.frame.locsSize));
		file.printf(format, "", "INCMH", "$0," + Math.abs(0x0000FFFF00000000L & code.frame.locsSize));
		file.printf(format, "", "INCH",  "$0," + Math.abs(0xFFFF000000000000L & code.frame.locsSize));
		file.printf(format, "", "ADD", "$0,$0,8");
		file.printf(format, "", "SUB", "$0,$254,$0");	/// $0 <- SP - locsSize - 8;
		file.printf(format, "", "STO", "$253,$0,0");	/// store FP.

		file.printf(format, "", "SUB", "$0,$0,8");
		file.printf(format, "", "GET", "$1,rJ");
		file.printf(format, "", "STO", "$1,$0,0");	/// store RA.

		file.printf(format, "", "OR", "$253,$254,0");	/// FP <- SP

		file.printf(format, "", "SETL",  "$0," + Math.abs(0x000000000000FFFFL & code.frame.size));
		file.printf(format, "", "INCML", "$0," + Math.abs(0x00000000FFFF0000L & code.frame.size));
		file.printf(format, "", "INCMH", "$0," + Math.abs(0x0000FFFF00000000L & code.frame.size));
		file.printf(format, "", "INCH",  "$0," + Math.abs(0xFFFF000000000000L & code.frame.size));
		file.printf(format, "", "SUB", "$254,$254,$0");	/// SP <- SP - frame size.

		file.printf(format, "", "JMP", code.entryLabel.name);
	///	file.printf(format, "", "LDA", "$0," + code.entryLabel.name);
	///	file.printf(format, "", "GO", "$0,$0,0");
	}

	public void generateBody(Code code) {
		for (int i = 0; i < code.instrs.size(); i++) {
			if (code.instrs.get(i) instanceof AsmLABEL)
				file.printf(format, ((AsmLABEL) code.instrs.get(i)).label.name, "OR", "$0,$0,0");
			else
				file.printf(format, "", ((AsmOPER) code.instrs.get(i)).toString(code.regs).split(" ")[0], ((AsmOPER) code.instrs.get(i)).toString(code.regs).split(" ")[1]);
		}
	}

	public void generateEpilogue(Code code) {
		file.printf(format, code.exitLabel.name, "OR", "$0,$" + code.regs.get(code.frame.RV) + ",0");	/// $0 <- RV

		file.printf(format, "", "OR", "$1,$253,0");							/// $1 <- FP

		file.printf(format, "", "STO", "$0,$1,0");							/// Store RV.

		file.printf(format, "", "OR", "$254,$253,0");							/// SP <- FP

		file.printf(format, "", "SETL",  "$0," + Math.abs(0x000000000000FFFFL & code.frame.locsSize));
		file.printf(format, "", "INCML", "$0," + Math.abs(0x00000000FFFF0000L & code.frame.locsSize));
		file.printf(format, "", "INCMH", "$0," + Math.abs(0x0000FFFF00000000L & code.frame.locsSize));
		file.printf(format, "", "INCH",  "$0," + Math.abs(0xFFFF000000000000L & code.frame.locsSize));
		file.printf(format, "", "ADD", "$0,$0,8");
		file.printf(format, "", "SUB", "$0,$253,$0");
		file.printf(format, "", "LDO", "$253,$0,0");							/// FP <- oldFP

		file.printf(format, "", "SUB", "$0,$0,8");
		file.printf(format, "", "LDO", "$0,$0,0");							/// $0 <- RA

		file.printf(format, "", "PUT", "rJ,$0");							/// rJ <- $0 (RA)

		file.printf(format, "", "POP", numOfRegs + ",0");
	}

	public void generateFunctionASMCode(Code code) {
		file.printf(format, "", "GREG", "@");
		generatePrologue(code);
		file.printf(format, "", "GREG", "@");
		generateBody(code);
		file.printf(format, "", "GREG", "@");
		generateEpilogue(code);
	}

	public void generateMMIXMainBootstrap() {
		file.printf(format, "", "GREG", "@");
		file.printf(format, "Main", "SETH", "$254,#3000");			/// SP
		file.printf(format, "", "SETH", "$253,#3000");				/// FP
		file.printf(format, "", "SETH", "$252,#2000");				/// HP
		file.printf(format, "", "PUSHJ", "$" + numOfRegs + ",_main");
		file.printf(format, "", "TRAP", "0,Halt,0");
	}

	public void generateCode() {
	///	file.printf(format, "", "LOC", "#40000000");
		for (int i = 0; i < AsmGen.codes.size(); i++) generateFunctionASMCode(AsmGen.codes.get(i));
		generateMMIXMainBootstrap();
		file.printf("\n");
	}
}
