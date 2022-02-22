package tables;

import java.util.Formatter;
import java.util.HashMap;

@SuppressWarnings("serial")
public final class StringTable extends HashMap<String, Boolean>{

	public StringTable() {
		super();
	}

    public void addString(String string) {
		this.put(string, true);
	}

	public String toString() {
		StringBuilder stringBuilder = new StringBuilder();
		Formatter formatter = new Formatter(stringBuilder);
		formatter.format("Strings table:\n");
		for (String string : this.keySet()) {
			formatter.format("%s\n", this.get(string));
		}
		formatter.close();
		return stringBuilder.toString();
	}
}
