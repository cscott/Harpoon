import java.net.*;
import java.io.*;
import java.util.*;

public class HTTPServices{

    static private String webRoot = "/home/httpd/html";

    static private Reader get_reader(String fileName,HTTPResponse resp) throws IOException{
	try{
//  	    if(fileName.equals("/daytime")){
//  		String date_str = (new Date()).toString();
//  		resp.sentBytes = date_str.length();
//  		return
//  		    new StringReader(date_str);
//  	    }
	    
	    if(fileName.equals("/viewlog"))
		fileName = LogFile.log_file_name;
	    else
		fileName = webRoot + fileName;
	    
			// PG
	    File f = new File(fileName);
	    resp.sentBytes = f.length();
	    return new FileReader(f);
	}
	catch(IOException e){
	    resp.returnCode = 501;
	    return
		new StringReader("Error accessing " + fileName);
	}
    }

    public static void GET_handler(
		String fileName, BufferedWriter out,
		HTTPResponse resp) {
	
		BufferedReader reader  = null;
		char buffer[];
		int size;

		if((reader = HEAD_handler_int(fileName,out,resp)) == null) return;

		buffer = new char[1024];
		
		try{
			while((size = reader.read(buffer,0,buffer.length)) != -1)
				out.write(buffer,0,size);
			reader.close();
		}
		catch(IOException e){
			resp.returnCode = 501; // error during transmision
		}

		
    }

    public static void POST_handler(
		String fileName, BufferedWriter out, HTTPResponse resp){
		GET_handler(fileName,out,resp);
    }

    static private BufferedReader HEAD_handler_int(
		String fileName,
		BufferedWriter out, HTTPResponse resp) {
		
		BufferedReader reader = null;
		
		try{
			reader = new BufferedReader(get_reader(fileName, resp));
			resp.returnCode = 200;
		}
		catch(IOException e){
			resp.returnCode = 404; // file not found
		}
		
		if(resp.returnCode == 200)
			HTTPHeader.send_header(out, resp.returnCode, fileName, resp.sentBytes);
		else{
			HTTPHeader.send_header(out, resp.returnCode, fileName, 0);
			return null;
		}
    	
		return reader;
    }
	

    public static void HEAD_handler(String fileName, 
				   BufferedWriter out, HTTPResponse resp){
		HEAD_handler_int(fileName,out,resp);
    }
}
