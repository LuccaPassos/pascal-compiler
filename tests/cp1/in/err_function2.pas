program err_function2;

function E(x: real): real;
begin
    function F(y: real): real; (* Unexpected declaration of function *)
    begin
        F := x + y;
    end;
    E := F(3)
end;


begin
    result := E(2)
end.