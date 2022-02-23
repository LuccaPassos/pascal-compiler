package tables;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

import scope.Scope;
import typing.Type;

public final class FunctionTable {
	private HashMap<String, Integer> table;
	private ArrayList<Function> functions;

	public FunctionTable() {
		this.table = new HashMap<>();
		this.functions = new ArrayList<>();
	}

	public int addEntry(String name, int line, Type type, Scope parentScope) {
        Function entry = new Function(name, line, type, parentScope);
		int index = this.functions.size();

		this.functions.add(entry);
        this.table.put(name, index);

		return index;
    }

	public int lookup(String name) {
		Integer index = this.table.get(name);
        return index != null ? index : -1;
    }

	public String getName(String name) {
		int index = this.table.get(name);
        return this.getName(index);
    }

	public String getName(int index) {
		if (index >= this.functions.size()) return null;
		return this.functions.get(index).getName();
	}
	
    public Type getType(String name) {
		int index = this.table.get(name);
        return this.getType(index);
    }

	public Type getType(int index) {
		if (index >= this.functions.size()) return null;
        return this.functions.get(index).getType();
    }
	
    public Integer getLine(String name) {
		int index = this.table.get(name);
        return this.getLine(index);
    }

	public Integer getLine(int index) {
		if (index >= this.functions.size()) return null;
		return this.functions.get(index).getLine();
	}

    public void addParameter(String functionName, Type type) {
		int index = this.table.get(functionName);
        this.addParameter(index, type);
    }

	public void addParameter(int index, Type type) {
		if (index >= this.functions.size()) return;
        this.functions.get(index).addParameter(type);
    }

    public Scope getScope(String functionName) {
		int index = this.table.get(functionName);
        return this.getScope(index);
    }
	
	public Scope getScope(int index) {
		if (index >= this.functions.size()) return null;
        return this.functions.get(index).getScope();
    }

	public Integer getParametersSize(String functionName) {
		int index = this.table.get(functionName);
		return this.getParametersSize(index);
	}

	public Integer getParametersSize(int index) {
		if (index >= this.functions.size()) return null;
		return this.functions.get(index).getParametersSize();
	}

    public ArrayList<Type> getParameters(String functionName) {
		int index = this.table.get(functionName);
		return this.getParameters(index);
	}
	
	public ArrayList<Type> getParameters(int index) {
		if (index >= this.functions.size()) return null;
		return this.functions.get(index).getParameters();
	}

	public String toString() {
		if (table.size() == 0) return "";

		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("Functions table:\n");

		int index = 0;
		for (Function function : functions) {
			formatter.format("Entry %d -- Name: %s, line: %d, type: %s, parameters: %s\n%s",
				index++, function.getName(), function.getLine(), function.getType(), function.getParameters(), function.getScope());
		}
		formatter.close();
		return stringBuilder.toString();
	}
	
	private final class Function extends Entry {
        ArrayList<Type> parameters;
        Scope scope;

		Function(String name, int line, Type type) {
			super(name, line, type);
            this.parameters = new ArrayList<Type>();
            this.scope = new Scope();
		}

		Function(String name, int line, Type type, Scope parentScope) {
			super(name, line, type);
            this.parameters = new ArrayList<Type>();
            this.scope = new Scope(parentScope);
		}

		Scope getScope() {
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
