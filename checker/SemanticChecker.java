package checker;

import static typing.Type.BOOL_TYPE;
import static typing.Type.INT_TYPE;
import static typing.Type.NO_TYPE;
import static typing.Type.REAL_TYPE;
import static typing.Type.STR_TYPE;
import static typing.Type.CHAR_TYPE;

import java.util.List;

import org.antlr.v4.misc.Graph.Node;
import org.antlr.v4.runtime.Token;

import ast.AST;
import ast.NodeKind;
import parser.pascalParser;
import parser.pascalParser.ExprStrValContext;
import parser.pascalParser.IdentifierContext;
import parser.pascalParser.StatementContext;
import parser.pascalParser.VariableDeclarationPartContext;
import parser.pascalParserBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import typing.Type;
import typing.Conv;
import typing.Conv.Unif;

public class SemanticChecker extends pascalParserBaseVisitor<AST> {

	private StrTable st = new StrTable();
    private VarTable vt = new VarTable();
    
    Type lastDeclType;

	AST root;

    // Check if token was declared or not.
    AST checkVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
    	if (idx == -1) {
    		System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' was not declared.\n",
				line, text);
			System.exit(1);
            return null;
        }

		return new AST(NodeKind.VAR_USE_NODE, idx, vt.getType(idx));
    }
    
    // Creates a new variable with name `token`.
    AST newVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = vt.lookupVar(text);
        if (idx != -1) {
        	System.err.printf(
    			"SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
                line, text, vt.getLine(idx));
			System.exit(1);
            return null;
        }
        idx = vt.addVar(text, line, lastDeclType);
		return new AST(NodeKind.VAR_DECL_NODE, idx, lastDeclType);
    }

	// Catch a type error
	private void typeError(int lineNo, String op, Type t1, Type t2) {
		System.out.printf("SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
				lineNo, op, t1.toString(), t2.toString());
		System.exit(1);
	}

	// Check if the assignment is valid
	private AST checkAssign(int lineNo, AST left, AST right) {
		Type leftType = left.type;
		Type rightType = right.type;

		// BOOL := BOOL
		if (leftType == BOOL_TYPE && rightType != BOOL_TYPE)
			typeError(lineNo, ":=", leftType, rightType);

		// STR := STR || STR := CHAR
		if (leftType == STR_TYPE) {
			if (rightType == CHAR_TYPE) {
				right = Conv.createConvNode(Conv.C2S, right);
			} else if (rightType != STR_TYPE) {
				typeError(lineNo, ":=", leftType, rightType);
			}
		}

		// INT := INT
		if (leftType == INT_TYPE  && rightType != INT_TYPE)  typeError(lineNo, ":=", leftType, rightType);

		// REAL := REAL || REAL := INT
		if (leftType == REAL_TYPE) {
			if (rightType == INT_TYPE) {
				right = Conv.createConvNode(Conv.I2R, right);
			} else if (rightType != STR_TYPE) {
				typeError(lineNo, ":=", leftType, rightType);
			}
		}

		// CHAR := CHAR
		if (leftType == CHAR_TYPE  && rightType != CHAR_TYPE)  typeError(lineNo, ":=", leftType, rightType);

		return AST.newSubtree(NodeKind.ASSIGN_NODE, Type.NO_TYPE, left, right);
	}

	private void checkBoolExpr(int lineNo, String cmd, Type t) {
        if (t != BOOL_TYPE) {
            System.out.printf("SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
               lineNo, cmd, t.toString(), BOOL_TYPE.toString());
			System.exit(1);
        }
    }
    
    // Show literal and variable table contents.
    void printTables() {
        System.out.print("\n\n");
        System.out.print(st);
        System.out.print("\n\n");
    	System.out.print(vt);
    	System.out.print("\n\n");
    }

    void printAST() {
    	AST.printDot(root, vt);
    }

	@Override
    public AST visitBoolType(pascalParser.BoolTypeContext ctx) {
    	this.lastDeclType = Type.BOOL_TYPE;
    	return null;
    }
	
	@Override
	public AST visitIntType(pascalParser.IntTypeContext ctx) {
		this.lastDeclType = Type.INT_TYPE;
		return null;
	}

	@Override
	public AST visitRealType(pascalParser.RealTypeContext ctx) {
		this.lastDeclType = Type.REAL_TYPE;
		return null;
    }

	@Override
	public AST visitStrType(pascalParser.StrTypeContext ctx) {
		this.lastDeclType = Type.STR_TYPE;
		return null;
	}

	@Override
	public AST visitCharType(pascalParser.CharTypeContext ctx) {
		this.lastDeclType = Type.CHAR_TYPE;
		return null;
	}

	@Override
    public AST visitVariableDeclaration(pascalParser.VariableDeclarationContext ctx) {
    	visit(ctx.type_());
		List<IdentifierContext> identifiers = ctx.identifierList().identifier();
		AST node = AST.newSubtree(NodeKind.VAR_LIST_NODE, Type.NO_TYPE);

		// There can be more than one declaration in one line
		for (int i = 0; i < identifiers.size(); i++) {
			Token token = identifiers.get(i).IDENT().getSymbol();
			node.addChild(newVar(token));
		}
    	return node;
    }

	@Override
	public AST visitExprIntVal(pascalParser.ExprIntValContext ctx) {
		int intData = Integer.parseInt(ctx.getText());
		return new AST(NodeKind.INT_VAL_NODE, intData, INT_TYPE);
	}

	@Override
	public AST visitExprRealVal(pascalParser.ExprRealValContext ctx) {
		float floatData = Float.parseFloat(ctx.getText());
		return new AST(NodeKind.REAL_VAL_NODE, floatData, REAL_TYPE);
	}

	@Override
	public AST visitExprTrue(pascalParser.ExprTrueContext ctx) {
		return new AST(NodeKind.BOOL_VAL_NODE, 1, BOOL_TYPE);
	}

	@Override
	public AST visitExprFalse(pascalParser.ExprFalseContext ctx) {
		return new AST(NodeKind.BOOL_VAL_NODE, 0, BOOL_TYPE);
	}


	@Override
	public AST visitExprStrVal(ExprStrValContext ctx) {
		// Add string to literal table
		int idx = st.addStr(ctx.STRING_LITERAL().getText());
		return new AST(NodeKind.STR_VAL_NODE, idx, STR_TYPE);
	}

	@Override
	public AST visitExprCharVal(pascalParser.ExprCharValContext ctx) {
		char charData = (char)Integer.parseInt(ctx.unsignedInteger().getText());
		return new AST(NodeKind.CHAR_VAL_NODE, charData, CHAR_TYPE);
	}

	@Override
	public AST visitFactor(pascalParser.FactorContext ctx) {

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
			return visit(ctx.bool_());
		}

		return null;
	}

	@Override
	public AST visitTerm(pascalParser.TermContext ctx) {

		AST leftNode = visit(ctx.signedFactor().factor());

		if (ctx.term() != null) {
			AST rightNode = visit(ctx.term());
			
			Type leftType = leftNode.type;
			Type rightType = rightNode.type;
			Unif unif = null;
			int operator = ctx.multiplicativeoperator().op.getType();

			if (operator == pascalParser.STAR || operator == pascalParser.SLASH) {
				unif = leftType.unifyOtherArith(rightType);
			} else if (operator == pascalParser.AND) {
				unif = leftType.unifyAndOr(rightType);
			}

			if (unif.type == NO_TYPE) {
				typeError(ctx.multiplicativeoperator().op.getLine(), ctx.multiplicativeoperator().op.getText(), leftType, rightType);
			}

			leftNode = Conv.createConvNode(unif.lc, leftNode);
			rightNode = Conv.createConvNode(unif.rc, rightNode);

			NodeKind kind = null;
			if (operator == pascalParser.STAR) {
				kind = NodeKind.TIMES_NODE;
			} else if (operator == pascalParser.SLASH) {
				kind = NodeKind.OVER_NODE;
			} else if (operator == pascalParser.AND) {
				kind = NodeKind.AND_NODE;
			}

			return AST.newSubtree(kind, unif.type, leftNode, rightNode);
		}

		return leftNode;
	}

	@Override
	public AST visitSimpleExpression(pascalParser.SimpleExpressionContext ctx) {
		
		AST leftNode = visit(ctx.term());

		if (ctx.simpleExpression() != null) {
			AST rightNode = visit(ctx.simpleExpression());
			
			Type leftType = leftNode.type;
			Type rightType = rightNode.type;
			Unif unif = null;

			int operator = ctx.additiveoperator().op.getType();
			if (operator == pascalParser.PLUS) {
				unif = leftType.unifyPlus(rightType);
			} else if (operator == pascalParser.OR) {
				unif = leftType.unifyAndOr(rightType);
			} else if (operator == pascalParser.MINUS) {
				unif = leftType.unifyOtherArith(rightType);
			}

			if (unif.type == Type.NO_TYPE) {
				typeError(ctx.additiveoperator().op.getLine(), ctx.additiveoperator().op.getText(), leftType, rightType);
			}

			leftNode = Conv.createConvNode(unif.lc, leftNode);
			rightNode = Conv.createConvNode(unif.rc, rightNode);

			NodeKind kind = null;
			if (operator == pascalParser.PLUS) {
				kind = NodeKind.PLUS_NODE;
			} else if (operator == pascalParser.OR) {
				kind = NodeKind.OR_NODE;
			} else if (operator == pascalParser.MINUS) {
				kind = NodeKind.MINUS_NODE;
			}

			return AST.newSubtree(kind, unif.type, leftNode, rightNode);
		}

		return leftNode;
	}

	@Override
	public AST visitExpression(pascalParser.ExpressionContext ctx) {

		AST leftNode = visit(ctx.simpleExpression());

		// There is a relational operation
		if (ctx.expression() != null) {
			AST rightNode = visit(ctx.expression());

			Type leftType = leftNode.type;
			Type rightType = rightNode.type;
			Unif unif = leftType.unifyComp(rightType);

			// Operation not valid
			if (unif.type == Type.NO_TYPE) {
				typeError(ctx.relationaloperator().op.getLine(), ctx.relationaloperator().op.getText(), leftType, rightType);
			}

			leftNode = Conv.createConvNode(unif.lc, leftNode);
			rightNode = Conv.createConvNode(unif.rc, rightNode);

			NodeKind kind = null;
			switch (ctx.relationaloperator().op.getType()) {
				case pascalParser.EQUAL:
				kind = NodeKind.EQ_NODE;
				break;
				
				case pascalParser.NOT_EQUAL:
				kind = NodeKind.NEQ_NODE;
				break;

				case pascalParser.LT:
				kind = NodeKind.LT_NODE;
				break;

				case pascalParser.GT:
				kind = NodeKind.GT_NODE;
				break;

				case pascalParser.LE:
				kind = NodeKind.LE_NODE;
				break;

				case pascalParser.GE:
				kind = NodeKind.GE_NODE;
				break;

				default:
				break;
			}
			return AST.newSubtree(kind, unif.type, leftNode, rightNode);
		}

		return leftNode;
	}

	@Override
	public AST visitAssignmentStatement(pascalParser.AssignmentStatementContext ctx) {
		Token token = ctx.variable().identifier().get(0).IDENT().getSymbol();
		AST identifierNode = checkVar(token);

		AST expressionNode = visit(ctx.expression());

		return checkAssign(token.getLine(), identifierNode, expressionNode);
	}

	@Override
	public AST visitIfStatement(pascalParser.IfStatementContext ctx) {
		AST expressionNode = visit(ctx.expression());
		checkBoolExpr(ctx.IF().getSymbol().getLine(), "IF", expressionNode.type);

		AST thenNode = visit(ctx.statement(0));
		
		if (ctx.ELSE() != null) {
			AST elseNode = visit(ctx.statement(1));
			return AST.newSubtree(NodeKind.IF_NODE, NO_TYPE, expressionNode, thenNode, elseNode);
		}
		
		return AST.newSubtree(NodeKind.IF_NODE, NO_TYPE, expressionNode, thenNode);
	}

	@Override
	public AST visitWhileStatement(pascalParser.WhileStatementContext ctx) {
		AST expressionNode = visit(ctx.expression());
		checkBoolExpr(ctx.WHILE().getSymbol().getLine(), "WHILE", expressionNode.type);
		return null;
	}

	
	@Override
	public AST visitVariable(pascalParser.VariableContext ctx) {

		// Check if the variable exists
		Token token = ctx.identifier().get(0).IDENT().getSymbol();

		return checkVar(token);
	}

	@Override
	public AST visitVariableDeclarationPart(pascalParser.VariableDeclarationPartContext ctx) {
		AST node = AST.newSubtree(NodeKind.VAR_LIST_NODE, Type.NO_TYPE);
		for (int i = 0; i < ctx.variableDeclaration().size(); i++) {
			AST child = visit(ctx.variableDeclaration(i));
			for (int j = 0; j < child.getChildrenSize(); j++) {
				node.addChild(child.getChild(j));
			}
		}
		return node;
	}

	@Override
	public AST visitCompoundStatement(pascalParser.CompoundStatementContext ctx) {
		AST node = AST.newSubtree(NodeKind.STMTS_NODE, Type.NO_TYPE);
		List<StatementContext> statementsSections = ctx.statements().statement();
		for (int i = 0; i < statementsSections.size()-1; i++) {
			AST child = visit(statementsSections.get(i));
			node.addChild(child);
		}
		return node;
	}

	@Override
	public AST visitBlock(pascalParser.BlockContext ctx) {

		AST varsSection = AST.newSubtree(NodeKind.VAR_LIST_NODE, Type.NO_TYPE);
		List<VariableDeclarationPartContext> varsSections= ctx.variableDeclarationPart();
		for (int i = 0; i < varsSections.size(); i++) {
			AST varsSubSection = visit(varsSections.get(i));
			for (int j = 0; j < varsSubSection.getChildrenSize(); j++) {
				varsSection.addChild(varsSubSection.getChild(j));
			}
		}

		AST statementsSection = visit(ctx.compoundStatement());

		AST node = AST.newSubtree(NodeKind.BLOCK_NODE, Type.NO_TYPE, varsSection, statementsSection);
		return node;
	}

	@Override
	public AST visitProgram(pascalParser.ProgramContext ctx) {
		this.root = visit(ctx.block());
		return this.root;
	}
	
}
