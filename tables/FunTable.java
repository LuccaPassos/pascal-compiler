package tables;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import typing.Type;

public final class FunTable {
	// FIXME: This should be a hash
	private List<Entry> table = new ArrayList<Entry>(); 

	public int lookupFun(String s) {
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).name.equals(s)) {
				return i;
			}
		}
		return -1;
	}
	
	public int addFun(String s, int line, Type type) {
		Entry entry = new Entry(s, line, type);
		int idxAdded = table.size();
		table.add(entry);
		return idxAdded;
	}

    public void addParam(int i, Type type) {
        this.table.get(i).parameters.add(type);
    }

    public VarTable getScope(int i) {
        return this.table.get(i).scope;
    }

	public String getName(int i) {
		return table.get(i).name;
	}
	
	public int getLine(int i) {
		return table.get(i).line;
	}
	
	public Type getType(int i) {
		return table.get(i).type;
	}

	public int getParametersSize(int i) {
		return table.get(i).parameters.size();
	}

    public ArrayList<Type> getParameters(int i) {
		return table.get(i).parameters;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Functions table:\n");
		for (int i = 0; i < table.size(); i++) {
			f.format("Entry %d -- name: %s, line: %d, type: %s, parameters: %s\n%s", i,
	                 getName(i), getLine(i), getType(i).toString(), getParameters(i), getScope(i));
		}
		f.close();
		return sb.toString();
	}
	
	private final class Entry {
		String name;
		int line;
		Type type;
        ArrayList<Type> parameters;
        VarTable scope;

		Entry(String name, int line, Type type) {
			this.name = name;
			this.line = line;
			this.type = type;
            this.parameters = new ArrayList<Type>();
            this.scope = new VarTable();
		}
	}
}
