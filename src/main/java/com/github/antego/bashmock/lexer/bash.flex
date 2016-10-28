/*** JFlex specification for Bash ****
    The Bash language is a beast. It contains many strange or unusual constructs
    and has a great flexibility in what is possible.
    The lexer tries to do as much as possible in the lexing phase to help the parser
    later on.

    A major problem is that tokens have to interpreted according to their context.
    e.g. a=b echo a=b
    has an assignment in front and a string as parameter to the echo command. So the EQ
    token (for the = character) has to be remapped to a WORD later on (see BashTokenTypeRemapper).

    Another problem is that string can contain unescaped substrings, e.g.
        "$(echo hello "$(echo "world")")" is just one stringParsingState(). But this string contains
    two levels of embedded strings in the embedded subshell command.
    The lexer parses a string as STRING_BEGIN, STRING_DATA and STRING_END. These
    tokens are mapped to a STRING later on by the lexer.MergingLexer class.

    Lexing all as a STRING token was the way to go. This worked, but for some strange
    reason the lexer got wrong offsets for this complex setup (returning the string only at the
    last occurence of "). That's why the token merging strategy was established.

    If you really want to hack on this lexer: Be careful :)
    There are unit tests for the lexer but there's no 100% coverage for all cases.

     @author Joachim Ansorg, mail@ansorg-it.com
**/

/** Based on the arc lexer (http://code.google.com/p/intelli-arc/) **/

package com.github.antego.bashmock.lexer;

import com.intellij.psi.tree.IElementType;

%%

%class _BashLexerBase
%implements BashLexerDef
%abstract
%unicode
%public
%char

%function advance
%type IElementType

%{
    long yychar = 0;
%}

/***** Custom user code *****/

LineTerminator = \r\n | \r | \n
InputCharacter = [^\r\n]
WhiteSpace=[ \t\f]
ContinuedLine = "\\" {LineTerminator}

Shebang = "#!" {InputCharacter}* {LineTerminator}?
Comment = "#"  {InputCharacter}*

EscapedChar = "\\" [^\n]
StringStart = "$\"" | "\""

