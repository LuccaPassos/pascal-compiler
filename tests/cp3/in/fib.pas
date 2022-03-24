program fibonacci;

function fib(n:integer): integer;
begin
    if (n <= 2) then
        fib := 1
    else
        fib := fib(n-1) + fib(n-2);
end;

var
    i, n:integer;

begin
	i := 1;
	read (n);
    while (i <= n) do begin
        write(fib(i), ', ');
		i := i + 1;
	end;	
	write('...',chr(10));
end.