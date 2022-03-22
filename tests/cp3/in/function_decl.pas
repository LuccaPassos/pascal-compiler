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
	c := a * b;
    r := foo(a, b, c);
	write(r);

    bar := s;
end;

var a, b : real;
var s, t : string;
begin
	a := 7.5;
	b := 2.33;
    write(foo(a, b, 15), chr(10));
    s := 'string';
    t := bar(s);
	write(chr(10), t);
end.