program err_variable2

var a : real;
    b : integer;
    c, d: string[10];
    1e : real; (* Forbidden variable name *)

begin
    readln(a, b, c, d);
end;