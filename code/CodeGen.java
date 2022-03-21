package code;

import ast.AST;
import ast.ASTBaseVisitor;
import tables.VariableTable;
import tables.StringTable;
import tables.FunctionTable;
import typing.Type;
import static typing.Type.INT_TYPE;
import static typing.Type.REAL_TYPE;
import static typing.Type.STR_TYPE;
import static typing.Type.BOOL_TYPE;
import static typing.Type.CHAR_TYPE;
import static typing.Type.ARRAY_TYPE;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

import org.antlr.v4.parse.ANTLRParser.sync_return;

// Declarar o tipo como string permite que nós retornem valores diversos, 
// a serem tratados dentro dos pais
// Os valores de ponto flutuante foram tratados como double devido a 
// peculiaridades da LLVM (ex não aceitar o literal float 1.3)
public final class CodeGen extends ASTBaseVisitor<String> {

	// private final Instruction code[]; // Code memory
	private final StringTable st;
	private final VariableTable gvt; // global

	private static ArrayList<String> declares;
	private static HashMap<Print, Print> printStrs;

	private static StringBuilder sb;
	private static Formatter strs;

	private static int globalRegsCount;
	private static int localRegsCount;
	private static int jumpLabel;

	public CodeGen(StringTable stringTable, VariableTable variableTable) {
		this.st = stringTable;
		this.gvt = variableTable;
		declares = new ArrayList<String>();
		printStrs = new HashMap<Print, Print>();
		sb = new StringBuilder();
		strs = new Formatter(sb);
	}

	public void execute(AST root) {
		globalRegsCount = 0;
		localRegsCount = 1;
		visit(root);
		// dumpProgram();
	}

	// ----------------------------------------------------------------------------
	// Prints ---------------------------------------------------------------------

	private void getStringTable() {
		for (int i = 0; i < st.size(); i++) {
			String s = st.getString(i);
			int x = newGlobalReg();
			strs.format("@%d = private constant [%d x i8] c\"%s\\00\"\n", x,
					s.length() + 1, s);
		}

	}

	private void getPrintStrings() {
		ArrayList<Print> a = new ArrayList<Print>(printStrs.values());
		PrintComparator pc = new PrintComparator();
		a.sort(pc);
		for (Print ele : a) {
			strs.format("%s", ele);
		}
	}

	private void dumpStrings() {
		System.out.println(sb.toString());
	}

	private void dumpFuncDeclare() {
		for (String declare : declares) {
			System.out.println(declare);
		}
	}

	// ----------------------------------------------------------------------------
	// Emits ----------------------------------------------------------------------

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

	private int newJumpLabel() {
		return jumpLabel++;
	}

	@Override
	protected String visitProgram(AST node) {
		getStringTable();

		System.out.println("\ndefine void @main() {");
		visit(node.getChild(0)); // var_list
		visit(node.getChild(1)); // fun_list
		visit(node.getChild(2)); // block
		System.out.println("  ret void \n}");

		getPrintStrings();
		dumpStrings();

		dumpFuncDeclare();

		return "";
	}

	@Override
	protected String visitBlock(AST node) {
		for (int i = 0; i < node.getChildrenSize(); i++) {
			visit(node.getChild(i));
		}
		return ""; // This is not an expression, hence no value to return.
	}

