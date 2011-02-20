//****************************************************************************
// Programmer: Duane M. Gran, ragnar@cs.bsu.edu
// Program:    JhttpServer
// Date:       April 24, 1998
//****************************************************************************

import java.net.*;
import java.util.*;
import java.io.*;

//****************************************************************************
// Class:   httpResponse
// Purpose: constructs the header to be returned by the server
//****************************************************************************

public class HTTPHeader {

  // make a hashtable of return codes to messages
  static private HashStrings rc = new HashStrings();
  static
  {
    rc.put("200", "OK");
    rc.put("403", "Fobidden");
    rc.put("404", "Not found");
    rc.put("501", "Method not implemented");
  }

  // hashtable of content type matchings
  static private HashStrings ct = new HashStrings();   // p. 817
  static
  {
    ct.put("txt",   "text/plain");
    ct.put("text",  "text/plain");
    ct.put("log",   "text/plain");
    ct.put("htm",   "text/html");
    ct.put("html",  "text/html");
    ct.put("gif",   "image/gif");
    ct.put("jpg",   "image/jpg");
    ct.put("jpeg",  "image/jpg");
    ct.put("jpe",   "image/jpg");
    ct.put("mpg",   "video/mpeg");
    ct.put("mpeg",  "video/mpeg");
    ct.put("mpe",   "video/mpeg");
    ct.put("qt",    "video/quicktime");
    ct.put("mov",   "video/quicktime");
    ct.put("au",    "audio/basic");
    ct.put("snd",   "audio/basic");
    ct.put("wav",   "audio/x-wave");
    ct.put("class", "application/octet-stream");
  }
  
//*************************************************************************
// Constructor: send_header(int, String, int)
// Purpose:     Send an HTTP header
//*************************************************************************

  static public void send_header(BufferedWriter out, int returnCode,
				   String filename, long fileLength){
      String contentType  = getContentTypeFor(filename);
      String returnString = (String) rc.get(String.valueOf(returnCode));
      String header;

      header = "HTTP/1.0 " + returnCode + " " + returnString + "\n" +
	  "Date: " + "1/1/00" + "\n" +                   // date
	  "Allow: GET\n" +                               // allowed methods
	  "MIME-Version: 1.0\n" +                        // mime version
	  "Server : SpinWeb Custom HTTP Server\n" +      // server type
	  "Content-Type: " + contentType + "\n" +        // type
	  "Content-Length: "+ fileLength + "\n\n";       // length
      try{
	  out.write(header,0,header.length());
      }
      catch(IOException e){
	  ; // do nothing!
      }
  }

//*************************************************************************
// Method:  getContentTypeFor(String)
// Purpose: Looks up the content type (MIME) in a hashtable for the given
//          file suffix.  It removes any anchors (#) in case the string is
//          a URL and then operates on the name without path.
//*************************************************************************
  
  static private String getContentTypeFor(String filename)
  {
    int position = filename.lastIndexOf('#');
    if (position != -1)
      filename = filename.substring(0, position - 1);
      
    File f      = new File(filename);
    String name = f.getName();         // name w/o directory

    position = name.lastIndexOf('.');
    
    String contentType;

    if (position == -1)  // if no extension, txt is assigned by default
	contentType = "txt";    
    else  
	contentType = name.substring(position + 1);
    
    return (String) ct.get(contentType);
  } 

}
