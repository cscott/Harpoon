JCC=javac
JAR=jar
JAVADOC=javadoc


all:    clean doc realtime.jar

clean:
	@echo Cleaning up docs and realtime.jar.
	@rm -rf doc
	@rm -rf ../Code/Support/realtime.jar

doc:
	@echo Generating documentation...
	@mkdir doc
	@javadoc -quiet -private -linksource -d doc/ *.java

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
