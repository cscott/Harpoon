import javax.realtime.AbsoluteTime;
import javax.realtime.AperiodicParameters;
import javax.realtime.AsyncEventHandler;
import javax.realtime.HeapMemory;
import javax.realtime.ImmortalMemory;
import javax.realtime.ImmortalPhysicalMemory;
import javax.realtime.ImportanceParameters;
import javax.realtime.LTMemory;
import javax.realtime.LTPhysicalMemory;
import javax.realtime.MemoryArea;
import javax.realtime.MemoryParameters;
import javax.realtime.NoHeapRealtimeThread;
import javax.realtime.PeriodicParameters;
import javax.realtime.ProcessingGroupParameters;
import javax.realtime.RealtimeThread;
import javax.realtime.RelativeTime;
import javax.realtime.ReleaseParameters;
import javax.realtime.SchedulingParameters;
import javax.realtime.ScopedMemory;
import javax.realtime.VTMemory;
import javax.realtime.VTPhysicalMemory;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.text.ParseException;

public class ThreadBenchAutomated {
    // AsyncEventHandler (schedulingParams, releaseParams, memoryParams,
    //                    memoryArea, processingGroupParams, logic)
    
    public static class RealtimeThreadWithID extends RealtimeThread {
	public static int ID;
	
	public RealtimeThreadWithID(final int ID, SchedulingParameters scheduling,
				    ReleaseParameters release, MemoryParameters memory,
				    MemoryArea area, ProcessingGroupParameters group) {
	    super(scheduling, release, memory, area, group,
		  new Runnable() {
		      public void run() {
			  long startTime = System.currentTimeMillis();
			  for (int k = 0; k < 1000; k++)
			      System.out.println("Thread " + ID + ": " + (k+1) + ". " +
						 (System.currentTimeMillis() - startTime) + "ms.");
		      }
		  });

	    this.ID = ID;
	}
    }

    public static class NoHeapRealtimeThreadWithID extends NoHeapRealtimeThread {
	public static int ID;

	public NoHeapRealtimeThreadWithID(final int ID, SchedulingParameters scheduling,
					  ReleaseParameters release, MemoryParameters memory,
					  MemoryArea area, ProcessingGroupParameters group) {
	    super(scheduling, release, memory, area, group,
		  new Runnable() {
		      public void run() {
			  long startTime = System.currentTimeMillis();
			  for (int k = 0; k < 1000; k++)
			      System.out.println("Thread " + ID + ": " + (k+1) + ". " +
						 (System.currentTimeMillis() - startTime) + "ms.");
		      }
		  });

	    this.ID = ID;
	}
    }

    public static final int HEAP_MEMORY_TYPE = 1;
    public static final int IMMORTAL_MEMORY_TYPE = 2;
    public static final int IMMORTAL_PHYSICAL_MEMORY_TYPE = 3;
    public static final int LT_MEMORY_TYPE = 4;
    public static final int LT_PHYSICAL_MEMORY_TYPE = 5;
    public static final int VT_MEMORY_TYPE = 6;
    public static final int VT_PHYSICAL_MEMORY_TYPE = 7;

    static int totalMisses = 0;
    static int totalOverruns = 0;

    private static int numberOfThreads = 0;
    private static RealtimeThread[] threads = null;
    private static String[] paramList;
    private static int memAreaType;
    private static MemoryArea memArea;
    private static MemoryParameters memParams;
    private static ProcessingGroupParameters group;
    private static boolean noHeap;
    private static ReleaseParameters release;
    private static boolean asPeriodic;
    private static SchedulingParameters scheduling;

//     static AsyncEventHandler missHandler = 
// 	new AsyncEventHandler() {
// 	    public void handleAsyncEvent() {
// 		int fireCount = getAndClearPendingFireCount();
// 		System.out.println("Missed " + fireCount + " deadline(s).");
// 		totalMisses += fireCount;
// 	    }
// 	};

//     static AsyncEventHandler overrunHandler = 
// 	new AsyncEventHandler () {
// 	    public void handleAsyncEvent() {
// 		int fireCount = getAndClearPendingFireCount();
// 		System.out.println("Overrun " + fireCount + " deadline(s).");
// 		totalOverruns += fireCount;
// 	    }
// 	};

