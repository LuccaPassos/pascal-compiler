Program funcoesAninhadas;

Function E(x: real): real;
    Function F(y: real): real;
    Begin
        F := x + y;
    End;
Begin
    E := F(3)
End;


Begin
    result := E(2)

End.