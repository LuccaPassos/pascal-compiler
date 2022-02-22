package ast;

public enum NodeKind {
	ASSIGN_NODE {
		public String toString() {
            return ":=";
        }
	},
    EQ_NODE {
		public String toString() {
            return "=";
        }
	},
    NEQ_NODE {
		public String toString() {
            return "<>";
        }
	},
    LT_NODE {
        public String toString() {
            return "<";
        }
    },
    GT_NODE {
        public String toString() {
            return ">";
        }
    },
    GE_NODE {
        public String toString() {
            return ">=";
        }
    },
    LE_NODE {
        public String toString() {
            return "<=";
        }
    },
    BLOCK_NODE {
		public String toString() {
            return "block";
        }
	},
    BOOL_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    IF_NODE {
		public String toString() {
            return "if";
        }
	},
    INT_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    OVER_NODE {
        public String toString() {
            return "/";
        }
	},
    AND_NODE {
		public String toString() {
            return "AND";
        }
	},
    MINUS_NODE {
        public String toString() {
            return "-";
        }
    },
    PLUS_NODE {
		public String toString() {
            return "+";
        }
	},
    OR_NODE {
		public String toString() {
            return "OR";
        }
	},
    PROGRAM_NODE {
		public String toString() {
            return "program";
        }
	},
    READ_NODE {
		public String toString() {
            return "read";
        }
	},
    REAL_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    REPEAT_NODE {
		public String toString() {
            return "repeat";
        }
	},
    STR_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    CHAR_VAL_NODE {
		public String toString() {
            return "";
        }
	},
    TIMES_NODE {
		public String toString() {
            return "*";
        }
	},
    VAR_DECL_NODE {
		public String toString() {
            return "var_decl";
        }
	},
    VAR_LIST_NODE {
		public String toString() {
            return "var_list";
        }
	},
    FUN_LIST_NODE {
		public String toString() {
            return "fun_list";
        }
	},
    FUN_DECL_NODE {
		public String toString() {
            return "fun_decl";
        }
	},
    FUN_USE_NODE {
        public String toString() {
            return "fun_use";
        }
    },
    VAR_USE_NODE {
		public String toString() {
            return "var_use";
        }
	},
    WRITE_NODE {
		public String toString() {
            return "write";
        }
	},
    ARRAY_ACCESS {
        public String toString() {
            return "array_acess";
        }
    },

    B2I_NODE { // Type conversion.
		public String toString() {
            return "B2I";
        }
	},
    B2R_NODE {
		public String toString() {
            return "B2R";
        }
	},
    B2S_NODE {
		public String toString() {
            return "B2S";
        }
	},
    I2R_NODE {
		public String toString() {
            return "I2R";
        }
	},
    I2S_NODE {
		public String toString() {
            return "I2S";
        }
	},
    R2S_NODE {
		public String toString() {
            return "R2S";
        }
	},
    C2S_NODE {
        public String toString() {
            return "C2S";
        }
    },
    S2C_NODE {
        public String toString() {
            return "S2C";
        }
    };
	
	public static boolean hasData(NodeKind kind) {
		switch(kind) {
	        case BOOL_VAL_NODE:
	        case INT_VAL_NODE:
	        case REAL_VAL_NODE:
	        case STR_VAL_NODE:
            case CHAR_VAL_NODE:
	        case VAR_DECL_NODE:
            case FUN_DECL_NODE:
	        case VAR_USE_NODE:
	            return true;
	        default:
	            return false;
		}
	}
}
