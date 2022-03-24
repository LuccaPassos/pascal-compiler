program array_assign;

var
    a : array[0..10] of integer;
    b : array[0..10] of array[0..50] of array[1..10] of char;
    facts : array[0..50] of boolean;
    i : integer;
begin
    i := 3;
    a[0] := 20;
    b[0, 40, 15] := chr (65);
	facts[16] := false;
	facts[32] := true;
	write(facts[32], chr (10), a[0], chr (10), b[0, 40, 15]);
end.