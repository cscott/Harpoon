package MCC.IR;

import java.util.Vector;

public class SimpleIRErrorReporter implements IRErrorReporter {
    
    public boolean error;

    String filename;
    Vector messages;
    

    public SimpleIRErrorReporter () {
	messages = new Vector();
	error = false;
        filename = null;
    }

    public void setFilename(String filename) {
        this.filename = new String(filename + ":");
    } 

    public void report(ParseNode v, String s) {

	LinedMessage sem = new LinedMessage();
	sem.error = true;
	sem.pn = v;
	sem.message = s;

	add(sem);
	
	error = true;
    }

    public void warn(ParseNode v, String s) {
	
	LinedMessage sem = new LinedMessage();
	sem.error = false;
	sem.pn = v;
	sem.message = s;
	
	add(sem);
    }

    private void add(LinedMessage sem) {

	if (sem.pn == null) {
	    messages.addElement(sem);
	    return;
	}

	int i;
	for (i = 0; i < messages.size(); i++) {

	    LinedMessage cur = (LinedMessage)messages.elementAt(i);

	    if (cur.pn.getLine() > sem.pn.getLine()) {
		break;
	    }
	}

	messages.insertElementAt(sem, i);
    }

    public String toString() {
	String output = new String();

	for (int i = 0; i < messages.size(); i++) {
	    LinedMessage sem = (LinedMessage)messages.elementAt(i);
	    if (sem.error) {
		output += "error";
	    } else {
		output += "warning";
	    }

	    if (sem.pn != null) {
		output += " (" + filename + sem.pn.getLine() + "): ";
	    } else {
                output += " : ";
            }

	    output += sem.message;
	    output += "\n";
	}
	return output;
    }
}

class LinedMessage {
    boolean error;
    public ParseNode pn;
    public String message;
}
