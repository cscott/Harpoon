//  package javax.realtime.memtests;


/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 1999, 2000  All Rights Reserved
 */

import javax.realtime.*;
import java.util.*;

public class MemTest 
{
  final static int k = 1024;
  final static int defaultSize = 8*k;
  final static ObjectHolder objectHolderInHeap = new ObjectHolder();

  final static Runnable r = new Runnable() {
    public void run() {
      final Integer one = new Integer(1);
      Integer two = new Integer(one.intValue() + one.intValue());
      System.out.println(one + " + " + one + " = " + two);
      System.out.print("Current memory area: ");
      System.out.println(RealtimeThread.currentRealtimeThread().getMemoryArea());
    }
  };

  /*
    Runs the runnable from within the given memory area
    (The test is run in a separate thread but the thread is immediately
     joined, so the second thread will have finished when the first thread
     begins)
   */
  static class RunnableTest implements Runnable {
    private MemoryArea memoryArea;
    private Runnable runnable;
    private RealtimeThread realtimeThread;
	RunnableTest (MemoryArea ma, Runnable r) {
	  this.memoryArea = ma;	  
	  this.runnable = r;	  
	}
	RealtimeThread getThread() {
	  return realtimeThread;
	}
	MemoryArea getMemoryArea() {
	  return memoryArea;
	}
	Runnable getRunnable() {
	  return runnable;
	}
	public void run() {
	  realtimeThread = new RealtimeThread(new MemoryParameters(memoryArea), runnable);
	  realtimeThread.start();
	  try {
        realtimeThread.join();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
	}
  }
   
  public static void main (String[] args) {
    System.out.println();
    testLTMemory();  
    testVTMemory();
    testScopedPhysicalMemory();
    testImmortalPhysicalMemory();
    testImmortalMemory();
    testHeapMemory();
    
	testTwoScopedPhysicalMemories();

    testAssignment();
    testScopedAssignment();
    testIllegalScopedAssignment();  // should fail (but currently doesn't?)
    testIllegalAssignment();		// should fail
    testIllegalAssignmentToArray(); // should fail
  }

   private static void testLTMemory() {	
    System.out.println("-Begin LTMemory test-");
    MemoryArea ma = new LTMemory(defaultSize, 2*defaultSize);
    System.out.println("Created: " + ma);
    RunnableTest test = new RunnableTest(ma, r);
    test.run();
    System.out.println("-End LTMemory test-");
    System.out.println();    
  }

  private static void testVTMemory() {	
    System.out.println("-Begin VTMemory test-");    
    MemoryArea ma = new VTMemory(defaultSize, defaultSize);
    System.out.println("Created: " + ma);
    RunnableTest test = new RunnableTest(ma, r);
    test.run();
    System.out.println("-End VTMemory test-");
    System.out.println();    
  } 
   
  private static void testScopedPhysicalMemory() {	
    System.out.println("-Begin ScopedPhysicalMemory test-");    
    MemoryArea ma = (MemoryArea)DefaultPhysicalMemoryFactory.instance().create("scoped", false, 1024*64*k, defaultSize);
    System.out.println("Created: " + ma);
    RunnableTest test = new RunnableTest(ma, r);
    test.run();
    System.out.println("-End ScopedPhysicalMemory test-");
    System.out.println();    
  }
  
  private static void testTwoScopedPhysicalMemories() {	
    System.out.print("-Begin TwoScopedPhysicalMemories test-\n");    
    MemoryArea ma = (MemoryArea)DefaultPhysicalMemoryFactory.instance().create("scoped", false, 32*1024*k, defaultSize);
    System.out.print("Created: " + ma + "\n");
    
    MemoryArea ma2 = (MemoryArea)DefaultPhysicalMemoryFactory.instance().create("scoped", false, 33*1024*k, defaultSize);
    System.out.print("Created: " + ma2 + "\n");
    
    RunnableTest test = new RunnableTest(ma, r);    
    test.run();
    
    RunnableTest test2 = new RunnableTest(ma2, r);
    test2.run();
            
    System.out.print("-End TwoScopedPhysicalMemories test-\n");
    System.out.print("\n");    
  }  
  
