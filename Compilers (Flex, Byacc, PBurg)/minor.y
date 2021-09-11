%{
  #include <stdio.h>
  #include <stdlib.h>
  #include "node.h"
  #include "tabid.h"
  #include "minor.h"

  #define NUMBER_V 1
  #define ARRAY_V 2
  #define STRING_V 3
  #define VOID_V 4
  #define CONST_V 5
  #define PUBLIC_V 10
  #define NOT_PUBLIC_V 0
  #define FORWARD_V 20
  #define FUNCTION_V 30

  int yyparse();
  int yyerror(char *s);
  extern int yylex();
  extern void evaluate(Node *p, int i);
  extern void variable(int, int, Node *);

  
  void function(int, int, char* , int, Node *);
  int equalTypes(int, int), removeTypeDependencies(int);
  void verifyFoward(int, Node*);
  int pos = 0;
  int nCycles = 0;
  int nConditions = 0;
  int primaRetfunc = 0;

%}

%union {
	int i;
	char crct;
  char *s;
  Node *n;
}

%token <s> IDENT CHARS
%token <i> INTEGER
%token <crct> CHAR

%token PROGRAM MODULE START END PUBLIC FORWARD CNST ASSIG
%token DECLRS DECLR VAR_DECLR FUNCVAL
%token FOR FORCONDITION UNTIL FORBODY STEP
%token FUNCTION FUNCARG FUNCATR FUNCNME
%token VOID CONST NUMBER ARRAY STRING
%token VARIABLES VARIABLE DIM ASSIGN
%token BODY DO DONE INSTRS INSTR
%token LEFT NOT PRIORITY MMRY LEFTEXP LOCAL ADDR
%token IF IFCONDITION THEN
%token RETURN REPEAT STOP
%token ELS ELSE ELIF FI
%token CALL ARGS ARG
%token GE LE NE END
%token NIL ERROR

%right ASSIGN
%left '|'
%left '&'
%nonassoc '~'
%left NE '='
%left '<' '>' GE LE
%left '+' '-'
%left '*' '/' '%'
%right '^'
%nonassoc UADDR UMINUS '?'
%nonassoc '(' ')' '[' ']'

%type<n> instructions instructionsBody  instructionsOpt instruction forCondition forBody instructionFinal
%type<n> program module declarationOpt declarations declaration function body bodyFunc qualifier
%type<n> stringVar numbVar arrayVar varAssign literalsArray literalsCad literal optCad
%type<n> variableFunc variableBody variablesFunc variable
%type<n> expression leftValue
%type<n> if elifs else args
%type<n> const

%type<i> ret
%%

file  : {IDpush(); } program 	{ evaluate($2, -pos); freeNode($2); IDpop();}
      | {IDpush(); } module    { evaluate($2, 0); freeNode($2); IDpop();}
      ;

program : PROGRAM declarationOpt START { IDpush(); pos = 0; } body END  { IDpop(); $$ = binNode(PROGRAM, $2, $5);  }
        ;

module  : MODULE declarationOpt END { $$ = uniNode(MODULE, $2);}
        ;

declarationOpt  : 							{ $$ = nilNode(NIL); }
                | declarations 	{ $$ = $1; }
                ;

declarations  : declaration											{ $$ = binNode(DECLRS, nilNode(NIL), $1); }
              | declarations ';' declaration 		{ $$ = binNode(DECLRS, $1, $3); }
              | declarations ';' error          { $$ = binNode(DECLRS, nilNode(ERROR), $1);}
              | error                           { $$ = nilNode(ERROR);}
              ;

declaration : function                    { $$ = $1;}
						| qualifier const varAssign		{ wasfowarded($1->info + $3->info + $2->info, LEFT_CHILD($3)->value.s); 
                                            $$ = binNode(DECLR, $1, binNode(CNST, $2, uniNode(VAR_DECLR, $3))); 
                                            variable($1->info, $2->info, $3); }
            ;

function  : FUNCTION qualifier ret IDENT { wasfowarded(FUNCTION_V + $3 + $2->info , $4); primaRetfunc = $3; IDpush(); } variableFunc {changeFunctionNode($4, $6); pos = 0;} bodyFunc
                                         { IDpop(); $$ = binNode(FUNCTION, $2, binNode(FUNCVAL, binNode(FUNCATR ,strNode(FUNCNME, $4), uniNode(FUNCARG, $6)), $8)); primaRetfunc = 0;
                                           verifyFoward($2->info, $8); $$->info = $3; if($8->info == 0 && $2->info != FORWARD_V) { yyerror("Not Declared function must be foward");} function($2->info, $3, $4, -pos, $8); }
					;

