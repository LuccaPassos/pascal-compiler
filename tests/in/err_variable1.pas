program err_variable1

f : real; (* Unexpected declaration *)

var a : real;
    b : integer;
    c, d: string[10];
    e : real;

begin
    readln(a, b, c, d);
end;