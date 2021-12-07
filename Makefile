JAVA=java
JAVAC=javac

ROOT=/home/joao/graduacao/compiladores/pascal-compiler

ANTLR_PATH=$(ROOT)/tools/antlr-4.9.3-complete.jar
CLASS_PATH_OPTION=-cp .:$(ANTLR_PATH)

ANTLR4=$(JAVA) -jar $(ANTLR_PATH)
GRUN=$(JAVA) $(CLASS_PATH_OPTION) org.antlr.v4.gui.TestRig

# Directory where files will be generated in
GEN_PATH=parser

# Directory for the test cases
DATA=$(ROOT)/tests
IN=$(DATA)/in

all: antlr javac
	@echo "Done."

antlr: pascalLexer.g4 pascalParser.g4
	$(ANTLR4) -no-listener -o $(GEN_PATH) pascalLexer.g4 pascalParser.g4

javac:
	$(JAVAC) $(CLASS_PATH_OPTION) $(GEN_PATH)/*.java

run:
	cd $(GEN_PATH) && $(GRUN) pascal program $(FILE)

# Run all tests
runall:
	@-for FILE in $(IN)/*.pas; do \
	 	cd $(GEN_PATH) && \
	 	echo -e "\nRunning $${FILE}" && \
	 	$(GRUN) pascal program $${FILE} && \
	 	cd .. ; \
	done;

clean:
	@rm -rf $(GEN_PATH)
