program err_function1;

function E(x): real; (* missing type of 'x' *)
begin
    E := x*3;
end;

begin
    result := E(2)
end.