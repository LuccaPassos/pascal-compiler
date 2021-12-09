program if2;
   
var X, Y, A, B : integer;
   
begin
  Readln (X,Y,A,B);
  if (X > Y) then
    if(A > B)
      then begin
        Writeln ('X é maior que Y e A é maior que B');
        Writeln ('O Valor de X é = ', X);
        Writeln ('O Valor de A é = ', A);
      end
      else begin
        Writeln ('A não é Maior que B');
        Writeln ('O Valor de B é = ', B);
      end
    else Writeln ('X não é maior que Y');
end.