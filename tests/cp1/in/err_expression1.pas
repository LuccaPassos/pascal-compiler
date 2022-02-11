program err_expression1;

var a : real;
    b : integer;
    c, d: string[10];
    e : real;

begin
    readln(a, b, c, d);
    e := a + b;
    a := a + (b - e) * b;
    e := (a + (b - e) / e) # (a + (b)); (* Unkown operator '#' *)
end.