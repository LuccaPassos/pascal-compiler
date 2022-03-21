program op_int;

var 
    a : integer;
    b : integer;
    c : integer;
    d : integer;
    e : integer;
    f : real;
    br : char;

begin
	br := chr (10);
    a := 64;
    b := 32;
	write(a);
	write('|');
	write(b);
	write('|');
	write(br);
	c := a + b + 2;
	write(c);
	write('|');

	d := (a - b) - 2;
	write(d);
	write('|');

	e := a * b * 2;
	write(e);
	write('|');

	f := (a / b) / 2;
	write(f);

end.