import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Nets {
    
    public static void main(String argv[]) {
        kickOff kickoffObject = new kickOff(Integer.parseInt(argv[0]));
	kickoffObject.run();
    }
    
    
} // end of Nets class

class kickOff extends Thread {
    
    DataInputStream dis;
    int i = 0;
    int x = 0;
    String name;
    int port;
    
    kickOff(int port) {
	this.port=port;
    }
    
    public void run() {
        server serverObject = new server(port);
	serverObject.run();
    }
    
    private void pause (int time) {
	
        try { Thread.sleep(time); }
        catch (InterruptedException e) { }
    }
    
} // end of kickOff class

class server extends Thread {
    read_from_connection readobj;
    int num = 1;
    Thread readthread;
    Socket sock;
    ServerSocket listen;
    static int i = 0;
    static Vector v = new Vector();
    int port;
    
    server(int port) {
	this.port=port;
    }
    
    private static void startthread(server t) throws IOException {
	read_from_connection readobj = 
	    new read_from_connection(t.listen.accept(), t);
	readobj.start();
    }
    
    public void run() {
        System.out.println("Server thread started");
	
        try {
	    listen = new ServerSocket(port);

	    while(true) {
		startthread(this);
		System.out.println("connection accepted");
	    }   
        }
        catch (UnknownHostException e) {
	    System.out.println("Can't find host");
	}
        catch (IOException e) {
	    System.out.println("Error connecting to host");
	}
    }


    synchronized void add_to_vector(read_from_connection obj) {
        v.addElement(obj);
    }


     synchronized void remove_from_vector(read_from_connection obj) {
        v.removeElement(obj);
    }

     synchronized boolean duplicate_name(read_from_connection obj) {
        read_from_connection readobj;

        for (int i = 0; i < v.size(); i++) {
            readobj = (read_from_connection)v.elementAt(i);
            if (readobj.name.equals(obj.name))
                return true;
        }

        return false;
    }


    void broadcast_message(String message) {
       read_from_connection readobj;

       for (int i = 0; i < v.size(); i++) {
           readobj = (read_from_connection)v.elementAt(i);
           readobj.pout.println(message);
       }
    }


     synchronized void list_names(PrintStream pout, String name) {
        String line;
        read_from_connection readobj;

        pout.println("Welcome " + name);

        if (v.size() > 0 )
            pout.println("The following people are in the chat room: ");
        else
           pout.println("You are alone in the chat room");


        line = "";
        int cnt = 0;

        for (int i = 0; i < v.size(); i++) {
            readobj = (read_from_connection)v.elementAt(i);

            if (line.equals(""))
               line += readobj.name;
            else
               line += ", " +readobj.name;

            cnt++;
            if (cnt == 10) {
		pout.println(line);
               line = "";
               cnt = 0;
            }
        }
	
        if (line != "")
	    pout.println(line);
    }

   private void pause (int time) {
       try { Thread.sleep(time); }
       catch (InterruptedException e) { }
   }
    
} // end of server class


class read_from_connection extends Thread {

    Socket sock;
    //DataInputStream din; // deprecated
    PrintStream pout;
    int hours;
    int minutes;
    int seconds;
    String name = " ";
    String request;
    server serverobj;
    read_from_connection readobj;

    read_from_connection(Socket sock, server serverobj) {
        this.sock = sock;
        this.serverobj = serverobj;
    }

     public void run() {

        System.out.println("read_from_connection thread started");

        try {
            InputStream in = sock.getInputStream();
            OutputStream out = sock.getOutputStream();
            pout = new PrintStream(out);
            //din = new DataInputStream(in); // deprecated
	    BufferedReader d = new BufferedReader(new InputStreamReader(in)); 
            pout.println("You have connected to the chat server, type something to begin");
            request = d.readLine();
            format_message();
	    
            if (serverobj.duplicate_name(this)) {
		pout.println("Name already in use - try another");
		pout.println("disconecting...");
		sock.close();
		return;
            }
	    
            serverobj.list_names(pout, name);
            serverobj.add_to_vector(this);
            serverobj.broadcast_message(name + " has entered the chat room");
            pause(1000); // pause before sending
            serverobj.broadcast_message(format_message());
	    
	    
            while(true) {
		request = d.readLine();
		if (request!=null)
		    serverobj.broadcast_message(format_message());
		
            }
        }
	catch (UnknownHostException e ) {
	    System.out.println("can't find host");
	}
        catch (IOException e) {
	    serverobj.remove_from_vector(this);
	    if (name.equals(" ") == false) {
		serverobj.broadcast_message(name + " has left the chat room");
	    }
	    return;
	}
     }

    private String format_message() {
        String text;
        String hoursout;
        String hoursS;
        String minutesS;
        String secondsS;
        String name;
        int a;
        int b;
        int c;

	text = "";
        name = "";
	/*
	  Date date = new Date();

	  hours = date.getHours();
	  hoursS = Integer.toString(hours);
	  if (hours <= 9)
	  hoursS = "0"+hoursS;

	  minutes = date.getMinutes();
	  minutesS = Integer.toString(minutes);
	  if (minutes <= 9)
	  minutesS = "0"+minutesS;
	  
	  seconds =  date.getSeconds();
	  secondsS = Integer.toString(seconds);
	  if (seconds <= 9)
	  secondsS = "0"+secondsS;

	  hoursout = "[" + hoursS + ":" + minutesS + ":" + secondsS +"]";
	*/

	hoursout = "[0:00:00]";

        a = request.indexOf('|');
        if (a == -1) return hoursout + " " + name + " - " +  text;

        b = request.indexOf('|', a + 1);
        if (a == -1) return hoursout + " " + name + " - " +  text;

        name = request.substring(a + 1,b);
        this.name = name;
        c = request.length();

        text = request.substring(b + 1, c - 1);

        return hoursout + " " + name + " - " +  text;
    }

    private void pause (int time) {
        try { Thread.sleep(time); }
        catch (InterruptedException e) { }
    }
} // end of read_from_connection

