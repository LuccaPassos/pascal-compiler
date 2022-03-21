program comps;

var 
    a, b : integer;
	c, d : real;
	e, f : boolean;
	g, h : string;

begin
    a := 20;
    b := 10;
	write(a);
	write('       |');
	write(b);
	write('        || ');
	write(a = b);
	write('|');
	write(a <> b);
	write('|');
	write(a > b);
	write('|');
	write(a < b);
	write('|');
	write(a >= b);
	write('|');
	write(a <= b);
	write(chr(10));

    c := 12.0234;
    d := 94.454;
	write(c);
	write('|');
	write(d);
	write(' || ');
	write(c = d);
	write('|');
	write(c <> d);
	write('|');
	write(c > d);
	write('|');
	write(c < d);
	write('|');
	write(c >= d);
	write('|');
	write(c <= d);
	write(chr(10));

    e := true;
    f := false;
	write(e);
	write('        |');
	write(f);
	write('         || ');
	write(e = f);
	write('|');
	write(e <> f);
	write('|');
	write(e > f);
	write('|');
	write(e < f);
	write('|');
	write(e >= f);
	write('|');
	write(e <= f);
	write(chr(10));

    g := 'abcd';
    h := 'efgh';
	write(g);
	write('     |');
	write(h);
	write('      || ');
	write(g = h);
	write('|');
	write(g <> h);
	write('|');
	write(g > h);
	write('|');
	write(g < h);
	write('|');
	write(g >= h);
	write('|');
	write(g <= h);
	write(chr(10));
end.
