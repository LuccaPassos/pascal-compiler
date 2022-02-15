program function_decl;

function foo(a, b : real; c : integer) : real;
begin
    if (c > 10) then
        foo := a
    else
        foo := b;
end;

function bar(s : string) : string;
var a, b, c : integer;
var r : real;
begin
    a := 10;
    b := 5;
    c := a + 2*b;
    r := foo(a, b, c);

    s := s + c;
    bar := s;
end;

var a, b : real;
var s, t : string;
begin
    foo(a, b, 15);
    s := 'string';
    t := bar(s);
end.