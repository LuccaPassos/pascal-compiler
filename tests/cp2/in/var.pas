program variables;

var a : integer;
var b : string;
var c : char;
var
    num : real;
    first_name : string;
    went_to_college : boolean;
    age, siblings, month_of_birth : integer;
begin
    a := 2*num;
    went_to_college := (age > 18);
    first_name := 'Bob';

    siblings := month_of_birth + 3*(a + age - (num/5));
end.