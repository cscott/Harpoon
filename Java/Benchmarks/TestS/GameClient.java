import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class GameClient extends Thread {

    static boolean debug;
    public static void main(String argv[]) {
	String host=null;
	int numberofclients=0;
	int numberofmessages=0;
	int port=4321;
	GameClient.debug=false;
	try {
	    host=argv[0];
	    port=Integer.parseInt(argv[1]);
	    numberofclients=Integer.parseInt(argv[2]);
	    numberofmessages=Integer.parseInt(argv[3]);
	} catch (Exception e) {
	    System.out.println("GameClient host port numberofclients numberofmessages debugflag");
	}
	try {
	    GameClient.debug=(Integer.parseInt(argv[4])==1);
	} catch (Exception e) {
	}


	GameClient[] tarray=new GameClient[numberofclients];
	for (int i=0;i<numberofclients;i++) {
	    tarray[i]=new GameClient(i,host,port,numberofmessages,numberofclients);
	    if (debug)
		System.out.println("Attempting to start "+i);
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
    }

    public GameClient(int clientnumber, String host,int port,int nom, int noc) {
	this.port=port;
	this.clientnumber=clientnumber;
	this.host=host;
	this.nom=nom;
	this.noc=noc;
    }
    int port;
    int nom, noc,clientnumber;
    String host;
    DataInputStream dis;
    Socket sock;
    PrintStream pout;
    InputStream in;
    OutputStream out;
    DataInputStream din;

    public void run() {
	if (debug)
	    System.out.println("client thread started");
	int ns=0;


        try {

            sock = new Socket(host, port); // unix server
	    if (debug)
		System.out.println("connection made");
            in = sock.getInputStream();
            out = sock.getOutputStream();
            pout = new PrintStream(out);
            din = new DataInputStream(in);

	    for(int nr=0;nr<nom;) {
		String received=din.readLine();
		pout.println("message: "+clientnumber+" "+nr);

		if (debug)
		    System.out.println(received+":"+nr);
            }
	    pout.flush();

        }

        catch (UnknownHostException e ) {System.out.println("can't find host"); }
        catch ( IOException e ) {System.out.println("Error connecting to host");}

    }

} // end of client class








