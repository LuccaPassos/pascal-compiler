# pascal-compiler
This is a compiler from Pascal to LLVM.

## Lexer and Parser
You can run the `make` command to build the lexer and parser, `make run` to run a single file (set the `FILE` var to the file you want to run), `make runall` to run all the files in the `IN` dir (set to the `examples/` folder by default).

By running `make dot FILE=path/of/input.pas` runs the parser with the specified input file and generates a `.png` file containing the AST's representation.
