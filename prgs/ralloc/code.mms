                	LOC	#0
                	GREG	0
                	GREG	0
                	GREG	0

                	LOC	#20000000
                	GREG	@
OutData         	BYTE	0

                	LOC	#30000000
                	GREG	@
_putChar        	LDO	$0,$254,8
                	LDA	$1,OutData
                	OR	$255,$1,0
                	STB	$0,$1,0
                	ADD	$1,$1,1
                	SETL	$0,0
                	STB	$0,$1,0
                	TRAP	0,Fputs,StdOut
                	POP	8,0

                	GREG	@
_putInt         	SETL	$0,0
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
                	SETL	$0,32
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	SUB	$254,$254,$0
                	JMP	L18
                	GREG	@
L18             	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	CMP	$0,$1,$0
                	ZSZ	$0,$0,1
                	OR	$0,$0,0
                	BNZ	$0,L6
L15             	OR	$0,$0,0
                	JMP	L7
L6              	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,48
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$1,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	JMP	L8
L7              	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	CMP	$0,$1,$0
                	ZSN	$0,$0,1
                	OR	$0,$0,0
                	BNZ	$0,L3
L16             	OR	$0,$0,0
                	JMP	L4
L3              	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,45
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$1,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$0,$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	STO	$2,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putInt
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	JMP	L5
L4              	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,10
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	CMP	$0,$1,$0
                	ZSN	$0,$0,1
                	OR	$0,$0,0
                	BNZ	$0,L0
L17             	OR	$0,$0,0
                	JMP	L1
L0              	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,48
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$1,$0,0
                	SETL	$0,256
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	DIV	$0,$1,$0
                	GET	$0,rR
                	OR	$0,$0,0
                	STO	$2,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	JMP	L2
L1              	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,10
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	DIV	$0,$1,$0
                	OR	$0,$0,0
                	STO	$2,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putInt
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	OR	$2,$253,0
                	SETL	$1,8
                	INCML	$1,0
                	INCMH	$1,0
                	INCH	$1,0
                	OR	$1,$1,0
                	ADD	$1,$2,$1
                	OR	$1,$1,0
                	LDO	$1,$1,0
                	OR	$2,$1,0
                	SETL	$1,10
                	INCML	$1,0
                	INCMH	$1,0
                	INCH	$1,0
                	OR	$1,$1,0
                	DIV	$1,$2,$1
                	GET	$1,rR
                	OR	$2,$1,0
                	SETL	$1,48
                	INCML	$1,0
                	INCMH	$1,0
                	INCH	$1,0
                	OR	$1,$1,0
                	ADD	$1,$2,$1
                	OR	$2,$1,0
                	SETL	$1,256
                	INCML	$1,0
                	INCMH	$1,0
                	INCH	$1,0
                	OR	$1,$1,0
                	DIV	$1,$2,$1
                	GET	$1,rR
                	OR	$1,$1,0
                	STO	$0,$254,0
                	STO	$1,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
L2              	OR	$0,$0,0
L5              	OR	$0,$0,0
L8              	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	OR	$0,$0,0
                	JMP	L19
                	GREG	@
L19             	OR	$0,$0,0
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
                	PUT	rJ,$0
                	POP	8,0
                	GREG	@
_g              	SETL	$0,0
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
                	SETL	$0,16
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	SUB	$254,$254,$0
                	JMP	L20
                	GREG	@
L20             	OR	$0,$0,0
                	SETL	$0,17
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	JMP	L21
                	GREG	@
L21             	OR	$0,$0,0
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
                	PUT	rJ,$0
                	POP	8,0
                	GREG	@
_h              	SETL	$0,88
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
                	SETL	$0,120
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	SUB	$254,$254,$0
                	JMP	L24
                	GREG	@
L24             	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$1,$0,0
                	SETL	$0,1
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$0,$1,0
                	OR	$1,$253,0
                	SETL	$0,88
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$2,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	MUL	$0,$1,$0
                	OR	$0,$0,0
                	ADD	$0,$2,$0
                	OR	$1,$0,0
                	SETL	$0,1
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$0,$1,0
L9              	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,10
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	CMP	$0,$1,$0
                	ZSN	$0,$0,1
                	OR	$0,$0,0
                	BNZ	$0,L10
