package checker;

import static typing.Type.BOOL_TYPE;
import static typing.Type.INT_TYPE;
import static typing.Type.NO_TYPE;
import static typing.Type.REAL_TYPE;
import static typing.Type.STR_TYPE;
import static typing.Type.CHAR_TYPE;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.misc.Graph.Node;
import org.antlr.v4.runtime.Token;

import ast.AST;
import ast.NodeKind;
import parser.pascalParser;
import parser.pascalParser.ExprStrValContext;
import parser.pascalParser.FormalParameterSectionContext;
import parser.pascalParser.IdentifierContext;
import parser.pascalParser.ProcedureAndFunctionDeclarationPartContext;
import parser.pascalParser.StatementContext;
import parser.pascalParser.VariableDeclarationPartContext;
import parser.pascalParserBaseVisitor;
import tables.StrTable;
import tables.VarTable;
import tables.FunTable;
import typing.Type;
import typing.Conv;
import typing.Conv.Unif;

public class SemanticChecker extends pascalParserBaseVisitor<AST> {

	private StrTable st = new StrTable();
    private VarTable vt = new VarTable();
	private FunTable ft = new FunTable();
    
    Type lastDeclType;
	VarTable lastScope;

	AST root;

    // Check if token was declared or not.
    AST checkVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = lastScope.lookupVar(text);
    	if (idx == -1) {
    		System.out.printf(
    			"SEMANTIC ERROR (%d): variable '%s' was not declared.\n",
				line, text);
			System.exit(1);
            return null;
        }

