                	LOC	#0
                	GREG	0
                	GREG	0
                	GREG	0

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
L5              	OCTA	L
L5              	OCTA	5
