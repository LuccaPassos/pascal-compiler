package code;

// Provavelmente não vai ser usado

public final class Instruction {

	public final int op;
	// Estes campos não podem ser final por causa do backpatching...
	public int o1; // Operands, which can be int or float registers,
	public int o2; // int addresses or offsets, or
	public int o3; // integer or float constants (must be in an integer repr.)

	public Instruction(int op, int o1, int o2, int o3) {
		this.op = op;
		this.o1 = o1;
		this.o2 = o2;
		this.o3 = o3;
	}

	// public String toString() {
	// StringBuilder sb = new StringBuilder();
	// Formatter f = new Formatter(sb);
	// f.format("%s", this.op.toString());
	// if (this.op.opCount == 1) {
	// f.format(" %d", this.o1);
	// } else if (this.op.opCount == 2) {
	// f.format(" %d, %d", this.o1, this.o2);
	// } else if (this.op.opCount == 3) {
	// f.format(" %d, %d, %d", this.o1, this.o2, this.o3);
	// }
	// f.close();
	// return sb.toString();
	// }
}
