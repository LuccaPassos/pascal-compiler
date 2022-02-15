program err_function_params;

function foo(a, b : real) : real;
begin
    foo := a;
end;

function bar(a, b : integer) : integer;
var s : string;
begin
    s := ' ' + b;
    bar := a;
end;

var c : char;
begin
    foo(1);
    bar(c, c);
end.