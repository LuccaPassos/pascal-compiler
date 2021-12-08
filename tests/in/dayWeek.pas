program DiaDaSemana;

var Dia, Mes, Ano, DiasDoAno, Dias31, AnosBiss : integer;
    Anos, NumDias : longint;
    Biss : boolean;

begin
    writeln('Digite dia, mes e ano ');
    readln(Dia, Mes, Ano);
    Biss := true;
    {Verifica se o ano e bissexto}
    if ((Ano mod 4 <> 0) or ((Ano mod 100 = 0) and (Ano mod 400 <> 0)))
        then
            Biss := false;
    Anos := Ano - 1600;
    {Numero de meses com 31 dias ate o mes dado}
    if (Mes < 9)
        then
            Dias31 := Mes div 2
        else
            Dias31 := (Mes + 1) div 2;
    {Numero de dias do ano dado, considerando fevereiro com tendo 30 dias}
    DiasDoAno := 30*(Mes - 1) + Dia + Dias31;
    {Retifica o numero de dias de fevereiro}
    if (Mes > 2)
        then
            if Biss
                then
                    DiasDoAno := DiasDoAno - 1
                else
                    DiasDoAno := DiasDoAno - 2;
    {Numero de anos bissextos entre o ano dado (exclusive) e 1600}
    if Biss
        then
            AnosBiss := (Ano div 4 - 400) - (Ano div 100 - 16) + (Ano div 400 - 4)
        else
            AnosBiss := (Ano div 4 - 400) - (Ano div 100 - 16) + (Ano div 400 - 4) + 1;
    {Numero de dias entre a data dada e 01/01/1600}
    NumDias := Anos*365 + DiasDoAno + AnosBiss;
    {Dia da semana}
    write('O dia ', Dia, '/', Mes,'/', Ano, ' caiu(caira) num(a) ');
    case NumDias mod 7 of
        1 : writeln(' Sabado');
        2 : writeln(' Domingo');
        3 : writeln(' Segunda');
        4 : writeln(' Terca');
        5 : writeln(' Quarta');
        6 : writeln(' Quinta');
        0 : writeln(' Sexta');
    end;
end.    