	@Override
	protected String visitAssign(AST node) {
		int addr = node.getChild(0).intData;
		AST r = node.getChild(1);
		String x = visit(r);
		Type varType = gvt.getType(addr);

		if (varType == INT_TYPE) {
			System.out.printf("  store i32 %s, i32* %%%d\n", x, addr + 1);

		} else if (varType == REAL_TYPE) {
			System.out.printf("  store double %s, double* %%%d\n", x, addr + 1);

		} else if (varType == BOOL_TYPE) {
			System.out.printf("  store i1 %s, i1* %%%d\n", x, addr + 1);

		} else if (varType == CHAR_TYPE) {
			System.out.printf("  store i8 %s, i8* %%%d\n", x, addr + 1);

		} else if (varType == STR_TYPE) {
			// A string pode ser pura (@str) ou de um registrador (%num)
			if (x.startsWith("@")) {
				int pointer = newLocalReg();
				String s = st.getString(Integer.parseInt(x.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", pointer,
						len, len, x);

				System.out.printf(
						"  store i8* %%%d, i8** %%%d\n", pointer, addr + 1);
			} else {
				System.out.printf("  store i8* %s, i8** %%%d\n", x, addr + 1);

			}

		} else if (varType == ARRAY_TYPE) {
			System.out.printf("Assign ARRAY_TYPE\n");

		} else {
			System.err.println("Assign type not known!");
		}
		return "";
	}

	@Override
	protected String visitEq(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String y = visit(l);
		String z = visit(r);
		int x = newLocalReg();

		if (r.type == INT_TYPE) {
			System.out.printf("  %%%d = icmp eq i32 %s, %s\n", x, y, z);

		} else if (r.type == REAL_TYPE) {
			System.out.printf("  %%%d = fcmp oeq double %s, %s\n", x, y, z);

		} else if (r.type == BOOL_TYPE) {
			System.out.printf("  %%%d = icmp eq i1 %s, %s\n", x, y, z);

		} else if (r.type == STR_TYPE) {
			System.out.println("Equal STR_TYPE\n");

		} else {
			System.err.println("Equal type not known!");
		}

		return String.format("%%%d", x);
	}

	@Override
	protected String visitNeq(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String y = visit(l);
		String z = visit(r);
		int x = newLocalReg();

		if (r.type == INT_TYPE) {
			System.out.printf("  %%%d = icmp ne i32 %s, %s\n", x, y, z);

		} else if (r.type == REAL_TYPE) {
			System.out.printf("  %%%d = fcmp one double %s, %s\n", x, y, z);

		} else if (r.type == BOOL_TYPE) {
			System.out.printf("  %%%d = icmp ne i1 %s, %s\n", x, y, z);

		} else if (r.type == STR_TYPE) {
			System.out.println("Nequal STR_TYPE\n");

		} else {
			System.err.println("Nequal type not known!");
		}

		return String.format("%%%d", x);
	}

	@Override
	protected String visitLt(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String y = visit(l);
		String z = visit(r);
		int x = 0;

		if (r.type == INT_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = icmp slt i32 %s, %s\n", x, y, z);

		} else if (r.type == REAL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = fcmp olt double %s, %s\n", x, y, z);

		} else if (r.type == BOOL_TYPE) {
			int convY = newLocalReg();
			int convZ = newLocalReg();
			x = newLocalReg();
			System.out.printf("  %%%d = zext i1 %s to i32\n", convY, y);
			System.out.printf("  %%%d = zext i1 %s to i32\n", convZ, z);
			System.out.printf("  %%%d = icmp slt i32 %%%d, %%%d\n", x, convY, convZ);

		} else if (r.type == STR_TYPE) {
			System.out.println("Nequal STR_TYPE\n");

		} else {
			System.err.println("Nequal type not known!");
		}

		return String.format("%%%d", x);
	}

	@Override
	protected String visitGt(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String y = visit(l);
		String z = visit(r);
		int x = 0;

		if (r.type == INT_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = icmp sgt i32 %s, %s\n", x, y, z);

		} else if (r.type == REAL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = fcmp ogt double %s, %s\n", x, y, z);

		} else if (r.type == BOOL_TYPE) {
			int convY = newLocalReg();
			int convZ = newLocalReg();
			x = newLocalReg();
			System.out.printf("  %%%d = zext i1 %s to i32\n", convY, y);
			System.out.printf("  %%%d = zext i1 %s to i32\n", convZ, z);
			System.out.printf("  %%%d = icmp sgt i32 %%%d, %%%d\n", x, convY, convZ);

		} else if (r.type == STR_TYPE) {
			System.out.println("Nequal STR_TYPE\n");

		} else {
			System.err.println("Nequal type not known!");
		}

		return String.format("%%%d", x);
	}

	@Override
	protected String visitGe(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String y = visit(l);
		String z = visit(r);
		int x = 0;

		if (r.type == INT_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = icmp sge i32 %s, %s\n", x, y, z);

		} else if (r.type == REAL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = fcmp oge double %s, %s\n", x, y, z);

		} else if (r.type == BOOL_TYPE) {
			int convY = newLocalReg();
			int convZ = newLocalReg();
			x = newLocalReg();
			System.out.printf("  %%%d = zext i1 %s to i32\n", convY, y);
			System.out.printf("  %%%d = zext i1 %s to i32\n", convZ, z);
			System.out.printf("  %%%d = icmp sge i32 %%%d, %%%d\n", x, convY, convZ);

		} else if (r.type == STR_TYPE) {
			System.out.println("Nequal STR_TYPE\n");

		} else {
			System.err.println("Nequal type not known!");
		}

		return String.format("%%%d", x);
	}

	@Override
	protected String visitLe(AST node) {
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String y = visit(l);
		String z = visit(r);
		int x = 0;

		if (r.type == INT_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = icmp sle i32 %s, %s\n", x, y, z);

		} else if (r.type == REAL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = fcmp ole double %s, %s\n", x, y, z);

		} else if (r.type == BOOL_TYPE) {
			int convY = newLocalReg();
			int convZ = newLocalReg();
			x = newLocalReg();
			System.out.printf("  %%%d = zext i1 %s to i32\n", convY, y);
			System.out.printf("  %%%d = zext i1 %s to i32\n", convZ, z);
			System.out.printf("  %%%d = icmp sle i32 %%%d, %%%d\n", x, convY, convZ);

		} else if (r.type == STR_TYPE) {
			System.out.println("Nequal STR_TYPE\n");

		} else {
			System.err.println("Nequal type not known!");
		}

		return String.format("%%%d", x);
	}

	@Override
	protected String visitAnd(AST node) {
		// Abstraindo a possibilidade de ser INT
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String y = visit(l);
		String z = visit(r);
		int x = newLocalReg();

		System.out.printf("  %%%d = and i1 %s, %s\n", x, y, z);

		return String.format("%%%d", x);
	}

	@Override
	protected String visitOr(AST node) {
		// Abstraindo a possibilidade de ser INT
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String y = visit(l);
		String z = visit(r);
		int x = newLocalReg();

		System.out.printf("  %%%d = or i1 %s, %s\n", x, y, z);

		return String.format("%%%d", x);
	}

	// @Override
	// protected String visitIf(AST node) {
	// // O child 0 é o teste
	// // O child 1 é o if
	// // O child 2 é o else

	// String testReg = visit(node.getChild(0));
	// int ifTrue = newJumpLabel();
	// StringBuilder sb = new StringBuilder();
	// Formatter f = new Formatter(sb);
	// f.format(" br i1 %s, label %%jmp%d, label ", testReg, ifTrue);
	// // System.out.printf(" br i1 %s, label %%%d, label %7\n")
	// // System.out.printf(" br i1 %s, label %%%d, label %7\n", testReg, ifTrue);

	// System.out.printf("\n%d:\n", ifTrue);
	// // int condJumpInstr = nextInstr;
	// // emit(BOFb, testReg, 0); // Leave offset empty now, will be backpatched.

	// // // Code for TRUE block.
	// // int trueBranchStart = nextInstr;
	// visit(node.getChild(1)); // Generate TRUE block.
	// int ifFalse = newLocalReg();
	// f.format("%%%d", ifFalse);
	// System.out.println(f.toString());
	// // // Code for FALSE block.
	// // int falseBranchStart;
	// // if (node.getChildCount() == 3) { // We have an else.
	// // // Emit unconditional jump for TRUE block.
	// // int uncondJumpInstr = nextInstr;
	// // emit(JUMP, 0); // Leave address empty now, will be backpatched.
	// // falseBranchStart = nextInstr;
	// // visit(node.getChild(2)); // Generate FALSE block.
	// // // Backpatch unconditional jump at end of TRUE block.
	// // backpatchJump(uncondJumpInstr, nextInstr);
	// // } else {
	// // falseBranchStart = nextInstr;
	// // }

	// // // Backpatch test.
	// // backpatchBranch(condJumpInstr, falseBranchStart - trueBranchStart + 1);

	// return "";
	// }

	@Override
	protected String visitIntVal(AST node) {
		String val = Integer.toString(node.intData);

		return val;
	}

	@Override
	protected String visitRealVal(AST node) {
		String val = Float.toString(node.floatData);

		return val;
	}

	@Override
	protected String visitStrVal(AST node) {
		int index = node.intData;

		return String.format("@%d", index);
	}

	@Override
	protected String visitCharVal(AST node) {
		String val = Integer.toString(node.intData);

		return val;
	}

	@Override
	protected String visitBoolVal(AST node) {
		boolean b;
		if (node.intData == 0) {
			b = false;
		} else {
			b = true;
		}
		String val = Boolean.toString(b);
		return val;
	}

	@Override // TODO
	protected String visitFunList(AST node) {
		// Not right now
		return "";
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

	@Override
	protected String visitMinus(AST node) {
		String y = visit(node.getChild(0));
		String z = visit(node.getChild(1));
		int x = newLocalReg();

		if (node.type == INT_TYPE) {
			System.out.printf("  %%%d = sub i32 %s, %s\n", x, y, z);

		} else if (node.type == REAL_TYPE) {
			System.out.printf("  %%%d = fsub double %s, %s\n", x, y, z);

		} else {
			System.err.println("This type is impossible to sub");
		}

		return String.format("%%%d", x);
	}

	// @Override
	protected String visitOver(AST node) {
		String y = visit(node.getChild(0));
		String z = visit(node.getChild(1));
		int x = newLocalReg();

		if (node.type == INT_TYPE) {
			System.out.printf("  %%%d = sdiv i32 %s, %s\n", x, y, z);

		} else if (node.type == REAL_TYPE) {
			System.out.printf("  %%%d = fdiv double %s, %s\n", x, y, z);

		} else {
			System.err.println("This type is impossible to divide");
		}

		return String.format("%%%d", x);
	}

	@Override
	protected String visitPlus(AST node) {
		String y = visit(node.getChild(0));
		String z = visit(node.getChild(1));
		int x = newLocalReg();

		if (node.type == INT_TYPE) {
			System.out.printf("  %%%d = add i32 %s, %s\n", x, y, z);
		} else if (node.type == REAL_TYPE) {
			System.out.printf("  %%%d = fadd double %s, %s\n", x, y, z);
		} else if (node.type == STR_TYPE) {
			// Concat
		} else if (node.type == CHAR_TYPE) {
			// Não sei se isso existe...
		} else {
			System.err.println("This type is impossible to add");
		}

		return String.format("%%%d", x);
	}

	// @Override
	protected String visitTimes(AST node) {
		String y = visit(node.getChild(0));
		String z = visit(node.getChild(1));
		int x = newLocalReg();

		if (node.type == INT_TYPE) {
			System.out.printf("  %%%d = mul i32 %s, %s\n", x, y, z);

		} else if (node.type == REAL_TYPE) {
			System.out.printf("  %%%d = fmul double %s, %s\n", x, y, z);

		} else {
			System.err.println("This type is impossible to mul");
		}

		return String.format("%%%d", x);
	}

	// Ignorar parametros no read (ex read(a,b) por enquanto
	@Override
	protected String visitRead(AST node) {
		String printPrototype = "declare i32 @__isoc99_scanf(i8*, ...)";

		if (!declares.contains(printPrototype))
			declares.add(printPrototype);

		AST var = node.getChild(0);
		int addr = var.intData;
		int x = 0;

		if (var.type == INT_TYPE) {
			if (!printStrs.containsKey(Print.INT)) {
				int i = newGlobalReg();
				Print p = Print.INT.setIndex(i);
				printStrs.put(Print.INT, p);
			}
			int a = printStrs.get(Print.INT).index;
			int pointer = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = getelementptr inbounds [3 x i8], [3 x i8]* @%d, i64 0, i64 0\n", pointer, a);
			System.out.printf("  %%%d = call i32 (i8*, ...) @__isoc99_scanf(i8* %%%d, i32* %%%d)\n", x, pointer,
					addr + 1);

		} else if (var.type == REAL_TYPE) {
			if (!printStrs.containsKey(Print.REAL)) {
				int i = newGlobalReg();
				Print p = Print.REAL.setIndex(i);
				printStrs.put(Print.REAL, p);
			}
			int a = printStrs.get(Print.REAL).index;
			int pointer = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = getelementptr inbounds [4 x i8], [4 x i8]* @%d, i64 0, i64 0\n", pointer, a);
			System.out.printf("  %%%d = call i32 (i8*, ...) @__isoc99_scanf(i8* %%%d, double* %%%d)\n", x, pointer,
					addr + 1);

		} else if (var.type == CHAR_TYPE) {
			if (!printStrs.containsKey(Print.CHAR)) {
				int i = newGlobalReg();
				Print p = Print.CHAR.setIndex(i);
				printStrs.put(Print.CHAR, p);
			}
			int a = printStrs.get(Print.CHAR).index;
			int pointer = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = getelementptr inbounds [3 x i8], [3 x i8]* @%d, i64 0, i64 0\n", pointer, a);
			System.out.printf("  %%%d = call i32 (i8*, ...) @__isoc99_scanf(i8* %%%d, i8* %%%d)\n", x, pointer,
					addr + 1);
		} else if (var.type == STR_TYPE) {
			// Segfault
		} else {
			System.err.printf("This type is impossible to read: %s\n", var.type);
		}

		return "";
	}

	// @Override
	// protected Integer visitRepeat(AST node) {
	// int beginRepeat = nextInstr;
	// visit(node.getChild(0)); // Emit code for body.
	// int testReg = visit(node.getChild(1)); // Emit code for test.
	// emit(BOFb, testReg, beginRepeat - nextInstr);
	// return -1; // This is not an expression, hence no value to return.
	// }

	@Override
	protected String visitVarDecl(AST node) {
		int x = newLocalReg();
		if (node.type == INT_TYPE) {
			System.out.printf("  %%%d = alloca i32\n", x);

		} else if (node.type == REAL_TYPE) {
			System.out.printf("  %%%d = alloca double\n", x);

		} else if (node.type == BOOL_TYPE) {
			System.out.printf("  %%%d = alloca i1\n", x);

		} else if (node.type == CHAR_TYPE) {
			System.out.printf("  %%%d = alloca i8\n", x);

		} else if (node.type == STR_TYPE) {
			System.out.printf("  %%%d = alloca i8*\n", x);

		} else {
			System.err.println("Missing VarDecl!");
		}
		return "";
	}

	@Override
	protected String visitVarList(AST node) {
		for (int i = 0; i < node.getChildrenSize(); i++) {
			visit(node.getChild(i));
		}
		return "";
	}

	@Override
	protected String visitVarUse(AST node) {
		int x = newLocalReg();
		int addr = node.intData;
		if (node.type == INT_TYPE) {
			System.out.printf("  %%%d = load i32, i32* %%%d\n", x, addr + 1);

		} else if (node.type == REAL_TYPE) {
			System.out.printf("  %%%d = load double, double* %%%d\n", x, addr + 1);

		} else if (node.type == BOOL_TYPE) {
			System.out.printf("  %%%d = load i1, i1* %%%d\n", x, addr + 1);

		} else if (node.type == CHAR_TYPE) {
			System.out.printf("  %%%d = load i8, i8* %%%d\n", x, addr + 1);

		} else if (node.type == STR_TYPE) {
			System.out.printf("  %%%d = load i8*, i8** %%%d\n", x, addr + 1);

		} else {
			System.err.println("Missing VarUse!");
		}

		return String.format("%%%d", x);
	}

	// Ignorar parametros no write (ex write(1,2, 'Hey')) por enquanto
	@Override
	protected String visitWrite(AST node) {
		String printPrototype = "declare i32 @printf(i8*, ...)";

		if (!declares.contains(printPrototype))
			declares.add(printPrototype);

		AST expr = node.getChild(0);
		String x = visit(expr);

		if (expr.type == STR_TYPE) {
			int pointer = newLocalReg();
			int result = newLocalReg();

			// O parâmetro string do write pode vir de uma string pura (@)
			// ou de um registrador (%).
			// Sendo de um registrador não tem como saber o tamanho dela
			if (x.startsWith("@")) {
				// Pega a string salva para obter o tamanho dela
				String s = st.getString(Integer.parseInt(x.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n",
						pointer,
						len, len, x);
				System.out.printf("  %%%d = call i32 (i8*, ...) @printf(i8* %%%d)\n", result,
						pointer);
			} else {
				if (!printStrs.containsKey(Print.STR)) {
					int i = newGlobalReg();
					Print p = Print.STR.setIndex(i);
					printStrs.put(Print.STR, p);
				}
				int a = printStrs.get(Print.STR).index;

				System.out.printf("  %%%d = getelementptr inbounds [3 x i8], [3 x i8]* @%d, i64 0, i64 0\n",
						pointer,
						a);
				System.out.printf("  %%%d = call i32 (i8*, ...) @printf(i8* %%%d, i8* %s)\n", result, pointer, x);
			}

		} else if (expr.type == REAL_TYPE) {
			int pointer = newLocalReg();
			int result = newLocalReg();

			// Caso ainda não haja uma string de impressão de reais,
			// ("%f"), adiciona ela
			if (!printStrs.containsKey(Print.REAL)) {
				int i = newGlobalReg();
				Print p = Print.REAL.setIndex(i);
				printStrs.put(Print.REAL, p);
			}
			int a = printStrs.get(Print.REAL).index;

			System.out.printf("  %%%d = getelementptr inbounds [4 x i8], [4 x i8]* @%d, i64 0, i64 0\n", pointer,
					a);
			System.out.printf("  %%%d = call i32 (i8*, ...) @printf(i8* %%%d, double %s)\n", result, pointer, x);

		} else if (expr.type == INT_TYPE) {
			int pointer = newLocalReg();
			int result = newLocalReg();

			if (!printStrs.containsKey(Print.INT)) {
				int i = newGlobalReg();
				Print p = Print.INT.setIndex(i);
				printStrs.put(Print.INT, p);
			}
			int a = printStrs.get(Print.INT).index;

			System.out.printf("  %%%d = getelementptr inbounds [3 x i8], [3 x i8]* @%d, i64 0, i64 0\n", pointer,
					a);
			System.out.printf("  %%%d = call i32 (i8*, ...) @printf(i8* %%%d, i32 %s)\n", result, pointer, x);

		} else if (expr.type == BOOL_TYPE) {
			int pointer = newLocalReg();
			int result = newLocalReg();

			// Vamos imprimir como um inteiro, já que o printf não tem bool
			if (!printStrs.containsKey(Print.INT)) {
				int i = newGlobalReg();
				Print p = Print.INT.setIndex(i);
				printStrs.put(Print.INT, p);
			}
			int a = printStrs.get(Print.INT).index;

			System.out.printf("  %%%d = getelementptr inbounds [3 x i8], [3 x i8]* @%d, i64 0, i64 0\n", pointer,
					a);
			System.out.printf("  %%%d = call i32 (i8*, ...) @printf(i8* %%%d, i1 %s)\n", result, pointer, x);

		} else if (expr.type == CHAR_TYPE) {
			int pointer = newLocalReg();
			int result = newLocalReg();

			if (!printStrs.containsKey(Print.CHAR)) {
				int i = newGlobalReg();
				Print p = Print.CHAR.setIndex(i);
				printStrs.put(Print.CHAR, p);
			}
			int a = printStrs.get(Print.CHAR).index;

			System.out.printf("  %%%d = getelementptr inbounds [3 x i8], [3 x i8]* @%d, i64 0, i64 0\n", pointer,
					a);
			System.out.printf("  %%%d = call i32 (i8*, ...) @printf(i8* %%%d, i8 %s)\n", result, pointer, x);

		} else {
			System.err.printf("Invalid type: %s!\n", expr.type.toString());
			System.exit(1);
		}

		return "";
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

	@Override
	protected String visitI2R(AST node) {
		String i = visit(node.getChild(0));
		int r = newLocalReg();
		System.out.printf("  %%%d = sitofp i32 %s to double\n", r, i);

		return String.format("%%%d", r);
	}

	// @Override
	// protected String visitI2S(AST node) {
	// String i = visit(node.getChild(0));
	// int r = newLocalReg();
	// System.out.printf(" %%%d = sitofp i32 %s to double\n", r, i);

	// return String.format("%%%d", r);
	// }

	// @Override
	// protected Integer visitR2S(AST node) {
	// int x = newIntReg();
	// int y = visit(node.getChild(0));
	// emit(R2Ss, x, y);
	// return x;
	// }

}