		return new AST(NodeKind.VAR_USE_NODE, idx, lastScope.getType(idx));
    }
    
    // Creates a new variable with name `token`.
    AST newVar(Token token) {
    	String text = token.getText();
    	int line = token.getLine();
   		int idx = lastScope.lookupVar(text);
        if (idx != -1) {
        	System.out.printf(
    			"SEMANTIC ERROR (%d): variable '%s' already declared at line %d.\n",
                line, text, lastScope.getLine(idx));
			System.exit(1);
            return null;
        }
        idx = lastScope.addVar(text, line, lastDeclType);
		return new AST(NodeKind.VAR_DECL_NODE, idx, lastDeclType);
    }

	AST checkFunction(Token token) {
		String functionName = token.getText();
		int line = token.getLine();
		int idx = ft.lookupFun(functionName);
		if (idx == -1) {
        	System.out.printf(
    			"SEMANTIC ERROR (%d): function '%s' was not declared.\n",
                line, functionName);
			System.exit(1);
            return null;
        }
		
		return new AST(NodeKind.FUN_USE_NODE, idx, ft.getType(idx));
	}

	AST newParameter(String functionName, Token token) {
		int idx = ft.lookupFun(functionName);
		ft.addParam(idx, lastDeclType);

		return newVar(token);
	}

	AST newFunction(Token token) {
		String functionName = token.getText();
    	int line = token.getLine();
   		int idx = ft.lookupFun(functionName);
        if (idx != -1) {
        	System.out.printf(
    			"SEMANTIC ERROR (%d): function '%s' already declared at line %d.\n",
                line, functionName, line);
			System.exit(1);
            return null;
        }
		idx = ft.addFun(functionName, line, this.lastDeclType);
		this.lastScope = ft.getScope(idx);

		AST functionNode = new AST(NodeKind.FUN_DECL_NODE, idx, lastDeclType);
		AST parameterListNode = AST.newSubtree(NodeKind.VAR_LIST_NODE, NO_TYPE, newVar(token));
		functionNode.addChild(parameterListNode);
		
		return functionNode;
    }

	// Catch a type error
	private void typeError(int lineNo, String op, Type t1, Type t2) {
		System.out.printf("SEMANTIC ERROR (%d): incompatible types for operator '%s', LHS is '%s' and RHS is '%s'.\n",
				lineNo, op, t1.toString(), t2.toString());
		System.exit(1);
	}

	// Catch a parameter type error
	private void paramTypeError(Token token, int i, Type expected, Type got) {
		String functionName = token.getText();
		int line = token.getLine();

		System.out.printf("SEMANTIC ERROR (%d): incompatible types for parameter %d of function '%s'. Expected %s but got %s.\n",
				line, i, functionName, expected.toString(), got.toString());
		System.exit(1);
	}

	private void paramQuantityError(Token token, int expected, int got) {
		String functionName = token.getText();
		int line = token.getLine();

		System.out.printf("SEMANTIC ERROR (%d): incompatible amount of parameters for function '%s'. Expected %d but got %d.\n",
				line, functionName, expected, got);
		System.exit(1);
	}


	private void checkBoolExpr(int lineNo, String cmd, Type t) {
        if (t != BOOL_TYPE) {
            System.out.printf("SEMANTIC ERROR (%d): conditional expression in '%s' is '%s' instead of '%s'.\n",
               lineNo, cmd, t.toString(), BOOL_TYPE.toString());
			System.exit(1);
        }
    }
    
    // Show literal, variable and function table contents.
    void printTables() {
        System.out.print("\n\n");
        System.out.print(st);
        System.out.print("\n\n");
    	System.out.print(vt);
    	System.out.print("\n\n");
		System.out.print(ft);
		System.out.print("\n\n");
    }

    void printAST() {
    	AST.printDot(root, vt, ft);
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
		
		if (ctx.functionDesignator() != null) {
			System.out.println("Factor");
			return visit(ctx.functionDesignator());
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
		AST leftNode = checkVar(token);
		AST rightNode = visit(ctx.expression());

		Type leftType = leftNode.type;
		Type rightType = rightNode.type;
		Unif unif = leftType.unifyAssign(rightType);

		if (unif.type == NO_TYPE) {
			typeError(token.getLine(), ":=", leftType, rightType);
		}

		leftNode = Conv.createConvNode(unif.lc, leftNode);
		rightNode = Conv.createConvNode(unif.rc, rightNode);

		return AST.newSubtree(NodeKind.ASSIGN_NODE, Type.NO_TYPE, leftNode, rightNode);
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

		AST statementNode = visit(ctx.statement());
		return AST.newSubtree(NodeKind.REPEAT_NODE, NO_TYPE, expressionNode, statementNode);
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
		AST node = AST.newSubtree(NodeKind.BLOCK_NODE, Type.NO_TYPE);
		List<StatementContext> statementsSections = ctx.statements().statement();
		for (int i = 0; i < statementsSections.size()-1; i++) {
			AST child = visit(statementsSections.get(i));
			node.addChild(child);
		}
		return node;
	}

	@Override
	public AST visitFunctionDesignator(pascalParser.FunctionDesignatorContext ctx) {

		Token token = ctx.identifier().IDENT().getSymbol();
		AST functionDesignatorNode = checkFunction(token);
		int idx = functionDesignatorNode.intData;

		int expectedParameters = ft.getParametersSize(idx);
		int gotParametes = ctx.parameterList().actualParameter().size();

		if (gotParametes != expectedParameters) {
			paramQuantityError(token, expectedParameters, gotParametes);
		}

		for (int i = 0; i < gotParametes; i++) {
			AST parameterNode = visit(ctx.parameterList().actualParameter(i));
			Type expected = ft.getType(idx);
			Type got = parameterNode.type;
			Unif unif = expected.unifyAssign(got);

			if (unif.type == NO_TYPE) {
				paramTypeError(token, i, expected, got);
			}

			parameterNode = Conv.createConvNode(unif.rc, parameterNode);
			functionDesignatorNode.addChild(parameterNode);
		}

		
		return functionDesignatorNode;
	}

	public AST readWriteCall(pascalParser.ProcedureStatementContext ctx) {
		AST readNode;
		
		if (ctx.identifier().IDENT().getText().equals("read")) {
			readNode = AST.newSubtree(NodeKind.READ_NODE, NO_TYPE);
		} else {
			readNode = AST.newSubtree(NodeKind.WRITE_NODE, NO_TYPE);
		}

		for (int i = 0; i < ctx.parameterList().actualParameter().size(); i++) {
			AST parameterNode = visit(ctx.parameterList().actualParameter(i));
			readNode.addChild(parameterNode);
		}
		
		return readNode;
	}

	public AST visitProcedureStatement(pascalParser.ProcedureStatementContext ctx) {

		Token token = ctx.identifier().IDENT().getSymbol();

		if (token.getText().equals("read") || token.getText().equals("write")) {
			return readWriteCall(ctx);
		}
		
		AST procedureStatementNode = checkFunction(token);
		int idx = procedureStatementNode.intData;

		int expectedParameters = ft.getParametersSize(idx);
		int gotParametes = ctx.parameterList().actualParameter().size();

		if (gotParametes != expectedParameters) {
			paramQuantityError(token, expectedParameters, gotParametes);
		}

		for (int i = 0; i < gotParametes; i++) {
			AST parameterNode = visit(ctx.parameterList().actualParameter(i));
			Type expected = ft.getType(procedureStatementNode.intData);
			Type got = parameterNode.type;
			Unif unif = expected.unifyAssign(got);

			if (unif.type == NO_TYPE) {
				paramTypeError(token, i, expected, got);
			}

			parameterNode = Conv.createConvNode(unif.rc, parameterNode);
			
			procedureStatementNode.addChild(parameterNode);
		}

		return procedureStatementNode;
	}

	@Override
	public AST visitFunctionDeclaration(pascalParser.FunctionDeclarationContext ctx) {

		visit(ctx.resultType());
		AST functionNode = newFunction(ctx.identifier().IDENT().getSymbol());

		AST parameterListNode = functionNode.getChild(0);
		if (ctx.formalParameterList() != null) {
			List<FormalParameterSectionContext> formalParameterList = ctx.formalParameterList().formalParameterSection();
			for (int i = 0; i < formalParameterList.size(); i++) {
				// All parameters in parameterGroup should have same type
				visit(formalParameterList.get(i).parameterGroup().typeIdentifier());
				
				List<IdentifierContext> identifiers = formalParameterList.get(i).parameterGroup().identifierList().identifier();
				
				// There can be more than one declaration per type
				for (int j = 0; j < identifiers.size(); j++) {
					Token token = identifiers.get(j).IDENT().getSymbol();
					parameterListNode.addChild(newParameter(ctx.identifier().IDENT().getText(), token));
				}
			}
		}

		AST blockNode = visit(ctx.block());
		AST variableListNode = blockNode.getChild(0);
		AST statementNode = blockNode.getChild(2);

		for (int i = 0; i < variableListNode.getChildrenSize(); i++) {
			parameterListNode.addChild(variableListNode.getChild(i));
		}
		functionNode.addChild(statementNode);

		return functionNode;
	}

	@Override
	public AST visitBlock(pascalParser.BlockContext ctx) {

		AST varsSectionNode = AST.newSubtree(NodeKind.VAR_LIST_NODE, Type.NO_TYPE);
		List<VariableDeclarationPartContext> varsSections= ctx.variableDeclarationPart();
		for (int i = 0; i < varsSections.size(); i++) {
			AST varsSubSection = visit(varsSections.get(i));
			for (int j = 0; j < varsSubSection.getChildrenSize(); j++) {
				varsSectionNode.addChild(varsSubSection.getChild(j));
			}
		}

		VarTable scope = lastScope;

		AST functionsSectionNode = AST.newSubtree(NodeKind.FUN_LIST_NODE, NO_TYPE);
		List<ProcedureAndFunctionDeclarationPartContext> functionsSectionList = ctx.procedureAndFunctionDeclarationPart();
		for (int i = 0; i < functionsSectionList.size(); i++) {
			AST functionNode = visit(functionsSectionList.get(i).procedureOrFunctionDeclaration().functionDeclaration());
			functionsSectionNode.addChild(functionNode);
		}

		lastScope = scope;

		AST statementsSectionNode = visit(ctx.compoundStatement());

		AST node = AST.newSubtree(NodeKind.BLOCK_NODE, Type.NO_TYPE, varsSectionNode, functionsSectionNode, statementsSectionNode);
		return node;
	}

	@Override
	public AST visitProgram(pascalParser.ProgramContext ctx) {
		this.lastScope = vt;
		AST blockNode = visit(ctx.block());
		this.root = AST.newSubtree(NodeKind.PROGRAM_NODE, NO_TYPE, blockNode.getChild(0), blockNode.getChild(1), blockNode.getChild(2));

		return this.root;
	}
	
}