    private static void fillParamList() {
	paramList = new String[8];
	paramList[0] = new String("MemoryAreaType");
	paramList[1] = new String("MemoryArea");
	paramList[2] = new String("MemoryParameters");
	paramList[3] = new String("ProcessingGroupParameters");
	paramList[4] = new String("RealtimeThreadIsNoHeap");
	paramList[5] = new String("ReleaseParametersAsPeriodic");
	paramList[6] = new String("ReleaseParameters");
	paramList[7] = new String("SchedulingParameters");
    }	

    private static int parseMemAreaType(String st) throws ParseException {
	if (st.length() == 0) return HEAP_MEMORY_TYPE;
	if (st.equals("HeapMemory")) return HEAP_MEMORY_TYPE;
	if (st.equals("ImmortalMemory")) return IMMORTAL_MEMORY_TYPE;
	if (st.equals("ImmoratalPhysicalMemory")) return IMMORTAL_PHYSICAL_MEMORY_TYPE;
	if (st.equals("LTMemory")) return LT_MEMORY_TYPE;
	if (st.equals("LTPhysicalMemory")) return LT_PHYSICAL_MEMORY_TYPE;
	if (st.equals("VTMemory")) return VT_MEMORY_TYPE;
	if (st.equals("VTPhysicalMemory")) return VT_PHYSICAL_MEMORY_TYPE;

	throw new ParseException("ParseException in parseMemAreaType: Unknown type of memory.", 0);
    }

    private static MemoryArea parseMemArea(String st) throws ParseException {
	int size;
	try {
	    size = Integer.parseInt(st);
	} catch (NumberFormatException e) {
	    throw new ParseException("ParseException in parseMemArea: NumberFormatException raised.", 0);
	}

	try {
	    switch (memAreaType) {
	    case HEAP_MEMORY_TYPE: return HeapMemory.instance();
	    case IMMORTAL_MEMORY_TYPE: return ImmortalMemory.instance();
		// "null" should be changed to something else
	    case IMMORTAL_PHYSICAL_MEMORY_TYPE: return new ImmortalPhysicalMemory(null, size);
	    case LT_MEMORY_TYPE: return new LTMemory(size, 10 * size);
		// "null" should be changed to something else
	    case LT_PHYSICAL_MEMORY_TYPE: return new LTPhysicalMemory(null, size);
	    case VT_MEMORY_TYPE: return new VTMemory(size, 10 * size);
		// "null" should be changed to something else
	    case VT_PHYSICAL_MEMORY_TYPE: return new VTPhysicalMemory(null, size);
	    default: throw new ParseException("ParseException in parseMemArea: Unknown type of memory.", 0);
	    }
	} catch (Throwable e) {
	    throw new ParseException("Some error occured when creating some memory.", 0);
	}
    }

    private static MemoryParameters parseMemParams(String st) throws ParseException {
	int size;
	try {
	    size = Integer.parseInt(st);
	} catch (NumberFormatException e) {
	    throw new ParseException("ParseException in parseMemParams: NumberFormatException raised.", 0);
	}

	return new MemoryParameters(size, size);
    }

