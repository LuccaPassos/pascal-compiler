package typing;

public enum Type {
	INT_TYPE {
		public String toString() {
            return "int";
        }
	},
    REAL_TYPE {
		public String toString() {
			return "real";
		}
	},
    BOOL_TYPE {
		public String toString() {
            return "bool";
        }
	},
    STR_TYPE {
		public String toString() {
            return "string";
        }
	},
    CHAR_TYPE {
		public String toString() {
            return "char";
        }
	},

	NO_TYPE {
		public String toString() {
			return "noType";
		}
	};
	
	private static Type plus[][] = {
				 /*INT*/	 /*REAL*/	/*BOOL*/ /*STR*/   /*CHAR*/
		/*INT*/ { INT_TYPE,  REAL_TYPE, NO_TYPE, STR_TYPE, NO_TYPE  },
		/*REAL*/{ REAL_TYPE, REAL_TYPE,	NO_TYPE, NO_TYPE,  NO_TYPE  },
		/*BOOL*/{ NO_TYPE,   NO_TYPE,	NO_TYPE, NO_TYPE,  NO_TYPE  },
		/*STR*/ { STR_TYPE,  NO_TYPE, 	NO_TYPE, STR_TYPE, STR_TYPE },
		/*CHAR*/{ NO_TYPE,   NO_TYPE, 	NO_TYPE, STR_TYPE, STR_TYPE }
	};
	
	public Type unifyPlus(Type that) {
		return plus[this.ordinal()][that.ordinal()];
	}

	private static Type andOr[][] = {
			/*INT*/	 	/*REAL*/ /*BOOL*/ 	/*STR*/   /*CHAR*/
	/*INT*/ { INT_TYPE, NO_TYPE, INT_TYPE,  NO_TYPE,  NO_TYPE },
	/*REAL*/{ NO_TYPE,  NO_TYPE, NO_TYPE,   NO_TYPE,  NO_TYPE },
	/*BOOL*/{ INT_TYPE, NO_TYPE, BOOL_TYPE, NO_TYPE,  NO_TYPE },
	/*STR*/ { NO_TYPE,  NO_TYPE, NO_TYPE,   NO_TYPE,  NO_TYPE },
	/*CHAR*/{ NO_TYPE,  NO_TYPE, NO_TYPE,   NO_TYPE,  NO_TYPE }
	};

	public Type unifyAndOr(Type that) {
	return andOr[this.ordinal()][that.ordinal()];
	}

	private static Type other[][] = {
				/*INT*/	 	 /*REAL*/	/*BOOL*/ /*STR*/  /*CHAR*/
		/*INT*/ { INT_TYPE,  REAL_TYPE, NO_TYPE, NO_TYPE, NO_TYPE },
		/*REAL*/{ REAL_TYPE, REAL_TYPE,	NO_TYPE, NO_TYPE, NO_TYPE },
		/*BOOL*/{ NO_TYPE,   NO_TYPE,	NO_TYPE, NO_TYPE, NO_TYPE },
		/*STR*/ { NO_TYPE,   NO_TYPE, 	NO_TYPE, NO_TYPE, NO_TYPE },
		/*CHAR*/{ NO_TYPE,   NO_TYPE, 	NO_TYPE, NO_TYPE, NO_TYPE }
		};

	public Type unifyOtherArith(Type that) {
	return other[this.ordinal()][that.ordinal()];
	}

	private static Type comp[][] = {
				/*INT*/	 	 /*REAL*/	/*BOOL*/   /*STR*/    /*CHAR*/
		/*INT*/ { BOOL_TYPE, BOOL_TYPE, NO_TYPE,   NO_TYPE,   NO_TYPE   },
		/*REAL*/{ BOOL_TYPE, BOOL_TYPE,	NO_TYPE,   NO_TYPE,   NO_TYPE   },
		/*BOOL*/{ NO_TYPE,   NO_TYPE,	BOOL_TYPE, NO_TYPE,   NO_TYPE   },
		/*STR*/ { NO_TYPE,   NO_TYPE, 	NO_TYPE,   BOOL_TYPE, BOOL_TYPE },
		/*CHAR*/{ NO_TYPE,   NO_TYPE, 	NO_TYPE,   BOOL_TYPE, BOOL_TYPE }
		};

	public Type unifyComp(Type that) {
		return comp[this.ordinal()][that.ordinal()];
	}
}
