program function2;

function E(x: real): real;
    function F(y: real): real;
    begin
        F := x + y;
    end;
begin
    E := F(3)
end;


begin
    result := E(2)
end.