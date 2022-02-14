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
            b := b + 1;
            s := s + c;
        end;
        a := a - 1;
    end;    

end.