package code;

import ast.AST;
import ast.ASTBaseVisitor;
import ast.NodeKind;
import static ast.NodeKind.ARRAY_ACCESS;
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
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;

import org.antlr.runtime.tree.RewriteRuleNodeStream;

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

	private static HashMap<Integer, ArrayVar> arrayVarList;

	private static StringBuilder sb;
	private static Formatter strs;

	private static int globalRegsCount;
	private static int localRegsCount;
	private static int jumpLabel;

	private static String compPrototype = "declare i32 @strcmp(i8*, i8*)";
	private static String scanPrototype = "declare i32 @__isoc99_scanf(i8*, ...)";
	private static String printPrototype = "declare i32 @printf(i8*, ...)";
	// private static String strPrototype = "declare i8* @strcpy(i8*, i8*)\ndeclare
	// i8* @strcat(i8*, i8*)";

	public CodeGen(StringTable stringTable, VariableTable variableTable) {
		this.st = stringTable;
		this.gvt = variableTable;
		declares = new ArrayList<>();
		printStrs = new HashMap<>();
		sb = new StringBuilder();
		strs = new Formatter(sb);
		arrayVarList = new HashMap<>();
	}

	public void execute(AST root) {
		globalRegsCount = 0;
		localRegsCount = 1;
		visit(root);
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
	// AST Traversal --------------------------------------------------------------

	private int newGlobalReg() {
		return globalRegsCount++;
	}

	// É necessário mudar para múltiplas funções, com múltiplos escopos
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
		System.out.println("  ret void \n}\n");

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
		AST l = node.getChild(0);
		AST r = node.getChild(1);
		String x = visit(r);
		int addr = l.intData;
		Type varType = gvt.getType(addr);
		NodeKind nodeKind = l.kind;

		if (nodeKind == ARRAY_ACCESS) {
			// Ignores load from varUse
			int reg = Integer.parseInt(visit(l).substring(1));
			String type = ArrayVar.getSingleType(l.type);

			// Abstraindo atribuição de string a array
			System.out.printf("  store %s %s, %s* %%%d\n", type, x, type, reg - 1);

		} else if (varType == INT_TYPE) {
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
		int x = 0;

		if (r.type == INT_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = icmp eq i32 %s, %s\n", x, y, z);

		} else if (r.type == REAL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = fcmp oeq double %s, %s\n", x, y, z);

		} else if (r.type == BOOL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = icmp eq i1 %s, %s\n", x, y, z);

		} else if (r.type == STR_TYPE) {
			if (!declares.contains(compPrototype))
				declares.add(compPrototype);

			// Se as strings forem puras, precisa do ponteiro delas
			if (y.startsWith("@")) {
				int b = newLocalReg();
				String s = st.getString(Integer.parseInt(y.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", b, len,
						len, y);

				y = String.format("%%%d", b);
			}
			if (z.startsWith("@")) {
				int c = newLocalReg();
				String s = st.getString(Integer.parseInt(z.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", c, len,
						len, z);

				z = String.format("%%%d", c);
			}

			int a = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = call i32 @strcmp(i8* %s, i8* %s)\n", a, y, z);
			// Se o valor for = 0, zero as string são iguais
			System.out.printf("  %%%d = icmp eq i32 %%%d, 0\n", x, a);

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
		int x = 0;

		if (r.type == INT_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = icmp ne i32 %s, %s\n", x, y, z);

		} else if (r.type == REAL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = fcmp one double %s, %s\n", x, y, z);

		} else if (r.type == BOOL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = icmp ne i1 %s, %s\n", x, y, z);

		} else if (r.type == STR_TYPE) {
			if (!declares.contains(compPrototype))
				declares.add(compPrototype);

			// Se as strings forem puras, precisa do ponteiro delas
			if (y.startsWith("@")) {
				int b = newLocalReg();
				String s = st.getString(Integer.parseInt(y.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", b, len,
						len, y);

				y = String.format("%%%d", b);
			}
			if (z.startsWith("@")) {
				int c = newLocalReg();
				String s = st.getString(Integer.parseInt(z.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", c, len,
						len, z);

				z = String.format("%%%d", c);
			}

			int a = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = call i32 @strcmp(i8* %s, i8* %s)\n", a, y, z);
			// Se o valor for != 0, as string são diferentes
			System.out.printf("  %%%d = icmp ne i32 %%%d, 0\n", x, a);

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
			if (!declares.contains(compPrototype))
				declares.add(compPrototype);

			// Se as strings forem puras, precisa do ponteiro delas
			if (y.startsWith("@")) {
				int b = newLocalReg();
				String s = st.getString(Integer.parseInt(y.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", b, len,
						len, y);

				y = String.format("%%%d", b);
			}
			if (z.startsWith("@")) {
				int c = newLocalReg();
				String s = st.getString(Integer.parseInt(z.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", c, len,
						len, z);

				z = String.format("%%%d", c);
			}

			int a = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = call i32 @strcmp(i8* %s, i8* %s)\n", a, y, z);
			// Se o valor for < 0 as string y é antes de z
			System.out.printf("  %%%d = icmp slt i32 %%%d, 0\n", x, a);
		} else {
			System.err.println("Lt type not known!");
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
			if (!declares.contains(compPrototype))
				declares.add(compPrototype);

			// Se as strings forem puras, precisa do ponteiro delas
			if (y.startsWith("@")) {
				int b = newLocalReg();
				String s = st.getString(Integer.parseInt(y.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", b, len,
						len, y);

				y = String.format("%%%d", b);
			}
			if (z.startsWith("@")) {
				int c = newLocalReg();
				String s = st.getString(Integer.parseInt(z.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", c, len,
						len, z);

				z = String.format("%%%d", c);
			}

			int a = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = call i32 @strcmp(i8* %s, i8* %s)\n", a, y, z);
			// Se o valor for > 0 as string y é depois de z
			System.out.printf("  %%%d = icmp sgt i32 %%%d, 0\n", x, a);

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
			if (!declares.contains(compPrototype))
				declares.add(compPrototype);

			// Se as strings forem puras, precisa do ponteiro delas
			if (y.startsWith("@")) {
				int b = newLocalReg();
				String s = st.getString(Integer.parseInt(y.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", b, len,
						len, y);

				y = String.format("%%%d", b);
			}
			if (z.startsWith("@")) {
				int c = newLocalReg();
				String s = st.getString(Integer.parseInt(z.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", c, len,
						len, z);

				z = String.format("%%%d", c);
			}

			int a = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = call i32 @strcmp(i8* %s, i8* %s)\n", a, y, z);
			// Se o valor for > 0 as string y é depois de z
			System.out.printf("  %%%d = icmp sge i32 %%%d, 0\n", x, a);

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
			if (!declares.contains(compPrototype))
				declares.add(compPrototype);

			// Se as strings forem puras, precisa do ponteiro delas
			if (y.startsWith("@")) {
				int b = newLocalReg();
				String s = st.getString(Integer.parseInt(y.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", b, len,
						len, y);

				y = String.format("%%%d", b);
			}
			if (z.startsWith("@")) {
				int c = newLocalReg();
				String s = st.getString(Integer.parseInt(z.substring(1)));
				int len = s.length() + 1;

				System.out.printf("  %%%d = getelementptr inbounds [%d x i8], [%d x i8]* %s, i64 0, i64 0\n", c, len,
						len, z);

				z = String.format("%%%d", c);
			}

			int a = newLocalReg();
			x = newLocalReg();

			System.out.printf("  %%%d = call i32 @strcmp(i8* %s, i8* %s)\n", a, y, z);
			System.out.printf("  %%%d = icmp sle i32 %%%d, 0\n", x, a);

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

	@Override
	protected String visitIf(AST node) {
		String testReg = visit(node.getChild(0));
		boolean hasElse = node.getChildrenSize() == 3;
		int ifTrue = newJumpLabel();
		int ifFalse = 0;
		if (hasElse)
			ifFalse = newJumpLabel();

		int cont = newJumpLabel();

		String l1 = String.format("if.false.%d", ifFalse);
		String l2 = String.format("if.cont.%d", cont);

		System.out.printf("  br i1 %s, label %%if.true.%d, label %%%s\n", testReg, ifTrue, hasElse ? l1 : l2);

		System.out.printf("\nif.true.%d:\n", ifTrue);
		visit(node.getChild(1));
		System.out.printf("  br label %%if.cont.%d\n", cont);

		System.out.printf("\n%s:\n", hasElse ? l1 : l2);
		if (hasElse) {
			visit(node.getChild(2));
			System.out.printf("  br label %%if.cont.%d\n", cont);
			System.out.printf("\nif.cont.%d:\n", cont);
		}

		return "";
	}

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

	@Override
	protected String visitArrayAcc(AST node) {
		AST l = node.getChild(0);
		int addr = l.intData;

		// First iteration
		ArrayVar arr = arrayVarList.get(addr);
		String arrayType = arr.getInnerArrayType(0);
		String idx = visit(node.getChild(1));
		int reg = newLocalReg();

		System.out.printf("  %%%d = getelementptr inbounds %s, %s* %%%d, i32 0, i32 %s\n", reg, arrayType, arrayType,
				addr + 1, idx);

		for (int i = 1, j = 2; i < arr.getDimension(); i++, j++) {
			reg = newLocalReg();
			arrayType = arr.getInnerArrayType(i);
			idx = visit(node.getChild(j));
			System.out.printf("  %%%d = getelementptr inbounds %s, %s* %%%d, i32 0, i32 %s\n", reg, arrayType,
					arrayType, reg - 1, idx);
		}
		String loadReg = visit(l);
		System.out.printf("%%%d\n", reg);
		return loadReg;
	}

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

	@Override
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
		int x = 0;

		if (node.type == INT_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = add i32 %s, %s\n", x, y, z);
		} else if (node.type == REAL_TYPE) {
			x = newLocalReg();
			System.out.printf("  %%%d = fadd double %s, %s\n", x, y, z);
		} else if (node.type == STR_TYPE) {
			// if (!declares.contains(strPrototype))
			// declares.add(strPrototype);

			// int a = newLocalReg();
			// int b = newLocalReg();
			// int c = newLocalReg();
			// x = newLocalReg();
			// System.out.printf(" %%%d = alloca [100 x i8]\n", a);
			// System.out.printf(" %%%d = getelementptr inbounds [100 x i8], [100 x i8]*
			// %%%d, i64 0, i64 0\n", b, a);

			// System.out.printf(" %%%d = call i8* @strcpy(i8* %%%d, i8* %s)\n", c, b, y);
			// System.out.printf(" %%%d = call i8* @strcat(i8* %%%d, i8* %s)\n", x, b, z);
		} else {
			System.err.println("This type is impossible to add");
		}

		return String.format("%%%d", x);
	}

	@Override
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

	@Override
	protected String visitRead(AST node) {
		if (!declares.contains(scanPrototype))
			declares.add(scanPrototype);

		for (int k = 0; k < node.getChildrenSize(); k++) {
			AST var = node.getChild(k);
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

				System.out.printf("  %%%d = getelementptr inbounds [3 x i8], [3 x i8]* @%d, i64 0, i64 0\n", pointer,
						a);
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

				System.out.printf("  %%%d = getelementptr inbounds [4 x i8], [4 x i8]* @%d, i64 0, i64 0\n", pointer,
						a);
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

				System.out.printf("  %%%d = getelementptr inbounds [3 x i8], [3 x i8]* @%d, i64 0, i64 0\n", pointer,
						a);
				System.out.printf("  %%%d = call i32 (i8*, ...) @__isoc99_scanf(i8* %%%d, i8* %%%d)\n", x, pointer,
						addr + 1);
			} else if (var.type == STR_TYPE) {
				// Segfault
			} else {
				System.err.printf("This type is impossible to read: %s\n", var.type);
			}
		}
		return "";
	}

	@Override
	protected String visitRepeat(AST node) {
		int test = newJumpLabel();
		int repeat = newJumpLabel();
		int cont = newJumpLabel();
		System.out.printf("  br label %%while.test.%d\n", test);

		System.out.printf("\nwhile.test.%d:\n", test);
		String testReg = visit(node.getChild(0));
		System.out.printf("  br i1 %s, label %%while.repeat.%d, label %%while.cont.%d\n", testReg, repeat, cont);

		System.out.printf("\nwhile.repeat.%d:\n", repeat);
		visit(node.getChild(1));
		System.out.printf("  br label %%while.test.%d\n", test);

		System.out.printf("\nwhile.cont.%d:\n", cont);
		return "";
	}

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

		} else if (node.type == ARRAY_TYPE) {
			int idx = node.intData;
			Type contentType = gvt.getContentType(idx);
			ArrayList<Integer[]> arrRanges = gvt.getRanges(idx);

			StringBuilder sb = new StringBuilder();
			Formatter formatter = new Formatter(sb);

			formatter.format("  %%%d = alloca ", x);

			ArrayVar arr = new ArrayVar(contentType, arrRanges.size());
			for (Integer[] range : arrRanges) {
				arr.addLength((range[1] - range[0]));
			}
			arrayVarList.put(idx, arr);

			formatter.format("%s", arrayVarList.get(idx).getInnerArrayType(0));

			System.out.println(formatter.toString());
			formatter.close();
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

		} else if (node.type == ARRAY_TYPE) {
			Type contentType = gvt.getContentType(addr);
			String type = ArrayVar.getSingleType(contentType);
			System.out.printf("  %%%d = load %s, %s* ", x, type, type);

		} else {
			System.err.println("Missing VarUse!");
		}

		return String.format("%%%d", x);
	}

	@Override
	protected String visitWrite(AST node) {
		if (!declares.contains(printPrototype))
			declares.add(printPrototype);

		for (int k = 0; k < node.getChildrenSize(); k++) {
			AST expr = node.getChild(k);
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
		}

		return "";
	}

	@Override // TODO
	protected String visitC2S(AST node) {
		String i = visit(node.getChild(0));
		int a = newLocalReg();
		int b = newLocalReg();
		System.out.printf("  %%%d = alloca [2 x i8]\n", a);
		System.out.printf("  %%%d = getelementptr inbounds [2 x i8], [2 x i8]* %%%d, i64 0, i64 0\n", b, a);
		System.out.printf("  store i8 %s, i8* %%%d\n", i, b);

		return String.format("%%%d", b);
	}

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

class ArrayVar {
	public Integer dimension;
	private String type;
	private ArrayList<Integer> lengths;

	ArrayVar(Type contentType, Integer dimension) {
		this.dimension = dimension;
		this.lengths = new ArrayList<>();
		type = getSingleType(contentType);
	}

	public static String getSingleType(Type contentType) {
		String type = "";
		if (contentType == INT_TYPE) {
			type = "i32";
		} else if (contentType == REAL_TYPE) {
			type = "double";
		} else if (contentType == BOOL_TYPE) {
			type = "i1";
		} else if (contentType == CHAR_TYPE || contentType == STR_TYPE) {
			type = "i8";
		}
		return type;
	}

	public Integer getDimension() {
		return this.dimension;
	}

	public void addLength(int len) {
		lengths.add(len);
	}

	public String getInnerArrayType(int dim) {
		StringBuilder sb = new StringBuilder();
		Formatter formatter = new Formatter(sb);

		if (dim >= dimension)
			dim = dimension - 1;

		for (int i = dim; i < dimension; i++) {
			formatter.format("[%d x ", lengths.get(i));
		}

		formatter.format("%s", type);
		formatter.format("]".repeat(dimension - dim));

		String s = formatter.toString();
		formatter.close();
		return s;
	}
}