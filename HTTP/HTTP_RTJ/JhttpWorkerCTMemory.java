//****************************************************************************
// Programmer: Duane M. Gran, ragnar@cs.bsu.edu
// Program:    JhttpServer
// Date:       April 24, 1998
//****************************************************************************


import java.net.*;
import java.io.*;
import java.util.*;

import sun.io.CharToByteConverter;

//****************************************************************************
// Class:   JhttpWorker
// Purpose: Takes an HTTP request and executes it in a separate thread
//****************************************************************************

public class JhttpWorkerCTMemory extends Thread{
    private Socket client;
    public  int    fileLength, returnCode;
    private boolean logging;

    public JhttpWorkerCTMemory(Socket client, boolean logging) {
	this.client = client;
	this.logging=logging;
    }
    
    private class ParamsCTMemory {
	String fileName = null;
	String methodType = null;
	String httpVersion = "http/1.0";
    };
    
    public void run() { 
	ParamsCTMemory params = new ParamsCTMemory();

	try {
	    HTTPResponseCTMemory resp = new HTTPResponseCTMemory();
	    
	    BufferedReader  in = null;
	    BufferedWriter out = null;
	    
	    resp.returnCode = 200;
	    resp.sentBytes = 0;
	    
	    try {
		
		in =
		    new BufferedReader
			(new InputStreamReader
			    (client.getInputStream()));
		
		out = 
		    new BufferedWriter
			(new OutputStreamWriter
			    (client.getOutputStream()));
		
	    }
	    catch(IOException e) {
		// I'm not too good at HTTP. Normally, we should put some
		// error code here. Anyway, I have assumed that an error
		// is equivalent to an unhandled request / method (501)
		resp.returnCode = 501; 
	    }

	    if(resp.returnCode == 200) {
		// call the appropriate hanndler
		switch(method(in, params)) {
		case 0:
		    HTTPServicesCTMemory.GET_handler(params.fileName, 
						     out, resp);
		    break;
		case 1:
		    HTTPServicesCTMemory.HEAD_handler(params.fileName, 
						      out, resp);
		break;
		case 2:
		    HTTPServicesCTMemory.POST_handler(params.fileName, 
						      out, resp);
		    break;
		default:
		    resp.returnCode = 501; //error
		}
		
		try {
		    out.flush();
		    if (logging)
			LogFileCTMemory.write_log(client,
						  params.methodType, 
						  params.fileName,
						  params.httpVersion,
						  resp.returnCode, 
						  resp.sentBytes);
		    out.close();
		    in.close();
		    client.close();
		}
		catch(IOException e) {
		    ; // do nothing
		}
	    }
	//	System.out.println(fileName + " is going to finish"); // debug 
	} catch (Exception e) { // Shouldn't need this
	    System.out.println(e.toString());
	    System.exit(1);
	}
    }
  
//*****************************************************************************
// Function: method()
// Purpose:  Open an InputStream and parse the request made.  
// Note:     Regardless of what method is requested, right now it performs a
//           GET operation.
// Calls:    
// Returns:  Boolean value for success or failure
//*****************************************************************************

  private int method(BufferedReader in, ParamsCTMemory params){
      int ret = -1;

      try{
	  String line;
	  
	  // read just the first line
	  line = in.readLine();
	  // only spaces used
	  StringTokenizer tok = new StringTokenizer(line, " ");  
	  if (tok.hasMoreTokens())  // make sure there is a request
	      {
		  String str = tok.nextToken();
          
		  if ( str.equals("GET") ){
		      ret = 0;
		      params.methodType = "GET";
		  }
		  else if ( str.equals("HEAD") ){
		      ret = 1;
		      params.methodType = "HEAD";
		  }
		  else if ( str.equals("POST") ){
		      ret = 2;
		      params.methodType = "POST";
		  }
		  else{
		      // System.out.println("501 - unsupported request");
		      return -1;
		  } 
	      }      
	  else{
	      // System.out.println("Request from browser was empty!");
	      return -1;
	  }
	  
	  // get the filename
	  if (tok.hasMoreTokens())
	      {
		  params.fileName = tok.nextToken();
		  if(params.fileName.equals("/"))
		      {
			  params.fileName = "/index.html";
		      }
	      }
	  else
	      {
		  // this is weird... why am i taking the first character of
		  // the filename if there are no more tokens?
		  // - catch should take care of this
		  params.fileName = params.fileName.substring(1);
	      }  
	  
	  // read the http version number
	  // - right now nothing is done with this information
	  if (tok.hasMoreTokens())
	      {
		  params.httpVersion = tok.nextToken();
	      }
	  else
	      {
		  params.httpVersion = "http/1.0";              // default
	      }
	  
	  // read remainder of the browser's header
	  // - nothing done right now with this info... placeholder
	  while((line = in.readLine()) != null)
	      {
		  StringTokenizer token = new StringTokenizer(line," ");
		  
		  // do processing here
		  if(!token.hasMoreTokens())
		      { 
			  break;          
		      }
	      }
      }
      catch(Exception e){
	  System.err.println(e);
	  return -1;
      }
      
    return ret;
  }
}
