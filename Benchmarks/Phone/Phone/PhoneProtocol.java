// PhoneProtocol.java, created Sun Apr  2 16:35:59 EDT 2000 govereau
// Licensed under the terms of the GNU GPL; see COPYING for details.


/**
 * <code>PhoneProtocol</code> formats and interprets phone requests and responses
 *
 * request strig format is name#number# where number may be null to indicate a get request,
 * name and number may be null to indicate an error,
 * and both name and number non-null indicate a put request
 *
 * @author P.Govereau govereau@mit.edu
 * @version $Id: PhoneProtocol.java,v 1.1 2000-07-18 22:18:32 bdemsky Exp $
 */
public class PhoneProtocol {
    public static int PUT = 1;
    public static int GET = 2;
    public static int ERROR = 3;
    
    public int type;
    public String name;
    public String number;
    
    public String request;
    
    public PhoneProtocol(String name, String number) {
	this.name = name;
	this.number = number;
	if (name == null) {
	    this.type = PhoneProtocol.ERROR;
	    this.request = "##";
	} else if (number == null) {
	    this.type = PhoneProtocol.GET;
	    this.request = name + "##";
	} else {
	    this.type = PhoneProtocol.PUT;
	    this.request = name + "#" + number + "#";
	}
    }
    
    public PhoneProtocol(String request) {
	this.request = request;
	if (request == null) {
	    this.name = null;
	    this.number = null;
	    this.type = PhoneProtocol.ERROR;
	    return;
	}
	
	int name_end = request.indexOf('#');
	int number_end = request.lastIndexOf('#');
	if (name_end == -1) {  // note also handles -1 case
	    name = null;
	    number = null;
	    type = PhoneProtocol.ERROR;
	    request = "##";
	} else {
	    name = request.substring(0, name_end);
	    if (name_end == number_end - 1 || name_end == number_end) {
		number = null;
		type = PhoneProtocol.GET;
	    } else {
		number = request.substring(name_end + 1, number_end);
		type = PhoneProtocol.PUT;
	    }
	}
    }
    
    public void setNumber(String number) {
	this.number = number;
	this.request = name + "#" + number + "#";
    }
    
    public void setError() {
	this.type = PhoneProtocol.ERROR;
	this.name = null;
	this.number = null;
	this.request = "##";
    }
}
