package typing;

import static ast.NodeKind.B2I_NODE;
import static ast.NodeKind.B2R_NODE;
import static ast.NodeKind.B2S_NODE;
import static ast.NodeKind.I2R_NODE;
import static ast.NodeKind.I2S_NODE;
import static ast.NodeKind.R2S_NODE;
import static ast.NodeKind.C2S_NODE;
import static ast.NodeKind.S2C_NODE;

import ast.AST;

public enum Conv {
	B2I,  // Bool to Int
    B2R,  // Bool to Real
    B2S,  // Bool to String
    I2R,  // Int to Real
    I2S,  // Int to String
    R2S,  // Real to String
	C2S,  // Char to String
	S2C,  // String to Char
    NONE; // No type conversion
    
	public static AST createConvNode(Conv conv, AST n) {
	    switch(conv) {
	        case B2I:  return AST.newSubtree(B2I_NODE, Type.INT_TYPE, n);
	        case B2R:  return AST.newSubtree(B2R_NODE, Type.REAL_TYPE, n);
	        case B2S:  return AST.newSubtree(B2S_NODE, Type.STR_TYPE, n);
	        case I2R:  return AST.newSubtree(I2R_NODE, Type.REAL_TYPE, n);
	        case I2S:  return AST.newSubtree(I2S_NODE, Type.STR_TYPE, n);
	        case R2S:  return AST.newSubtree(R2S_NODE, Type.STR_TYPE, n);
			case C2S:  return AST.newSubtree(C2S_NODE, Type.STR_TYPE, n);
			case S2C:  return AST.newSubtree(S2C_NODE, Type.CHAR_TYPE, n);
	        case NONE: return n;
	        default:
	            System.err.printf("INTERNAL ERROR: invalid conversion of types!\n");
	            System.exit(1);
	            return null;
	    }
	}
	
    public static final class Unif {
    	
    	public final Type type;
		public final Conv lc;
		public final Conv rc;
    	
		public Unif(Type type, Conv lc, Conv rc) {
			this.type = type;
			this.lc = lc;
			this.rc = rc;
		}
		
	}
}
