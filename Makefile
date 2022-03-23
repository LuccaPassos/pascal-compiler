JAVA=java
JAVAC=javac

ROOT = $(PWD)

ANTLR_PATH=$(ROOT)/tools/antlr-4.9.3-complete.jar
CLASS_PATH_OPTION=-cp .:$(ANTLR_PATH)

ANTLR4=$(JAVA) -jar $(ANTLR_PATH)
GRUN=$(JAVA) $(CLASS_PATH_OPTION) org.antlr.v4.gui.TestRig

# Directory where files will be generated in
GEN_PATH=parser

# `main` function path
MAIN_PATH=checker

# .class files directory
BIN_PATH=bin

# Directory for the test cases
DATA=$(ROOT)/tests
IN=$(DATA)/cp3/in
OUT=$(DATA)/cp3/out

FILE =
OUT_DOT = $(OUT)/$(basename $(notdir $(FILE))).dot
OUT_LL = $(OUT)/$(basename $(notdir $(FILE))).ll


all: antlr javac
	@echo "Done."

antlr: pascalLexer.g4 pascalParser.g4
	@$(ANTLR4) -no-listener -visitor -o $(GEN_PATH) -package $(GEN_PATH) pascalLexer.g4 pascalParser.g4

javac:
	@rm -rf $(BIN_PATH)
	@mkdir $(BIN_PATH)
	@$(JAVAC) $(CLASS_PATH_OPTION) -d $(BIN_PATH) */*.java

$(OUT_DOT) dot:
	@mkdir -p $(OUT)
	
	@$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $(FILE) > $(OUT_DOT)
	@dot -Tpng $(OUT_DOT) -o $(basename $(OUT_FILE)).png 

# This generates the targer <file>.ll
$(OUT_LL) ll: javac
	@mkdir -p tests/cp3/out -p
	@$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $(FILE) > $(OUT_LL)

# This runs the .ll file generated from the input pascal program
lli: $(OUT_LL)
	-@lli $(OUT_LL) || true


# Run all tests
runall:
	@-for FILE in $(IN)/*.pas; do \
	 	echo -e "\nRunning $${FILE}" && \
		make dot FILE=$${FILE} \
	 	# $(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $${FILE}; \
	done;

clean:
	@rm -rf $(GEN_PATH) $(BIN_PATH)
