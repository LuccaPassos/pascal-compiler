package scope;

import java.util.Formatter;

import tables.FunctionTable;
import tables.VariableTable;

public class Scope {
    private VariableTable varaibleTable;
    private FunctionTable functionTable;
    private Scope parentScope;

    public VariableTable getVaraibleTable() {
        return this.varaibleTable;
    }

    public FunctionTable getFunctionTable() {
        return this.functionTable;
    }

    
    public Scope(Scope parentScope) {
        this.varaibleTable = new VariableTable();
        this.functionTable = new FunctionTable();
        this.parentScope = parentScope;
    }

    public Scope() {
        this.varaibleTable = new VariableTable();
        this.functionTable = new FunctionTable();
        this.parentScope = null;
    }

    public Scope getParentScope() {
        return this.parentScope;
    }

    public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("%s%s", varaibleTable, functionTable);

		formatter.close();
		return stringBuilder.toString();
	}
}
