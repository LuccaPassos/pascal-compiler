package tables;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;

import typing.Type;

public final class VarTable {
	// FIXME: This should be a hash
	private List<Entry> table = new ArrayList<Entry>(); 

	public int lookupVar(String s) {
		for (int i = 0; i < table.size(); i++) {
			if (table.get(i).name.equals(s)) {
				return i;
			}
		}
		return -1;
	}
	
	public int addVar(String s, int line, Type type) {
		Entry entry = new Entry(s, line, type);
		int idxAdded = table.size();
		table.add(entry);
		return idxAdded;
	}

	public int addVar(String name, int line, Type contentType, ArrayList<Integer[]> range) {
		Entry entry = new Entry(name, line, contentType, range);
		int idxAdded = table.size();
		table.add(entry);
		return idxAdded;
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

	public Type getContentType(int i) {
		return table.get(i).contentType;
	}

	public ArrayList<Integer[]> getRanges(int i) {
		return table.get(i).ranges;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Variables table:\n");
		for (int i = 0; i < table.size(); i++) {
			f.format("Entry %d -- name: %s, line: %d, type: %s\n", i,
	                 getName(i), getLine(i), getType(i).toString());
		}
		f.close();
		return sb.toString();
	}
	
	private final class Entry {
		String name;
		int line;
		Type type, contentType;
		ArrayList<Integer[]> ranges;
		
		Entry(String name, int line, Type type) {
			this.name = name;
			this.line = line;
			this.type = type;
			this.ranges = null;
			this.contentType = null;
		}

		Entry(String name, int line, Type contentType, ArrayList<Integer[]> ranges) {
			this.name = name;
			this.line = line;
			this.type = Type.ARRAY_TYPE;
			this.contentType = contentType;

			this.ranges = new ArrayList<Integer[]>();
			for (int i = 0; i < ranges.size(); i++) {
				Integer[] range = new Integer[2];
				range[0] = ranges.get(i)[0];
				range[1] = ranges.get(i)[1];
				this.ranges.add(range);
			}
		}
	}
}
