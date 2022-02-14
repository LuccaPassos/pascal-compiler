program err_function_redeclaration;

function foo(a : integer) : integer;
begin
    foo := a;
end;

function foo(a, b : integer) : integer;
var s : string;
begin
    s := ' ' + b;
    bar := a;
end;

var a : integer;
begin
    
end.