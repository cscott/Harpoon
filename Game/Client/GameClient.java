import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameClient extends Thread {

    static boolean DEBUG;

    public static void main(String argv[]) {
	String host = null;
	int numberofclients = 0;
	int numberofmessages = 0;
	int port = 4321;
	GameClient.DEBUG = false;
	try {
	    host = argv[0];
	    port = Integer.parseInt(argv[1]);
	    numberofclients = Integer.parseInt(argv[2]);
	    numberofmessages = Integer.parseInt(argv[3]);
	} catch (Exception e) {
	    System.out.println("GameClient host port numberofclients numberofmessages debugflag");
	}

	try {
	    GameClient.DEBUG = (Integer.parseInt(argv[4])==1);
	} catch (Exception e) {}

	long starttime = System.currentTimeMillis();

	GameClient[] tarray = new GameClient[numberofclients];
	for (int i = 0; i < numberofclients; i++) {
	    tarray[i] = new GameClient(i, host, port,
				       numberofmessages, numberofclients);
	    if(DEBUG)
		System.out.println("Attempting to start "+i);
	    tarray[i].start();
	}
	
	try {
	    for (int i = 0; i < numberofclients; i++) {
		tarray[i].join();
	    }
	}  catch (InterruptedException e) {
	    e.printStackTrace();
	    System.out.println(e);
	}
	
	long endtime=System.currentTimeMillis();
	System.out.println("ChatClient");
	System.out.println("numclients:" + numberofclients);
	System.out.println("port:" + port);
	System.out.println("number of messages:" + numberofmessages);
	System.out.println("Elapsed time:(mS)" + (endtime-starttime));
	System.out.println("Throughput:" + (double) numberofclients *
			   numberofmessages/((double) (endtime-starttime)));
    }

    public GameClient(int clientnumber, String host,
		      int port, int nom, int noc) {
	this.port = port;
	this.clientnumber = clientnumber;
	this.host = host;
	this.nom = nom;
	this.noc = noc;
    }

    int port;
    int nom, noc, clientnumber;
    String host;

    public void run() {
	if (DEBUG)
	    System.out.println("client thread started");
	int ns = 0;

        try {
            Socket sock = new Socket(host, port); // unix server
	    if (DEBUG)
		System.out.println("connection made");
            InputStream in = sock.getInputStream();
            OutputStream out = sock.getOutputStream();
            PrintStream pout = new PrintStream(out);
	    BufferedReader d = new BufferedReader(new InputStreamReader(in)); 

	    for(int nr = 0; nr < nom; nr++) {
		String received = d.readLine();
		pout.println("message: " + clientnumber + " " + nr);
		if (DEBUG)
		    System.out.println(received + ":" + nr);
            }
	    pout.flush();
        }
        catch (UnknownHostException e) {
	    System.out.println("Can't find host");
	}
        catch (IOException e) {
	    System.out.println("Error connecting to host");
	}
    }

} // end of client class