  private static void testImmortalPhysicalMemory() {
    System.out.println("-Begin ImmortalPhysicalMemory test-");    
    MemoryArea ma = (MemoryArea)DefaultPhysicalMemoryFactory.instance().create("immortal", false, 256*k, defaultSize);
    RunnableTest test = new RunnableTest(ma, r);
    System.out.println("Created: " + ma);
    test.run();
    System.out.println("-End ImmortalPhysicalMemory test-");
    System.out.println();    
  }    

  private static void testImmortalMemory() {	
    System.out.println("-Begin ImmortalMemory test-");    
    MemoryArea ma = ImmortalMemory.instance();
    System.out.println("Created: " + ma);
    RunnableTest test = new RunnableTest(ma, r);
    test.run();
    System.out.println("-End ImmortalMemory test-");
    System.out.println();    
  }
  
  private static void testHeapMemory() {
    System.out.println("-Begin HeapMemory test-");    
    MemoryArea ma = HeapMemory.instance();
    System.out.println("Created: " + ma);
    RunnableTest test = new RunnableTest(ma, r);
    test.run();
    System.out.println("-End HeapMemory test-");
    System.out.println();    
  }      
     
  private static void delay (int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  private static void testAssignment() {
    System.out.println("-Begin Assignment test-");	
	try {	
		ObjectHolder oh;
		MemoryArea heapMemoryArea = HeapMemory.instance();
		MemoryArea immortalPhysicalMemoryArea = immortalPhysicalMemoryArea = (MemoryArea)(DefaultPhysicalMemoryFactory.instance().create("immortal", false, 4096));
		MemoryArea immortalMemoryArea = ImmortalMemory.instance();
	
		System.out.println("Using the following memory areas:");
		System.out.println(heapMemoryArea.toString());
		System.out.println(immortalPhysicalMemoryArea.toString());
		System.out.println(immortalMemoryArea.toString());
		
	    System.out.println("Creating 'holder' object in heap");
		oh = new ObjectHolder();
	    System.out.println("DONE");

	    System.out.println("Assigning object from heap to heap");
	    oh.data = heapMemoryArea.newInstance(Object.class);
	    System.out.println("DONE");	    
	    
	    System.out.println("Assigning object from immortalPhysicalMemory to heap");
	    oh.data = immortalPhysicalMemoryArea.newInstance(Object.class);    
	    System.out.println("DONE");	    

	    System.out.println("Assigning object from the immortal memory to heap");
    	oh.data = immortalMemoryArea.newInstance(Object.class);
	    System.out.println("DONE");    	

	    System.out.println("Creating 'holder' object in immortalPhysicalMemory");
		oh = (ObjectHolder)(immortalPhysicalMemoryArea.newInstance(ObjectHolder.class));
	    System.out.println("DONE");		

	    System.out.println("Assigning object from heap to immortalPhysicalMemory");
	    oh.data = heapMemoryArea.newInstance(Object.class);
	    System.out.println("DONE");	    
	    
	    System.out.println("Assigning object from immortalPhysicalMemory to immortalPhysicalMemory");
	    oh.data = immortalPhysicalMemoryArea.newInstance(Object.class);    
	    System.out.println("DONE");	    

	    System.out.println("Assigning object from the immortal memory to immortalPhysicalMemory");
    	oh.data = immortalMemoryArea.newInstance(Object.class);
	    System.out.println("DONE");    	

	    System.out.println("Creating 'holder' object in immortalMemory");
		oh = (ObjectHolder)(immortalMemoryArea.newInstance(ObjectHolder.class));    
	    System.out.println("DONE");		

	    System.out.println("Assigning object from heap to immortalMemory");
	    oh.data = heapMemoryArea.newInstance(Object.class);
	    System.out.println("DONE");	    
	    
	    System.out.println("Assigning object from immortalPhysicalMemory to immortalMemory");
	    oh.data = immortalPhysicalMemoryArea.newInstance(Object.class);    
	    System.out.println("DONE");

	    System.out.println("Assigning object from the immortal memory to immortalMemory");
    	oh.data = immortalMemoryArea.newInstance(Object.class);
	    System.out.println("DONE");    	    	    	
	} catch (Exception e) {
		e.printStackTrace();
	}
    System.out.println("-End Assignment test-");
    System.out.println();    
  }
  
  private static void testScopedAssignment() {
    System.out.println("-Begin Scoped Assignment test-");	
    class InnerScopedMemoryTest implements Runnable {
    	public void run() {
		    System.out.println("Creating inner VTMemory");		
			MemoryArea innerVTMemory = new VTMemory(1024, 1024);
		    System.out.println("Creating 'holder' object and assigning object from outer VTMemory");
		    try {
				((ObjectHolder)innerVTMemory.newInstance(ObjectHolder.class)).data = new Object();
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
    	}
    }
	try {
	    System.out.println("Creating outer VTMemory");
		MemoryArea vtMemory = new VTMemory(4096, 4096);
	    System.out.println("Creating realtime thread using created VTMemory");
	    RealtimeThread t = new RealtimeThread(new MemoryParameters(vtMemory), new InnerScopedMemoryTest());
	    t.start();
	    t.join();
	    System.out.println("DONE");		
	} catch (Exception e) {
		e.printStackTrace();
	}
    System.out.println("-End Scoped Assignment test-");
    System.out.println();    
  }
  
  private static void testIllegalScopedAssignment() {
    System.out.println("-Begin Illegal Scoped Assignment test-");	
    class IllegalScopedMemoryTest implements Runnable {
    	public void run() {
		    System.out.println("Creating inner VTMemory");		
			MemoryArea innerVTMemory = new VTMemory(1024, 1024);
		    System.out.println("Creating 'holder' object and assigning object to outer VTMemory");
		    try {
		    	ObjectHolder oh = new ObjectHolder();
		    	oh.data = innerVTMemory.newInstance(Object.class);
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
    	}
    }
	try {
	    System.out.println("Creating outer VTMemory");
		MemoryArea vtMemory = new VTMemory(4096, 4096);
	    System.out.println("Creating realtime thread using created VTMemory");
	    RealtimeThread t = new RealtimeThread(new MemoryParameters(vtMemory), new IllegalScopedMemoryTest());
	    t.start();
	    t.join();
	    System.out.println("DONE");		
	} catch (Exception e) {
		e.printStackTrace();
	}
    System.out.println("-End Illegal Scoped Assignment test-");
    System.out.println();    
  }  
  
  
  private static void testIllegalAssignment() {
    System.out.println("-Begin Illegal Assignment test-");
    MemoryArea mem = new VTMemory(4096, 4096);
    System.out.println("Attempting to add object from " + mem + " to " + objectHolderInHeap + " in " + HeapMemory.instance());
    try {
	    objectHolderInHeap.data = mem.newInstance(Object.class);
    } catch (Throwable t) {
    	t.printStackTrace();
    }
    System.out.println("added " + objectHolderInHeap.data);
    System.out.println("-End Illegal Assignment test-");
    System.out.println();
  }    
  
  private static void testIllegalAssignmentToArray() {
    System.out.println("-Begin Illegal Assignment To Array test-");
    MemoryArea mem = new VTMemory(4096, 4096);;
    Object[] objArray = new Object[1];
    System.out.println("Attempting to add object from " + mem + " to " + objArray + " in " + HeapMemory.instance());
    try {
	    objArray[0] = mem.newInstance(Object.class);
    } catch (Throwable t) {
    	t.printStackTrace();
    }
    System.out.println("added " + objArray[0]);
    System.out.println("-End Illegal Assignment To Array test-");
    System.out.println();    
  }
}
