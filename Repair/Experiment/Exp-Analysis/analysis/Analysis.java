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

    String hours, minutes, seconds;
    hours = ""+h;

    if (m>=10)
	minutes = ""+m;
    else minutes = "0"+m;
    
    if (s>=10)
	seconds = ""+s;
    else seconds = "0"+s;
    
    return hours+":"+minutes+":"+seconds;
}



public static String printTime2(long time) {
    if ( (time == 12000) || (time == 0) )
	return "Not solved";
    if (time < 0)
	return "Not provided";

    return printTime(time);
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
    /*
    participants.changePopulation("nigham", "notool");
    participants.changePopulation("daipeng", "notool");
    participants.changePopulation("chenhui", "notool");
    participants.changePopulation("caixia", "notool");
    participants.changePopulation("sajindra", "notool");
    participants.changePopulation("neelu", "notool");
    participants.changePopulation("joey6", "notool");
    */
    
    // people who didn't use the tool in some rounds
    /*
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
    */

    participants.fixSubmissions(); //System.out.println("\n");        
    //participants.checkCalltoolsInNotool(); System.out.println("\n");
    //participants.checkNoCalltoolsInTool(); System.out.println("\n");
    participants.setCorrect();

    //participants.printPopulation("tool"); System.out.println("\n");
        	
    participants.setExperience();
    participants.sortByExperience();    

    participants.setOrder();


    //printCorrectnessVsExperience();
    //printTimeVsExperience(4);

    participants.setLocalized();
    //printLocalizedVsExperience(3);

    //printStatistics();

    //printParticipants();

    printTimeVsExperience(1, 1);

}


public static void printStatistics()
{
    int noTotal, noCorrect, percentage;
    long totalTime, avTime, localizeTime, avLocalizedTime;

    System.out.println("Note:  When computing average times, we ignore incorrect solutions and unreported times\n");

    for (int prg=1; prg<=4; prg++) {	   
	System.out.println("Program "+prg);
	if (prg<=2)
	    System.out.println("========= (DS corruption)");
	else System.out.println("=========");

	System.out.println("Correct solutions:"); 

	noTotal = participants.numberRounds("tool", prg);
	noCorrect = participants.numberCorrectRounds("tool", prg);
	percentage = (100*noCorrect)/noTotal;
	System.out.println("   TOOL:   "+noCorrect+"/"+noTotal+" ("+percentage+"%)");
	
	noTotal = participants.numberRounds("notool", prg);
	noCorrect = participants.numberCorrectRounds("notool", prg);
	percentage = (100*noCorrect)/noTotal;
	System.out.println("   NOTOOL: "+noCorrect+"/"+noTotal+" ("+percentage+"%)");
	

	System.out.println("\nTotal time to fix/participant:");	
	noTotal = participants.numberCorrectRounds("tool", prg);
	totalTime = participants.getTotalTime("tool", prg, true);
	avTime = totalTime/noTotal;
	System.out.println("   TOOL:   "+printTime(avTime));

	noTotal = participants.numberCorrectRounds("notool", prg);
	totalTime = participants.getTotalTime("notool", prg, true);
	avTime = totalTime/noTotal;
	System.out.println("   NOTOOL: "+printTime(avTime));



	System.out.println("\nTotal time to localize/participant:");
	noTotal = participants.numberCorrectAndLocalizedRounds("tool", prg);
	localizeTime = 60*participants.getLocalizedTime("tool", prg);
	avTime = localizeTime/noTotal;
	System.out.println("   TOOL:   "+printTime(avTime));

	noTotal = participants.numberCorrectAndLocalizedRounds("notool", prg);
	localizeTime = 60*participants.getLocalizedTime("notool", prg);
	avTime = localizeTime/noTotal;
	System.out.println("   NOTOOL: "+printTime(avTime));

	System.out.println("\n");
    }
}



public static void printParticipants() {
    Iterator it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	System.out.println("User name: "+p.getUserName());
	System.out.println("Population: "+p.getPopulation());
	System.out.println("Experience: "+p.getExperience());
	System.out.println("Correct solutions: "+p.numberCorrectRounds());
	System.out.println("Total time to fix: "+printTime(p.getTotalTime(true)));
	System.out.println("Total time to localize: "+printTime(60*p.getLocalizedTime())+"\n");
	
	for (int i=1; i<=4; i++)
	    System.out.println("Time to fix program "+i+": "+printTime2(p.getTotalTime(i, true)));
	
	System.out.println();

	for (int i=1; i<=4; i++)
	    System.out.println("Time to localize bug "+i+": "+printTime2(60*p.getLocalizedTime(i)));
	
      	System.out.println("==========================================\n\n");
    }	
}


public static void printCorrectnessVsExperience() {
    // Tool
    Iterator it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if (p.getPopulation().equals("tool"))
	    System.out.println(p.getExperience()+" "+p.numberCorrectRounds());
    }
    
    // NoTool
    System.out.println();
    it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if (p.getPopulation().equals("notool"))
	    System.out.println(p.getExperience()+" "+p.numberCorrectRounds());
    }
}


public static void printTimeVsExperience(int program) {
    // Tool
    Iterator it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if (p.getPopulation().equals("tool"))
	    System.out.println(p.getExperience()+" "+p.getTotalTime(program, false));
    }
    
    // NoTool
    System.out.println();
    it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if (p.getPopulation().equals("notool"))
	    System.out.println(p.getExperience()+" "+p.getTotalTime(program, false));
    }
}


public static void printLocalizedVsExperience(int program) {
    // Tool
    Iterator it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if (p.getPopulation().equals("tool"))
	    if (p.getLocalized(program, false)>0)
		System.out.println(p.getExperience()+" "+60*p.getLocalized(program, false));
	    else System.out.println(p.getExperience()+" "+p.getLocalized(program, false));
    }
    
    // NoTool
    System.out.println();
    it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if (p.getPopulation().equals("notool"))
	    if (p.getLocalized(program, false)>0)
		System.out.println(p.getExperience()+" "+60*p.getLocalized(program, false));
	    else System.out.println(p.getExperience()+" "+p.getLocalized(program, false));
    }
}



public static void printTimeVsExperience(int program, int order) {
    // Tool
    Iterator it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if ( (p.getPopulation().equals("tool")) && (p.getOrder() == 1) )
	    System.out.println(p.getExperience()+" "+p.getTotalTime(program, false));
    }
    
    // NoTool
    System.out.println();
    it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if ( (p.getPopulation().equals("notool")) && (p.getOrder() == order) )
	    System.out.println(p.getExperience()+" "+p.getTotalTime(program, false));
    }
}


public static void printLocalizedVsExperience(int program, int order) {
    // Tool
    Iterator it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if (p.getPopulation().equals("tool"))
	    if (p.getLocalized(program, false)>0)
		System.out.println(p.getExperience()+" "+60*p.getLocalized(program, false));
	    else System.out.println(p.getExperience()+" "+p.getLocalized(program, false));
    }
    
    // NoTool
    System.out.println();
    it = participants.iterator();
    while (it.hasNext()) {
	Participant p = (Participant) it.next();	
	if (p.getPopulation().equals("notool"))
	    if (p.getLocalized(program, false)>0)
		System.out.println(p.getExperience()+" "+60*p.getLocalized(program, false));
	    else System.out.println(p.getExperience()+" "+p.getLocalized(program, false));
    }
}



}
