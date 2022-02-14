package checker;

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import parser.pascalLexer;
import parser.pascalParser;

public class Main {
	public static void main(String[] args) throws IOException {
		CharStream input = CharStreams.fromFileName(args[0]);

		pascalLexer lexer = new pascalLexer(input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		pascalParser parser = new pascalParser(tokens);

		ParseTree tree = parser.program();

		if (parser.getNumberOfSyntaxErrors() != 0) {
			return;
		}

		SemanticChecker checker = new SemanticChecker();
		checker.visit(tree);

		System.out.println("PARSE SUCCESSFUL!");
		checker.printTables();
		checker.printAST();
	}

}
