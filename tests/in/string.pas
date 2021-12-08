PROGRAM P;

VAR s : String;

BEGIN
    writeln('Sua frase: ');
    readln(s);

    writeln('Você disse: ',s);

    writeln(s[4]); {escreve o "a" de "essa é minha frase"}

    s[5] := 'z'; {agora s contém "essazé minha frase" - mudei o 5º caracter}
END.