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


    public long getLocalizedTime(String population, int program) {
	long t = 0;
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    t += p.getLocalizedTime(population, program);
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


    public int numberCorrectAndLocalizedRounds(String population, int program) {
	int n = 0;
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    n += p.numberCorrectAndLocalizedRounds(population, program);
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
	    n += p.numberCorrectRounds(population, program);
	}
	return n;
    }


    public void setCorrect(String username, int program, boolean correct) {
	Participant p = getParticipant(username);
	Round r = p.getRound(program);

	r.setCorrect(correct);
	
	if (!correct) {
	    r.setStartTime(0);
	    r.setEndTime(12000);
	}
    }


    // manually set what programs are correctly solved
    public void setCorrect() {	
	setCorrect("aakrosh", 1, true);
	setCorrect("aakrosh", 3, true);
	setCorrect("aakrosh", 2, false);
	setCorrect("aakrosh", 4, false);
	
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

	setCorrect("catqueen", 1, true);
	setCorrect("catqueen", 2, true);
	setCorrect("catqueen", 3, true);
	setCorrect("catqueen", 4, true);

	setCorrect("chengxia", 1, true);
	setCorrect("chengxia", 2, true);
	setCorrect("chengxia", 3, true);
	setCorrect("chengxia", 4, true);

	setCorrect("chenhui", 2, true);
	setCorrect("chenhui", 3, true);
	setCorrect("chenhui", 4, true);
	setCorrect("chenhui", 1, true);

	setCorrect("cpyee", 1, false);
	setCorrect("cpyee", 2, false);
	setCorrect("cpyee", 3, false);
	setCorrect("cpyee", 4, false);

	setCorrect("daipeng", 2, true);
	setCorrect("daipeng", 3, true);
	setCorrect("daipeng", 4, true);
	setCorrect("daipeng", 1, false);

	setCorrect("donnysoh", 3, true);
	setCorrect("donnysoh", 1, false);
	setCorrect("donnysoh", 2, false);
	setCorrect("donnysoh", 4, false);
	
	setCorrect("girish_k", 3, true);
	setCorrect("girish_k", 1, false);
	setCorrect("girish_k", 2, false);
	setCorrect("girish_k", 4, false);

	setCorrect("hamailan", 3, true);	
	setCorrect("hamailan", 2, true);
	setCorrect("hamailan", 4, true);
	setCorrect("hamailan", 1, false);

	setCorrect("ianhall", 1, true);
	setCorrect("ianhall", 2, true);
	setCorrect("ianhall", 3, true);
	setCorrect("ianhall", 4, true);

	setCorrect("jiahui81", 3, true);
	setCorrect("jiahui81", 1, false);
	setCorrect("jiahui81", 2, false);
	setCorrect("jiahui81", 4, false);

	setCorrect("jingwei", 2, true);
	setCorrect("jingwei", 3, true);
	setCorrect("jingwei", 4, true);
	setCorrect("jingwei", 1, false);
		
	setCorrect("joey6", 2, true);
	setCorrect("joey6", 3, true);	
	setCorrect("joey6", 4, true);
	setCorrect("joey6", 1, false);

	setCorrect("josekj", 2, true);
	setCorrect("josekj", 3, true);
	setCorrect("josekj", 4, true);
	setCorrect("josekj", 1, false);

	setCorrect("namit_c", 1, true);
	setCorrect("namit_c", 2, true);
	setCorrect("namit_c", 3, true);
	setCorrect("namit_c", 4, false);
	
	setCorrect("neelu", 3, true);
	setCorrect("neelu", 1, false);
	setCorrect("neelu", 2, false);
	setCorrect("neelu", 4, false);

	setCorrect("nelly", 2, true);
	setCorrect("nelly", 3, true);
	setCorrect("nelly", 4, true);
	setCorrect("nelly", 1, true);	

	setCorrect("nguyenh2", 2, true);
	setCorrect("nguyenh2", 3, true);
	setCorrect("nguyenh2", 4, true);
	setCorrect("nguyenh2", 1, false);

	setCorrect("nigham", 1, true);
	setCorrect("nigham", 2, true);
	setCorrect("nigham", 3, true);
	setCorrect("nigham", 4, true);

	setCorrect("philipt", 1, true);
	setCorrect("philipt", 3, true);
	setCorrect("philipt", 4, true);
	setCorrect("philipt", 2, true);

	setCorrect("sajindra", 1, true);
	setCorrect("sajindra", 2, true);
	setCorrect("sajindra", 3, true);
	setCorrect("sajindra", 4, true);
	
	setCorrect("saroop", 3, true);
	setCorrect("saroop", 4, true);
	setCorrect("saroop", 1, false);
	setCorrect("saroop", 2, false);
	
	setCorrect("sharad", 4, true);
	setCorrect("sharad", 3, false);
	setCorrect("sharad", 1, false);
	setCorrect("sharad", 2, false);

	setCorrect("xieyong", 1, true);
	setCorrect("xieyong", 2, true);
	setCorrect("xieyong", 3, true);
	setCorrect("xieyong", 4, true);

	setCorrect("xujin", 3, true);
	setCorrect("xujin", 1, false);
	setCorrect("xujin", 2, false);
	setCorrect("xujin", 4, false);
	
	setCorrect("zhoulei", 2, true);
	setCorrect("zhoulei", 3, true);
	setCorrect("zhoulei", 4, true);	
	setCorrect("zhoulei", 1, false);
    }


    public void setLocalized(String username, int program, long minutes) {
	Participant p = getParticipant(username);
	Round r = p.getRound(program);

	r.setLocalized(minutes);
    }


    public void setLocalized(String username, int program, boolean f) {
	if (f != false)
	    return;

	setLocalized(username, program, 200);
    }


    // manually set the time spent to localize the bugs
    public void setLocalized() {
	setLocalized("aakrosh", 1, -1000);     // not provided
	setLocalized("aakrosh", 3, 120);
	setLocalized("aakrosh", 2, false);
	setLocalized("aakrosh", 4, false);
	
	setLocalized("advait", 1, 74);
	setLocalized("advait", 2, 15);
	setLocalized("advait", 3, 28);
	setLocalized("advait", 4, 17);

	setLocalized("aiai", 1, 12);
	setLocalized("aiai", 2, 15);
	setLocalized("aiai", 3, 44);
	setLocalized("aiai", 4, 10);

	setLocalized("anghk", 1, false);
	setLocalized("anghk", 2, false);
	setLocalized("anghk", 3, -1000);       // not provided
	setLocalized("anghk", 4, false);

	setLocalized("caixia", 1, 50);
	setLocalized("caixia", 2, 5);
	setLocalized("caixia", 3, 10);
	setLocalized("caixia", 4, 5);

	setLocalized("catqueen", 1, 63);
	setLocalized("catqueen", 2, 10);
	setLocalized("catqueen", 3, 2);
	setLocalized("catqueen", 4, 28);

	setLocalized("chengxia", 1, -1000);    // not provided
	setLocalized("chengxia", 2, 9);
	setLocalized("chengxia", 3, 5);
	setLocalized("chengxia", 4, 5);

	setLocalized("chenhui", 1, 47);
	setLocalized("chenhui", 2, 34);
	setLocalized("chenhui", 3, 4);
	setLocalized("chenhui", 4, 22);

	setLocalized("cpyee", 1, false);
	setLocalized("cpyee", 2, false);
	setLocalized("cpyee", 3, false);
	setLocalized("cpyee", 4, false);

	setLocalized("daipeng", 1, false);
	setLocalized("daipeng", 2, 12);
	setLocalized("daipeng", 3, 64);
	setLocalized("daipeng", 4, 11);
       
	setLocalized("donnysoh", 1, false);
	setLocalized("donnysoh", 2, false);
	setLocalized("donnysoh", 3, 125);
	setLocalized("donnysoh", 4, false);
	
	setLocalized("girish_k", 1, false);
	setLocalized("girish_k", 2, false);
	setLocalized("girish_k", 3, 45);
	setLocalized("girish_k", 4, false);

	setLocalized("hamailan", 1, false);
	setLocalized("hamailan", 2, 3);
	setLocalized("hamailan", 3, 104);
	setLocalized("hamailan", 4, 2);

	setLocalized("ianhall", 1, 28);
	setLocalized("ianhall", 2, 3);
	setLocalized("ianhall", 3, 94);
	setLocalized("ianhall", 4, 8);

	setLocalized("jiahui81", 1, false);
	setLocalized("jiahui81", 2, false);
	setLocalized("jiahui81", 3, -1000);       // not provided
	setLocalized("jiahui81", 4, false);

	setLocalized("jingwei", 1, false);
	setLocalized("jingwei", 2, 20);
	setLocalized("jingwei", 3, 9);
	setLocalized("jingwei", 4, 20);
		
	setLocalized("joey6", 1, false);
	setLocalized("joey6", 2, -1000);          // not provided
	setLocalized("joey6", 3, 10);	
	setLocalized("joey6", 4, -1000);          // not provided

	setLocalized("josekj", 1, false);
	setLocalized("josekj", 2, 17);
	setLocalized("josekj", 3, 50);
	setLocalized("josekj", 4, 40);

	setLocalized("namit_c", 1, 25);
	setLocalized("namit_c", 2, 5);
	setLocalized("namit_c", 3, 9);
	setLocalized("namit_c", 4, false);
	
	setLocalized("neelu", 1, false);
	setLocalized("neelu", 2, false);
	setLocalized("neelu", 3, 35);
	setLocalized("neelu", 4, false);

	setLocalized("nelly", 1, 23);	
	setLocalized("nelly", 2, 8);
	setLocalized("nelly", 3, 88);
	setLocalized("nelly", 4, 11);

	setLocalized("nguyenh2", 1, false);
	setLocalized("nguyenh2", 2, 12);
	setLocalized("nguyenh2", 3, 38);
	setLocalized("nguyenh2", 4, 3);

	setLocalized("nigham", 1, 20);
	setLocalized("nigham", 2, 1);
	setLocalized("nigham", 3, 35);
	setLocalized("nigham", 4, 4);

	setLocalized("philipt", 1, -1000);        // not provided
	setLocalized("philipt", 2, 19); 
	setLocalized("philipt", 3, 4);
	setLocalized("philipt", 4, 1);

	setLocalized("sajindra", 1, 0);
	setLocalized("sajindra", 2, 40);
	setLocalized("sajindra", 3, 3);
	setLocalized("sajindra", 4, 5);
	
	setLocalized("saroop", 1, false);
	setLocalized("saroop", 2, false);
	setLocalized("saroop", 3, 13);
	setLocalized("saroop", 4, 5);
       
	setLocalized("sharad", 1, false);
	setLocalized("sharad", 2, false);
	setLocalized("sharad", 4, 15);
	setLocalized("sharad", 3, false);

	setLocalized("xieyong", 1, 128);
	setLocalized("xieyong", 2, -1000);        // not provided
	setLocalized("xieyong", 3, 5);
	setLocalized("xieyong", 4, 5);

	setLocalized("xujin", 1, false);
	setLocalized("xujin", 2, false);
	setLocalized("xujin", 3, 60);
	setLocalized("xujin", 4, false);
	
	setLocalized("zhoulei", 1, false);
	setLocalized("zhoulei", 2, 2);
	setLocalized("zhoulei", 3, 30);
	setLocalized("zhoulei", 4, 6);	
    }


    public void setExperience(String username, double experience) {
	Participant p = getParticipant(username);
	p.setExperience(experience);
    }


    // manually set the experience level of each participant
    public void setExperience() {	
	setExperience("aakrosh", 5);
	setExperience("advait", 10.5);
	setExperience("aiai", 4.5);
	setExperience("anghk", 5.5);
	setExperience("caixia", 4.5);
	setExperience("catqueen", 8.5);
	setExperience("chengxia", 5.5);
	setExperience("chenhui", 4.5);
	setExperience("cpyee", 2.5);
	setExperience("daipeng", 1.16);
	setExperience("donnysoh", 8);
	setExperience("girish_k", 9);
	setExperience("hamailan", 0.99);
	setExperience("ianhall", 4.4);
	setExperience("jiahui81", 9.25);
	setExperience("jingwei", 6.5);
	setExperience("joey6", 10.5);
	setExperience("josekj", 12);
	setExperience("namit_c", 4.5);
	setExperience("neelu", 6.5);
	setExperience("nelly", 4.5);
	setExperience("nguyenh2", 4.5);
	setExperience("nigham", 14);
	setExperience("philipt", 5.5);
	setExperience("sajindra", 5.5);
	setExperience("saroop", 14.5);
	setExperience("sharad", 12.5);
	setExperience("xieyong", 9.5);
	setExperience("xujin", 0.8);
	setExperience("zhoulei", 4.25);
    }


    // sort by experience level
    public void sortByExperience() {
	Collections.sort(participants);
    }


    public void setOrder() {
	Iterator it = participants.iterator();
	while (it.hasNext()) {
	    Participant p = (Participant) it.next();
	    if (p.getRoundNumber(0).getProgram() == 1)
		p.setOrder(1);
	    else p.setOrder(2);
	}	
    }

}
