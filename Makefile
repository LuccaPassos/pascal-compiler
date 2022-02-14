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
IN=$(DATA)/in
OUT=$(DATA)/cp2/out

FILE=
OUT_FILE = $(OUT)/$(basename $(notdir $(FILE))).dot

all: antlr javac
	@echo "Done."

antlr: pascalLexer.g4 pascalParser.g4
	$(ANTLR4) -no-listener -visitor -o $(GEN_PATH) -package $(GEN_PATH) pascalLexer.g4 pascalParser.g4

javac:
	@rm -rf $(BIN_PATH)
	@mkdir $(BIN_PATH)
	$(JAVAC) $(CLASS_PATH_OPTION) -d $(BIN_PATH) */*.java

$(OUT_FILE) dot:
	@mkdir -p $(OUT)
	
	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $(FILE) 2> $(OUT_FILE)
	@dot -Tpng $(OUT_FILE) -o $(basename $(OUT_FILE)).png 

run:
	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $(FILE)

# Run all tests
runall:
	@-for FILE in $(IN)/*.pas; do \
	 	echo -e "\nRunning $${FILE}" && \
	 	$(JAVA) $(CLASS_PATH_OPTION):$(BIN_PATH) $(MAIN_PATH)/Main $${FILE}; \
	done;

clean:
	@rm -rf $(GEN_PATH) $(BIN_PATH)
