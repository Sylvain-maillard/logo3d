/*
BSD License

Copyright (c) 2013, Tom Everett
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
3. Neither the name of Tom Everett nor the names of its contributors
   may be used to endorse or promote products derived from this software
   without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

grammar Logo;

prog
    : (line? EOL)+ line?
    ;

line
    : cmd+ comment?
    | comment
    | print comment?
    | procedureDeclaration
    ;
     
cmd
    : repeat
    | fd
    | bk
    | rt
    | lt
    | cs
    | pu
    | pd
    | ht
    | st
    | home
    | label
    | setxy
    | make
    | procedureInvocation
    | ife
    | stop
    | fore
    ;

procedureInvocation
    : name expression*
    ;

procedureDeclaration
    : 'to' name parameterDeclarations* EOL? (line? EOL)+ 'end'
    ;

parameterDeclarations
    : ':' name (',' parameterDeclarations)*
    ;

func
    : random
    ;

repeat
    : 'repeat' number block
    ;

block
    : '[' cmd+ ']'
    ;
    
ife
    : 'if' comparison block
    ;

comparison
    : expression comparisonOperator expression
    ;

comparisonOperator
    : LT 
    | GT 
    | EQ
    ;

make
    : 'make' stringliteral value
    ;

print
    : 'print' (value | quotedstring)
    ;

quotedstring
    : '[' (quotedstring | ~']')* ']'
    ;

name
    : STRING
    ;

value
    : stringliteral  #literalValue
    | deref          #derefValue
    | expression     #expressionValue
    ;

/*
signExpression 
    : ((PLUS|MINUS))* (number | deref | func)
    ;

/*
multiplyingExpression
    : signExpression (op=(MULT | DIVIDE) signExpression)*
    ;

expression 
     : multiplyingExpression (op=(PLUS|MINUS) multiplyingExpression)*
     ;
*/
expression
    : expression op=(PLUS|MINUS) expression      #additiveExpression
    | expression op=(MULT | DIVIDE) expression   #multiplicationExpression
    | ((PLUS|MINUS))* (number | deref | func)    #signExpression
    ;

deref
    : ':' name
    ;

fd
    : ('fd' | 'forward') expression
    ;

bk
    : ('bk' | 'backward') expression
    ;

rt
    : ('rt' | 'right') expression
    ;

lt
    : ('lt' | 'left') expression
    ;

cs
    : 'cs' | 'clearscreen'
    ;

pu
    : 'pu' | 'penup'
    ;

pd
    : 'pd' | 'pendown'
    ;

ht
    : 'ht' | 'hideturtle'
    ;

st
    : 'st' | 'showturtle'
    ;

home
    : 'home'
    ;

stop
    : 'stop'
    ;
        
label
    : 'label'
    ;

setxy
    : 'setxy' expression expression
    ;

random
    : 'random' expression
    ;

fore
    : 'for' '[' name expression expression expression ']' block
    ;

number
    : NUMBER
    ;

comment
    : COMMENT
    ;
     
stringliteral
    : '"' STRING
    ;

STRING
    : [a-zA-Z] [a-zA-Z0-9_]*
    ;
    
NUMBER
    : [0-9]+
    ;

COMMENT
    : ';' ~[\r\n]*
    ;

EOL
    : '\r'? '\n'
    ;
    
PLUS  : '+' ;
MINUS : '-' ;
DIVIDE: '/' ;
MULT  : '*' ;

LT : '<' ;
GT : '>' ;
EQ : '=' ;

WS
    : [ \t\r\n]->skip
    ;