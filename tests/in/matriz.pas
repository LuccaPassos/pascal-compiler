program matriz3;
uses crt;
var d: array[1..3,1..3] of integer;
    i,j,x,teste: integer;
begin
    teste := 0;
    clrscr;
    for i:=1 to 3 do
        for j:=1 to 3 do
            begin
                write ('Digite um numero inteiro para a matriz D3X3: ');
                readln (d[i,j]);
            end
        ;
    ;
    writeln;
    write ('Digite um numero inteiro para verificar se existe na matriz D: ');
    readln (x);
    for i:=1 to 3 do
        for j:=1 to 3 do
            if x = d[i,j] then
            teste := 1
            ;
        ;
    ;
    if teste = 1 then
        writeln ('Este numero existe na matriz D')
    else
        writeln ('Este numero nao existe na matriz D')
    ;
    readln;
end.