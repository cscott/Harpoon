package analysis;

/*
  This class manages the participants from an experiment
*/

import java.util.*;


public class Participants {

    private ArrayList participants;


    public Participants() {
	participants = new ArrayList();
    }
    

    // adds a new participant, and returns the newly created object
    public Participant addParticipant(String userName, String name, String population) {
	Participant p = new Participant(userName, name, population);
	participants.add(p);
	return p;
    }


    // adds a new participant, and returns the newly created object
    public Participant addParticipant(String userName) {
	Participant p = new Participant(userName, "", "");
	participants.add(p);
	return p;
    }


    public void removeParticipant(String userName) {
	Participant p = getParticipant(userName);
	participants.remove(p);
    }

    public void changePopulation(String userName, String population) {
	Participant p = getParticipant(userName);	
	p.setPopulation(population);

	p.changePopulation(1, population);
	p.changePopulation(2, population);
	p.changePopulation(3, population);
	p.changePopulation(4, population);		   
    }    

    
    public void changePopulation(String userName, int program, String population) {
	Participant p = getParticipant(userName);	
	p.changePopulation(program, population);
    } 
 
   

    public Participant getParticipant(String userName) {
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    if (p.getUserName().equals(userName))
		return p;
	}
	
	/* if the participant with the given user name wasn't found, 
	   create a new participant */
	return addParticipant(userName);
    }

    
    public Participant getParticipant(String userName, String name, String population) {
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    if (p.getUserName().equals(userName))
		return p;
	}
	
	/* if the participant with the given user name wasn't found, 
	   create a new participant */
	return addParticipant(userName, name, population);
    }


    // if only_correct is true, then we ignore the incorrect solutions
    public long getTotalTime(String population, boolean only_correct) {
	long t = 0;
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    if (p.getPopulation().equals(population))
		t += p.getTotalTime(only_correct);
	}
	
	return t;
    }

  
    // if only_correct is true, then we ignore the incorrect solutions
    public long getTotalTime(String population, int program, boolean only_correct) {
	long t = 0;
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    t += p.getTotalTime(population, program, only_correct);
	}
	
	return t;
    }

  
    public int numberParticipants(String population) {
	int n = 0;
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    if (p.getPopulation().equals(population))
		n++;
	}
	
	return n;
    }


    public int numberRounds(String population, int program) {
	int n = 0;
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    n += p.numberRounds(population, program);
	}
	
	return n;
    }


    public int numberCorrectRounds(String population, int program) {
	int n = 0;
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    n += p.numberCorrectRounds(population, program);
	}
	
	return n;
    }


    public Iterator iterator() {
	return participants.iterator();
    }


    public void fixSubmissions() {
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    p.fixSubmissions();
	}	
    }


    /* check if there are any builds from the notool population
       with invokations to "calltool"
    */
    public void checkCalltoolsInNotool() {
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    if (p.getPopulation().equals("notool"))
		p.checkCalltoolsInNotool();
	}	
    }


    /* check if there are any rounds from the tool population
       with NO invokations to "calltool"
    */
    public void checkNoCalltoolsInTool() {
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    if (p.getPopulation().equals("tool"))
		p.checkNoCalltoolsInTool();
	}	
    }


    public void printPopulation(String population) {
	System.out.println("Population "+population+": ");
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    if (p.getPopulation().equals(population))
		System.out.print(p.getUserName()+" ");
	}	
    }


    public int numberCorrect(String population, int program) {
	int n = 0;
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    n += p.numberCorrect(population, program);
	}
	return n;
    }


    public void setCorrect(String username, int program, boolean correct) {
	Participant p = getParticipant(username);
	Round r = p.getRound(program);

	r.setCorrect(correct);
	
	if (!correct) {
	    r.setStartTime(0);
	    r.setEndTime(2*3600);
	}
    }


    // manually set what programs are correctly solved
    public void setCorrect() {	
	setCorrect("aakrosh", 1, true);
	setCorrect("aakrosh", 3, true);
	setCorrect("aakrosh", 2, false);
	setCorrect("aakrosh", 4, true);

	setCorrect("catqueen", 1, true);
	setCorrect("catqueen", 2, true);
	setCorrect("catqueen", 3, true);
	setCorrect("catqueen", 4, true);

	setCorrect("donnysoh", 3, true);
	setCorrect("donnysoh", 1, false);
	setCorrect("donnysoh", 2, false);
	setCorrect("donnysoh", 4, false);

	setCorrect("ianhall", 1, true);
	setCorrect("ianhall", 2, true);
	setCorrect("ianhall", 3, true);
	setCorrect("ianhall", 4, true);

	setCorrect("namit_c", 1, true);
	setCorrect("namit_c", 2, true);
	setCorrect("namit_c", 3, true);
	setCorrect("namit_c", 4, false);


	
	setCorrect("advait", 1, true);
	setCorrect("advait", 2, true);
	setCorrect("advait", 3, true);
	setCorrect("advait", 4, true);

	setCorrect("aiai", 1, true);
	setCorrect("aiai", 2, true);
	setCorrect("aiai", 3, true);
	setCorrect("aiai", 4, true);

	setCorrect("anghk", 3, true);
	setCorrect("anghk", 1, false);
	setCorrect("anghk", 2, false);
	setCorrect("anghk", 4, false);


	setCorrect("caixia", 1, true);
	setCorrect("caixia", 2, true);
	setCorrect("caixia", 3, true);
	setCorrect("caixia", 4, true);

	setCorrect("chengxia", 1, true);
	setCorrect("chengxia", 2, true);
	setCorrect("chengxia", 3, true);
	setCorrect("chengxia", 4, false);

	setCorrect("chenhui", 2, true);
	setCorrect("chenhui", 3, true);
	setCorrect("chenhui", 4, true);
	setCorrect("chenhui", 1, false);

	setCorrect("cpyee", 1, false);
	setCorrect("cpyee", 2, false);
	setCorrect("cpyee", 3, false);
	setCorrect("cpyee", 4, false);

	setCorrect("daipeng", 2, true);
	setCorrect("daipeng", 3, true);
	setCorrect("daipeng", 4, true);
	setCorrect("daipeng", 1, false);
	
	setCorrect("girish_k", 3, true);
	setCorrect("girish_k", 1, false);
	setCorrect("girish_k", 2, false);
	setCorrect("girish_k", 4, false);

	setCorrect("hamailan", 3, true);
	setCorrect("hamailan", 1, false);
	setCorrect("hamailan", 2, false);
	setCorrect("hamailan", 4, false);

	setCorrect("jiahui81", 3, true);
	setCorrect("jiahui81", 1, false);
	setCorrect("jiahui81", 2, false);
	setCorrect("jiahui81", 4, false);

	setCorrect("jingwei", 2, true);
	setCorrect("jingwei", 3, true);
	setCorrect("jingwei", 4, true);
	setCorrect("jingwei", 1, false);

	setCorrect("joey6", 3, true);
	setCorrect("joey6", 1, false);
	setCorrect("joey6", 2, false);
	setCorrect("joey6", 4, false);

	setCorrect("josekj", 2, true);
	setCorrect("josekj", 3, true);
	setCorrect("josekj", 4, true);
	setCorrect("josekj", 1, false);
	
	setCorrect("neelu", 3, true);
	setCorrect("neelu", 1, false);
	setCorrect("neelu", 2, false);
	setCorrect("neelu", 4, false);

	setCorrect("nelly", 2, true);
	setCorrect("nelly", 4, true);
	setCorrect("nelly", 1, false);
	setCorrect("nelly", 3, false);

	setCorrect("nguyenh2", 2, true);
	setCorrect("nguyenh2", 3, true);
	setCorrect("nguyenh2", 1, false);
	setCorrect("nguyenh2", 4, false);

	setCorrect("nigham", 1, true);
	setCorrect("nigham", 2, true);
	setCorrect("nigham", 3, true);
	setCorrect("nigham", 4, true);

	setCorrect("philipt", 1, true);
	setCorrect("philipt", 3, true);
	setCorrect("philipt", 4, true);
	setCorrect("philipt", 2, false);

	setCorrect("sajindra", 1, true);
	setCorrect("sajindra", 2, false);
	setCorrect("sajindra", 3, false);
	setCorrect("sajindra", 4, false);
	
	setCorrect("saroop", 3, true);
	setCorrect("saroop", 4, true);
	setCorrect("saroop", 1, false);
	setCorrect("saroop", 2, false);

	setCorrect("sharad", 3, true);
	setCorrect("sharad", 4, true);
	setCorrect("sharad", 1, false);
	setCorrect("sharad", 2, false);

	setCorrect("xieyong", 1, true);
	setCorrect("xieyong", 2, true);
	setCorrect("xieyong", 3, true);
	setCorrect("xieyong", 4, true);

	setCorrect("xujin", 1, true);
	setCorrect("xujin", 3, true);
	setCorrect("xujin", 2, false);
	setCorrect("xujin", 4, false);

	setCorrect("zhoulei", 2, true);
	setCorrect("zhoulei", 3, true);
	setCorrect("zhoulei", 4, true);
	setCorrect("zhoulei", 1, false);
    }
}
