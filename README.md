# pascal-compiler
This is a compiler from Pascal to LLVM. It was implemented in Java using [`antlr`](https://www.antlr.org/).

## Running

**Compile the compiler**

Firstly, compile the compiler using

    make javac

This will generate all the Jaca `.class` files in the `bin/` folder.

**Running the compiler and the program**

To run the compiler and generate the `.ll` target from a `.pas` input, you can use

    make ll FILE=path/to/file.pas

This will generate a file in the root directory called `file.ll`. You can use `lli` to run the program

    lli file.ll

You can also compile the program and automaticaly run it with

    make lli FILE=path/to/file.pas

**Tests**

There are a few tests for some of the implemented features of the language. To run them, simply run

    make runall

This will compile all the programs in `tests/cp3/in/` and generate the `.ll` files in `tests/cp3/out/`

