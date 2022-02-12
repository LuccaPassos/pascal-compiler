program assignment;

var
    num, another : integer;
    real_num : real;
    p0, p1, p2 : boolean;
    first_name, city : string;
    initial : char;

begin
    num := 1 + 2*(15 - 2);
    real_num := num / 4;
    another := num + 3*(15 - num);

    p0 := true;
    p1 := another > num;
    p2 := p0 AND p1;

    first_name := 'Bob';
    city := 'Paris';
    first_name := first_name + ' from ' + city;
    city := city + ' ' + num;

    initial := CHR (56);
    city := city + ' ' + initial;

end.