    private static ProcessingGroupParameters parseGroup(String st) throws ParseException {
	AsyncEventHandler overrunHandler = 
	    new AsyncEventHandler () {
		public void handleAsyncEvent() {
		    int fireCount = getAndClearPendingFireCount();
		    System.out.println("Overrun " + fireCount + " deadline(s).");
		    totalOverruns += fireCount;
		}
	    };
	
	AsyncEventHandler missHandler = 
	    new AsyncEventHandler() {
		public void handleAsyncEvent() {
		    int fireCount = getAndClearPendingFireCount();
		    System.out.println("Missed " + fireCount + " deadline(s).");
		    totalMisses += fireCount;
		}
	    };
	
	AbsoluteTime time1;
	RelativeTime time2, time3, time4;
	
	// Getting AbsoluteTime start
	if (st.charAt(0) != '(') {
	    if (st.startsWith("null,")) {
		time1 = null;
		st = st.substring("null,".length(), st.length());
	    }
	    else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ start", 0);
	}
	else {
	    int pos = st.indexOf(')');
	    if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ start", 0);
	    String s = st.substring(1, pos);
	    st = st.substring(pos + 2, st.length());
	    pos = s.indexOf(',');
	    if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ start", 0);
	    int a, b;
	    try {
		a = Integer.parseInt(s.substring(0, pos));
		b = Integer.parseInt(s.substring(pos + 1, s.length()));
	    } catch (NumberFormatException e) {
		throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ start", 0);
	    }
	    time1 = new AbsoluteTime(a, b);
	}
	
	// Getting RelativeTime period
	if (st.charAt(0) != '(') {
	    if (st.startsWith("null,")) {
		time2 = null;
		st = st.substring("null,".length(), st.length());
	    }
	    else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ period", 0);
	}
	else {
	    int pos = st.indexOf(')');
	    if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ period", 0);
	    String s = st.substring(1, pos);
	    st = st.substring(pos + 2, st.length());
	    pos = s.indexOf(',');
	    if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ period", 0);
	    int a, b;
	    try {
		a = Integer.parseInt(s.substring(0, pos));
		b = Integer.parseInt(s.substring(pos + 1, s.length()));
	    } catch (NumberFormatException e) {
		throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ period", 0);
	    }
	    time2 = new RelativeTime((long)a, b);
	}
	
	// Getting RelativeTime cost
	if (st.charAt(0) != '(') {
	    if (st.startsWith("null,")) {
		time3 = null;
		st = st.substring("null,".length(), st.length());
	    }
	    else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ cost", 0);
	}
	else {
	    int pos = st.indexOf(')');
	    if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ cost", 0);
	    String s = st.substring(1, pos);
	    st = st.substring(pos + 2, st.length());
	    pos = s.indexOf(',');
	    if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ cost", 0);
	    int a, b;
	    try {
		a = Integer.parseInt(s.substring(0, pos));
		b = Integer.parseInt(s.substring(pos + 1, s.length()));
	    } catch (NumberFormatException e) {
		throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ cost", 0);
	    }
	    time3 = new RelativeTime(a, b);
	}
	
	// Getting RelativeTime deadline
	if (st.charAt(0) != '(') {
	    if (st.startsWith("null")) {
		time4 = null;
		if (!st.equals("null")) throw new ParseException("ParseException in parseRelease: found additional characters.", 0);
	    }
	    else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ deadline", 0);
	}
	else {
	    int pos = st.indexOf(')');
	    if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ deadline", 0);
	    String s = st.substring(1, pos);
	    if (pos != st.length()) throw new ParseException("ParseException in parseRelease: found additional characters.", 0);
	    pos = s.indexOf(',');
	    if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ deadline", 0);
	    int a, b;
	    try {
		a = Integer.parseInt(s.substring(0, pos));
		b = Integer.parseInt(s.substring(pos + 1, s.length()));
	    } catch (NumberFormatException e) {
		throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ deadline", 0);
	    }
	    time4 = new RelativeTime(a, b);
	}
	
	return new ProcessingGroupParameters(time1, time2, time3, time4, overrunHandler, missHandler);
    }

    private static boolean parseNoHeap(String st) throws ParseException {
	if ((!st.equals("0")) && (!st.equals("1"))) throw new ParseException("ParseException in parseNoHeap.", 0);
	else return (st.equals("1"));
    }
	
    private static boolean parseAsPeriodic(String st) throws ParseException {
	if ((!st.equals("0")) && (!st.equals("1"))) throw new ParseException("ParseException in parseAsPeriodic", 0);
	else return (st.equals("1"));
    }

