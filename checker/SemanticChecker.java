package checker;

import static typing.Type.BOOL_TYPE;
import static typing.Type.INT_TYPE;
import static typing.Type.NO_TYPE;
import static typing.Type.REAL_TYPE;
import static typing.Type.STR_TYPE;
import static typing.Type.CHAR_TYPE;

import java.util.List;

import org.antlr.v4.runtime.Token;

import parser.pascalParser;
import parser.pascalParser.ExprStrValContext;
import parser.pascalParser.IdentifierContext;
import parser.pascalParserBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import typing.Type;

public class SemanticChecker extends pascalParserBaseVisitor<Type> {

	private StrTable st = new StrTable();
    private VarTable vt = new VarTable();
    
    Type lastDeclType;
    
    private boolean passed = true;

    // Check if token was declared or not.
    Type checkVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
    	if (idx == -1) {
    		System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' was not declared.\n",
				line, text);
    		passed = false;
            return NO_TYPE;
        }

		return vt.getType(idx);
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

	// Catch a type error
	private void typeError(int lineNo, String op, Type t1, Type t2) {
		System.out.printf("SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
				lineNo, op, t1.toString(), t2.toString());
		passed = false;
	}

	// Check if the assignment is valid
	private void checkAssign(int lineNo, Type l, Type r) {
		// BOOL := BOOL
		if (l == BOOL_TYPE && r != BOOL_TYPE) typeError(lineNo, ":=", l, r);
		// STR := STR || STR := CHAR
		if (l == STR_TYPE  && !(r == STR_TYPE || r == CHAR_TYPE))  typeError(lineNo, ":=", l, r);
		// INT := INT
		if (l == INT_TYPE  && r != INT_TYPE)  typeError(lineNo, ":=", l, r);
		// REAL := REAL || REAL := INT
		if (l == REAL_TYPE && !(r == INT_TYPE || r == REAL_TYPE)) typeError(lineNo, ":=", l, r);
		// CHAR := CHAR
		if (l == CHAR_TYPE  && r != CHAR_TYPE)  typeError(lineNo, ":=", l, r);
	}

	private void checkBoolExpr(int lineNo, String cmd, Type t) {
        if (t != BOOL_TYPE) {
            System.out.printf("SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
               lineNo, cmd, t.toString(), BOOL_TYPE.toString());
            passed = false;
        }
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
    public Type visitBoolType(pascalParser.BoolTypeContext ctx) {
    	this.lastDeclType = Type.BOOL_TYPE;
    	return NO_TYPE;
    }
	
	@Override
	public Type visitIntType(pascalParser.IntTypeContext ctx) {
		this.lastDeclType = Type.INT_TYPE;
		return NO_TYPE;
	}

	@Override
	public Type visitRealType(pascalParser.RealTypeContext ctx) {
		this.lastDeclType = Type.REAL_TYPE;
		return NO_TYPE;
    }

	@Override
	public Type visitStrType(pascalParser.StrTypeContext ctx) {
		this.lastDeclType = Type.STR_TYPE;
		return NO_TYPE;
	}

	@Override
	public Type visitCharType(pascalParser.CharTypeContext ctx) {
		this.lastDeclType = Type.CHAR_TYPE;
		return NO_TYPE;
	}

	@Override
    public Type visitVariableDeclaration(pascalParser.VariableDeclarationContext ctx) {
    	visit(ctx.type_());
		List<IdentifierContext> identifiers = ctx.identifierList().identifier();

		// There can be more than one declaration in one line
		for (int i = 0; i < identifiers.size(); i++) {
			Token token = identifiers.get(i).IDENT().getSymbol();
			newVar(token);
		}
    	return NO_TYPE;
    }

	@Override
	public Type visitExprIntVal(pascalParser.ExprIntValContext ctx) {
		return INT_TYPE;
	}

	@Override
	public Type visitExprRealVal(pascalParser.ExprRealValContext ctx) {
		return REAL_TYPE;
	}

	@Override
	public Type visitExprBoolVal(pascalParser.ExprBoolValContext ctx) {
		return BOOL_TYPE;
	}

	@Override
	public Type visitExprStrVal(ExprStrValContext ctx) {
		// Add string to literal table
		st.add(ctx.STRING_LITERAL().getText());
		return STR_TYPE;
	}

	@Override
	public Type visitExprCharVal(pascalParser.ExprCharValContext ctx) {
		return CHAR_TYPE;
	}

	@Override
	public Type visitFactor(pascalParser.FactorContext ctx) {

		if (ctx.variable() != null) {
			return visit(ctx.variable());
		}

		if (ctx.expression() != null) {
			return visit(ctx.expression());
		}

		if (ctx.unsignedConstant() != null) {
			return visit(ctx.unsignedConstant());
		}

		if (ctx.factor() != null) {
			return visit(ctx.factor());
		}

		if (ctx.bool_() != null) {
			return BOOL_TYPE;
		}

		return NO_TYPE;
	}

	@Override
	public Type visitTerm(pascalParser.TermContext ctx) {

		Type factorType = visit(ctx.signedFactor().factor());

		if (ctx.term() != null) {
			Type termType = visit(ctx.term());
			int operator = ctx.multiplicativeoperator().op.getType();

			if (operator == pascalParser.AND){
				return factorType.unifyAndOr(termType);
			}

			return factorType.unifyOtherArith(termType);
		}

		return factorType;
	}

	@Override
	public Type visitSimpleExpression(pascalParser.SimpleExpressionContext ctx) {
		
		Type termType = visit(ctx.term());

		if (ctx.simpleExpression() != null) {
			Type simpleExpressionType = visit(ctx.simpleExpression());
			
			int operator = ctx.additiveoperator().op.getType();
			if (operator == pascalParser.PLUS) {
				return termType.unifyPlus(simpleExpressionType);
			}

			if (operator == pascalParser.OR) {
				return termType.unifyAndOr(simpleExpressionType);
			}

			if (operator == pascalParser.MINUS) {
				return termType.unifyOtherArith(simpleExpressionType);
			}
		}

		return termType;
	}

	@Override
	public Type visitExpression(pascalParser.ExpressionContext ctx) {

		Type simpleExpressionType = visit(ctx.simpleExpression());

		if (ctx.expression() != null) {
			Type expressionType = visit(ctx.expression());
			return simpleExpressionType.unifyComp(expressionType);
		}

		return simpleExpressionType;
	}

	@Override
	public Type visitAssignmentStatement(pascalParser.AssignmentStatementContext ctx) {
		Token token = ctx.variable().identifier().get(0).IDENT().getSymbol();
		Type identifierType = checkVar(token);

		Type expressionType = visit(ctx.expression());

		checkAssign(token.getLine(), identifierType, expressionType);
		return NO_TYPE;
	}

	@Override
	public Type visitIfStatement(pascalParser.IfStatementContext ctx) {
		Type expressionType = visit(ctx.expression());
		checkBoolExpr(ctx.IF().getSymbol().getLine(), "IF", expressionType);
		return NO_TYPE;
	}

	@Override
	public Type visitWhileStatement(pascalParser.WhileStatementContext ctx) {
		Type expressionType = visit(ctx.expression());
		checkBoolExpr(ctx.WHILE().getSymbol().getLine(), "WHILE", expressionType);
		return NO_TYPE;
	}

	
	@Override
	public Type visitVariable(pascalParser.VariableContext ctx) {

		// Check if the variable exists
		Token token = ctx.identifier().get(0).IDENT().getSymbol();

		return checkVar(token);
	}
	
}
