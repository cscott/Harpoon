JCC=javac
JAR=jar
JAVADOC=javadoc
SSH=ssh
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/
RELEASE=*.java README BUILDING Makefile

# eventually we'd like to use something like Code/bin/find-flex-dir
# to set this variable.  The below is an interim hack.
FLEX_DIR=../Code

all:    clean doc realtime.jar # realtime.tgz

clean:
	@echo Cleaning up docs and realtime.jar.
	@rm -rf doc
	@rm -rf $(FLEX_DIR)/Support/realtime.jar
	@rm -rf realtime.tgz

doc:	doc/TIMESTAMP
doc/TIMESTAMP:
	@echo Generating documentation...
	@$(RM) -rf doc
	@mkdir -p doc
	@javadoc -quiet -private -linksource -d doc/ *.java > /dev/null
	@date '+%-d-%b-%Y at %r %Z.' > doc/TIMESTAMP
	@chmod -R a+rX doc

realtime.jar: 
	@echo Generating realtime.jar file...
	@rm -f *.class
	@rm -rf javax
	@$(JCC) *.java
	@mkdir javax
	@mkdir javax/realtime
	@rm -f Object.class
	@mv *.class javax/realtime/
	@jar -c javax/realtime/*.class > $(FLEX_DIR)/Support/realtime.jar
	@rm -rf javax

realtime.tgz realtime.tgz.TIMESTAMP:
	@echo Generating realtime.tgz file.
	tar c $(RELEASE) | gzip -9 > realtime.tgz 	
	@date '+%-d-%b-%Y at %r %Z.' > realtime.tgz.TIMESTAMP

jar-install: realtime.jar
	@echo Installing realtime.jar.
	tar -C $(FLEX_DIR)/Support -c realtime.jar | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR) -x"

tar-install: realtime.tgz realtime.tgz.TIMESTAMP
	@echo Installing realtime.tgz.
	tar c realtime.tgz realtime.tgz.TIMESTAMP | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR) -x"

doc-install: doc
	@echo Installing documentation.
	tar c doc | \
		$(SSH) $(INSTALLMACHINE) \
	"mkdir -p $(INSTALLDIR)/Realtime ; tar -C $(INSTALLDIR)/Realtime -x"

install: jar-install tar-install doc-install
