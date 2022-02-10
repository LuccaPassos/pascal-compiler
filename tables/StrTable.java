package tables;

import java.util.ArrayList;
import java.util.Formatter;

@SuppressWarnings("serial")
// FIXME: this should be a hash
public final class StrTable extends ArrayList<String>{

    @Override
    public boolean add(String s) {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).equals(s)) {
				return false;
			}
		}
		return super.add(s);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		Formatter f = new Formatter(sb);
		f.format("Strings table:\n");
		for (int i = 0; i < this.size(); i++) {
			f.format("Entry %d -- %s\n", i, this.get(i));
		}
		f.close();
		return sb.toString();
	}

}
