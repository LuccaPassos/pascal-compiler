program if_str;

var a, b : integer;
var s, t : string;

begin
    a := 1;
    b := 2;
    s := 'a_string';
    t := 'b_string';
    if (a < b) then 
		begin
			a := b;
			s := 'string';
		end
    else if (s > t) then 
		begin
			s := 'greater';
			a := 2*(5 + b);
		end;
    if (a = b) then 
		begin
			if ((s <> 'a') or (s = t)) then
				s := 'Hello';
		end;
	write(s);
end.