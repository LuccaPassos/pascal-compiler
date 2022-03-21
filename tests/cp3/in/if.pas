program if_statement;

var a, b : integer;
var c, d : boolean;
var s : string;

begin
    a := 2;
    b := 1;
    s := 'a_string';
    if (a < b) then
		begin
			a := b;
			s := 'string';
		end
    else if (a > b) then
		begin
			s := 'greater';
			a := 2*(5 + b);
		end;
    write(s);
    if (a = b)then 
		begin
			c := true;
			d := false;
			if (c or d) then
				s := 'a';
    	end;
    write(chr(10));
    write(a);
end.