SingleCharacter = [^\'] | {EscapedChar}
UnescapedCharacter = [^\']

WordFirst = [a-zA-Z0-9] | "_" | "/" | "@" | "?" | "." | "*" | ":" | "&" | "%"
    | "-" | "^" | "+" | "-" | "," | "~" | "*" | "_"
    | {EscapedChar} | [\u00C0-\u00FF]
WordAfter =  {WordFirst} | "#" | "!" | "[" | "]"

ArithWordFirst = [a-zA-Z] | "_" | "@" | "?" | "." | ":" | {EscapedChar}
// No "[" | "]"
ArithWordAfter =  {ArithWordFirst} | "#" | "!" | [0-9]

ParamExpansionWordFirst = [a-zA-Z0-9_,] | {EscapedChar}
ParamExpansionWordAfter =  {ParamExpansionWordFirst}
ParamExpansionWord = {ParamExpansionWordFirst}{ParamExpansionWordAfter}*

AssignListWordFirst = [a-zA-Z0-9] | "_" | "/" | "@" | "?" | "." | "*" | ":" | "&" | "%"
    | "-" | "^" | "+" | "-" | "~" | "*" | "," | ";"
    | {EscapedChar}
AssignListWordAfter =  {AssignListWordFirst} | "$" | "#" | "!"
AssignListWord={AssignListWordFirst}{AssignListWordAfter}*

Word = {WordFirst}{WordAfter}*
ArithWord = {ArithWordFirst}{ArithWordAfter}*
AssignmentWord = [a-zA-Z_][a-zA-Z0-9_]*
Variable = "$" {AssignmentWord} | "$@" | "$$" | "$#" | "$"[0-9] | "$?" | "$!" | "$*" | "$-" | "$_"

ArithExpr = ({ArithWord} | [0-9a-z+*-] | {Variable} )+

IntegerLiteral = [0] | ([1-9][0-9]*)
HexIntegerLiteral = "0x" [0-9a-fA-F]+
OctalIntegerLiteral = "0" [0-7]+

CaseFirst={EscapedChar} | [^|\"'$)(# \n\r\f\t\f]
CaseAfter={EscapedChar} | [^|\"'$`)( \n\r\f\t\f;]
CasePattern = {CaseFirst}{CaseAfter}*

Filedescriptor = "&" {IntegerLiteral} | "&-"

/************* STATES ************/
/* If in a conditional expression */
%state S_TEST

/* If in a conditional command  [[  ]]*/
%state S_TEST_COMMAND

/*  If in an arithmetic expression */
%state S_ARITH

/*  If in an arithmetic expression */
%state S_ARITH_SQUARE_MODE

/*  If in an arithmetic expression in an array reference */
%state S_ARITH_ARRAY_MODE

/*  If in a case */
%state S_CASE

/*  If in a case pattern */
%state S_CASE_PATTERN

/*  If in a subshell */
%state S_SUBSHELL

/*  If in the start of a subshell pre expression, i.e. after DOLLAR of $( . The same rules apply as for S_SUBSHELL except that the first ( expression does not open up a new subshell expression
    This is done by switching into the S_SUBSHELL state right after the first LEFT_PAREN token encountered.
*/
%state S_DOLLAR_PREFIXED

/*  If in an array reference, e.g. a[0]=x */
%state S_ARRAY

/*  If in an array list init, e.g. a=(first second) */
%state S_ASSIGNMENT_LIST

/*  If currently a string is parsed */
%xstate S_STRINGMODE

/*  To match tokens in pattern expansion mode ${...} . Needs special parsing of # */
%state S_PARAM_EXPANSION

/* To match tokens which are in between backquotes. Necessary for nested lexing, e.g. inside of conditional expressions */
%state S_BACKQUOTE

/* To match heredoc documents */
%xstate S_HEREDOC_MARKER
%xstate S_HEREDOC_MARKER_IGNORE_TABS
%xstate S_HEREDOC

%%
/***************************** INITIAL STAATE ************************************/
<YYINITIAL, S_CASE, S_CASE_PATTERN, S_SUBSHELL, S_ASSIGNMENT_LIST> {
  {Shebang}                     { return SHEBANG; }
  {Comment}                     { return COMMENT; }
}

<S_HEREDOC_MARKER, S_HEREDOC_MARKER_IGNORE_TABS> {
    {WhiteSpace}+                { return WHITESPACE; }
    {ContinuedLine}+             { /* ignored */ }
    {LineTerminator}             { return LINE_FEED; }

      ("$"? "'" [^\']+ "'")+
    | ("$"? \" [^\"]+ \")+
    | [^ \t\n\r\f;&|]+ {
        heredocState().pushMarker(yytext(), yystate() == S_HEREDOC_MARKER_IGNORE_TABS);
        backToPreviousState();

        return HEREDOC_MARKER_START;
    }

    .                            { return BAD_CHARACTER; }
}

<S_HEREDOC> {
    {LineTerminator}+           { if (!heredocState().isEmpty()) {
                                        return HEREDOC_LINE;
                                  }
                                  return LINE_FEED;
                                }

    //escaped dollar
    \\ "$" ?                    { return HEREDOC_LINE; }

    {Variable} {
            if (heredocState().isNextMarker(yytext())) {
                boolean ignoreTabs = heredocState().isIgnoringTabs();

                heredocState().popMarker(yytext());
                popStates(S_HEREDOC);

                return ignoreTabs ? HEREDOC_MARKER_IGNORING_TABS_END : HEREDOC_MARKER_END;
            }

            return yystate() == S_HEREDOC && heredocState().isExpectingEvaluatingHeredoc() && !"$".equals(yytext().toString())
                ? VARIABLE
                : HEREDOC_LINE;
    }

    [^$\n\r\\]+  {
            if (heredocState().isNextMarker(yytext())) {
                boolean ignoreTabs = heredocState().isIgnoringTabs();

                heredocState().popMarker(yytext());
                popStates(S_HEREDOC);

                return ignoreTabs ? HEREDOC_MARKER_IGNORING_TABS_END : HEREDOC_MARKER_END;
            }

            return HEREDOC_LINE;
    }

    "$"  {
            if (heredocState().isNextMarker(yytext())) {
                boolean ignoreTabs = heredocState().isIgnoringTabs();

                heredocState().popMarker(yytext());
                popStates(S_HEREDOC);

                return ignoreTabs ? HEREDOC_MARKER_IGNORING_TABS_END : HEREDOC_MARKER_END;
         }

         return HEREDOC_LINE;
     }

    .                            { return BAD_CHARACTER; }
}

<YYINITIAL, S_CASE, S_SUBSHELL, S_BACKQUOTE> {
  "[ ]"                         { yypushback(1); goToState(S_TEST); setEmptyConditionalCommand(true); return EXPR_CONDITIONAL; }
  "[ "                          { goToState(S_TEST); setEmptyConditionalCommand(false); return EXPR_CONDITIONAL; }

  "time"                        { return TIME_KEYWORD; }

   <S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE> {
       "&&"                         { return AND_AND; }

       "||"                         { return OR_OR; }
   }
}

<S_ARRAY> {
    "["     { backToPreviousState(); goToState(S_ARITH_ARRAY_MODE); return LEFT_SQUARE; }
}

<S_ARITH_ARRAY_MODE> {
    "]" / "=("|"+=("        { backToPreviousState(); goToState(S_ASSIGNMENT_LIST); return RIGHT_SQUARE; }
    "]"                     { backToPreviousState(); return RIGHT_SQUARE; }
}

// Parenthesis lexing
<S_STRINGMODE, S_HEREDOC, S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, S_CASE> {
    "$" / "("               { if (yystate() == S_HEREDOC && !heredocState().isExpectingEvaluatingHeredoc()) return HEREDOC_LINE; goToState(S_DOLLAR_PREFIXED); return DOLLAR; }
    "$" / "["               { if (yystate() == S_HEREDOC && !heredocState().isExpectingEvaluatingHeredoc()) return HEREDOC_LINE; goToState(S_DOLLAR_PREFIXED); return DOLLAR; }
}

<YYINITIAL, S_BACKQUOTE, S_DOLLAR_PREFIXED, S_TEST, S_CASE> {
    //this is not lexed in state S_SUBSHELL, because BashSupport treats ((((x)))) as subshell>arithmetic and not as subshell>subshell>arithmetic
    //this is different to the official Bash interpreter
    //currently it's too much effort to rewrite the lexer and parser for this feature
    <S_PARAM_EXPANSION> {
        "((("                   { if (yystate() == S_DOLLAR_PREFIXED) backToPreviousState(); yypushback(2); goToState(S_SUBSHELL); return LEFT_PAREN; }
    }

    <S_SUBSHELL, S_PARAM_EXPANSION> {
        "(("                { if (yystate() == S_DOLLAR_PREFIXED) backToPreviousState(); goToState(S_ARITH); return EXPR_ARITH; }
        "("                 { if (yystate() == S_DOLLAR_PREFIXED) backToPreviousState(); stringParsingState().enterSubshell(); goToState(S_SUBSHELL); return LEFT_PAREN; }
    }

    <S_SUBSHELL> {
        "["                 { if (yystate() == S_DOLLAR_PREFIXED) backToPreviousState(); goToState(S_ARITH_SQUARE_MODE); return EXPR_ARITH_SQUARE; }
    }
}

<YYINITIAL, S_CASE> {
    ")"                     { return RIGHT_PAREN; }
}
<S_SUBSHELL> {
    ")"                     { backToPreviousState(); if (stringParsingState().isInSubshell()) stringParsingState().leaveSubshell(); return RIGHT_PAREN; }
}
<S_CASE_PATTERN> {
    "("                     { return LEFT_PAREN; }
    ")"                     { backToPreviousState(); return RIGHT_PAREN; }
}


<S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE> {
  "))"                          { if (openParenthesisCount() > 0) {
                                    decOpenParenthesisCount();
                                    yypushback(1);

                                    return RIGHT_PAREN;
                                  } else {
                                    backToPreviousState();

                                    return _EXPR_ARITH;
                                  }
                                }

  "("                           { incOpenParenthesisCount(); return LEFT_PAREN; }
  ")"                           { decOpenParenthesisCount(); return RIGHT_PAREN; }
}


<YYINITIAL, S_ARITH, S_ARITH_SQUARE_MODE, S_CASE, S_SUBSHELL, S_BACKQUOTE> {
   /* The long followed-by match is necessary to have at least the same length as to global Word rule to make sure this rules matches */
   {AssignmentWord} / "[" {ArithExpr} "]"
                                      { goToState(S_ARRAY); return ASSIGNMENT_WORD; }

   {AssignmentWord} / "=("|"+=("      { goToState(S_ASSIGNMENT_LIST); return ASSIGNMENT_WORD; }
   {AssignmentWord} / "="|"+="        { return ASSIGNMENT_WORD; }
}

<YYINITIAL, S_CASE, S_SUBSHELL, S_BACKQUOTE> {
    <S_ARITH, S_ARITH_SQUARE_MODE> {
       "="                                { return EQ; }
   }

   "+="                               { return ADD_EQ; }
}

<S_ASSIGNMENT_LIST> {
  "("                             { return LEFT_PAREN; }
  ")"                             { backToPreviousState(); return RIGHT_PAREN; }
  "+="                            { return ADD_EQ; }
  "="                             { return EQ; }

 "["                              { goToState(S_ARITH_ARRAY_MODE); return LEFT_SQUARE; }
  {AssignListWord}                { return WORD; }
}

<S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE> {
  ","                             { return COMMA; }
}

<YYINITIAL, S_SUBSHELL, S_BACKQUOTE> {
  "in"                          { return IN_KEYWORD; }
}

<YYINITIAL, S_CASE, S_SUBSHELL, S_BACKQUOTE> {
/* keywords and expressions */
  "case"                        { setInCaseBody(false); goToState(S_CASE); return CASE_KEYWORD; }

  "!"                           { return BANG_TOKEN; }
  "do"                          { return DO_KEYWORD; }
  "done"                        { return DONE_KEYWORD; }
  "elif"                        { return ELIF_KEYWORD; }
  "else"                        { return ELSE_KEYWORD; }
  "fi"                          { return FI_KEYWORD; }
  "for"                         { return FOR_KEYWORD; }
  "function"                    { return FUNCTION_KEYWORD; }
  "if"                          { return IF_KEYWORD; }
  "select"                      { return SELECT_KEYWORD; }
  "then"                        { return THEN_KEYWORD; }
  "until"                       { return UNTIL_KEYWORD; }
  "while"                       { return WHILE_KEYWORD; }
  "[[ "                         { goToState(S_TEST_COMMAND); return BRACKET_KEYWORD; }
  "trap"                        { return TRAP_KEYWORD; }
  "let"                         { return LET_KEYWORD; }
}
/***************** _______ END OF INITIAL STATE _______ **************************/

<S_TEST_COMMAND> {
  " ]]"                         { backToPreviousState(); return _BRACKET_KEYWORD; }
  "&&"                          { return AND_AND; }
  "||"                          { return OR_OR; }
  "("                           { return LEFT_PAREN; }
  ")"                           { return RIGHT_PAREN; }
}

<S_TEST> {
  "]"                          { if (isEmptyConditionalCommand()) {
                                    setEmptyConditionalCommand(false);
                                    backToPreviousState();
                                    return _EXPR_CONDITIONAL;
                                 } else {
                                    setEmptyConditionalCommand(false);
                                    return WORD;
                                 }
                               }
  " ]"                         { backToPreviousState(); setEmptyConditionalCommand(false); return _EXPR_CONDITIONAL; }
}

<S_TEST, S_TEST_COMMAND> {
  {WhiteSpace}                 { return WHITESPACE; }
  {ContinuedLine}+             { /* ignored */ }

  /*** Test / conditional expressions ***/

  /* param expansion operators */
  "=="                         { return COND_OP_EQ_EQ; }

  /* regex operator */
  "=~"                         { return COND_OP_REGEX; }

  /* misc */
  "!"                          { return COND_OP_NOT; }
  "-a"                         |
  "-o"                         |
  "-eq"                        |
  "-ne"                        |
  "-lt"                        |
  "-le"                        |
  "-gt"                        |
  "-ge"                        |

  /* string operators */
  "!="                         |
  ">"                          |
  "<"                          |
  "="                          |
  "-n"                         |
  "-z"                         |

  /* conditional operators */
  "-nt"                        |
  "-ot"                        |
  "-ef"                        |
  "-n"                         |
  "-o"                         |
  "-qq"                        |
  "-a"                         |
  "-b"                         |
  "-c"                         |
  "-d"                         |
  "-e"                         |
  "-f"                         |
  "-g"                         |
  "-h"                         |
  "-k"                         |
  "-p"                         |
  "-r"                         |
  "-s"                         |
  "-t"                         |
  "-u"                         |
  "-w"                         |
  "-x"                         |
  "-O"                         |
  "-G"                         |
  "-L"                         |
  "-S"                         |
  "-N"                         { return COND_OP; }
}

/*** Arithmetic expressions *************/
<S_ARITH> {
    "["                           { return LEFT_SQUARE; }
    "]"                           { return RIGHT_SQUARE; }
}

<S_ARITH_SQUARE_MODE> {
  "["                           { return EXPR_ARITH_SQUARE; }

  "]"                           { backToPreviousState(); return _EXPR_ARITH_SQUARE; }
}

<S_ARITH_ARRAY_MODE> {
  "]"                           { backToPreviousState(); return RIGHT_SQUARE; }
}

<S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE> {
  {HexIntegerLiteral}           { return ARITH_HEX_NUMBER; }
  {OctalIntegerLiteral}         { return ARITH_OCTAL_NUMBER; }
  {IntegerLiteral}              { return ARITH_NUMBER; }

  ">"                           { return ARITH_GT; }
  "<"                           { return ARITH_LT; }
  ">="                          { return ARITH_GE; }
  "<="                          { return ARITH_LE; }
  "!="                          { return ARITH_NE; }

  "<<"                          { return ARITH_SHIFT_LEFT; }
  ">>"                          { return ARITH_SHIFT_RIGHT; }

  "*="                          { return ARITH_ASS_MUL; }
  "/="                          { return ARITH_ASS_DIV; }
  "%="                          { return ARITH_ASS_MOD; }
  "+="                          { return ARITH_ASS_PLUS; }
  "-="                          { return ARITH_ASS_MINUS; }
  ">>="                         { return ARITH_ASS_SHIFT_RIGHT; }
  "<<="                         { return ARITH_ASS_SHIFT_LEFT; }

  "+"                           { return ARITH_PLUS; }
  "++"                          { return ARITH_PLUS_PLUS; }
  "-"                           { return ARITH_MINUS; }

  "--"/"-"
                                { yypushback(1); return ARITH_MINUS; }

  "--"/{WhiteSpace}+"-"
                                { yypushback(1); return ARITH_MINUS; }

  "--"/({HexIntegerLiteral}|{OctalIntegerLiteral}|{IntegerLiteral})
                                { yypushback(1); return ARITH_MINUS; }

  "--"/{WhiteSpace}+({HexIntegerLiteral}|{OctalIntegerLiteral}|{IntegerLiteral})
                                { yypushback(1); return ARITH_MINUS; }

  "--"                          { return ARITH_MINUS_MINUS; }
  "=="                          { return ARITH_EQ; }

  "**"                          { return ARITH_EXPONENT; }
  "*"                           { return ARITH_MULT; }
  "/"                           { return ARITH_DIV; }
  "%"                           { return ARITH_MOD; }
  "<<"                          { return ARITH_SHIFT_LEFT; }

  "!"                           { return ARITH_NEGATE; }

  "&"                           { return ARITH_BITWISE_AND; }
  "~"                           { return ARITH_BITWISE_NEGATE; }
  "^"                           { return ARITH_BITWISE_XOR; }

  "?"                           { return ARITH_QMARK; }
  ":"                           { return ARITH_COLON; }

  "#"                           { return ARITH_BASE_CHAR; }

  {AssignmentWord} / "["
                                { goToState(S_ARRAY); return ASSIGNMENT_WORD; }

  {ArithWord}                   { return WORD; }
}

<S_CASE> {
  "esac"                       { backToPreviousState(); return ESAC_KEYWORD; }

  ";&"                         { goToState(S_CASE_PATTERN);
                                 if (isBash4()) {
                                    return CASE_END;
                                 }
                                 else {
                                    yypushback(1);
                                    return SEMI;
                                 }
                               }

  ";;&"                        { goToState(S_CASE_PATTERN);
                                 if (!isBash4()) {
                                    yypushback(1);
                                 }
                                 return CASE_END;
                               }

  ";;"                         { goToState(S_CASE_PATTERN); return CASE_END; }
  "in"                         { if (!isInCaseBody()) { setInCaseBody(true); goToState(S_CASE_PATTERN); }; return IN_KEYWORD; }
}

<S_CASE_PATTERN> {
  "esac"                        { backToPreviousState(); yypushback(yylength()); }
}

//////////////////// END OF STATE TEST_EXPR /////////////////////

/* string literals */
<S_STRINGMODE> {
  \"                            { if (!stringParsingState().isInSubstring() && stringParsingState().isSubstringAllowed()) {
                                    stringParsingState().enterString();
                                    goToState(S_STRINGMODE);
                                    return STRING_BEGIN;
                                  }

                                  stringParsingState().leaveString();
                                  backToPreviousState();
                                  return STRING_END;
                                }

  /* Backquote expression inside of evaluated strings */
  `                           { if (yystate() == S_BACKQUOTE) {
                                    backToPreviousState();
                                }
                                else {
                                    goToState(S_BACKQUOTE);
                                }
                                return BACKQUOTE; }

  {EscapedChar}               { return STRING_DATA; }
  [^\"]                       { return STRING_DATA; }
}

<YYINITIAL, S_BACKQUOTE, S_SUBSHELL, S_CASE> {
  /* Bash 4 */
    "&>>"                         { if (isBash4()) {
                                        return REDIRECT_AMP_GREATER_GREATER;
                                    } else {
                                        yypushback(2);
                                        return AMP;
                                    }
                                  }

    "&>"                          { if (isBash4()) {
                                        return REDIRECT_AMP_GREATER;
                                    } else {
                                        yypushback(1);
                                        return AMP;
                                    }
                                  }

  /* Bash v3 */
  "<<<"                         { return REDIRECT_LESS_LESS_LESS; }
  "<>"                          { return REDIRECT_LESS_GREATER; }

  "<&" / {ArithWord}            { return REDIRECT_LESS_AMP; }
  ">&" / {ArithWord}            { return REDIRECT_GREATER_AMP; }
  "<&" / {WhiteSpace}           { return REDIRECT_LESS_AMP; }
  ">&" / {WhiteSpace}           { return REDIRECT_GREATER_AMP; }

  ">|"                          { return REDIRECT_GREATER_BAR; }

  {Filedescriptor}              { return FILEDESCRIPTOR; }
}

<S_PARAM_EXPANSION> {
  "!"                           { return PARAM_EXPANSION_OP_EXCL; }
  ":="                          { return PARAM_EXPANSION_OP_COLON_EQ; }
  "="                           { return PARAM_EXPANSION_OP_EQ; }

  ":-"                          { return PARAM_EXPANSION_OP_COLON_MINUS; }
  "-"                           { return PARAM_EXPANSION_OP_MINUS; }

  ":+"                          { return PARAM_EXPANSION_OP_COLON_PLUS; }
  "+"                           { return PARAM_EXPANSION_OP_PLUS; }

  ":?"                          { return PARAM_EXPANSION_OP_COLON_QMARK; }

  ":"                           { return PARAM_EXPANSION_OP_COLON; }

  "#"                           { setParamExpansionHash(isParamExpansionWord() && true); return PARAM_EXPANSION_OP_HASH; }
  "@"                           { return PARAM_EXPANSION_OP_AT; }
  "*"                           { return PARAM_EXPANSION_OP_STAR; }
  "%"                           { setParamExpansionOther(true); return PARAM_EXPANSION_OP_PERCENT; }
  "?"                           { setParamExpansionOther(true); return PARAM_EXPANSION_OP_QMARK; }
  "."                           { setParamExpansionOther(true); return PARAM_EXPANSION_OP_DOT; }
  "/"                           { setParamExpansionOther(true); return PARAM_EXPANSION_OP_SLASH; }
  "^"                           { setParamExpansionOther(true); return PARAM_EXPANSION_OP_UNKNOWN; }

  "[" / [@*]                    { return LEFT_SQUARE; }
  "["                           { if (!isParamExpansionOther() && (!isParamExpansionWord() || !isParamExpansionHash())) {
                                    // If we expect an array reference parse the next tokens as arithmetic expression
                                    goToState(S_ARITH_ARRAY_MODE);
                                  }

                                  return LEFT_SQUARE;
                                }

  "]"                           { return RIGHT_SQUARE; }

  "{"                           { setParamExpansionWord(false); setParamExpansionHash(false); setParamExpansionOther(false);
                                  return LEFT_CURLY;
                                }
  "}"                           { setParamExpansionWord(false); setParamExpansionHash(false); setParamExpansionOther(false);
                                  backToPreviousState();
                                  return RIGHT_CURLY;
                                }

  {EscapedChar}                 { setParamExpansionWord(true); return WORD; }
  {IntegerLiteral}              { setParamExpansionWord(true); return WORD; }
  {ParamExpansionWord}          { setParamExpansionWord(true); return WORD; }
 }


/** Match in all except of string */
<YYINITIAL, S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, S_CASE, S_CASE_PATTERN, S_SUBSHELL, S_ASSIGNMENT_LIST, S_PARAM_EXPANSION, S_BACKQUOTE, S_STRINGMODE> {
    /*
     Do NOT match for Whitespace+ , we have some whitespace sensitive tokens like " ]]" which won't match
     if we match repeated whtiespace!
    */
    {WhiteSpace}                 { return WHITESPACE; }
    {ContinuedLine}+             { /* ignored */ }
}

<YYINITIAL, S_TEST, S_TEST_COMMAND, S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, S_CASE, S_CASE_PATTERN, S_SUBSHELL, S_ASSIGNMENT_LIST, S_PARAM_EXPANSION, S_BACKQUOTE> {
    {StringStart}                 { stringParsingState().enterString(); goToState(S_STRINGMODE); return STRING_BEGIN; }

    "$"\'{SingleCharacter}*\'     |
    \'{UnescapedCharacter}*\'        { return STRING2; }

    /* Single line feeds are required to properly parse heredocs*/
    {LineTerminator}             {
                                        if ((yystate() == S_PARAM_EXPANSION || yystate() == S_SUBSHELL || yystate() == S_ARITH || yystate() == S_ARITH_SQUARE_MODE) && isInState(S_HEREDOC)) {
                                            backToPreviousState();
                                            return LINE_FEED;
                                        }

                                        if (!heredocState().isEmpty()) {
                                            // first linebreak after the start marker
                                            goToState(S_HEREDOC);
                                            return LINE_FEED;
                                        }

                                       return LINE_FEED;
                                 }

    /* Backquote expression */
    `                             { if (yystate() == S_BACKQUOTE) backToPreviousState(); else goToState(S_BACKQUOTE); return BACKQUOTE; }


  /* Bash reserved keywords */
    "{"                           { return LEFT_CURLY; }

    "|&"                          { if (isBash4()) {
                                        return PIPE_AMP;
                                     } else {
                                        yypushback(1);
                                        return PIPE;
                                     }
                                  }
    "|"                           { return PIPE; }

  /** Misc expressions */
    "&"                           { return AMP; }
    "@"                           { return AT; }
    "$"                           { return DOLLAR; }
    ";"                           { return SEMI; }
    "<<-" {
        goToState(S_HEREDOC_MARKER_IGNORE_TABS);
        return HEREDOC_MARKER_TAG;
    }
    "<<" {
        goToState(S_HEREDOC_MARKER);
        return HEREDOC_MARKER_TAG;
    }
    ">"                           { return GREATER_THAN; }
    "<"                           { return LESS_THAN; }
    ">>"                          { return SHIFT_RIGHT; }

    <S_STRINGMODE>{
        {Variable}                 { return VARIABLE; }
    }

    "$["                          { yypushback(1); goToState(S_ARITH_SQUARE_MODE); return DOLLAR; }

    "\\"                          { return BACKSLASH; }
}

<YYINITIAL, S_HEREDOC, S_PARAM_EXPANSION, S_TEST, S_TEST_COMMAND, S_CASE, S_CASE_PATTERN, S_SUBSHELL, S_ARITH, S_ARITH_SQUARE_MODE, S_ARITH_ARRAY_MODE, S_ARRAY, S_ASSIGNMENT_LIST, S_BACKQUOTE, S_STRINGMODE> {
    "${"                        { if (yystate() == S_HEREDOC && !heredocState().isExpectingEvaluatingHeredoc()) return HEREDOC_LINE; goToState(S_PARAM_EXPANSION); yypushback(1); return DOLLAR; }
    "}"                         { if (yystate() == S_HEREDOC && !heredocState().isExpectingEvaluatingHeredoc()) return HEREDOC_LINE; return RIGHT_CURLY; }
}

<S_CASE_PATTERN> {
  {CasePattern}                 { return WORD; }
}

<YYINITIAL, S_CASE, S_SUBSHELL, S_BACKQUOTE, S_ARRAY> {
    {IntegerLiteral}            { return INTEGER_LITERAL; }
}

<YYINITIAL, S_CASE, S_TEST, S_TEST_COMMAND, S_SUBSHELL, S_BACKQUOTE> {
  {Word}                       { return WORD; }
  {WordAfter}+                 { return WORD; }
}

/** END */
  .                            { return BAD_CHARACTER; }
