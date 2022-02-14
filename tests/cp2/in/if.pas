program if_statement;

var a, b : integer;
var s : string;

begin
    a := 1;
    b := 2;
    s := 'a_string';
    if (a < b)
    then
        a := b;
    
    if (a = b)
    then begin
        if (s <> 'a')
        then
            s := 'a';
    end
end.