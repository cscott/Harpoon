## Process this with automake to create Makefile.in

AUTOMAKE_OPTIONS = foreign

SUFFIXES = .java .class

# Use an environment variable for CLASSPATH so we pick up standard classes:
CLASSPATH_ENV = CLASSPATH=../..:$(srcdir)/../..:$$CLASSPATH
JAVAC = javac
JCFLAGS = -g

.java.class:
	$(CLASSPATH_ENV) $(JAVAC) $(JCFLAGS) -d ../.. $<

javadir=$(datadir)/java/$(subdir)

OBJ=\
Access.class \
Attribute.class \
AttrContainer.class \
ObjectType.class ArrayType.class \
ClassType.class \
ConstantPool.class \
CpoolClass.class \
CpoolEntry.class \
CpoolValue1.class \
CpoolValue2.class \
CpoolNameAndType.class \
CpoolRef.class \
CpoolString.class \
CpoolUtf8.class \
Location.class Field.class \
Label.class IfState.class TryState.class \
Method.class \
CodeAttr.class \
LineNumbersAttr.class LocalVarsAttr.class \
MiscAttr.class \
PrimType.class \
Scope.class \
SourceFileAttr.class \
Type.class \
Variable.class \
VarEnumerator.class \
ZipArchive.class \
ZipMember.class \
ZipLoader.class \
ClassFileInput.class \
ClassTypeWriter.class \
dump.class

java_DATA=$(OBJ)

# Following only works for GNU make, but is only needed for make dist or TAGS.
SOURCES = $(OBJ:.class=.java)

CLEANFILES=$(OBJ)

all: $(OBJ)

HTML_HOME = $$HOME/public_html

install-html:
	-mkdir $(HTML_HOME)/gnu.bytecode
	cp $(srcdir)/overview.html $(HTML_HOME)/gnu.bytecode/index.html
	cp $(srcdir)/COPYING $(HTML_HOME)/gnu.bytecode/COPYING
	chmod a+r $(HTML_HOME)/gnu.bytecode/*
