package analysis;

import java.io.*;
import java.util.*;


public class Analysis {

// the set of participants
private static Participants participants = new Participants();


public static void process(String filename) {

    String name, userName, population, action, sprogram, stime;
    StringBuffer source;
    int program;
    long time;
try{
    BufferedReader br = new BufferedReader(new FileReader(filename));

    String line = br.readLine();
    while (line != null) {
	if (line.startsWith("Participant:")) {
	    // new message
	    // extract the name of the participant
	    if (line.length() >= 15)
		name = line.substring(14, line.length());
	    else name = ""; // no name provided
	    	    
	    // extract the user name
	    line = br.readLine();
	    userName = line.substring(12, line.length());
	    
	    // extract the population type
	    line = br.readLine();
	    population = line.substring(15, line.length()-1);
	    if (population.equals("no tool"))
		population = "notool";


	    // find the participant, or create a new one
	    Participant newP = participants.getParticipant(userName, name, population);	    
	    //System.out.println(newP);
	     	    

	    // extract the action performed
	    line = br.readLine();
	    action = line.substring(8, line.length());
	    // get just the first word "copied", "built" or "submitted"
	    int space = action.indexOf(" ");
	    action = action.substring(0, space);

	    sprogram = line.substring(line.length()-1, line.length());
	    program = Integer.parseInt(sprogram);

	    //System.out.println("Action: "+action);
	    //System.out.println("Program: "+program);

	    // extract the time
	    line = br.readLine();

	    if (line.substring(line.length()-2, line.length()).equals("\\c"))
		stime = br.readLine();	    
	    else stime = line.substring(6, line.length());
	    int h = Integer.parseInt(stime.substring(0,2));
	    int m = Integer.parseInt(stime.substring(3,5));
	    int s = Integer.parseInt(stime.substring(6,8));

	    time = h*3600 + m*60 +s;
	    
	    //System.out.println("Time: "+time+"\n");
	    
	    Round round = newP.getRound(program);
		
	    if (action.equals("copied")) {		
		if (round.getStartTime() != 0) {
		    //System.out.println(userName+" copied program " + program + " multiple times!");
		}
		else round.setStartTime(time);
	    }
	    else {
		if (action.equals("submitted")) {
		    if (round.submitted()){
			//System.out.println(userName+" submitted program " + program + " multiple times!");
		    }
		    round.setSubmitted();
		    round.setEndTime(time);
		}

		// get the C++ source file
		line = br.readLine(); // read an empty line
		line = br.readLine(); // read "Source file:"

		source = new StringBuffer();
		line = br.readLine();
		while ((line != null) && (!line.startsWith("From"))) {
		    source = source.append(line).append("\r\n");
		    line = br.readLine();
		}
		
		round.addBuild(time, userName, population, source.toString());

		//System.exit(0);
	    }
	    
	}	
	
	line = br.readLine();
	
    }
    
    br.close();
    
} catch(FileNotFoundException e) {
    System.out.println("File not found: "+filename);
    System.exit(1); }
  catch (IOException e) {
    System.out.println("IOException msg: "+e.getMessage());
    System.exit(1); }
  catch (NumberFormatException e) {
    System.out.println("NumberFormatException msg: "+e.getMessage());
    System.exit(1); }
}


class FieldNotFound extends Exception {
    
    private String fieldName;

    public FieldNotFound(String fieldName) {
	this.fieldName = fieldName;
    }

