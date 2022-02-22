program array_assign_err;

var
    a : array[0..10] of integer;
begin
    a[6] := 'string'; { <- Type error}
end.