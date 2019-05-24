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
_i              	OCTA	0
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
_j              	OCTA	0
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
_k              	OCTA	0

                	LOC	#40000000
_main           	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	ADD	$0,$0,8
                	SUB	$0,$254,$0
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
                	LDA	$0,L1
                	GO	$0,$0,0
_b              	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	ADD	$0,$0,8
                	SUB	$0,$254,$0
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
                	LDA	$0,L3
                	GO	$0,$0,0

