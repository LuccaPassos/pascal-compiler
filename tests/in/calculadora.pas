program calculadora;
uses crt;
var a, b : real;
    oper : string[1];
 
begin
     REPEAT
 
     textcolor(15);
     writeln('CALCULADORA SIMPLES');
     write('Valor 1: ');
     readln(a);
     write('Operador (+, -, *, /): ');
     readln(oper);
     if (oper='/') then begin
        repeat
              write('Valor 2: ');
              readln(b);
              if (b=0) then begin
                 textcolor(12);
                 writeln('ERRO! Divisao por zero!');
                 textcolor(15);
              end;
        until (b<>0);
     end else begin
         write('Valor 2: ');
         readln(b);
     end;
     writeln;
     write('RESULTADO da operacao: ');
     textcolor(14);
     if (oper='+') then write(a+b:0:10)
     else begin
          if (oper='-') then write(a-b:0:10)
          else begin
               if (oper='*') then write(a*b:0:10)
               else write(a/b:0:10);
          end;
     end;
     textcolor(15);
     writeln('1 > Repetir operacao');
     writeln('0 > Sair');
     repeat
           readln(oper);
     until (oper='1') or (oper='0');
     if (oper='1') then writeln;
 
     UNTIL (oper='0');
end.