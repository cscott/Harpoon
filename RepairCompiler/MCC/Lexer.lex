package MCC;
%%

%implements java_cup.runtime.Scanner
%function next_token
%type java_cup.runtime.Symbol
%class Lexer
%char
%line
%full
%state COMMENT
%state MLCOMMENT
%eofval{
    return tok(Sym.EOF, null);
%eofval}

%{
private java_cup.runtime.Symbol tok(int kind, Object value) {
    return new Symbol(kind, yychar, yychar + yylength(), value, yyline);
}
%}

ALPHA=[A-Za-z_]
DIGIT=[0-9]
HEX_DIGIT=[0-9a-fA-F]
WHITESPACE=[\ \t\b\r\f]
CHAR=(\\\"|\\\'|\\\\|\\t|\\n|[\x20-\x21\x23-\x26\x28-\x5B\x5D-\x7E])

%%

<YYINITIAL> {WHITESPACE}		{}
<YYINITIAL> \n				{ LineCount.addLineBreak(yychar+1); } 

<YYINITIAL> "{"				{ return tok(Sym.OPENBRACE, yytext()); } 
<YYINITIAL> "}"				{ return tok(Sym.CLOSEBRACE, yytext()); } 
<YYINITIAL> "("				{ return tok(Sym.OPENPAREN, yytext()); } 
<YYINITIAL> ")"				{ return tok(Sym.CLOSEPAREN, yytext()); } 
<YYINITIAL> "["				{ return tok(Sym.OPENBRACKET, yytext()); } 
<YYINITIAL> "]"				{ return tok(Sym.CLOSEBRACKET, yytext()); } 

<YYINITIAL> "+"				{ return tok(Sym.ADD, yytext()); }
<YYINITIAL> "-"				{ return tok(Sym.SUB, yytext()); }
<YYINITIAL> "*"				{ return tok(Sym.MULT, yytext()); }
<YYINITIAL> "/"				{ return tok(Sym.DIV, yytext()); }


<YYINITIAL> "<"				{ return tok(Sym.LT, yytext()); }
<YYINITIAL> ">"				{ return tok(Sym.GT, yytext()); }
<YYINITIAL> "<="			{ return tok(Sym.LE, yytext()); }
<YYINITIAL> ">="			{ return tok(Sym.GE, yytext()); }
<YYINITIAL> "="				{ return tok(Sym.EQ, yytext()); }
<YYINITIAL> "!="			{ return tok(Sym.NE, yytext()); }
<YYINITIAL> "!"				{ return tok(Sym.NOT, yytext()); }

<YYINITIAL> forall			{ return tok(Sym.FORALL, yytext()); }
<YYINITIAL> in\?                        { return tok(Sym.INTEST, yytext()); }
<YYINITIAL> in                          { return tok(Sym.IN, yytext()); }

<YYINITIAL> ","				{ return tok(Sym.COMMA, yytext()); } 
<YYINITIAL> sizeof                      { return tok(Sym.SIZEOF, yytext()); }

<YYINITIAL> ".~"			{ return tok(Sym.DOTINV, yytext()); } 
<YYINITIAL> "."				{ return tok(Sym.DOT, yytext()); } 

<YYINITIAL> "and"			{ return tok(Sym.AND, yytext()); }
<YYINITIAL> "or" 			{ return tok(Sym.OR, yytext()); }

<YYINITIAL> literal			{ return tok(Sym.LITERAL, yytext()); }
<YYINITIAL> param  			{ return tok(Sym.PARAM, yytext()); }
<YYINITIAL> "=>"		        { return tok(Sym.IMPLIES, yytext()); }
<YYINITIAL> true			{ return tok(Sym.TRUE, yytext()); }
<YYINITIAL> false			{ return tok(Sym.FALSE, yytext()); }
<YYINITIAL> isvalid			{ return tok(Sym.ISVALID, yytext()); }
<YYINITIAL> for				{ return tok(Sym.FOR, yytext()); }
<YYINITIAL> to				{ return tok(Sym.TO, yytext()); }

<YYINITIAL> structure			{ return tok(Sym.STRUCTURE, yytext()); }
<YYINITIAL> reserved	      		{ return tok(Sym.RESERVED, yytext()); }
<YYINITIAL> label			{ return tok(Sym.LABEL, yytext()); }
<YYINITIAL> int				{ return tok(Sym.INT, yytext()); }
<YYINITIAL> bit                         { return tok(Sym.BIT, yytext()); }
<YYINITIAL> byte                        { return tok(Sym.BYTE, yytext()); }

<YYINITIAL> subtype			{ return tok(Sym.SUBTYPE, yytext()); }
<YYINITIAL> of 				{ return tok(Sym.OF, yytext()); }

<YYINITIAL> ";"				{ return tok(Sym.SEMICOLON, yytext()); }
<YYINITIAL> ":"				{ return tok(Sym.COLON, yytext()); }

<YYINITIAL> set				{ return tok(Sym.SET, yytext()); }
<YYINITIAL> "->"	       		{ return tok(Sym.ARROW, yytext()); }
<YYINITIAL> many       			{ return tok(Sym.MANY, yytext()); }
<YYINITIAL> "|"		         	{ return tok(Sym.BAR, yytext()); }

<YYINITIAL> partition			{ return tok(Sym.PARTITION, yytext()); }
<YYINITIAL> element			{ return tok(Sym.ELEMENT, yytext()); }
<YYINITIAL> delay			{ return tok(Sym.DELAY, yytext()); }
<YYINITIAL> static			{ return tok(Sym.STATIC, yytext()); }

<YYINITIAL> cast			{ return tok(Sym.CAST, yytext()); }
<YYINITIAL> short			{ return tok(Sym.SHORT, yytext()); }
<YYINITIAL> null			{ return tok(Sym.NULL, yytext()); }
<YYINITIAL> NULL			{ return tok(Sym.NULL, yytext()); }
<YYINITIAL> crash			{ return tok(Sym.CRASH, yytext()); }

<YYINITIAL> {ALPHA}({ALPHA}|{DIGIT})*	{ return tok(Sym.ID, yytext()); }
<YYINITIAL> {DIGIT}+	                { return tok(Sym.DECIMAL, yytext()); }
<YYINITIAL> \'{CHAR}\'			{ return tok(Sym.CHAR, yytext()); }
<YYINITIAL> \"{CHAR}*\"			{ return tok(Sym.STRING, yytext()); }

<YYINITIAL> .				{ System.err.println("Bad token at line " + (yyline + 1) + ": " + yytext()); return tok(Sym.BAD, yytext()); }

<YYINITIAL> "//"			{ yybegin(COMMENT); }
<COMMENT>   \n				{ yybegin(YYINITIAL); LineCount.addLineBreak(yychar+1); }

<YYINITIAL> "/*"			{ yybegin(MLCOMMENT); }
<MLCOMMENT> "*/"                        { yybegin(YYINITIAL); }
<MLCOMMENT> \n				{ LineCount.addLineBreak(yychar+1); }
<MLCOMMENT> .			        { }


<COMMENT>   .				{ }
