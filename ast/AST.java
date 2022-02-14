package ast;

import static typing.Type.NO_TYPE;

import java.util.ArrayList;
import java.util.List;

import tables.VarTable;
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
	private static VarTable vt;

	private int printNodeDot() {
		int myNr = nr++;

	    System.err.printf("node%d[label=\"", myNr);
	    if (this.type != NO_TYPE) {
	    	System.err.printf("(%s) ", this.type.toString());
	    }
	    if (this.kind == NodeKind.VAR_DECL_NODE || this.kind == NodeKind.VAR_USE_NODE) {
	    	System.err.printf("%s@", vt.getName(this.intData));
	    } else {
	    	System.err.printf("%s", this.kind.toString());
	    }
	    if (NodeKind.hasData(this.kind)) {
	        if (this.kind == NodeKind.REAL_VAL_NODE) {
	        	System.err.printf("%.2f", this.floatData);
	        } else if (this.kind == NodeKind.STR_VAL_NODE) {
	        	System.err.printf("@%d", this.intData);
	        } else {
	        	System.err.printf("%d", this.intData);
	        }
	    }
	    System.err.printf("\"];\n");

	    for (int i = 0; i < this.children.size(); i++) {
	        int childNr = this.children.get(i).printNodeDot();
	        System.err.printf("node%d -> node%d;\n", myNr, childNr);
	    }
	    return myNr;
	}

	public static void printDot(AST tree, VarTable table) {
	    nr = 0;
	    vt = table;
	    System.err.printf("digraph {\ngraph [ordering=\"out\"];\n");
	    tree.printNodeDot();
	    System.err.printf("}\n");
	}
}