    private static ReleaseParameters parseRelease(String st) throws ParseException {
	AsyncEventHandler overrunHandler = 
	    new AsyncEventHandler () {
		public void handleAsyncEvent() {
		    int fireCount = getAndClearPendingFireCount();
		    System.out.println("Overrun " + fireCount + " deadline(s).");
		    totalOverruns += fireCount;
		}
	    };
	
	AsyncEventHandler missHandler = 
	    new AsyncEventHandler() {
		public void handleAsyncEvent() {
		    int fireCount = getAndClearPendingFireCount();
		    System.out.println("Missed " + fireCount + " deadline(s).");
		    totalMisses += fireCount;
		}
	    };
	
	if (asPeriodic) {
	    AbsoluteTime time1;
	    RelativeTime time2, time3, time4;

	    // Getting AbsoluteTime start
	    if (st.charAt(0) != '(') {
		if (st.startsWith("null,")) {
		    time1 = null;
		    st = st.substring("null,".length(), st.length());
		}
		else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ start", 0);
	    }
	    else {
		int pos = st.indexOf(')');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ start", 0);
		String s = st.substring(1, pos);
		st = st.substring(pos + 2, st.length());
		pos = s.indexOf(',');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ start", 0);
		int a, b;
		try {
		    a = Integer.parseInt(s.substring(0, pos));
		    b = Integer.parseInt(s.substring(pos + 1, s.length()));
		} catch (NumberFormatException e) {
		    throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ start", 0);
		}
		time1 = new AbsoluteTime(a, b);
	    }

	    // Getting RelativeTime period
	    if (st.charAt(0) != '(') {
		if (st.startsWith("null,")) {
		    time2 = null;
		    st = st.substring("null,".length(), st.length());
		}
		else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ period", 0);
	    }
	    else {
		int pos = st.indexOf(')');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ period", 0);
		String s = st.substring(1, pos);
		st = st.substring(pos + 2, st.length());
		pos = s.indexOf(',');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ period", 0);
		int a, b;
		try {
		    a = Integer.parseInt(s.substring(0, pos));
		    b = Integer.parseInt(s.substring(pos + 1, s.length()));
		} catch (NumberFormatException e) {
		    throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ period", 0);
		}
		time2 = new RelativeTime((long)a, b);
	    }

	    // Getting RelativeTime cost
	    if (st.charAt(0) != '(') {
		if (st.startsWith("null,")) {
		    time3 = null;
		    st = st.substring("null,".length(), st.length());
		}
		else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ cost", 0);
	    }
	    else {
		int pos = st.indexOf(')');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ cost", 0);
		String s = st.substring(1, pos);
		st = st.substring(pos + 2, st.length());
		pos = s.indexOf(',');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ cost", 0);
		int a, b;
		try {
		    a = Integer.parseInt(s.substring(0, pos));
		    b = Integer.parseInt(s.substring(pos + 1, s.length()));
		} catch (NumberFormatException e) {
		    throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ cost", 0);
		}
		time3 = new RelativeTime(a, b);
	    }

	    // Getting RelativeTime deadline
	    if (st.charAt(0) != '(') {
		if (st.startsWith("null")) {
		    time4 = null;
		    if (!st.equals("null")) throw new ParseException("ParseException in parseRelease: found additional characters.", 0);
		}
		else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ deadline", 0);
	    }
	    else {
		int pos = st.indexOf(')');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ deadline", 0);
		String s = st.substring(1, pos);
		if (pos != st.length()) throw new ParseException("ParseException in parseRelease: found additional characters.", 0);
		pos = s.indexOf(',');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ deadline", 0);
		int a, b;
		try {
		    a = Integer.parseInt(s.substring(0, pos));
		    b = Integer.parseInt(s.substring(pos + 1, s.length()));
		} catch (NumberFormatException e) {
		    throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ deadline", 0);
		}
		time4 = new RelativeTime(a, b);
	    }

	    return new PeriodicParameters(time1, time2, time3, time4, overrunHandler, missHandler);
	}
	else {
	    RelativeTime time3, time4;
	    
	    // Getting RelativeTime cost
	    if (st.charAt(0) != '(') {
		if (st.startsWith("null,")) {
		    time3 = null;
		    st = st.substring("null,".length(), st.length());
		}
		else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ cost (aperiodic)", 0);
	    }
	    else {
		int pos = st.indexOf(')');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ cost (aperiodic)", 0);
		String s = st.substring(1, pos);
		st = st.substring(pos + 2, st.length());
		pos = s.indexOf(",");
		if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ cost (aperiodic)", 0);
		int a, b;
		try {
		    a = Integer.parseInt(s.substring(0, pos));
		    b = Integer.parseInt(s.substring(pos + 1, s.length()));
		} catch (NumberFormatException e) {
		    throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ cost (aperiodic)", 0);
		}
		time3 = new RelativeTime(a, b);
	    }

	    // Getting RelativeTime deadline
	    if (st.charAt(0) != '(') {
		if (st.startsWith("null")) {
		    time4 = null;
		    if (!st.equals("null")) throw new ParseException("ParseException in parseRelease: found additional characters. (aperiodic)", 0);
		}
		else throw new ParseException("ParseException in parseRelease: st.charAt(0) != \"(\" @ deadline (aperiodic)", 0);
	    }
	    else {
		int pos = st.indexOf(')');
		if (pos == -1) throw new ParseException("ParseException in parseRelease: st.indexOf(\")\") == -1 @ deadline (aperiodic)", 0);
		String s = st.substring(1, pos);
		if (pos != st.length()) throw new ParseException("ParseException in parseRelease: found additional characters. (aperiodic)", 0);
		pos = s.indexOf(",");
		if (pos == -1) throw new ParseException("ParseException in parseRelease: s.indexOf(\",\") == -1 @ deadline (aperiodic)", 0);
		int a, b;
		try {
		    a = Integer.parseInt(s.substring(0, pos));
		    b = Integer.parseInt(s.substring(pos + 1, s.length()));
		} catch (NumberFormatException e) {
		    throw new ParseException("ParseException in parseRelease: a NumberFormatException raised. @ deadline (aperiodic)", 0);
		}
		time4 = new RelativeTime(a, b);
	    }

	    return new AperiodicParameters(time3, time4, overrunHandler, missHandler);
	}
    }

