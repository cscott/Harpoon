JCC=jikes
JAR=jar

realtime.jar: 
	rm -f *.class
	rm -rf javax
	$(JCC) *.java
	mkdir javax
	mkdir javax/realtime
	rm -f Object.class
	mv *.class javax/realtime/
	jar -c javax/realtime/*.class > ../Code/Support/realtime.jar
	rm -rf javax
