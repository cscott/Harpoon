//****************************************************************************
// Programmer: Duane M. Gran, ragnar@cs.bsu.edu
// Program:    JhttpServer
// Date:       April 24, 1998
//****************************************************************************

import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;
import java.net.*;

//****************************************************************************
// Class:   logFile
// Purpose: Handle the behavior for logging connections.  The methods simply
//          add to the private data fields.  Has implementation for standard
//          output, as well as output to file.
//
//          An example log entry looks like:
//
// 1.2.3.4 - - [29/JAN/1998:21:40:30 -06] "GET /file.html HTTP/1.0" 200 472
//
//****************************************************************************

public class LogFile
{
    static public final String log_file_name = "server.log";

    static public String write_log(Socket s, String Method, String URI, String Protocol, 
				   int ReturnCode, long BytesSent){

	String addr = s.toString();	
	String Address = addr.substring(addr.indexOf('/') + 1, addr.indexOf(','));

	//	SimpleDateFormat sdf =
	//	    new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");  // RFC 1123
	//	sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
	//	String Date = sdf.format(new Date());

	String Entry = 
	    Address + " - - [" +                      // IP address
	    "Date" + "] \"" +                           // date
	    Method + " " +                            // get, post, head
	    URI + " " +                               // filename
	    Protocol + "\" " +                        // http/1.?
	    ReturnCode + " " +      // 200-500
	    BytesSent + "\n";       // bytes sent

	try{
	    BufferedWriter out = new BufferedWriter(
				 new OutputStreamWriter(
				 new FileOutputStream(log_file_name, true)));
	    
	    out.write(Entry,0,Entry.length());
	    out.flush();
	    out.close();
	}
	catch (IOException e){
	    System.err.println("Gicu " + e);
	}

	return Entry;
    }
}