    private static SchedulingParameters parseScheduling(String st) throws ParseException {
	int pos = st.indexOf(',');
	if (pos == -1) throw new ParseException("ParseException in parseScheduling: no comma found.", 0);
	int a, b;
	try {
	    a = Integer.parseInt(st.substring(0, pos));
	    b = Integer.parseInt(st.substring(pos + 1, st.length()));
	} catch (NumberFormatException e) {
	    throw new ParseException("ParseException in parseScheduling: a NumberFormtException raised.", 0);
	}
	
	return new ImportanceParameters(a, b);
    }

    private static boolean init(String fileName) {
	fillParamList();

	FileReader fr;
	try {
	    fr = new FileReader(fileName);
	} catch (FileNotFoundException e) {
	    System.out.println("File \"" + fileName + "\" not found.");
	    return false;
	}
	LineNumberReader reader = new LineNumberReader(fr);

	String s;
	try {
	    s = reader.readLine();
	} catch (IOException e) {
	    System.out.println("An IOException occured while reading the number of threads... nobody knows why...");
	    return false;
	}
	if (s == null) {
	    System.out.println("File \"" + fileName + "\" is empty.");
	    return false;
	}

	if (!s.startsWith("NumberOfThreads=")) {
	    System.out.println("\"NumberOfThreads\" parameter must " +
			       "on the first line of the file.");
	    return false;
	}

	try {
	    numberOfThreads = Integer.parseInt(s.substring((new String("NumberOfThreads=")).length(),
							 s.length()));
	} catch (NumberFormatException e) {
	    System.out.println("Error converting the number of threads from String to Integer.");
	    return false;
	}
	threads = new RealtimeThread[numberOfThreads];

	for (int i = 0; i < numberOfThreads; i++) {
	    for (int j = 0; j < paramList.length; j++) {
		try {
		    s = reader.readLine();
		} catch (IOException e) {
		    System.out.println("An IOException occured inside the double loop... dont ask me why...");
		    return false;
		}
		if ((s == null) || (!s.startsWith("Thread" + (i+1) + "_" + paramList[j] + "="))) {
		    System.out.println("The file \"" + fileName + "\" is corrupted. " +
				       "Missing (or in the wrong place) parameter: Thread" +
				       (i+1) + "_" + paramList[j] + ".");
		    return false;
		}
		
		String st = s.substring(paramList[j].length() +
					("Thread" + (i+1) + "_").length() +
					"_".length(), s.length());
		try {
		    switch (j) {
		    case 0: memAreaType = parseMemAreaType(st);   break;
		    case 1: memArea = parseMemArea(st);   break;
		    case 2: memParams = parseMemParams(st);   break;
		    case 3: group = parseGroup(st);   break;
		    case 4: noHeap = parseNoHeap(st);   break;
		    case 5: asPeriodic = parseAsPeriodic(st);   break;
		    case 6: release = parseRelease(st);   break;
		    case 7: scheduling = parseScheduling(st);   break;
		    default: {
			System.out.println("Hmm, something's wrong with the parameters recognition.");
			return false;
		    }
		    }
		} catch (ParseException e) {
		    System.out.println("A ParseException occured while trying to get Thread" +
				       (i+1) + "_" + paramList[j] + " parameter. " + e.toString());
		    return false;
		}
	    }
	    if (noHeap) threads[i] = new NoHeapRealtimeThreadWithID(i+1, scheduling, release, memParams, memArea, group);
	    else threads[i] = new RealtimeThreadWithID(i+1, scheduling, release, memParams, memArea, group);
	}

	return true;
    }

