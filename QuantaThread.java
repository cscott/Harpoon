package javax.realtime;

public class QuantaThread extends RealtimeThread
{
  public static int timerFlag = 0;
  public static boolean done = false;

  public QuantaThread()
  {
      super();
      System.out.println("Constructing QuantaThread");
      start();
      System.out.println("Done starting Quanta Thread()");
  }

  public void run()
  {
    try { 
      while(!done)
	{
	  sleep(1000); 
	  System.out.println("Time: "+System.currentTimeMillis());
	  timerFlag = 1;
	}
    } catch(InterruptedException ie) 
      { 
	System.out.println("Thread interrupted");
      }
  }

  public static void flagHandler()
  {
    System.out.println("Java sees timerFlag set.");

    timerFlag = 0;
  }
}
