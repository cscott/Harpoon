import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class NetsClient extends Thread {

    static boolean debug;
    public static void main(String argv[]) {

	String host=null;
	int numberofclients=0;
	int numberofmessages=0;
	int port=4321;
	NetsClient.debug=false;
	try {
	    host=argv[0];
	    port=Integer.parseInt(argv[1]);
	    numberofclients=Integer.parseInt(argv[2]);
	    numberofmessages=Integer.parseInt(argv[3]);
	} catch (Exception e) {
	    System.out.println("NetsClient host port numberofclients numberofmessages debugflag");
	}
	try {
	    NetsClient.debug=(Integer.parseInt(argv[4])==1);
	} catch (Exception e) {
	}

	long starttime=System.currentTimeMillis();
	NetsClient[] tarray=new NetsClient[numberofclients];
	for (int i=0;i<numberofclients;i++) {
	    tarray[i]=new NetsClient(i,host,port,numberofmessages,numberofclients);
	    if (debug)
		System.out.println("Attempting to start "+i);
	    tarray[i].connectt();
	    tarray[i].start();
	}
	try {
	    for (int i=0;i<numberofclients;i++) {
		tarray[i].join();
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	    System.out.println(e);
	}
	long endtime=System.currentTimeMillis();
	System.out.println("ChatClient");
	System.out.println("numclients:"+numberofclients);
	System.out.println("port:"+port);
	System.out.println("number of messages:"+numberofmessages);
	System.out.println("Elapsed time:(mS)"+(endtime-starttime));
	System.out.println("Throughput:"+(double) numberofclients*numberofmessages/((double) (endtime-starttime)));

    }

    public NetsClient(int clientnumber, String host,int port,int nom, int noc) {
	this.port=port;
	this.clientnumber=clientnumber;
	this.host=host;
	this.nom=nom;
	this.noc=noc;
    }

    int nom, noc,clientnumber,port;
    String host;
    DataInputStream dis;
    Socket sock;
    PrintStream pout;
    InputStream in;
    OutputStream out;
    DataInputStream din;

    public void connectt() {
	try{
	    sock = new Socket(host, port); // unix server
	    if (debug)
		System.out.println("connection made");
	    in = sock.getInputStream();
	    out = sock.getOutputStream();
	    pout = new PrintStream(out);
	    din = new DataInputStream(in);
	    pout.println("0|"+clientnumber+"|howdy");
	} catch (UnknownHostException e ) {System.out.println("can't find host"); }
        catch ( IOException e ) {System.out.println("Error connecting to host");}
    }

    public void run() {
	if (debug)
	    System.out.println("client thread started");
	int ns=0;


        try {
	    for(int nr=0;nr<noc*nom;) {
		if (ns<nom) {
		    ns++;
		    pout.println("0|"+clientnumber+"|hello"+ns+"**");
		}
		String request = din.readLine();
		if (request.indexOf('-')!=-1)
		    nr++;
		if (debug)
		    System.out.println(request+nr);
            }
	    pout.flush();
	}

        catch (UnknownHostException e ) {System.out.println("can't find host"); }
        catch ( IOException e ) {System.out.println("Error connecting to host");}

    }

} // end of client class
