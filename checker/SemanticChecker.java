package checker;

import java.util.List;

import org.antlr.v4.runtime.Token;

import parser.pascalParser;
import parser.pascalParser.ExprStrValContext;
import parser.pascalParser.IdentifierContext;
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
    public Void visitBoolType(pascalParser.BoolTypeContext ctx) {
    	this.lastDeclType = Type.BOOL_TYPE;
    	return null;
    }
	
	@Override
	public Void visitIntType(pascalParser.IntTypeContext ctx) {
		this.lastDeclType = Type.INT_TYPE;
		return null;
	}

	@Override
	public Void visitRealType(pascalParser.RealTypeContext ctx) {
		this.lastDeclType = Type.REAL_TYPE;
		return null;
    }

	@Override
	public Void visitStrType(pascalParser.StrTypeContext ctx) {
		this.lastDeclType = Type.STR_TYPE;
		return null;
	}

	@Override
	public Void visitCharType(pascalParser.CharTypeContext ctx) {
		this.lastDeclType = Type.CHAR_TYPE;
		return null;
	}

	@Override
    public Void visitVariableDeclaration(pascalParser.VariableDeclarationContext ctx) {
    	visit(ctx.type_());
		List<IdentifierContext> identifiers = ctx.identifierList().identifier();

		// There can be more than one declaration in one line
		for (int i = 0; i < identifiers.size(); i++) {
			Token token = identifiers.get(i).IDENT().getSymbol();
			newVar(token);
		}
    	return null;
    }
	
	@Override
	public Void visitVariable(pascalParser.VariableContext ctx) {

		// Check if the variable exists
		Token token = ctx.identifier().get(0).IDENT().getSymbol();
		checkVar(token);

		return null;
	}

	@Override
	public Void visitExprStrVal(ExprStrValContext ctx) {
		// Add string to literal table
		st.add(ctx.STRING_LITERAL().getText());
		return null;
	}
	
}
