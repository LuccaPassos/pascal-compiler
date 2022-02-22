package tables;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

import typing.Type;

public final class FunctionTable {
	private HashMap<String, Function> table = new HashMap<String, Function>();

	public FunctionTable() {
		super();
		this.table = new HashMap<String, Function>();
	}

	public void addEntry(String name, int line, Type type) {
        Function entry = new Function(name, line, type);
        this.table.put(name, entry);
    }

	public boolean lookup(String name) {
        return this.table.get(name) != null;
    }
		
	public String getName(String name) {
        return this.table.get(name).getName();
    }
	
    public Type getType(String name) {
        return this.table.get(name).getType();
    }
	
    public int getLine(String name) {
        return this.table.get(name).getLine();
    }

    public void addParameter(String functionName, Type type) {
        this.table.get(functionName).addParameter(type);
    }

    public VariableTable getScope(String functionName) {
        return this.table.get(functionName).getScope();
    }
	
	public int getParametersSize(String functionName) {
		return table.get(functionName).getParametersSize();
	}

    public ArrayList<Type> getParameters(String functionName) {
		return table.get(functionName).getParameters();
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("Functions table:\n");
		for (Function function : table.values()) {
			formatter.format("Name: %s, line: %d, type: %s, parameters: %s\n%s",
				function.getName(), function.getLine(), function.getType(), function.getParameters(), function.getScope());
		}
		formatter.close();
		return stringBuilder.toString();
	}
	
	private final class Function extends Entry {
        ArrayList<Type> parameters;
        VariableTable scope;

		Function(String name, int line, Type type) {
			super(name, line, type);
            this.parameters = new ArrayList<Type>();
            this.scope = new VariableTable();
		}

		VariableTable getScope() {
			return this.scope;
		}

		ArrayList<Type> getParameters() {
			return this.parameters;
		}

		void addParameter(Type parameterType) {
			this.parameters.add(parameterType);
		}

		int getParametersSize() {
			return this.parameters.size();
		}
	}
}
