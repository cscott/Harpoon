package harpoon.Test.PA.TestS;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class Nets {

    public static void main(String argv[]) {

        kickOff kickoffObject = new kickOff();
   }


} // end of Nets class

class kickOff extends Thread {

    DataInputStream dis;
    client[] clientArray = new client[100];
    int i = 0;
    int x = 0;
    String name;

    kickOff() {

        start();
}

    public void run() {


        server serverObject = new server();

    }

    void pause (int time) {

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

    server() {

        start();

    }

    public void run() {

        System.out.println("Server thread started");

        try {

       listen = new ServerSocket(4321);
     //     listen = new ServerSocket(8090);

        while(true) {

            readobj = new read_from_connection(listen.accept(), this);
            System.out.println("connection accepted");
          }

        }

        catch (UnknownHostException e ) {System.out.println("can't find host"); }
        catch ( IOException e ) {System.out.println("Error connecting to host");}
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


     synchronized void broadcast_message(String message) {

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



   void pause (int time) {

        try { Thread.sleep(time); }
        catch (InterruptedException e) { }
    }

} // end of server class

class read_from_connection extends Thread {

    Socket sock;
    InputStream in;
    OutputStream out;
    DataInputStream din;
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

        start();


    }

     public void run() {

        System.out.println("read_from_connection thread started");

        try {

            in = sock.getInputStream();
            out = sock.getOutputStream();
            pout = new PrintStream(out);
            din = new DataInputStream(in);

            pout.println("You have connected to the chat server, type something to begin");
            request = din.readLine();
            format_message();

            if (serverobj.duplicate_name(this)) {
                    pout.println("Name already in use - try another");
                    pout.println("disconecting...");
                    stop();
                    sock.close();
            }

            serverobj.list_names(pout, name);
            serverobj.add_to_vector(this);
            serverobj.broadcast_message(name + " has entered the chat room");
            pause(1000); // pause before sending
            serverobj.broadcast_message(format_message());


            while(true) {
                 request = din.readLine();
                 serverobj.broadcast_message(format_message());

            }
        }

        catch (UnknownHostException e ) {System.out.println("can't find host"); }
        catch ( IOException e ) {
                 serverobj.remove_from_vector(this);
                 if (name.equals(" ") == false) {
                     serverobj.broadcast_message(name + " has left the chat room");
                 }
                 stop();
       }
    }




   String format_message() {

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


      void pause (int time) {

        try { Thread.sleep(time); }
        catch (InterruptedException e) { }
    }




} // end of read_from_connection


class client extends Thread {

    DataInputStream dis;
    Socket sock;
    PrintStream pout;
    InputStream in;
    OutputStream out;
    DataInputStream din;

    client() {

      start();

    }

    public void run() {

        System.out.println("client thread started");

        try {

         //   sock = new Socket("161.178.105.233", 4321); // work
         //   sock = new Socket("161.178.105.220", 8888); // mike
            sock = new Socket("161.178.121.1", 4321); // unix server
            System.out.println("connection made");
            in = sock.getInputStream();
            out = sock.getOutputStream();
            pout = new PrintStream(out);
            din = new DataInputStream(in);

            while(true) {
               String request = din.readLine();
               System.out.println(request);
            }

        }

        catch (UnknownHostException e ) {System.out.println("can't find host"); }
        catch ( IOException e ) {System.out.println("Error connecting to host");}

    }

} // end of client class

