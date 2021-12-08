program mode;
uses crt;
var
    a,b,c:integer;
begin
    clrscr;
    writeln('Digite 2 números inteiros');
    readln(a,b);
    c:=(a)mod(b);
    writeln('O resto da divisão do número ',a,' pelo número ',b,' é ',c);
    readkey;
end.