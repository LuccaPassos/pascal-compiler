package ast;

public abstract class ASTBaseVisitor<T> {

	public void execute(AST root) {
		visit(root);
	}

	protected T visit(AST node) {
		// System.out.println(">>> node start");
		// System.out.println(node.type);
		// System.out.println(node.intData);
		// System.out.println(node.kind);
		// System.out.println(">>> node end");
		switch (node.kind) {
			case ASSIGN_NODE:
				return visitAssign(node);
			case EQ_NODE:
				return visitEq(node);
			case NEQ_NODE:
				return visitNeq(node);
			case BLOCK_NODE:
				return visitBlock(node);
			case BOOL_VAL_NODE:
				return visitBoolVal(node);
			// case IF_NODE:
			// return visitIf(node);
			case INT_VAL_NODE:
				return visitIntVal(node);
			case LT_NODE:
				return visitLt(node);
			case GT_NODE:
				return visitGt(node);
			case GE_NODE:
				return visitGe(node);
			case LE_NODE:
				return visitLe(node);
			case AND_NODE:
				return visitAnd(node);
			case OR_NODE:
				return visitOr(node);
			case CHAR_VAL_NODE:
				return visitCharVal(node);
			case FUN_LIST_NODE:
				return visitFunList(node);
			// case FUN_DECL_NODE:
			// return visitFunDecl(node);
			// case FUN_USE_NODE:
			// return visitFunUse(node);
			// case ARRAY_ACCESS:
			// return visitArrayAcc(node);
			case MINUS_NODE:
				return visitMinus(node);
			case OVER_NODE:
				return visitOver(node);
			case PLUS_NODE:
				return visitPlus(node);
			case PROGRAM_NODE:
				return visitProgram(node);
			case READ_NODE:
				return visitRead(node);
			case REAL_VAL_NODE:
				return visitRealVal(node);
			// case REPEAT_NODE:
			// return visitRepeat(node);
			case STR_VAL_NODE:
				return visitStrVal(node);
			case TIMES_NODE:
				return visitTimes(node);
			case VAR_DECL_NODE:
				return visitVarDecl(node);
			case VAR_LIST_NODE:
				return visitVarList(node);
			case VAR_USE_NODE:
				return visitVarUse(node);
			case WRITE_NODE:
				return visitWrite(node);

			// case C2S_NODE:
			// return visitC2S(node);
			// case S2C_NODE:
			// return visitS2C(node);
			// case B2I_NODE:
			// return visitB2I(node);
			// case B2R_NODE:
			// return visitB2R(node);
			// case B2S_NODE:
			// return visitB2S(node);
			case I2R_NODE:
				return visitI2R(node);
			// case I2S_NODE:
			// return visitI2S(node);
			// case R2S_NODE:
			// return visitR2S(node);

			default:
				System.err.printf("Invalid kind: %s!\n", node.kind.toString());
				System.exit(1);
				return null;
		}
	}

	protected abstract T visitAssign(AST node);

	protected abstract T visitEq(AST node);

	protected abstract T visitNeq(AST node);

	protected abstract T visitBlock(AST node);

	protected abstract T visitBoolVal(AST node);

	// protected abstract T visitIf(AST node);

	protected abstract T visitIntVal(AST node);

	protected abstract T visitLt(AST node);

	protected abstract T visitGt(AST node);

	protected abstract T visitGe(AST node);

	protected abstract T visitLe(AST node);

	protected abstract T visitAnd(AST node);

	protected abstract T visitOr(AST node);

	protected abstract T visitCharVal(AST node);

	protected abstract T visitFunList(AST node);

	// protected abstract T visitFunDecl(AST node);

	// protected abstract T visitFunUse(AST node);

	// protected abstract T visitArrayAcc(AST node);

	protected abstract T visitMinus(AST node);

	protected abstract T visitOver(AST node);

	protected abstract T visitPlus(AST node);

	protected abstract T visitProgram(AST node);

	protected abstract T visitRead(AST node);

	protected abstract T visitRealVal(AST node);

	// protected abstract T visitRepeat(AST node);

	protected abstract T visitStrVal(AST node);

	protected abstract T visitTimes(AST node);

	protected abstract T visitVarDecl(AST node);

	protected abstract T visitVarList(AST node);

	protected abstract T visitVarUse(AST node);

	protected abstract T visitWrite(AST node);

	// protected abstract T visitB2I(AST node);

	// protected abstract T visitB2R(AST node);

	// protected abstract T visitB2S(AST node);

	protected abstract T visitI2R(AST node);

	// protected abstract T visitI2S(AST node);

	// protected abstract T visitR2S(AST node);

	// protected abstract T visitC2S(AST node);

	// protected abstract T visitS2C(AST node);

}