    public static void printInfo() {
	for (int i = 0; i < numberOfThreads; i++) {
	    System.out.print("\nThread " + (i+1) + ":\n\n");
	    switch (memAreaType) {
	    case HEAP_MEMORY_TYPE: System.out.println("MemoryAreaType: HeapMemory");   break;
	    case IMMORTAL_MEMORY_TYPE: System.out.println("MemoryAreaType: ImmortalMemory");   break;
	    case IMMORTAL_PHYSICAL_MEMORY_TYPE: System.out.println("MemoryAreaType: ImmortalPhysicalMemory");   break;
	    case LT_MEMORY_TYPE: System.out.println("MemoryAreaType: LTMemory");   break;
	    case LT_PHYSICAL_MEMORY_TYPE: System.out.println("MemoryAreaType: LTPhysicalMemory");   break;
	    case VT_MEMORY_TYPE: System.out.println("MemoryAreaType: VTMemory");   break;
	    case VT_PHYSICAL_MEMORY_TYPE: System.out.println("MemoryAreaType: VTPhysicalMemory");   break;
	    default: System.out.println("MemoryAreaType: Unknown");
	    }
	    MemoryArea mem = threads[i].getCurrentMemoryArea();
	    if (mem != null) System.out.println("MemoryArea: " + mem.toString());
	    else System.out.println("MemoryArea: null");
	    MemoryParameters memParams = threads[i].getMemoryParameters();
	    if (memParams != null) System.out.println("MemoryParameters: " + memParams.toString());
	    else System.out.println("MemoryParameters: null");
	    ProcessingGroupParameters group = threads[i].getProcessingGroupParameters();
	    if (group != null) System.out.println("ProcessingGroupParameters: " + group.toString());
	    else System.out.println("ProcessingGroupParameters: null");
	    ReleaseParameters release = threads[i].getReleaseParameters();
	    if (release != null) System.out.println("ReleaseParameters: " + release.toString());
	    else System.out.println("ReleaseParameters: null");
	    if (release instanceof PeriodicParameters) System.out.println("ReleaseParameters are instance of PeriodicParameters");
	    else System.out.println("ReleaseParameters are instance of  AperiodicParameters");
	    SchedulingParameters scheduling = threads[i].getSchedulingParameters();
	    if (scheduling != null) System.out.println("SchedulingParameters: " + scheduling.toString());
	    else System.out.println("SchedulingParameters: null");
	    if (threads[i] instanceof NoHeapRealtimeThread) System.out.println("Thread " + (i+1) + " is instance of NoHeapRealtimeThread");
	    else System.out.println("Thread " + (i+1) + " is instance of RealtimeThread");
	}
	System.out.print("\n\n");
    }

    public static void main(String args[]) {
	String s;
	if (args.length <= 0) s = "default";
	else s = args[0];
	if (!init(s)) {
	    System.out.println("Some error occured while initializing the system.");
	    System.exit(-1);
	}

	printInfo();

	for (int i = 0; i < numberOfThreads; i++)
	    threads[i].start();
	try {
	    for (int i = 0; i < numberOfThreads; i++)
		threads[i].join();
	} catch (InterruptedException e) {
	    System.out.println("Oops... An InterruptedException occured...");
	    System.out.println("Stack: ");
	    e.printStackTrace();
	}
	System.out.println("Total misses: " + totalMisses);
	System.out.println("Total overruns: " + totalOverruns);
    }
}
