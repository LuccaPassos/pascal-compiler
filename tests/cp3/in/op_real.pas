program op_real;

var 
    a : real;
    b : real;
    c : real;
    d : real;
    e : real;
    f : real;
    br : char;

begin
	br := chr (10);
    a := 64.3;
    b := 32.7;
	write(a);
	write('|');
	write(b);
	write('|');
	write(br);
	c := a + b + 2.1;
	write(c);
	write('|');

	d := (a - b) - 2.1;
	write(d);
	write('|');

	e := a * b * 2.1;
	write(e);
	write('|');

	f := (a / b) / 2.1;
	write(f);

end.