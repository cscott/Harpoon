JCC=javac
JAR=jar
JAVADOC=javadoc
INSTALLMACHINE=magic@www.magic.lcs.mit.edu
INSTALLDIR=public_html/Harpoon/
RELEASE=*.java README BUILDING Makefile

all:    clean doc realtime.jar # realtime.tgz

clean:
	@echo Cleaning up docs and realtime.jar.
	@rm -rf doc
	@rm -rf ../Code/Support/realtime.jar
	@rm -rf realtime.tgz

doc:
	@echo Generating documentation...
	@mkdir doc
	@javadoc -quiet -private -linksource -d doc/ *.java > /dev/null

realtime.jar: 
	@echo Generating realtime.jar file...
	@rm -f *.class
	@rm -rf javax
	@$(JCC) *.java
	@mkdir javax
	@mkdir javax/realtime
	@rm -f Object.class
	@mv *.class javax/realtime/
	@jar -c javax/realtime/*.class > ../Code/Support/realtime.jar
	@rm -rf javax

realtime.tgz:
	tar c $(RELEASE) | gzip -9 > realtime.tgz 	

jar-install: realtime.jar
	tar c ../Code/Support/realtime.jar | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR) -x"

tar-install: realtime.tgz
	tar c realtime.tgz | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR) -x"

doc-install: doc
	tar c doc | \
		$(SSH) $(INSTALLMACHINE) "tar -C $(INSTALLDIR)/Realtime -x"

install: jar-install tar-install doc-install