    public String getMessage() {
	return "The field '"+fieldName+"' was not found in the email message!";
    }
}



public static String printTime(long time) {
    long h = time/3600;
    time = time - h*3600;
    long m = time/60;
    long s = time - m*60;
    return h+":"+m+":"+s;
}



public static void main(String args[]) {
    if (args.length == 0) {
	System.out.println("Usage: java analysis.Analysis files_to_process");
	return;
    }
	
    File file = new File("Participants");
    if (!file.exists())
	file.mkdir();

    for (int i=0; i<args.length; i++)
	process(args[i]);

    // people who didn't participate in the experiment
    participants.removeParticipant("harveyj");
    participants.removeParticipant("bdemsky");

    // people who didn't use the tool
    participants.changePopulation("nigham", "notool");
    participants.changePopulation("daipeng", "notool");
    participants.changePopulation("chenhui", "notool");
    participants.changePopulation("caixia", "notool");
    participants.changePopulation("sajindra", "notool");
    participants.changePopulation("neelu", "notool");
    participants.changePopulation("joey6", "notool");
    
    // people who didn't use the tool in some rounds
    Participant pp = participants.getParticipant("hamailan");
    pp.setPopulation("notool");
    participants.changePopulation("hamailan", 1, "notool");
    participants.changePopulation("hamailan", 2, "notool");
    participants.changePopulation("hamailan", 4, "notool");
    
    pp = participants.getParticipant("sharad");
    pp.setPopulation("notool");
    participants.changePopulation("sharad", 1, "notool");
    participants.changePopulation("sharad", 2, "notool");
    participants.changePopulation("sharad", 3, "notool");

    pp = participants.getParticipant("xieyong");
    pp.setPopulation("notool");
    participants.changePopulation("xieyong", 1, "notool");
    participants.changePopulation("xieyong", 3, "notool");
    participants.changePopulation("xieyong", 4, "notool");

    //participants.changePopulation("catqueen", 3, "notool");    


    participants.fixSubmissions(); //System.out.println("\n");        
    //participants.checkCalltoolsInNotool(); System.out.println("\n");
    //participants.checkNoCalltoolsInTool(); System.out.println("\n");
    participants.setCorrect();

    System.out.println("Participants TOOL:   "+participants.numberParticipants("tool"));
    System.out.println("Participants NOTOOL:   "+participants.numberParticipants("notool"));
    System.out.println("\n");

    participants.printPopulation("tool"); System.out.println("\n");
    
    System.out.println("Rounds TOOL, program 1:   "+participants.numberRounds("tool", 1));
    System.out.println("Rounds NOTOOL, program 1: "+participants.numberRounds("notool", 1));
    System.out.println("\n");

    System.out.println("Rounds TOOL, program 2:   "+participants.numberRounds("tool", 2));
    System.out.println("Rounds NOTOOL, program 2: "+participants.numberRounds("notool", 2));
    System.out.println("\n");

    System.out.println("Rounds TOOL, program 3:   "+participants.numberRounds("tool", 3));
    System.out.println("Rounds NOTOOL, program 3: "+participants.numberRounds("notool", 3));
    System.out.println("\n");

    System.out.println("Rounds TOOL, program 4:   "+participants.numberRounds("tool", 4));
    System.out.println("Rounds NOTOOL, program 4: "+participants.numberRounds("notool", 4));
    System.out.println("\n");
    System.out.println("\n");
		       

    System.out.println("Correct TOOL, program 1:   "+participants.numberCorrect("tool", 1)+"/"+participants.numberRounds("tool", 1));
    System.out.println("Correct NOTOOL, program 1: "+participants.numberCorrect("notool", 1)+"/"+participants.numberRounds("notool", 1));
    System.out.println("\n");
    
    System.out.println("Correct TOOL, program 2:   "+participants.numberCorrect("tool", 2)+"/"+participants.numberRounds("tool", 2));
    System.out.println("Correct NOTOOL, program 2: "+participants.numberCorrect("notool", 2)+"/"+participants.numberRounds("notool", 2));
    System.out.println("\n");
    
    System.out.println("Correct TOOL, program 3:   "+participants.numberCorrect("tool", 3)+"/"+participants.numberRounds("tool", 3));
    System.out.println("Correct NOTOOL, program 3: "+participants.numberCorrect("notool", 3)+"/"+participants.numberRounds("notool", 3));
    System.out.println("\n");

    System.out.println("Correct TOOL, program 4:   "+participants.numberCorrect("tool", 4)+"/"+participants.numberRounds("tool", 4));
    System.out.println("Correct NOTOOL, program 4: "+participants.numberCorrect("notool", 4)+"/"+participants.numberRounds("notool", 4));
    System.out.println("\n");

    
    

    //System.out.println("Total time TOOL:   "+participants.getTotalTime("tool"));
    //System.out.println("Total time NOTOOL: "+participants.getTotalTime("notool"));

    /*
    System.out.println("Total time/participant TOOL:   "+ printTime(participants.getTotalTime("tool")/participants.numberParticipants("tool")));
    System.out.println("Total time/participant NOTOOL: "+printTime(participants.getTotalTime("notool")/participants.numberParticipants("notool")));
    System.out.println("\n");
    */
    
    System.out.println("Program 1 - Total time/participant TOOL:   "+printTime(participants.getTotalTime("tool", 1)/participants.numberRounds("tool", 1)));
    System.out.println("Program 1 - Total time/participant NOTOOL: "+printTime(participants.getTotalTime("notool", 1)/participants.numberRounds("notool", 1)));
    System.out.println("\n");

    System.out.println("Program 2 - Total time/participant TOOL:   "+printTime(participants.getTotalTime("tool", 2)/participants.numberRounds("tool", 2)));
    System.out.println("Program 2 - Total time/participant NOTOOL: "+printTime(participants.getTotalTime("notool", 2)/participants.numberRounds("notool", 2)));
    System.out.println("\n");

    System.out.println("Program 3 - Total time/participant TOOL:   "+printTime(participants.getTotalTime("tool", 3)/participants.numberRounds("tool", 3)));
    System.out.println("Program 3 - Total time/participant NOTOOL: "+printTime(participants.getTotalTime("notool", 3)/participants.numberRounds("notool", 3)));
    System.out.println("\n");

    System.out.println("Program 4 - Total time/participant TOOL:   "+printTime(participants.getTotalTime("tool", 4)/participants.numberRounds("tool", 4)));
    System.out.println("Program 4 - Total time/participant NOTOOL: "+printTime(participants.getTotalTime("notool", 4)/participants.numberRounds("notool", 4)));
    System.out.println("\n");

    
    /*
    Iterator it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();
	System.out.println("User name: "+p.getUserName()+" ("+p.getPopulation()+")");
	System.out.println("Total time: "+printTime(p.getTotalTime())+"\n");
    }	
    */
    
    /*
    Participant p = participants.getParticipant("daipeng");
    System.out.println(p);
    p.printRounds();
    */

    /*
    Round r1 = p.getRound(1);
    System.out.println("p1 - Start time: "+r1.getStartTime());
    System.out.println("p1 - End time: "+r1.getEndTime());
    System.out.println("p1 - Total time: "+r1.getTotalTime());
    */
    
    
}

}
