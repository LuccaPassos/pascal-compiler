program err_redeclaration;

var
    was_declared : integer;
    is_declared : string;

var is_declared : real; { < 'is_declared' already declared }

begin
end.