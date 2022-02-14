program function_decl;

function foo(a : integer) : integer;
begin
    foo := a;
end;

function bar(a, b : integer) : integer;
var s : string;
begin
    s := ' ' + b;
    bar := a;
end;

var a : integer;
begin
    
end.