ret : NUMBER { $$ = NUMBER_V; }
		| STRING { $$ = STRING_V; }
		| ARRAY  { $$ = ARRAY_V; }
    | VOID   { $$ = VOID_V; }
    ;

const :        { $$ = nilNode(NIL); $$->info = 0; }
      | CONST  { $$ = nilNode(CONST); $$->info = CONST_V; }

qualifier :						{ $$ = nilNode(NIL); $$->info = NOT_PUBLIC_V; }
          | PUBLIC		{ $$ = nilNode(PUBLIC); $$->info = PUBLIC_V; }
          | FORWARD   { $$ = nilNode(FORWARD); $$->info = FORWARD_V; }
          ;

variableFunc : 											{ $$ = nilNode(NIL); }
             | { pos = 8; } variablesFunc        { $$ = $2; }
             ;

variablesFunc :	variable			    			   { int *posPtr = (int *)malloc(sizeof(int)); *posPtr = pos; IDnew($1->info , $1->value.s, posPtr); $$ = binNode(VARIABLES, $1, nilNode(NIL) ); pos +=4; }
              | variablesFunc ';' variable { int *posPtr = (int *)malloc(sizeof(int)); *posPtr = pos; IDnew($3->info , $3->value.s, posPtr); $$ = binNode(VARIABLES, $3, $1); pos +=4; }
              ;

bodyFunc  : DONE      { $$ = nilNode(NIL); $$->info = 0; }
          | DO body   { $$ = uniNode(DO, $2); $$->info = 1; }
          ;

body  : variableBody instructions { $$ = binNode(BODY, $1, $2); }
      ;


variableBody : 													 { $$ = nilNode(NIL); pos = 0; }
             | variableBody { pos -=4; } variable ';' { int *posPtr = (int *)malloc(sizeof(int)); *posPtr = pos; if(IDsearch($3->value.s, 0, 0, 1)) { IDnew($3->info , $3->value.s, posPtr); } $$ = binNode(VARIABLES, $1, $3); }
             | variableBody error ';'    { $$ = binNode(VARIABLES, $1, nilNode(ERROR)); }
             ;

variable  : arrayVar 		{ $$ = $1; }
          | stringVar		{ $$ = $1; }
          | numbVar   	{ $$ = $1; }
          ;

arrayVar : ARRAY IDENT '[' INTEGER ']'  { $$ = strNode(IDENT, $2); $$->line = $4; $$->info = ARRAY_V; if($4 == 0) { yyerror("Invalid array size"); } $$->place = pos; }
				 | ARRAY IDENT  						{ $$ = strNode(IDENT, $2); $$->line = -1; $$->info = ARRAY_V;}
				 ;

stringVar	: STRING IDENT { $$ = strNode(IDENT, $2); $$->info = STRING_V; $$->line = -1; }
					;

numbVar : NUMBER IDENT 	{ $$ = strNode(IDENT, $2); $$->info = NUMBER_V; $$->line = -1;}
				;

varAssign  : variable													{ $$ = binNode(ASSIGN, $1, nilNode(NIL)); $$->info = $1->info; $$->line = $1->line;}
					 | arrayVar ASSIGN literalsArray    { $$ = binNode(ASSIGN, $1, $3); $$->info = $1->info; $$->line = $1->line;
                                                  if($1->line != -1 && findNumber($3) > $1->line) { yyerror("legnth given for array is smaller then the assigned list"); } }
        	 | stringVar ASSIGN literalsCad			{ $$ = binNode(ASSIGN, $1, $3); $$->info = $1->info; }
					 | numbVar ASSIGN INTEGER           { $$ = binNode(ASSIGN, $1, intNode(NUMBER, $3)); $$->info = $1->info; }
        	 ;


literalsArray : INTEGER                    { $$ = binNode(ARRAY, nilNode(NIL), intNode(NUMBER, $1)); }
              | literalsArray ',' INTEGER  { $$ = binNode(ARRAY, $1, intNode(NUMBER, $3)); }
							;

literalsCad	: CHARS                   { $$ = binNode(STRING, nilNode(NIL), strNode(CHARS, $1) ); }
			 			| literal literal optCad  { $$ = binNode(STRING, binNode(STRING, $1, $2), $3);}
						;

literal	: INTEGER   { $$ = intNode(NUMBER, $1); }
        | CHARS     { $$ = strNode(CHARS, $1); }
        | CHAR	    { $$ = intNode(CHAR, $1); }
        ;


optCad	: 							 { $$ = nilNode(NIL); }
        | optCad literal { $$ = binNode(STRING, $1, $2); }
        ;


