JCC=javac
JAR=jar

realtime.jar: 
	rm -f *.class
	rm -rf realtime
	$(JCC) *.java
	mkdir realtime
	rm -f Object.class
	mv *.class realtime/
	jar -c realtime/*.class > ../Code/Support/realtime.jar
	rm -rf realtime
