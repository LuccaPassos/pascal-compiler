program prints;

var 
    a : integer;
    b : real;
    c : boolean;
    d : string;
   	e : char;

begin
   	a := 12;
    b := 15.66;
	c := false;
	d := 'Hi!';
	e := chr(65);
	write(a, b, c, d, e);
	write(chr(10));
	write(12, 15.66, false, 'Hi!', chr(65));
	
end.