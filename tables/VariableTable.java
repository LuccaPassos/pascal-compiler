package tables;

import static typing.Type.ARRAY_TYPE;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

import typing.*;

public final class VariableTable {
	private HashMap<String, Integer> table;
	private ArrayList<Variable> variables;

	public VariableTable() {
		this.table = new HashMap<>();
		this.variables = new ArrayList<>();
	}

	public int addEntry(String name, int line, Type type) {
        Variable entry = new Variable(name, line, type);
		int index = this.variables.size();

		this.variables.add(entry);
        this.table.put(name, index);

		return index;
    }

	public int lookup(String name) {
		Integer index = this.table.get(name);
        return index != null ? index : -1;
    }
	
	public String getName(String name) {
		int index = this.table.get(name);
        return this.variables.get(index).getName();
    }

	public String getName(int index) {
		return this.variables.get(index).getName();
	}

    public Type getType(String name) {
        int index = this.table.get(name);
		return this.variables.get(index).getType();
    }

	public Type getType(int index) {
		return this.variables.get(index).getType();
	}

    public int getLine(String name) {
        int index = this.table.get(name);
		return this.variables.get(index).getLine();
    }

	public int getLine(int index) {
		return this.variables.get(index).getLine();
    }

	public int addEntry(String variableName, int line, Type contentType, ArrayList<Integer[]> range) {
		Variable entry = new Variable(variableName, line, contentType, range);
		int index = this.variables.size();

		this.variables.add(entry);
		this.table.put(variableName, index);

		return index;
	}

	public Type getContentType(String name) {
		int index = this.table.get(name);
		return this.variables.get(index).getContentType();
	}

	public Type getContentType(int index) {
		return variables.get(index).getContentType();
	}

	public ArrayList<Integer[]> getRanges(String variableName) {
		int index = table.get(variableName);
		return this.variables.get(index).getRanges();
	}

	public ArrayList<Integer[]> getRanges(int index) {
		return variables.get(index).getRanges();
	}

	public int getRangesSize(String variableName) {
		int index = table.get(variableName);
		return variables.get(index).getRangesSize();
	}

	public int getRangesSize(int index) {
		return variables.get(index).getRangesSize();
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("Variables table:\n");
		
		int index = 0;
		for (Variable variable : variables) {
			formatter.format("Entry %d -- Name: %s, line: %d, type: %s", index++, variable.getName(), variable.getLine(), variable.getType());
			if (variable.getType() == ARRAY_TYPE) {
				formatter.format(", content type: %s, range: [", variable.getContentType());
				int i = 0;
				for (Integer[] range : variable.getRanges()) {
					formatter.format("%d..%d", range[0], range[1]);
					if ((i++) < variable.getRanges().size() - 1) formatter.format(", ");
				}

				formatter.format("]");
			}
			formatter.format("\n");
		}

		formatter.close();
		return stringBuilder.toString();
	}

	private final class Variable extends Entry {
		Type contentType;
		ArrayList<Integer[]> ranges;

		Variable(String name, int line, Type type) {
			super(name, line, type);
			this.contentType = null;
			this.ranges = null;
		}

		Variable(String name, int line, Type contentType, ArrayList<Integer[]> ranges) {
			super(name, line, ARRAY_TYPE);
			this.contentType = contentType;
			this.ranges = ranges;
		}

		Type getContentType() {
			return this.contentType;
		}

		ArrayList<Integer[]> getRanges() {
			return this.ranges;
		}

		int getRangesSize() {
			return this.ranges.size();
		}
	}
}
