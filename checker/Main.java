package checker;

import java.io.IOException;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import code.CodeGen;
import parser.pascalLexer;
import parser.pascalParser;

public class Main {
	public static void main(String[] args) throws IOException {

		if (args.length == 0) {
			System.err.printf("No files passed!\n");
			System.exit(1);
		}

		CharStream input = null;
		try {
			input = CharStreams.fromFileName(args[0]);
		} catch (Exception exception) {
			System.err.printf("File '%s' does not exist!\n", args[0]);
			System.exit(1);
		}

		pascalLexer lexer = new pascalLexer(input);

		CommonTokenStream tokens = new CommonTokenStream(lexer);

		pascalParser parser = new pascalParser(tokens);

		ParseTree tree = parser.program();

		if (parser.getNumberOfSyntaxErrors() != 0) {
			return;
		}

		SemanticChecker checker = new SemanticChecker();
		checker.visit(tree);

		// System.err.println("PARSE SUCCESSFUL!");

		// System.err.println("Generating Code...\n\n");

		CodeGen codeGen = new CodeGen(
				checker.stringTable,
				checker.globalScope);
		codeGen.execute(checker.getAST());
	}

}