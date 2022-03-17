package code;

import ast.AST;
import ast.ASTBaseVisitor;
import tables.VariableTable;
import tables.StringTable;
import tables.FunctionTable;
import typing.Type;
import static typing.Type.INT_TYPE;

import java.util.ArrayList;

public final class CodeGen extends ASTBaseVisitor<Integer> {

	// private final Instruction code[]; // Code memory
	private final StringTable st;
	private final VariableTable gvt; // global

	// Próxima posição na memória de código para emit.
	private static int nextInstr;

	private static ArrayList<String> declares;

	private static int globalRegsCount;
	private static int localRegsCount;

	public CodeGen(StringTable stringTable, VariableTable variableTable) {
		// this.code = new Instruction[];
		this.st = stringTable;
		this.gvt = variableTable;
		declares = new ArrayList<String>();
	}

	public void execute(AST root) {
		nextInstr = 0;
		globalRegsCount = 0;
		localRegsCount = 1;
		dumpStringTable();
		visit(root);
		// emit(HALT);
		// dumpProgram();
	}

	// ----------------------------------------------------------------------------
	// Prints ---------------------------------------------------------------------

	// void dumpProgram() {
	// for (int addr = 0; addr < nextInstr; addr++) {
	// System.out.printf("%s\n", code[addr].toString());
	// }
	// }

	private void dumpStringTable() {
		for (int i = 0; i < st.size(); i++) {
			String s = st.getString(i);
			int x = newGlobalReg();
			System.out.printf("@%d = private constant [%d x i8] c\"%s\\00\"\n", x,
					s.length() + 1, s);
		}
	}

	// ----------------------------------------------------------------------------
	// Emits ----------------------------------------------------------------------

	// private void emit(OpCode op, int o1, int o2, int o3) {
	// Instruction instr = new Instruction(op, o1, o2, o3);
	// // Em um código para o produção deveria haver uma verificação aqui...
	// code[nextInstr] = instr;
	// nextInstr++;
	// }

	// private void emit(OpCode op) {
	// emit(op, 0, 0, 0);
	// }

	// private void emit(OpCode op, int o1) {
	// emit(op, o1, 0, 0);
	// }

	// private void emit(OpCode op, int o1, int o2) {
	// emit(op, o1, o2, 0);
	// }

	// private void backpatchJump(int instrAddr, int jumpAddr) {
	// code[instrAddr].o1 = jumpAddr;
	// }

	// private void backpatchBranch(int instrAddr, int offset) {
	// code[instrAddr].o2 = offset;
	// }

	// ----------------------------------------------------------------------------
	// AST Traversal --------------------------------------------------------------

	private int newGlobalReg() {
		return globalRegsCount++;
	}

	// É necessário mudar para múltiplas funções
	private int newLocalReg() {
		return localRegsCount++;
	}

	// Funcionamento dos visitadores abaixo deve ser razoavelmente explicativo
	// neste final do curso...

	@Override
	protected Integer visitAssign(AST node) {
		AST r = node.getChild(1);
		int x = visit(r);
		// int addr = node.getChild(0).intData;
		// Type varType = gvt.getType(addr);
		// if (varType == INT_TYPE) {
		// System.out.printf(" %%%d = alloca i32\n", addr);
		// }
		return -1;
	}

	// @Override
	// protected Integer visitEq(AST node) {
	// AST l = node.getChild(0);
	// AST r = node.getChild(1);
	// int y = visit(l);
	// int z = visit(r);
	// int x = newIntReg();
	// if (r.type == REAL_TYPE) { // Could equally test 'l' here.
	// emit(EQUf, x, y, z);
	// } else if (r.type == INT_TYPE) {
	// emit(EQUi, x, y, z);
	// } else { // Must be STR_TYPE
	// emit(EQUs, x, y, z);
	// }
	// return x;
	// }

	// @Override //TODO
	// protected Integer visitNeq(AST node) {
	// }

	@Override
	protected Integer visitBlock(AST node) {
		for (int i = 0; i < node.getChildrenSize(); i++) {
			visit(node.getChild(i));
		}
		return -1; // This is not an expression, hence no value to return.
	}

	// @Override
	// protected Integer visitBoolVal(AST node) {
	// int x = newIntReg();
	// int c = node.intData;
	// emit(LDIi, x, c);
	// return x;
	// }

	// @Override
	// protected Integer visitIf(AST node) {
	// // Code for test.
	// int testReg = visit(node.getChild(0));
	// int condJumpInstr = nextInstr;
	// emit(BOFb, testReg, 0); // Leave offset empty now, will be backpatched.

	// // Code for TRUE block.
	// int trueBranchStart = nextInstr;
	// visit(node.getChild(1)); // Generate TRUE block.

