package javax.realtime;
import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Iterator;

public class TestSched extends Scheduler {
    static TestSched instance = null;
    public static final int MAX_THREADS = 5;
    public long threads[] = new long[MAX_THREADS];
    public long period[] = new long[MAX_THREADS];
    public long cost[] = new long[MAX_THREADS];
    public long work[] = new long[MAX_THREADS];
    public long utility[] = new long[MAX_THREADS];
    public long nextArrivals[] = new long[MAX_THREADS];
    public long lastActivation[] = new long[MAX_THREADS];
    public long numFires[] = new long[MAX_THREADS];
    public long success[] = new long[MAX_THREADS];
    public boolean done[] = new boolean[MAX_THREADS];
    public double PVD[] = new double[MAX_THREADS];
    public int sortedPVD[] = new int[MAX_THREADS-2];
    public int sched[] = new int[MAX_THREADS-2];
    public int tentSched[] = new int[MAX_THREADS-2];
    public int numRounds, totalUtility, highPVDThread, tentSchedIndex, chosenThread;
    public int highPVDIndex;
    public boolean settingUp = true;
    public long quanta, nxtSchdPt, kickOff, nextArrival, timeElapsed;
    public double highPVD;

    public static TestSched instance() {
	if (instance == null) {
	    ImmortalMemory.instance().enter(new Runnable() {
		    public void run() {
			instance = new TestSched();
		    }
		});
	}
	return instance;
    }
    protected TestSched() {
	super();
	contextSwitch();
    }
    protected void initValues() {
	//	period[2]=10000;      period[3]=6000;       period[4]=5000;
	//	cost[2]=6000;         cost[3]=4000;         cost[4]=3000;
	//	utility[2]=20;        utility[3]=15;        utility[4]=9;
	numFires[2]=1;        numFires[3]=1;        numFires[4]=1;
    }
    public long chooseThread(long currentTime) {
	if ((++numRounds == 30) || deadlock()) System.exit(-1);
	if (settingUp && (!everyoneHere(currentTime))) {
	    System.out.println("Still dancing, and not everyone here...");
	    activate(1,1,currentTime);
	    return 1;
	}
	if ((threads[2]==0) || (threads[3]==0) || (threads[4]==0)) {
	    System.out.println("2, 3, or 4  blocked!... exiting...");
	    System.exit(-1);
	}
	timeElapsed = currentTime-kickOff;
	System.out.println("Time: "+(timeElapsed/1000)+"\t("+timeElapsed+")");
	updateWork(currentTime);
	nextArrival = findNxtArvl(currentTime);
	highPVDThread = updatePVD(currentTime);
	sortByPVD();
	System.out.println("********SortedPVD: ");
	for (int i=0;i<3;i++) System.out.print(sortedPVD[i]+"\t");
	System.out.println();
	clearTentSched();
	clearSched();
	for (int i=2;i<MAX_THREADS;i++) if(!done[i]) insertIntoTentSched(i);
	
	System.out.println("********TentSched: ");
	for (int j=0;j<3;j++) System.out.print(tentSched[j]+"\t");
	System.out.println();
	
	while (!tentSchedFeasible()) {

	    System.out.println("********Sched: ");
	    for (int k=0;k<3;k++) System.out.print(sched[k]+"\t");
	    System.out.println();
	    
	    System.out.println("Sched not feasible, dropping low PVD");
	    dropLowPVD();
	}
	commitTentSched();
	

	/*
	for (int i=0;i<MAX_THREADS-2;i++) {
	    if (sortedPVD[i] != 0) {
		System.out.println("inserting: "+sortedPVD[i]);
		insertIntoTentSched(sortedPVD[i]);
		if (tentSchedFeasible()) commitTentSched();
		else takeItOut(sortedPVD[i]);
	    }
	    System.out.println("********TentSched: ");
	    for (int j=0;j<3;j++) System.out.print(tentSched[j]+"\t");
	    System.out.println();

	    System.out.println("********Sched: ");
	    for (int k=0;k<3;k++) System.out.print(sched[k]+"\t");
	    System.out.println();
	}
	*/
	if (sched[0] > 0) {
	    long workToDo = cost[(int)sched[0]]-work[(int)sched[0]];
	    activate(sched[0],(workToDo<nextArrival?workToDo:nextArrival),currentTime);
	    return sched[0];
	}
	else {
	    activate(1,nextArrival,currentTime);
	    return 1;
	}
    }
    protected void dropLowPVD() {
	double low = Double.MAX_VALUE;
	int lowIndex = 0;
	for (int i=0;i<MAX_THREADS-2;i++) {
	    if (tentSched[i] != 0) {
		if (PVD[tentSched[i]] < low) {
		    low = PVD[i];
		    lowIndex = i;
		}
	    }
	}
	if (lowIndex == 0) {
	    tentSched[0] = tentSched[1];
	    tentSched[1] = tentSched[2];
	    tentSched[2] = 0;
	}
	if (lowIndex == 1) {
	    tentSched[1] = tentSched[2];
	    tentSched[2] = 0;
	}
	if (lowIndex == 2) tentSched[2] = 0;
    }
    protected void commitTentSched() {
	for (int i=0;i<MAX_THREADS-2;i++) sched[i] = tentSched[i];
    }
    protected void insertIntoTentSched(int threadID) {
	long deadline = numFires[threadID]*period[threadID];
	//       	System.out.println(">>>>>>>>   insertIntoTentSched:");
	/*
	System.out.println("deadline: "+deadline);
	System.out.println("tentSchedIndex: "+tentSchedIndex);
	System.out.println("Sched[0] deadline: "
			   +(numFires[sched[0]]*period[sched[0]]));
	*/
	if (tentSchedIndex == 0) tentSched[0] = threadID;
	if (tentSchedIndex == 1) {
	    if (deadline < numFires[tentSched[0]]*period[tentSched[0]]) {
		tentSched[1] = tentSched[0];
		tentSched[0] = threadID;
	    }
	    else tentSched[1] = threadID;
	}
	if (tentSchedIndex == 2) {
	    if (deadline <= numFires[tentSched[0]]*period[tentSched[0]]) {
		tentSched[2] = tentSched[1];
		tentSched[1] = tentSched[0];
		tentSched[0] = threadID;
	    }
	    else if (deadline > numFires[tentSched[1]]*period[tentSched[1]]) 
		tentSched[2]=threadID;
	    else {
		tentSched[2] = tentSched[1];
		tentSched[1] = threadID;
	    }
	}
	tentSchedIndex++;
    }
    protected void takeItOut(int id) {
	int index = 0;
	for (int i=0;i<MAX_THREADS-2;i++) {
	    if (tentSched[i]==id) {
		tentSched[i] = 0;
		index = i;
	    }
	}
	if (index == 0) {
	    tentSched[0] = tentSched[1];
	    tentSched[1] = tentSched[2];
	    tentSched[2] = 0;
	}
	if (index == 1) {
	    tentSched[1] = tentSched[2];
	    tentSched[2] = 0;
	}
  	if (index == 2) tentSched[2] = 0;
	tentSchedIndex--;
    }
    protected boolean tentSchedFeasible() {
	long workLeft = 0;
	long timeLeft = 0;
	long nextDeadline = 0;
	//	if ((tentSched[0] == 0) && (sched[0] == 0)) return true;
	for (int i=0;i<tentSchedIndex;i++) {
	    workLeft += (cost[tentSched[i]]-work[tentSched[i]]);
	    nextDeadline = numFires[tentSched[i]]*period[tentSched[i]];
	    if ((nextDeadline - timeElapsed) > timeLeft) 
		timeLeft = nextDeadline - timeElapsed;
	}
	if (workLeft > timeLeft) {
	    System.out.println("+++++ feasible: NO  -- workLeft: "+workLeft
			       +"\ttimeLeft: "+timeLeft);
	    return false;
	}
	else {
	    System.out.println("+++++ feasible: YES -- workLeft: "+workLeft
			       +"\ttimeLeft: "+timeLeft);
	    return true;
	}
    }
    protected void updateWork(long now) {
	/*
	System.out.print("Work before: ");
	for (int i=2;i<5;i++) System.out.print(i+": "+work[i]+"\t");
	System.out.println();
	*/
	if (chosenThread > 1) {
	    if ((work[chosenThread] < cost[chosenThread]) && !done[chosenThread]) {
		System.out.println("updating work of thread: "+chosenThread+"\tfor: "
				   +(now-lastActivation[chosenThread]));
		work[chosenThread] += (now - lastActivation[chosenThread]);
	    }
	    if ((work[chosenThread] >= cost[chosenThread]) && !done[chosenThread]) {
		// thread met deadline...
		totalUtility += utility[chosenThread];
		work[chosenThread] = 0;
		done[chosenThread] = true;
		System.out.println(chosenThread+"   just got done");
		success[chosenThread]++;
	    }
	    /*
      	    if ((cost[chosenThread]>period[chosenThread]-
		 (now-lastActivation[chosenThread])
		 && !done[chosenThread])) {
		// deadbeat thread, can't meet deadline.
		done[chosenThread] = true;
	    }
	    */
	}
	for (int i=2;i<MAX_THREADS;i++) {
	    if ((timeElapsed-(numFires[i]*period[i])) >= 0) {
		done[i] = false;
		work[i] = 0;
		numFires[i]++;
		System.out.println("Activation for thread "+i+": "+numFires[i]);
	    }
	}
	/*
	System.out.print("Work before: ");
	for (int i=2;i<5;i++) System.out.print(i+": "+work[i]+"\t");
	System.out.println();
	*/
    }
    protected void clearTentSched() {
	tentSchedIndex = 0;
	for (int i=0;i<MAX_THREADS-2;i++) tentSched[i] = 0;
    }
    protected void clearSched() {
	for (int i=0;i<MAX_THREADS-2;i++) sched[i] = 0;
    }
    protected void sortByPVD() {
	for (int i=0;i<MAX_THREADS-2;i++) sortedPVD[i] = 0;
	sortedPVD[0] = highPVDIndex;
	if (highPVDIndex == 1) sortedPVD[0] = 0;
	if (highPVDIndex == 2) {
	    sortedPVD[1] = (PVD[3]>PVD[4] ? 3:4);
	    sortedPVD[2] = (PVD[3]<PVD[4] ? 3:4);
	}
	if (highPVDIndex == 3) {
	    sortedPVD[1] = (PVD[2]>PVD[4] ? 2:4);
	    sortedPVD[2] = (PVD[2]<PVD[4] ? 2:4);
	}
	if (highPVDIndex == 4) {
	    sortedPVD[1] = (PVD[2]>PVD[3] ? 2:3);
	    sortedPVD[2] = (PVD[2]<PVD[3] ? 2:3);
	}
	for (int i=0;i<MAX_THREADS-2;i++) {
	    if (done[sortedPVD[i]]) sortedPVD[i] = 0;
	}
	/*
	if (PVD[sortedPVD[0]] == PVD[sortedPVD[1]]) {
	    if (cost[sortedPVD[0]] > cost[sortedPVD[1]]) {
		int temp;
		temp = sortedPVD[0];
		sortedPVD[0] = sortedPVD[1];
		sortedPVD[1] = temp;
	    }
	}
	*/
    }
    protected int updatePVD(long now) {
	highPVDIndex = 1;
	highPVD = 0;
	for (int i=2;i<MAX_THREADS;i++) {
	    PVD[i] = 0;
	    if (!done[i]) {
		PVD[i] = utility[i]/((cost[i]-work[i])/1000.0);
		if (PVD[i] > highPVD) {
		    highPVD = PVD[i];
		    highPVDIndex = i;
		}
	    }
	}
	return highPVDIndex;
    }
    protected long findNxtArvl(long now) {
	long temp[] = new long[MAX_THREADS];
	long min;
	for (int i=2;i<MAX_THREADS;i++) {
	    if (Math.round(timeElapsed/period[i]) > timeElapsed/period[i]) {
		temp[i] = Math.round(timeElapsed/period[i]) * period[i];
	    }
	    else temp[i] = (Math.round(timeElapsed/period[i]) + 1) * period[i];
	    nextArrivals[i] = 0;
	}
	min = findMin(temp[2],temp[3],temp[4]);
	for (int i=2;i<MAX_THREADS;i++) {
	    if (temp[i] == min) nextArrivals[i] = i;
	}
	return 1000*Math.round((min-timeElapsed)/1000.0);
    }
    protected long findMin(long a, long b, long c) {
	return Math.min(Math.min(a,b),Math.min(b,c));
    }
    protected void activate(long id, long slice, long now) {
	quanta = slice*1000;
	setQuanta(quanta);
	chosenThread = (int)id;
	prtChoice(chosenThread, now);
	if (chosenThread > 1) lastActivation[chosenThread] = now;
    }
    // comment this out in FLEX:
    //    protected void setQuanta(long time) {}
    //    protected void contextSwitch() {};
    protected void prtChoice(long threadID, long now) {
	//	System.out.println("Chosen thread: "+threadID+"\tfor: "+(quanta/1000000.0));
	System.out.println("|---> Chosen thread: "+threadID+"\tfor: "+quanta+" <---|");
	System.out.print("Next arrivals: \t");
	for (int i=2;i<MAX_THREADS;i++) {
	    if (nextArrivals[i] != 0) System.out.print(nextArrivals[i]+",");
	}
	System.out.print("\nNot done (ReadyQ): ");
	for (int i=2;i<MAX_THREADS;i++) {
	    if (!done[i]) System.out.print(i+"\t");
	}
	//	System.out.print("\nPVD: ");
	//	for (int i=2;i<MAX_THREADS;i++) System.out.print(PVD[i]+"\t");
	//	System.out.println("\nHigh PVD: "+highPVD);
	System.out.print("\nAccrued Utility: "+totalUtility);
	int allPossible = 0;
	for (int i=2;i<MAX_THREADS;i++) allPossible += numFires[i]*utility[i];
	for (int i=2;i<MAX_THREADS;i++) {
	    if (!done[i]) allPossible -= utility[i];
	}
	System.out.print("\tTotal Utility: "+allPossible);
	if (allPossible!=0) 
	    System.out.println("\tPercentage: "+
			       (100.0*(((float)totalUtility)/((float)allPossible))));
	System.out.println("\n-----------------------------------");
    }
    protected boolean everyoneHere(long now) {
	if ((threads[0]==0)||(threads[2]==0)||
	    (threads[3]==0)||(threads[4]==0)) return false;
	settingUp = false;
	kickOff = now;
	initValues();
	System.out.print("------->  Kickoff at: ");
	System.out.print(kickOff);
	System.out.print("\n");
	return true;
    }
    protected void printThreadList() {
	System.out.print("\nthreads:  ");
	for (int i=0; i<MAX_THREADS; i++) {
	    System.out.print(threads[i]);
	    System.out.print(" , ");
	}
	System.out.print("\n");
    }
    protected boolean deadlock() {
	boolean deadlock = true;
	for (int i=0; i<MAX_THREADS;i++) {
            if (threads[i] != 0) deadlock = false;
        }
        if (deadlock) {
            System.out.print("\n-->Deadlocked!!...\n");
            return true;
        }
	return false;
    }
    // comment out in simulation
    protected void addThread(RealtimeThread thread) {
	addThread(thread.getUID());
    }
    protected void addThread(long threadID) {
	if (threadID == -1) threads[0] = -1;
	else threads[(int)threadID] = (int)threadID;
	//	System.out.print("Adding thread: ");
	//	System.out.print(threadID);
	//	System.out.print("\n");
    }
    // comment out in simulation
    protected void removeThread(RealtimeThread thread) {
	removeThread(thread.getUID());
    }
    protected void removeThread(long threadID) {
	if (threadID == -1) threads[0] = 0;
	else threads[(int)threadID] = 0;
	//	System.out.print("Removing thread: ");
	//	System.out.print(threadID);
	//	System.out.print("\n");
    }
    protected void disableThread(long threadID) {
	removeThread(threadID);
    }
    protected void enableThread(long threadID) {
	addThread(threadID);
    }
    protected void addToFeasibility(Schedulable schedulable) {}
    public void fireSchedulable(Schedulable schedulable) {
	schedulable.run();
    }
    public String getPolicyName() {return "Simple DASA";}
    public boolean isFeasible() {return true;}
    protected boolean isFeasible(Schedulable s, ReleaseParameters rp) {return true;}
    protected void removeFromFeasibility(Schedulable schedulable) {}
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory) {
	return true;
    }
    public boolean setIfFeasible(Schedulable schedulable,
				 ReleaseParameters release,
				 MemoryParameters memory,
				 ProcessingGroupParameters group) {
	return true;
    }
    protected void waitForNextPeriod (RealtimeThread rt) {}
    public String toString() {return "blah";}
    public void printNoAlloc() {}
}
