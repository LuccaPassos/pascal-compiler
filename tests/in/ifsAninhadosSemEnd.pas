program aninhadoss;
   
Var X, Y, A, B : Integer;
   
Begin
  Readln (X,Y,A,B);
  If (X > Y) Then
    If(A > B)
      Then Begin
        Writeln ('X é maior que Y e A é maior que B');
        Writeln ('O Valor de X é = ', X);
        Writeln ('O Valor de A é = ', A);
      End
      Else Begin
        Writeln ('A não é Maior que B');
        Writeln ('O Valor de B é = ', B);
    Else Writeln ('X não é maior que Y');
End.