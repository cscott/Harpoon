package analysis;

/*
  This class represents a build action
*/


import java.util.*;


public class Build implements Comparable{

    private int number; // indexes the build actions
    private long time; // the time when the build occured
    private int program; // the program built (1,2,3, or 4)

    private String fileName; // the file name containing this source

    private int calltools; // the number of calltool invokations


    // constructor
    public Build(int number, long time, int program, String fileName, int calltools) {
	this.number = number;
	this.time = time;
	this.program = program;
	this.fileName = fileName;
	this.calltools = calltools;
    }


    // getters
    public int getNumber() {
	return number;
    }

    public long getTime() {
	return time;
    }

    public int getProgram() {
	return program;
    }

    public String getFileName() {
	return fileName;
    }    

    public int getCalltools() {
	return calltools;
    }



    public String toString() {
	return 
	    "Build #: "+getNumber()+"\n"+
	    "Time: "+getTime()+"\n"+
 	    "Program received: "+getProgram()+"\n"+
	    "C file: "+getFileName()+"\n"+
	    "Calltools: "+getCalltools();
    }


    public int compareTo(Object o) {
	if (this.getTime() == ((Build) o).getTime())
	    return 0;
	else 
	    if (this.getTime() < ((Build) o).getTime())
		return -1;
	    else return 1;
    }
    
    public boolean equals(Object o) {
	if (this.getClass() != o.getClass())
	    return false;
	
	if (this.getTime() == ((Build) o).getTime())
	    return true;
	else return false;
    }

}
