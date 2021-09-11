#define NUMBER_V 1
#define ARRAY_V 2
#define STRING_V 3
#define VOID_V 4
#define CONST_V 5
#define PUBLIC_V 10
#define NOT_PUBLIC_V 0
#define FORWARD_V 20
#define FUNCTION_V 30

#define isInt(type) ( removeTypeDependencies(type) == NUMBER_V )
#define isStr(type) ( removeTypeDependencies(type) == STRING_V )
#define isArray(type) ( removeTypeDependencies(type) == ARRAY_V )
#define isVoid(type) ( removeTypeDependencies(type) == VOID_V )
#define isFunc(type) ( type >= FUNCTION_V && type < 2*FUNCTION_V )
#define isConst(type) ((type >= (CONST_V + NOT_PUBLIC_V) && type < PUBLIC_V) || (type >= (CONST_V + PUBLIC_V) && type < FORWARD_V) || (type >= (CONST_V + FORWARD_V) && type < FUNCTION_V) )
#define isPublic(type) ( (type >= PUBLIC_V  && type < FORWARD_V) || ( type >= (FUNCTION_V + PUBLIC_V) && type < (FUNCTION_V + FORWARD_V)) )
#define isNotPublic(type) ( (type >= NOT_PUBLIC_V && type < PUBLIC_V) || (type >= (FUNCTION_V + NOT_PUBLIC_V) && type < (FUNCTION_V + PUBLIC_V)) )
#define isForward(type) ( (type >= FORWARD_V && type < FUNCTION_V) || (type >= (FUNCTION_V + FORWARD_V) && type < 2*FUNCTION_V ) )

int removeTypeDependencies(int);