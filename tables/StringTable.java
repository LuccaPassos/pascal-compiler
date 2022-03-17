package tables;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;

@SuppressWarnings("serial")
public final class StringTable {
	private HashMap<String, Integer> table;
	private ArrayList<String> strings;

	public StringTable() {
		this.table = new HashMap<>();
		this.strings = new ArrayList<>();
	}

	public int addString(String string) {
		Integer index = this.table.get(string);
		if (index != null)
			return index;

		index = this.strings.size();
		this.strings.add(string);
		this.table.put(string, index);

		return index;
	}

	public String getString(Integer index) {
		String str = strings.get(index);
		return str.substring(1, str.length() - 1);
	}

	public int size() {
		return strings.size();
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("Strings table:\n");

		int index = 0;
		for (String string : this.strings) {
			formatter.format("Entry %d -- %s\n", index++, string);
		}
		formatter.close();
		return stringBuilder.toString();
	}
}
