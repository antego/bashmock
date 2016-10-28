package com.github.antego.bashmock.lexer;

%%

%class SimpleLexer
%unicode
%intwrap

CRLF= \n|\r|\r\n
WHITE_SPACE=[\ \t\f]
FIRST_VALUE_CHARACTER=[^ \n\r\f\\] | "\\"{CRLF} | "\\".
VALUE_CHARACTER=[^\n\r\f\\] | "\\"{CRLF} | "\\".
END_OF_LINE_COMMENT=("#"|"!")[^\r\n]*
SEPARATOR=[:=]
KEY_CHARACTER=[^:=\ \n\r\t\f\\] | "\\ "

%state WAITING_VALUE

%%

<YYINITIAL> {END_OF_LINE_COMMENT}                           { yybegin(YYINITIAL); return 1; }

<YYINITIAL> {KEY_CHARACTER}+                                { yybegin(YYINITIAL); return 1; }

<YYINITIAL> {SEPARATOR}                                     { yybegin(WAITING_VALUE); return 1; }

<WAITING_VALUE> {CRLF}({CRLF}|{WHITE_SPACE})+               { yybegin(YYINITIAL); return 1; }

<WAITING_VALUE> {WHITE_SPACE}+                              { yybegin(WAITING_VALUE); return 1; }

<WAITING_VALUE> {FIRST_VALUE_CHARACTER}{VALUE_CHARACTER}*   { yybegin(YYINITIAL); return 1; }

({CRLF}|{WHITE_SPACE})+                                     { yybegin(YYINITIAL); return 1; }

{WHITE_SPACE}+                                              { yybegin(YYINITIAL); return 1; }

.                                                           { return 0; }