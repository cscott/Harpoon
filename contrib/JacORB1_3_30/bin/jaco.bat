@echo off
rem call java interpreter
java -Xbootclasspath:/home/andoni/projects/FLEX/JacORB1_3_30\lib\jacorb.jar;/usr/java/jdk1.3.1_02/jre\lib\rt.jar;%CLASSPATH% -Dorg.omg.CORBA.ORBClass=org.jacorb.orb.ORB -Dorg.omg.CORBA.ORBSingletonClass=org.jacorb.orb.ORBSingleton %*

