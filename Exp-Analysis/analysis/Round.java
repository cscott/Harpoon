package analysis;

/*
  This class represents a participant in the experiment
*/


import java.util.*;
import java.io.*;


public class Round {

    private int number; // the round number
    private int program; // the program received in this round (1,2,3, or 4)

    /* because some people in the tool population didn't use the tool in some
       rounds, we associate a population type with a round as well */
    private String population; // "tool" or "notool"

    private TreeSet builds; // the builds associated with this program

    /* we keep the submission of the program as a build;  
       this boolean tells us only if the participant used 
       the submit script in this round */
    private boolean submitted = false; 
    
    // the time when the program was copied
    private long startTime;
   
    // the time when the program was submitted
    private long endTime;

    // the time spent to localize the bug
    private long localized;

    // whether the bug was fixed or not
    private boolean correct = false;



    // constructor
    public Round(int number, int program, String population) {
	this.number = number;
	this.program = program;
	this.population = population;
	
	builds = new TreeSet();
    }


    // adds a build
    public void addBuild(long time, String username, String population, String source) {
	if (existsBuildWithTime(time)) {
	    System.out.println("We found another build with the same time in this round!!!");
	}
	int no = getNumberOfBuilds()+1;
	
	/* the source for this build should be kept in a 
	   file named "<username>_<population>_<time>. 
	   For example "cristis_tool_54543.cc"
	*/
	String filename = "Participants"+File.separatorChar+username+File.separatorChar+username+"_"+population+"_"+time+"_"+program+".cc";
	File file = new File(filename);
	if (!file.exists())
	    createNewFile(filename, source);

	// extract the number of calltool invokations
	int invokations = -1;
	int fromIndex = 0;
	while (source.indexOf("calltool(", fromIndex) != -1) {
	    invokations++;
	    fromIndex = source.indexOf("calltool(", fromIndex) +1;
	}
	




	if (invokations == -1)
	    invokations = 0;	


	Build b = new Build(no, time, getProgram(), filename, invokations);

	if ((invokations>0) && (population.equals("notool"))) {
	    //System.out.println("\n"+b+"\n");	
	}

	builds.add(b);
	
    }


    private void createNewFile(String fileName, String contents) {
	try{
	    BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
	    bw.write(contents);
	    bw.close();
	} catch(IOException e) {
	    System.out.println("IOException msg:"+e.getMessage());
	    System.exit(1);	    
	}	
    }



    // getters
    public int getNumber() {
	return number;
    }

    public int getProgram() {
	return program;
    }

    public String getPopulation() {
	return population;
    }

    public boolean correct() {
	return correct;
    }

    // if only_correct is true, then we ignore the incorrect solutions
    public long getLocalized(boolean only_correct) {
	if ((only_correct) && (!correct()))
	    return 0;
	else return localized;
    }

    
    public int getNumberOfBuilds() {
	return builds.size();
    }   

    public Build getBuild(int number) {
	Iterator it = builds.iterator();
	int k=0;
	while (it.hasNext()) {
	    Build b = (Build) it.next();
	    if (k == number)
		return b;
	    k++;
	}	
	return null;
    }

    public boolean existsBuildWithTime(long time) {
	Iterator it = builds.iterator();
	while (it.hasNext()) {
	    Build b = (Build) it.next();
	    if (b.getTime() == time)
		return true;
	}	

	return false; // not found
    }    


    public int numberBuildsWithCalltools() {
	int calltools = 0;
	Iterator it = builds.iterator();
	while (it.hasNext()) {
	    Build b = (Build) it.next();
	    if (b.getCalltools() != 0) {
		calltools++;
		//System.out.println(b);
	    }
	}
	return calltools;
    }

    
    public boolean submitted() {
	return submitted;
    }

    public long getStartTime() {
	return startTime;
    }

    public long getEndTime() {
	return endTime;
    }

    // if only_correct is true, then we ignore the incorrect solutions
    public long getTotalTime(boolean only_correct) {
	if ((only_correct) && (!correct()))
	    return 0;
	else return endTime-startTime;
    }



    // setters
    public void setStartTime(long startTime) {
	this.startTime = startTime;
    }

    public void setEndTime(long endTime) {
	this.endTime = endTime;
    }

    public void setPopulation(String population) {
	this.population = population;
    }
    
    public void setSubmitted() {
	submitted = true;
    }
    
    public void setCorrect(boolean correct) {
	this.correct = correct;
    }

    public void setLocalized(long minutes) {
	this.localized = minutes;
    }


    public String toString() {
	return 
	    "Round #: "+getNumber()+"\n"+
 	    "Program received: "+getProgram()+"\n"+
	    "Start time: "+getStartTime()+"\n"+
	    "End time: "+getEndTime()+"\n"+
	    "Total time: "+getTotalTime(false);
    }

}
