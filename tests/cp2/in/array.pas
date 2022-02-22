program array_assign;

function foo : integer;
var bar : array[1..10] of array[1..10] of real;
begin
    bar[5, 3] := 6.40;
end;

var
    a : array[0..10] of integer;
    b : array[-10..10] of array[0..50] of array[1..10] of char;
    names : array[0..50] of string;
    i : integer;
begin
    i := 3;
    a[0] := 20;
    b[0, 40, 15] := chr (56);
    names[i] := 'name';
    foo();
end.