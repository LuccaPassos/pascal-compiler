package typing;

import static typing.Conv.I2R;
import static typing.Conv.I2S;
import static typing.Conv.NONE;
import static typing.Conv.C2S;

import typing.Conv.Unif;

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

	private static Unif plus[][] = {
				/*INT*/						   /*REAL*/							/*BOOL*/ 					   /*STR*/						   /*CHAR*/
	/*INT*/  { new Unif(INT_TYPE, NONE, NONE), new Unif(REAL_TYPE, I2R, NONE),  new Unif(NO_TYPE, NONE, NONE), new Unif(STR_TYPE, I2S, NONE),  new Unif(NO_TYPE, NONE, NONE) },
	/*REAL*/ { new Unif(REAL_TYPE, NONE, I2R), new Unif(REAL_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE) },
	/*BOOL*/ { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE) },
	/*STR*/  { new Unif(STR_TYPE, NONE, I2S),  new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(STR_TYPE, NONE, NONE), new Unif(STR_TYPE, NONE, C2S) },
	/*CHAR*/ { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(STR_TYPE, C2S, NONE),  new Unif(STR_TYPE, C2S, C2S)  }
	};
	
	public Unif unifyPlus(Type that) {
		return plus[this.ordinal()][that.ordinal()];
	}

	private static Unif andOr[][] = {
			/*INT*/						   	   /*REAL*/						  /*BOOL*/ 					   	   /*STR*/						  /*CHAR*/
	/*INT*/  { new Unif(INT_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE) },
	/*REAL*/ { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE) },
	/*BOOL*/ { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE), new Unif(BOOL_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE) },
	/*STR*/  { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE) },
	/*CHAR*/ { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE)  }
	};

	public Unif unifyAndOr(Type that) {
	return andOr[this.ordinal()][that.ordinal()];
	}
	
	private static Unif other[][] = {
			/*INT*/						   	   /*REAL*/						  /*BOOL*/ 					   	   /*STR*/						  /*CHAR*/
	/*INT*/  { new Unif(INT_TYPE, NONE, NONE), new Unif(REAL_TYPE, I2R, NONE),  new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE) },
	/*REAL*/ { new Unif(REAL_TYPE, NONE, I2R), new Unif(REAL_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE) },
	/*BOOL*/ { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE), 	new Unif(NO_TYPE, NONE, NONE), 	 new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE) },
	/*STR*/  { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE), 	new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE) },
	/*CHAR*/ { new Unif(NO_TYPE, NONE, NONE),  new Unif(NO_TYPE, NONE, NONE), 	new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE)  }
	};

	public Unif unifyOtherArith(Type that) {
	return other[this.ordinal()][that.ordinal()];
	}

	private static Unif comp[][] = {
				/*INT*/						    /*REAL*/						 /*BOOL*/ 					   	  /*STR*/						   /*CHAR*/
	/*INT*/  { new Unif(BOOL_TYPE, NONE, NONE), new Unif(BOOL_TYPE, I2R, NONE),  new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE)  },
	/*REAL*/ { new Unif(BOOL_TYPE, NONE, I2R),  new Unif(BOOL_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE)  },
	/*BOOL*/ { new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE),   new Unif(BOOL_TYPE, NONE, NONE), new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE)  },
	/*STR*/  { new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE),   new Unif(BOOL_TYPE, NONE, NONE), new Unif(BOOL_TYPE, NONE, C2S) },
	/*CHAR*/ { new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE),   new Unif(NO_TYPE, NONE, NONE),   new Unif(BOOL_TYPE, C2S, NONE),  new Unif(BOOL_TYPE, C2S, C2S)  }
	};
		

	public Unif unifyComp(Type that) {
		return comp[this.ordinal()][that.ordinal()];
	}
}
