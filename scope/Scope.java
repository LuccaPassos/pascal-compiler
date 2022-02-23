package scope;

import java.util.Formatter;

import tables.FunctionTable;
import tables.VariableTable;

public class Scope {
    private VariableTable varaibleTable;
    private FunctionTable functionTable;

    public VariableTable getVaraibleTable() {
        return this.varaibleTable;
    }

    public FunctionTable getFunctionTable() {
        return this.functionTable;
    }

    public Scope() {
        this.varaibleTable = new VariableTable();
        this.functionTable = new FunctionTable();
    }

    public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("%s%s", varaibleTable, functionTable);

		formatter.close();
		return stringBuilder.toString();
	}
}
