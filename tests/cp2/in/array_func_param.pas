program array_assign;

function foo(arr : array of real) : real;
begin
    foo := arr[5];
end;

var
    arr : array of real;
begin
    foo(arr);
end.