# Model Constraint Compiler MAKEFILE
# Auth: Daniel Roy, David Ziegler

PROGRAM = MCC
SHELL = /bin/sh
TARGET = $(PROGRAM)-working

SCANNER = Lexer

SYMBOLS = Sym
PARSERS = \
MDLParser.java MDLParser.class \
TDLParser.java TDLParser.class \
CDLParser.java CDLParser.class \
SDLParser.java SDLParser.class

COMPILER_CLASS = Compiler.class
CLI_CLASS = CLI.class
OTHER_CLASS = State.class LineCount.class Symbol.class

IR_CLASS = IR/ParseNode.class IR/ParseNodeVector.class IR/ParseNodeDOTVisitor.class \
IR/SemanticChecker.class IR/Expr.class IR/VarExpr.class IR/OpExpr.class IR/DotExpr.class \
IR/LiteralExpr.class IR/IntegerLiteralExpr.class IR/BooleanLiteralExpr.class \
IR/TokenLiteralExpr.class IR/Descriptor.class IR/TypeDescriptor.class IR/StructureTypeDescriptor.class \
IR/MissingTypeDescriptor.class IR/ReservedTypeDescriptor.class IR/ArrayDescriptor.class \
IR/FieldDescriptor.class IR/LabelDescriptor.class IR/ReservedFieldDescriptor.class \
IR/SymbolTable.class IR/IRException.class IR/Opcode.class IR/SimpleIRErrorReporter.class \
IR/IRErrorReporter.class IR/SetDescriptor.class IR/MissingSetDescriptor.class \
IR/RelationDescriptor.class IR/ReservedSetDescriptor.class IR/TokenSetDescriptor.class \
IR/Quantifier.class IR/SetQuantifier.class IR/ImageSetExpr.class \
IR/CastExpr.class IR/SizeofExpr.class IR/SetExpr.class IR/VarDescriptor.class \
IR/LogicStatement.class IR/Predicate.class IR/InclusionPredicate.class \
IR/ComparisonPredicate.class IR/Constraint.class IR/RelationExpr.class \
IR/RelationQuantifier.class IR/ForQuantifier.class IR/GraphNode.class \
IR/DependencyBuilder.class IR/RelationInclusion.class IR/SetInclusion.class \
IR/TupleOfExpr.class IR/ElementOfExpr.class IR/Rule.class IR/Inclusion.class \
IR/NaiveGenerator.class IR/CodeWriter.class IR/SymbolTableStack.class \
IR/SizeofPredicate.class

#MODEL_CLASS = Field.class Literal.class Quantifier.class 		\
#Set.class TypeElement.class						\
#MDElementExpr.class Rule.class Guard.class Type.class 			\
#ParseException.class ModelInclusionConstraint.class

ALL_CLASS = $(COMPILER_CLASS) $(CLI_CLASS) $(OTHER_CLASS) $(IR_CLASS)

ALLCLASSES = *.class IR/*.class
ALLJAVA = *.java IR/*.java

.PHONY: java clean all tests javadoc

all: $(TARGET).tar.gz $(TARGET).jar 

tests: $(TARGET).tar.gz $(TARGET).jar
#	./tests.pl scanner

$(TARGET).tar.gz: $(TARGET).tar
	gzip $(TARGET).tar -c > $(TARGET).tar.gz

$(TARGET).tar: $(ALLJAVA) Makefile $(SCANNER).lex 
	tar cf $@ $(ALLJAVA) Makefile $(SCANNER).lex

$(TARGET).jar: java
	jar cf $@ $(ALLCLASSES)

clean:
	rm -f $(ALLCLASSES)
	rm -f $(SCANNER).lex.java $(SCANNER).java
	rm -f MDLParser.java TDLParser.java CDLParser.java SDLParser.java Sym.java
	rm -f $(TARGET).tar.gz $(TARGET).tar $(TARGET).jar
	rm -f *~ IR/*~

java: $(PARSERS) $(SCANNER).class $(ALL_CLASS)

parser: $(PARSERS) $(SCANNER).java

%Parser.java: %.cup
	java -classpath ../ java_cup.Main -nosummary -symbols $(SYMBOLS) -parser $*Parser < $*.cup
	javac $(SYMBOLS).java

%.class: %.java
	javac -classpath ../ -source 1.4 $<

%.lex.java: %.lex
	java -classpath ../ JLex.Main $<

$(SCANNER).java: $(SCANNER).lex.java
	cp $(SCANNER).lex.java $(SCANNER).java
	rm $(SCANNER).lex.java

javadoc:
	javadoc -package -d ../javadoc/ -windowtitle MCC $(ALLJAVA)