	// // Code for FALSE block.
	// int falseBranchStart;
	// if (node.getChildCount() == 3) { // We have an else.
	// // Emit unconditional jump for TRUE block.
	// int uncondJumpInstr = nextInstr;
	// emit(JUMP, 0); // Leave address empty now, will be backpatched.
	// falseBranchStart = nextInstr;
	// visit(node.getChild(2)); // Generate FALSE block.
	// // Backpatch unconditional jump at end of TRUE block.
	// backpatchJump(uncondJumpInstr, nextInstr);
	// } else {
	// falseBranchStart = nextInstr;
	// }

	// // Backpatch test.
	// backpatchBranch(condJumpInstr, falseBranchStart - trueBranchStart + 1);

	// return -1; // This is not an expression, hence no value to return.
	// }

	@Override
	protected Integer visitIntVal(AST node) {
		int x = newLocalReg();
		int c = node.intData;
		System.out.printf("  %%%d = alloca i32\n", x);
		System.out.printf("  store i32 %d, i32* %%%d\n", c, x);
		return x;
	}

	// @Override
	// protected Integer visitLt(AST node) {
	// AST l = node.getChild(0);
	// AST r = node.getChild(1);
	// int y = visit(l);
	// int z = visit(r);
	// int x = newIntReg();
	// if (r.type == REAL_TYPE) { // Could equally test 'l' here.
	// emit(LTHf, x, y, z);
	// } else if (r.type == INT_TYPE) {
	// emit(LTHi, x, y, z);
	// } else { // Must be STR_TYPE
	// emit(LTHs, x, y, z);
	// }
	// return x;
	// }

	// @Override //TODO
	// protected Integer visitGt(AST node) {
	// }

	// @Override //TODO
	// protected Integer visitGe(AST node) {
	// }

	// @Override //TODO
	// protected Integer visitLe(AST node) {
	// }

	// @Override //TODO
	// protected Integer visitAnd(AST node) {
	// }

	// @Override //TODO
	// protected Integer visitOr(AST node) {
	// }

	// @Override //TODO
	// protected Integer visitCharVal(AST node) {
	// }

	@Override // TODO
	protected Integer visitFunList(AST node) {
		// Not right now
		return -1;
	}

	// @Override //TODO
	// protected Integer visitFunDecl(AST node) {
	// }

	// @Override //TODO
	// protected Integer visitFunUse(AST node) {
	// }

	// @Override //TODO
	// protected Integer visitArrayAcc(AST node) {
	// }

	// @Override
	// protected Integer visitMinus(AST node) {
	// int x;
	// int y = visit(node.getChild(0));
	// int z = visit(node.getChild(1));
	// if (node.type == REAL_TYPE) {
	// x = newFloatReg();
	// emit(SUBf, x, y, z);
	// } else {
	// x = newIntReg();
	// emit(SUBi, x, y, z);
	// }
	// return x;
	// }

	// @Override
	// protected Integer visitOver(AST node) {
	// int x;
	// int y = visit(node.getChild(0));
	// int z = visit(node.getChild(1));
	// if (node.type == REAL_TYPE) {
	// x = newFloatReg();
	// emit(DIVf, x, y, z);
	// } else {
	// x = newIntReg();
	// emit(DIVi, x, y, z);
	// }
	// return x;
	// }

	// @Override
	// protected Integer visitPlus(AST node) {
	// int x;
	// int y = visit(node.getChild(0));
	// int z = visit(node.getChild(1));
	// if (node.type == REAL_TYPE) {
	// x = newFloatReg();
	// emit(ADDf, x, y, z);
	// } else if (node.type == INT_TYPE) {
	// x = newIntReg();
	// emit(ADDi, x, y, z);
	// } else if (node.type == BOOL_TYPE) {
	// x = newIntReg();
	// emit(OROR, x, y, z);
	// } else { // Must be STR_TYPE
	// x = newIntReg();
	// emit(CATs, x, y, z);
	// }
	// return x;
	// }

	@Override
	protected Integer visitProgram(AST node) {
		System.out.println("\ndefine void @main() {");
		visit(node.getChild(0)); // var_list
		visit(node.getChild(1)); // fun_list
		visit(node.getChild(2)); // block
		System.out.println("  ret void \n}");

		for (String declare : declares) {
			System.out.println(declare);
		}
		return -1; // This is not an expression, hence no value to return.
	}

