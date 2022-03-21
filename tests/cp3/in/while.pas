program while_statement;

var
    a, b : integer;
    c : char;
    s : string;

begin
    a := 10;
    b := 0;
    c := chr (67);
    s := 'CHECK';
    while (a > 1)
    do
    begin
        while (b < 10)
        do
        begin
			if (b = 2) then
				write(b);
            b := b + 1;
        end;
        a := a - 1;
    end;    
end.