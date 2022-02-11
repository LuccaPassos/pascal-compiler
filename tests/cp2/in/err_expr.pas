program err_expr;

var
    a : real;
    b, c : real;

begin
    b := 5;
    c := 42;

    a := b + c;

    e := 2*a; { <- e was not declared }
end.