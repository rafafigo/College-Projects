%{
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <limits.h>
#include "node.h"
#include "y.tab.h"

int yyerror(char *);
char *dupstr(const char *);
int createInteger(char *, int);
char *getyytext();
static char *str;
static int strsz;
%}

WHITE_SPACE [\ \t\n\r]
%x PRGR STR
%%
^"program"                              BEGIN PRGR; return PROGRAM;
^"module"                               BEGIN PRGR; return MODULE;
.|\n                                    ;

<PRGR>{WHITE_SPACE}                  ;

<PRGR>"$$".*                          ;
<PRGR>"$"[^$]*"$"                     ;

<PRGR>0|([1-9][0-9]*)                yylval.i = createInteger(yytext, 10);    return INTEGER;
<PRGR>0[0-7]+                        yylval.i = createInteger(yytext, 8);     return INTEGER;
<PRGR>"0x"([a-fA-F0-9])+             yylval.i = createInteger(yytext, 16);    return INTEGER;
<PRGR>"0b"[01]+                      yylval.i = createInteger(&yytext[2], 2); return INTEGER;

<PRGR>\'\\t\'                         yylval.crct ='\t';                           return CHAR;
<PRGR>\'\\r\'                         yylval.crct = '\r';                          return CHAR;
<PRGR>\'\\n\'                         yylval.crct = '\n';                          return CHAR;
<PRGR>\'(\\\'|\\\\)\'                yylval.crct = yytext[yyleng-2];              return CHAR;
<PRGR>\'\\[a-fA-F0-9][a-fA-F0-9]?\'  yylval.crct = createInteger(&yytext[2], 16); return CHAR;
<PRGR>\'(.)\'                        yylval.crct = yytext[yyleng-2];              return CHAR;
<PRGR>\"([^"\\]|\\.)*\"           	 strsz = yyleng; REJECT;
<PRGR>\"                             if (strsz > 0) { BEGIN STR; str = malloc(strsz); }
<STR>\"                              yylval.s = str; BEGIN PRGR; return CHARS;
<STR>\\n		                         strcat(str, "\n");
<STR>\\t		                         strcat(str, "\t");
<STR>\\r		                         strcat(str, "\r");
<STR>\\\"		                         strcat(str, "\"");
<STR>\\\\		                         strcat(str, "\\");
<STR>\\[a-fA-F0-9][a-fA-F0-9]?	     char s[2]; s[1] = 0; s[0] = createInteger(&yytext[2], 16); strcat(str, s);
<STR>.|\n		                     strcat(str, yytext);
<PRGR>^"program"                     return PROGRAM;
<PRGR>^"module"                      return MODULE;
<PRGR>"start"                        return START;
<PRGR>"void"                         return VOID;
<PRGR>"const"                        return CONST;
<PRGR>"number"                       return NUMBER;
<PRGR>"array"                        return ARRAY;
<PRGR>"string"                       return STRING;
<PRGR>"function"                     return FUNCTION;
<PRGR>"public"                       return PUBLIC;
<PRGR>"forward"                       return FORWARD;
<PRGR>"if"                           return IF;
<PRGR>"then"                         return THEN;
<PRGR>"else"                         return ELSE;
<PRGR>"elif"                         return ELIF;
<PRGR>"fi"                           return FI;
<PRGR>"for"                          return FOR;
<PRGR>"until"                        return UNTIL;
<PRGR>"step"                         return STEP;
<PRGR>"do"                           return DO;
<PRGR>"done"                         return DONE;
<PRGR>"repeat"                       return REPEAT;
<PRGR>"stop"                         return STOP;
<PRGR>"return"                       return RETURN;
<PRGR>:=                             return ASSIGN;
<PRGR>>=                             return GE;
<PRGR><=                             return LE;
<PRGR>~=                             return NE;
<PRGR>^"end"                         BEGIN INITIAL; return END;

<PRGR>[-+*/%^?<>=|&~#[\]()!;:,]      return *yytext;
<PRGR>[a-zA-Z][a-zA-Z0-9_]*          yylval.s = dupstr(yytext); return IDENT;
<PRGR>.                              yyerror("Unexpected Token");

%%

int yywrap() {
    if (YYSTATE != INITIAL) yyerror("Invalid place to end");
    return 1;
}

char *getyytext() { return yytext; }

int createInteger(char *number, int base) {

    long decimalNumber = strtol(number, 0, base);

    if (errno == ERANGE || decimalNumber > INT_MAX) { yyerror("Overflow");}
    return decimalNumber;
}