instructions  : instructionsBody instructionFinal { $$ = binNode(INSTRS, $1, $2); }
              ;

instructionsBody  :                   { $$ = nilNode(NIL); }
                  | instructionsOpt   { $$ = $1; }
                  ;

instructionsOpt  : instruction                        { $$ = binNode(INSTR, $1, nilNode(NIL));}
                 | instructionsOpt instruction        { $$ = binNode(INSTR, $1, $2); }
                 | instructionsOpt error ';'          { $$ = binNode(INSTR, $1, nilNode(ERROR)); }
                 | instructionsOpt error '!'          { $$ = binNode(INSTR, $1, nilNode(ERROR)); }
                 | instructionsOpt error FI           { $$ = binNode(INSTR, $1, nilNode(ERROR)); }
                 | instructionsOpt error DONE         { $$ = binNode(INSTR, $1, nilNode(ERROR)); }
                 ;

instructionFinal  :                       { $$ = nilNode(NIL);}
                  | REPEAT                { $$ = nilNode(REPEAT); if(!nCycles) {yyerror("Not inside a loop condition to do repeat");} }
                  | STOP                  { $$ = nilNode(STOP); if(!nCycles) {yyerror("Not inside a loop condition to do a stop");} }
                  | RETURN                { $$ = uniNode(RETURN, nilNode(NIL)); $$->info = VOID_V; }
                  | RETURN expression     { $$ = uniNode(RETURN, $2); $$->info = $2->info; verifyReturn($2->info); }
                  ;

instruction : if elifs else FI                     { $$ = binNode(IFCONDITION, $1, binNode(ELS, $2, $3 )); }
              | forCondition forBody DONE         { $$ = binNode(FOR, $1 , $2);nCycles--;nConditions--;}
              | expression ';'                    { $$ = uniNode(';', $1); }
              | expression '!'                    { $$ = uniNode('!', $1); $$->info = $1->info; if(isVoid($1->info)) {yyerror("Nothing to print");}}
              | leftValue '#' expression ';'      { $$ = binNode(MMRY, $1, $3); if(isInt($1->info) || isFunc($1->info) || isConst($1->info) || !isInt($3->info)) { yyerror("Invalid type for this operation '#'"); }}
              ;

forCondition : FOR { nCycles++; nConditions++; } expression UNTIL expression { $$ = binNode(FORCONDITION, $3 , uniNode(UNTIL, $5)); if(!isInt($5->info)) { yyerror("Invalid Type for Condition");} }
             ;

forBody   : STEP expression DO instructions { $$ = binNode(FORBODY, uniNode(DO, $4), uniNode(STEP, $2)); }
          ;

if  : IF { nConditions++;} expression THEN instructions { $$ = binNode(IF, $3, $5);nConditions--; if(!isInt($3->info)) { yyerror("Invalid Type for Condition");} }
    ;


elifs  :                                                     { $$ = nilNode(NIL); }
       | elifs ELIF { nConditions++;} expression THEN instructions { $$ = binNode(ELIF, $1, binNode(IF, $4, $6)); nConditions--; if(!isInt($4->info)) { yyerror("Invalid Type for Condition");} }
       ;

else  :                                       { $$ = uniNode(ELSE, nilNode(NIL)); }
      | ELSE { nConditions++;} instructions   { $$ = uniNode(ELSE, $3); nConditions--;}
      ;