L22             	OR	$0,$0,0
                	JMP	L11
L10             	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,88
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	MUL	$0,$1,$0
                	OR	$0,$0,0
                	ADD	$0,$2,$0
                	OR	$3,$0,0
                	OR	$1,$253,0
                	SETL	$0,88
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,1
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	SUB	$0,$1,$0
                	OR	$1,$0,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	MUL	$0,$1,$0
                	OR	$0,$0,0
                	ADD	$0,$2,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,17
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	MUL	$0,$1,$0
                	OR	$0,$0,0
                	STO	$0,$3,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,1
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	STO	$0,$2,0
                	JMP	L9
L11             	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$1,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$0,$1,0
L12             	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,10
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	CMP	$0,$1,$0
                	ZSN	$0,$0,1
                	OR	$0,$0,0
                	BNZ	$0,L13
L23             	OR	$0,$0,0
                	JMP	L14
L13             	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$0,$0,0
                	STO	$2,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putInt
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,32
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$1,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$3,$0,0
                	OR	$1,$253,0
                	SETL	$0,88
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	MUL	$0,$1,$0
                	OR	$0,$0,0
                	ADD	$0,$2,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$0,$0,0
                	STO	$3,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putInt
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$2,$0,0
                	SETL	$0,10
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,256
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	DIV	$0,$1,$0
                	GET	$0,rR
                	OR	$0,$0,0
                	STO	$2,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$2,$0,0
                	OR	$1,$253,0
                	SETL	$0,8
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	LDO	$0,$0,0
                	OR	$1,$0,0
                	SETL	$0,1
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	ADD	$0,$1,$0
                	OR	$0,$0,0
                	STO	$0,$2,0
                	JMP	L12
L14             	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	OR	$0,$0,0
                	JMP	L25
                	GREG	@
L25             	OR	$0,$0,0
                	OR	$1,$253,0
                	STO	$0,$1,0
                	OR	$254,$253,0
                	SETL	$0,88
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	ADD	$0,$0,8
                	SUB	$0,$253,$0
                	LDO	$253,$0,0
                	SUB	$0,$0,8
                	LDO	$0,$0,0
                	PUT	rJ,$0
                	POP	8,0
                	GREG	@
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
                	SETL	$0,32
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	SUB	$254,$254,$0
                	JMP	L26
                	GREG	@
L26             	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,99
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$1,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$2,$0,0
                	SETL	$0,10
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,256
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	DIV	$0,$1,$0
                	GET	$0,rR
                	OR	$0,$0,0
                	STO	$2,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,123
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$1,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putInt
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$2,$0,0
                	SETL	$0,10
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,256
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	DIV	$0,$1,$0
                	GET	$0,rR
                	OR	$0,$0,0
                	STO	$2,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$0,$254,0
                	PUSHJ	$8,_g
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	NEG	$0,0,$0
                	OR	$0,$0,0
                	STO	$1,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putInt
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$2,$0,0
                	SETL	$0,10
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$1,$0,0
                	SETL	$0,256
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	DIV	$0,$1,$0
                	GET	$0,rR
                	OR	$0,$0,0
                	STO	$2,$254,0
                	STO	$0,$254,8
                	PUSHJ	$8,_putChar
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	STO	$0,$254,0
                	PUSHJ	$8,_h
                	LDO	$0,$254,0
                	OR	$0,$0,0
                	SETL	$0,0
                	INCML	$0,0
                	INCMH	$0,0
                	INCH	$0,0
                	OR	$0,$0,0
                	OR	$0,$0,0
                	JMP	L27
                	GREG	@
L27             	OR	$0,$0,0
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
                	PUT	rJ,$0
                	POP	8,0
                	GREG	@
Main            	SETH	$254,#3000
                	SETH	$253,#3000
                	SETH	$252,#2000
                	PUSHJ	$8,_main
                	TRAP	0,Halt,0

                	LOC	#10000000

