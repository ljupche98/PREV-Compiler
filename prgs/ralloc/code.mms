                	LOC	#0
                	GREG	0
                	GREG	0
                	GREG	0
                	SETH	$254,#3000
                	SETH	$253,#3000
                	SETH	$252,#2000

                	LOC	#20000000
                	GREG	@
OutData         	BYTE	0

                	LOC	#30000000
                	GREG	@
_putChar        	LDO	$0,$254,0
                	LDA	$1,OutData
                	OR	$255,$1,0
                	STB	$0,$1,0
                	ADD	$1,$1,1
                	SETL	$0,0
                	STB	$0,$1,0
                	TRAP	0,Fputs,StdOut
                	POP	8,0

                	LOC	#10000000
                	GREG	@
_i              	OCTA	0
                	GREG	@
L0              	OCTA	115
                	OCTA	116
                	OCTA	117
                	OCTA	112
                	OCTA	105
                	OCTA	100
                	OCTA	32
                	OCTA	115
                	OCTA	116
                	OCTA	114
                	OCTA	105
                	OCTA	110
                	OCTA	103
                	OCTA	0
                	GREG	@
_j              	OCTA	0
                	GREG	@
_z              	OCTA	0
                	OCTA	0
                	OCTA	0
                	OCTA	0
                	OCTA	0
                	OCTA	0
                	OCTA	0
                	OCTA	0
                	OCTA	0
                	OCTA	0
                	GREG	@
_k              	OCTA	0

                	LOC	#40000000
                	GREG	@
_main           	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	ADD	$0,$0,8
                	SUB	$0,$253,$0
                	STO	$253,$0,0
                	SUB	$0,$0,8
                	GET	$1,rJ
                	STO	$1,$0,0
                	OR	$253,$254,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	SUB	$254,$254,$0
                	JMP	L1
                	GREG	@
L1              	OR	$0,$0,0
                	SETL	$0,1
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	LDA	$0,_i
                	STO	$0,$1,0
                	LDA	$0,L0
                	OR	$0,$0,0
                	SETL	$0,2
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,3
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	DIV	$0,$1,$0
                	GET	$0,rR
                	OR	$0,$0,0
                	OR	$0,$0,0
                	JMP	L2
                	GREG	@
L2              	OR	$0,$0,0
                	OR	$1,$253,0
                	STO	$0,$1,0
                	OR	$254,$253,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	ADD	$0,$0,8
                	SUB	$0,$253,$0
                	LDO	$253,$0,0
                	SUB	$0,$0,8
                	LDO	$0,$0,0
                	PUT	rJ,$1
                	POP	8,0
                	GREG	@
_b              	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	ADD	$0,$0,8
                	SUB	$0,$253,$0
                	STO	$253,$0,0
                	SUB	$0,$0,8
                	GET	$1,rJ
                	STO	$1,$0,0
                	OR	$253,$254,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	SUB	$254,$254,$0
                	JMP	L3
                	GREG	@
L3              	OR	$0,$0,0
                	SETL	$0,1
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	XOR	$0,$1,$0
                	OR	$0,$0,0
                	JMP	L4
                	GREG	@
L4              	OR	$0,$0,0
                	OR	$1,$253,0
                	STO	$0,$1,0
                	OR	$254,$253,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	ADD	$0,$0,8
                	SUB	$0,$253,$0
                	LDO	$253,$0,0
                	SUB	$0,$0,8
                	LDO	$0,$0,0
                	PUT	rJ,$1
                	POP	8,0