expression  : leftValue                     { $$ = $1; $$->info = $1->info; }
						| INTEGER											  { $$ = intNode(NUMBER, $1); $$->info = NUMBER_V; }
            | CHAR	                        { $$ = intNode(CHAR, $1); $$->info = NUMBER_V;}
            | literalsCad									  { $$ = $1; $$->info = STRING_V; }
            | '(' expression ')'            { $$ = uniNode(PRIORITY, $2); $$->info = $2->info; }
            | '~' expression                { $$ = uniNode(NOT, $2); $$->info = NUMBER_V; if(!isInt($2->info)) { yyerror("Invalid type(s) for this operation '~'"); }}
            | '?'                           { $$ = nilNode('?'); $$->info = NUMBER_V; }
            | '&' leftValue %prec UADDR     { $$ = uniNode(UADDR, $2); $$->info = ARRAY_V; if(isFunc($2->info) || !isInt($2->info)) { yyerror("This type cannot be referenced"); }}
            | '-' expression %prec UMINUS   { $$ = uniNode(UMINUS, $2); $$->info = NUMBER_V; if( $2->info != NUMBER_V) { yyerror("Invalid type for this operation '-'"); }}
            | expression '^' expression     { $$ = binNode('^', $3, $1); $$->info = NUMBER_V; if(!isInt($1->info) || !isInt($3->info)) { yyerror("Invalid type(s) for this operation '^'"); }} 
            | expression '*' expression     { $$ = binNode('*', $1, $3); $$->info = NUMBER_V; if(!isInt($1->info) || !isInt($3->info)) { yyerror("Invalid type(s) for this operation '*'"); }}
            | expression '/' expression     { $$ = binNode('/', $1, $3); $$->info = NUMBER_V; if(!isInt($1->info) || !isInt($3->info)) { yyerror("Invalid type(s) for this operation '/'"); }}
            | expression '%' expression     { $$ = binNode('%', $1, $3); $$->info = NUMBER_V; if(!isInt($1->info) || !isInt($3->info)) { yyerror("Invalid type(s) for this operation '%'"); }}
            | expression '+' expression     {  if (isInt($1->info)) { $$ = binNode('+', $3, $1); } else {$$ = binNode('+', $1, $3); } $$->info = verifyPlus($1->info, $3->info); if ($$->info == -1) { yyerror("Invalid type(s) for this operation '+'"); }}
            | expression '-' expression     { $$ = binNode('-', $1, $3); $$->info = verifyMinus($1->info, $3->info); if ($$->info == -1) { yyerror("Invalid type(s) for .this operation '-'"); }}
            | expression '<' expression     { $$ = binNode('<', $1, $3); $$->info = NUMBER_V; if(!equalTypes($1->info, $3->info) || isArray($1->info)) { yyerror("Invalid type(s) for this operation '<'"); }}
            | expression '>' expression     { $$ = binNode('>', $1, $3); $$->info = NUMBER_V; if(!equalTypes($1->info, $3->info) || isArray($1->info)) { yyerror("Invalid type(s) for this operation '>'"); }}
            | expression LE expression      { $$ = binNode(LE, $1, $3); $$->info = NUMBER_V; if(!equalTypes($1->info, $3->info) || isArray($1->info)) { yyerror("Invalid type(s) for this operation '<='"); }}
            | expression GE expression      { $$ = binNode(GE, $1, $3); $$->info = NUMBER_V; if(!equalTypes($1->info, $3->info) || isArray($1->info)) { yyerror("Invalid type(s) for this operation '>='"); }}
            | expression NE expression      { $$ = binNode(NE, $1, $3); $$->info = NUMBER_V; if(!equalTypes($1->info, $3->info) || isArray($1->info)) { yyerror("Invalid type(s) for this operation '~='"); }}
            | expression '=' expression     { $$ = binNode('=', $1, $3); $$->info = NUMBER_V; if(!equalTypes($1->info, $3->info) || isArray($1->info)) { yyerror("Invalid type(s) for this operation '='"); }}
            | expression '&' expression     { $$ = binNode('&', $1, $3); $$->info = NUMBER_V; if(!isInt($1->info) || !isInt($3->info)) { yyerror("Invalid type(s) for this operation '&'"); }}
            | expression '|' expression     { $$ = binNode('|', $1, $3); $$->info = NUMBER_V; if(!isInt($1->info) || !isInt($3->info)) { yyerror("Invalid type(s) for this operation '|'"); }}
            | leftValue ASSIGN expression   { $$ = binNode(ASSIG , $3, $1); $$->info = $1->info; verifyAssignExp($1->info, $3);}
            | IDENT '(' args ')'            { $$ = binNode(CALL,strNode(IDENT, $1), $3); $$->info = checkArgs($1, $3);}
            ;

args  : expression          { $$ = binNode(ARGS, $1, nilNode(NIL));}
      | args ',' expression { $$ = binNode(ARGS, $3, $1); }
      ;

leftValue : IDENT                        { $$ = leftType($1); }
          | leftValue '[' expression ']' { $$ = binNode(LEFTEXP, $1, $3); $$->info = NUMBER_V; if (isInt($1->info) || !isInt($3->info) ) { yyerror("Invalid type for expression, must be a number"); }  }
          ;
%%

void wasfowarded(int type, char *identifier) {
  Node ** argsP;
  if ((argsP = (Node **)malloc(sizeof(Node *))) == NULL ) { yyerror("out of memory"); exit(1);};

  int oldType = IDsearch(identifier, (void **)argsP, 0, 0);
  Node * args = *argsP;
  free(argsP);
  if (oldType == -1) { IDnew(type , identifier, 0);
  } else if(isPublic(oldType) || isNotPublic(oldType)) { yyerror("Function or Variable already Declared");
  } else if (removeTypeDependencies(oldType) == removeTypeDependencies(type)) { IDchange(type, identifier, args, 0);
  } else yyerror("Function or Variable of this type Undefined ");
}