	// @Override
	// protected Integer visitRead(AST node) {
	// AST var = node.getChild(0);
	// int addr = var.intData;
	// int x;
	// if (var.type == INT_TYPE) {
	// x = newIntReg();
	// emit(CALL, 0, x);
	// emit(STWi, addr, x);
	// } else if (var.type == REAL_TYPE) {
	// x = newFloatReg();
	// emit(CALL, 1, x);
	// emit(STWf, addr, x);
	// } else if (var.type == BOOL_TYPE) {
	// x = newIntReg();
	// emit(CALL, 2, x);
	// emit(STWi, addr, x);
	// } else { // Must be STR_TYPE
	// x = newIntReg();
	// emit(CALL, 3, x);
	// emit(STWi, addr, x);
	// }
	// return -1; // This is not an expression, hence no value to return.
	// }

	// @Override
	// protected Integer visitRealVal(AST node) {
	// int x = newFloatReg();
	// // We need to read as an int because the TM cannot handle floats directly.
	// // But we have a float stored in the AST, so we just convert it as an int
	// // and magically we have a float encoded as an int... :P
	// int c = Float.floatToIntBits(node.floatData);
	// emit(LDIf, x, c);
	// return x;
	// }

	// @Override
	// protected Integer visitRepeat(AST node) {
	// int beginRepeat = nextInstr;
	// visit(node.getChild(0)); // Emit code for body.
	// int testReg = visit(node.getChild(1)); // Emit code for test.
	// emit(BOFb, testReg, beginRepeat - nextInstr);
	// return -1; // This is not an expression, hence no value to return.
	// }

	@Override
	protected Integer visitStrVal(AST node) {
		int index = node.intData;
		return index;
	}

	// @Override
	// protected Integer visitTimes(AST node) {
	// int x;
	// int y = visit(node.getChild(0));
	// int z = visit(node.getChild(1));
	// if (node.type == REAL_TYPE) {
	// x = newFloatReg();
	// emit(MULf, x, y, z);
	// } else {
	// x = newIntReg();
	// emit(MULi, x, y, z);
	// }
	// return x;
	// }

	// @Override
	// protected Integer visitVarDecl(AST node) {
	// // Nothing to do here.
	// return -1; // This is not an expression, hence no value to return.
	// }

	@Override
	protected Integer visitVarList(AST node) {
		return -1;
	}

	// @Override
	// protected Integer visitVarUse(AST node) {
	// int addr = node.intData;
	// int x;
	// if (node.type == REAL_TYPE) {
	// x = newFloatReg();
	// emit(LDWf, x, addr);
	// } else {
	// x = newIntReg();
	// emit(LDWi, x, addr);
	// }
	// return x;
	// }

	@Override
	protected Integer visitWrite(AST node) {
		int pointer = newLocalReg();
		int result = newLocalReg();

		// É necessário checar se a declaração já existe (hashmap ou lista)
		declares.add("declare i32 @printf(i8*, ...)");

		AST expr = node.getChild(0);
		int x = visit(expr);

		switch (expr.type) {
			// case INT_TYPE:
			// emit(CALL, 4, x);
			// break;
			// case REAL_TYPE:
			// emit(CALL, 5, x);
			// break;
			// case BOOL_TYPE:
			// emit(CALL, 6, x);
			// break;
			case STR_TYPE:
				String s = st.getString(x);
				int len = s.length() + 1;
				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* @%d, i64 0, i64 0\n", pointer,
						len, len, x);
				System.out.printf("  %%%d = call i32 (i8*, ...) @printf(i8* %%%d)\n", result, pointer);
				break;
			case NO_TYPE:
			default:
				System.err.printf("Invalid type: %s!\n", expr.type.toString());
				System.exit(1);
		}

		return -1; // This is not an expression, hence no value to return.
	}

	// @Override //TODO
	// protected Integer visitC2S(AST node) {
	// }

	// @Override //TODO
	// protected Integer visitS2C(AST node) {
	// }

	// @Override
	// protected Integer visitB2I(AST node) {
	// int x = visit(node.getChild(0));
	// // Nothing else to do, a bool already is stored as an int.
	// return x;
	// }

	// @Override
	// protected Integer visitB2R(AST node) {
	// int i = visit(node.getChild(0));
	// int r = newFloatReg();
	// emit(WIDf, r, i);
	// return r;
	// }

	// @Override
	// protected Integer visitB2S(AST node) {
	// int x = newIntReg();
	// int y = visit(node.getChild(0));
	// emit(B2Ss, x, y);
	// return x;
	// }

	// @Override
	// protected Integer visitI2R(AST node) {
	// int i = visit(node.getChild(0));
	// int r = newFloatReg();
	// emit(WIDf, r, i);
	// return r;
	// }

	// @Override
	// protected Integer visitI2S(AST node) {
	// int x = newIntReg();
	// int y = visit(node.getChild(0));
	// emit(I2Ss, x, y);
	// return x;
	// }

	// @Override
	// protected Integer visitR2S(AST node) {
	// int x = newIntReg();
	// int y = visit(node.getChild(0));
	// emit(R2Ss, x, y);
	// return x;
	// }

}
