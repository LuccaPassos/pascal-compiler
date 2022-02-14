program err_function;

function foo(a : real) : integer;
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