int checkVariables(Node *node1, Node *node2) {
  while( node1->type != nodeNil && node2->type != nodeNil) {
    printNode(LEFT_CHILD(node1), 0, 0);
    printNode(LEFT_CHILD(node2), 0, 0);

    if (!equalTypes(LEFT_CHILD(node1)->info, LEFT_CHILD(node2)->info) && !(LEFT_CHILD(node2)->attrib == NUMBER && LEFT_CHILD(node2)->value.i == 0)) {
      yyerror("Function with this type of arguments is Undefined");
      return 0;
    }

    node1 = RIGHT_CHILD(node1);
    node2 = RIGHT_CHILD(node2);
  }
  if(node1->type != nodeNil || node2->type != nodeNil ){
    yyerror("Function with this number of arguments is Undefined");
    return 0;
  }
  return 1;
}

void changeFunctionNode(char * identifier, Node * Nnode) {
  Node ** argsP;
  if ((argsP = (Node **)malloc(sizeof(Node *))) == NULL ) { yyerror("out of memory"); exit(1);};

  int type = IDsearch(identifier, (void **)argsP, 1, 0);

  Node * args = *argsP;
  free(argsP);
  if(args == 0 || checkVariables(Nnode, args)) { IDchange(type, identifier, Nnode, 0);}
}


int checkArgs(char * identifier, Node * possArgs) {
    Node ** argsP;
    if ((argsP = (Node **)malloc(sizeof(Node *))) == NULL ) { yyerror("out of memory"); exit(1);};

    int type = IDfind(identifier, (void **)argsP);

    Node * args = *argsP;
    free(argsP);

    if (!isFunc(type)) {
      yyerror("Not a Function");
      return -1;
    }

    checkVariables(args, possArgs);
    return type;
}

int findNumber(Node * node) {
  int number = 0;

  while ( node->type != nodeNil) {
    number++;
    node = LEFT_CHILD(node);
  }
  return number;
}

int verifyPlus(int type1, int type2) {
  if ( (isInt(type1) && isArray(type2)) || (isArray(type1) && isInt(type2)) ) return ARRAY_V;
  if (isInt(type1) && isInt(type2)) return NUMBER_V;
  return -1;
}

int verifyMinus(int type1, int type2) {
  int variable = verifyPlus(type1, type2);
  return (variable == -1 && (isArray(type1) && isArray(type2))) ? NUMBER_V : variable;
}

void verifyAssignExp(int type1, Node * node2) {
  if (node2->type == nodeInt && node2->attrib == NUMBER && node2->value.i == 0) return;
  int type2 = node2->info;
  if(isFunc(type1)) { yyerror("Cannot do assignment for a function");
  } else if (isConst(type1)) { yyerror("Cannot do assignment for a Const Variable");
  } else if(!equalTypes(type1, type2)) yyerror("Assignment must be of same type");
}

void verifyFoward(int qualifier, Node *node) {
  if (qualifier == FORWARD_V && node->type != nodeNil) { yyerror("Variable Foward cannot be assigned"); }
}

void verifyReturn(int type) {
  if (primaRetfunc == 0 && nConditions == 0) { yyerror("Invalid Place for a Return"); }
  else if(primaRetfunc == 0 && isInt(type)) { return; }
  else if(!equalTypes(primaRetfunc, type)) yyerror("Invalid Return type");
  return;
}

int equalTypes(int type1, int type2) {
  if(isInt(type1) && isInt(type2)) return 1;
  if(isArray(type1) && isArray(type2)) return 1;
  if(isStr(type1) && isStr(type2)) return 1;
  return 0;
}

int removeTypeDependencies(int type) {
  if(isConst(type)) type -= CONST_V;
  if(isPublic(type)) type -= PUBLIC_V;
  if(isForward(type)) type -= FORWARD_V;
  if(isFunc(type)) type -= FUNCTION_V;
  return type;
}

Node *leftType(char * name) {
  Node *n;

  int **position = (int **)malloc(sizeof(int *));
  int info = IDfind(name, (void **)position); 
  int * ps = *position;
  free(position);

  if (ps) n = intNode(LOCAL, *ps);
  else n = strNode(ADDR, name);

  n->info = info;
  if (n->info == -1) yyerror("Invalid Identifier");
  if(isFunc(n->info)) { 
    checkArgs(name, nilNode(NIL)); 
    n = binNode(CALL, strNode(IDENT, name), nilNode(NIL)); 
    n->info = info;
  }
  

  return n;  
} 
char **yynames =
#if YYDEBUG > 0
	 (char**)yyname;
#else
	 0;
#endif
