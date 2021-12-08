program Primes(input,output);
var
    candidates, primes : Array[0..999] of Integer;
    n, i, j : Integer;
begin
    for i := 0 to 999 do
    begin
        candidates[i] := 1;
    end;
    candidates[0] := 0;
    candidates[1] := 0;
    i := 0;
    while i < 1000 do
    begin
        while (i < 1000) and (candidates[i] = 0) do
        begin
            i := i + 1;
        end;
        if i < 1000 then
        begin
            j := 2;
            while i*j < 1000 do
            begin
                candidates[i*j] := 0;
                j := j + 1;
            end;
            i := i + 1;
        end;
    end;
    for i := 0 to 999 do
    begin
        if candidates[i] <> 0 then
        begin
            primes[i] := i;
        end;
    end;
    for i := 0 to 999 do
    begin
        writeln(primes[i]);
    end;
end.