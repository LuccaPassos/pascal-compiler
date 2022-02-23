package ast;

import static ast.NodeKind.FUN_DECL_NODE;
import static typing.Type.NO_TYPE;

import java.util.ArrayList;
import java.util.List;

import scope.Scope;
import tables.FunctionTable;
import tables.VariableTable;
import typing.Type;

public class AST {

	public  final NodeKind kind;
	public  final int intData;
	public  final float floatData;
	public  final Type type;
	private final List<AST> children;

	private AST(NodeKind kind, int intData, float floatData, Type type) {
		this.kind = kind;
		this.intData = intData;
		this.floatData = floatData;
		this.type = type;
		this.children = new ArrayList<AST>();
	}

	public AST(NodeKind kind, int intData, Type type) {
		this(kind, intData, 0.0f, type);
	}

	public AST(NodeKind kind, float floatData, Type type) {
		this(kind, 0, floatData, type);
	}

	public void addChild(AST child) {
		this.children.add(child);
	}

	public int getChildrenSize() {
		return this.children.size();
	}

	public AST getChild(int idx) {
	    return this.children.get(idx);
	}

	public static AST newSubtree(NodeKind kind, Type type, AST... children) {
		AST node = new AST(kind, 0, type);
	    for (AST child: children) {
	    	node.addChild(child);
	    }
	    return node;
	}

	private static int nr;
	private static Scope currentScope;

	private int printNodeDot() {
		int myNr = nr++;
		VariableTable variableTable = currentScope.getVaraibleTable();
		FunctionTable functionTable = currentScope.getFunctionTable();

		// Does the node have a type?
	    System.out.printf("node%d[label=\"", myNr);
		if (this.type != NO_TYPE) {
	    	System.out.printf("(%s) ", this.type.toString());
	    }

		// What kind of node is it?
	    if (this.kind == NodeKind.VAR_DECL_NODE || this.kind == NodeKind.VAR_USE_NODE) {
			Type type = variableTable.getType(this.intData);
			if (type == Type.ARRAY_TYPE && this.kind == NodeKind.VAR_DECL_NODE) {
				System.out.printf("(%s) [", variableTable.getContentType(this.intData));

				ArrayList<Integer[]> ranges = variableTable.getRanges(this.intData);

				int i = 0;
				for (Integer[] range : ranges) {
					System.out.printf("%d..%d", range[0], range[1]);
					if (i++ < ranges.size() - 1) System.out.printf(", ");
				}
				System.out.printf("] %s@", variableTable.getName(this.intData));

			} else if (type == Type.ARRAY_TYPE && this.kind == NodeKind.VAR_USE_NODE) {
				System.out.printf("(%s) %s@", variableTable.getContentType(this.intData), variableTable.getName(this.intData));

			} else {
				System.out.printf("%s@",variableTable.getName(this.intData));
			}
	    }
		else if (this.kind == NodeKind.FUN_USE_NODE || this.kind == NodeKind.FUN_DECL_NODE) {
			// Look for the function on higher scopes
			String name = null;
			Scope scope = currentScope;
			while(name == null) {
				FunctionTable currentFunctionTable = scope.getFunctionTable();
				name = currentFunctionTable.getName(this.intData);
				scope = scope.getParentScope();
			}
			System.out.printf("%s@", name);
		} else {
	    	System.out.printf("%s", this.kind.toString());
	    }

		// Does the node hold numeric data?
	    if (NodeKind.hasData(this.kind)) {
	        if (this.kind == NodeKind.REAL_VAL_NODE) {
	        	System.out.printf("%.2f", this.floatData);
	        } else if (this.kind == NodeKind.STR_VAL_NODE) {
	        	System.out.printf("@%d", this.intData);
	        } else {
				System.out.printf("%d", this.intData);
			}
	    }

	    System.out.printf("\"];\n");

	    for (AST child : this.children) {
			Scope lastScope = currentScope;

			if (child.kind == FUN_DECL_NODE)
				currentScope = functionTable.getScope(child.intData);

	        int childNr = child.printNodeDot();

			currentScope = lastScope;
	        System.out.printf("node%d -> node%d;\n", myNr, childNr);
	    }
	    return myNr;
	}

	public static void printDot(AST tree, Scope scope) {
	    nr = 0;
		currentScope = scope;
	    System.out.printf("digraph {\ngraph [ordering=\"out\"];\n");
	    tree.printNodeDot();
	    System.out.printf("}\n");
	}
}
