package tables;

import static typing.Type.ARRAY_TYPE;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

import typing.*;

public final class VariableTable {
	protected HashMap<String, Variable> table;

	public VariableTable() {
		super();
		this.table = new HashMap<String, Variable>();
	}

	public void addEntry(String name, int line, Type type) {
        Variable entry = new Variable(name, line, type);
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

	public void addEntry(String variableName, int line, Type contentType, ArrayList<Integer[]> range) {
		Variable entry = new Variable(variableName, line, contentType, range);
		this.table.put(variableName, entry);
	}

	public Type getContentType(String variableName) {
		return table.get(variableName).getContentType();
	}

	public ArrayList<Integer[]> getRanges(String variableName) {
		return table.get(variableName).getRanges();
	}

	public int getRangesSize(String variableName) {
		return table.get(variableName).getRangesSize();
	}
	
	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("Variables table:\n");
		
		for (Variable variable : table.values()) {
			if (variable.getType() == ARRAY_TYPE) {
				formatter.format("Name: %s, line: %d, type: %s, content type: %s, range: [", variable.getName(), variable.getLine(), variable.getType(), variable.getContentType());
				
				int i = 0;
				for (Integer[] range : variable.getRanges()) {
					formatter.format("%d..%d", range[0], range[1]);
					if ((i++) < variable.getRanges().size() - 1) formatter.format(", ");
				}

				formatter.format("]\n");
			} else {
				formatter.format("Name: %s, line: %d, type: %s\n", variable.getName(), variable.getLine(), variable.getType());
			}
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
