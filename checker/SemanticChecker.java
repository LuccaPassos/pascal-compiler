package checker;

import org.antlr.v4.runtime.Token;

import parser.pascalParser;
import parser.pascalParser.ExprStrValContext;
import parser.pascalParserBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import typing.Type;

public class SemanticChecker extends pascalParserBaseVisitor<Void> {

	private StrTable st = new StrTable();
    private VarTable vt = new VarTable();
    
    Type lastDeclType;
    
    private boolean passed = true;

    // Check if token was declared or not.
    void checkVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
    	if (idx == -1) {
    		System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' was not declared.\n",
				line, text);
    		passed = false;
            return;
        }
    }
    
    // Creates a new variable with name `token`.
    void newVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
        if (idx != -1) {
        	System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
                line, text, vt.getLine(idx));
        	passed = false;
            return;
        }
        vt.addVar(text, line, lastDeclType);
    }
    
    // Return true if all tests passed.
    boolean hasPassed() {
    	return passed;
    }
    
    // Show literal and variable table contents.
    void printTables() {
        System.out.print("\n\n");
        System.out.print(st);
        System.out.print("\n\n");
    	System.out.print(vt);
    	System.out.print("\n\n");
    }

	@Override
	public Void visitExprStrVal(ExprStrValContext ctx) {
		// Add string to literal table
		st.add(ctx.STRING_LITERAL().getText());
		return null;
	}
	
}
