program array_assign_err;

var
    a : array[0..10] of integer;
begin
    a[0, 5] := 10; { <- Access error}
end.