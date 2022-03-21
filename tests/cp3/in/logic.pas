program logic;

var 
    a : boolean;
    b : boolean;
    c : boolean;
    d : boolean;
	
begin
    a := true;
    b := false;
	c := a and b or false;
	d := a or b and false;
	write(c);
	write(chr(10));
	write(d);
end.