import java.util.*;
import java.io.*;

class FastScan {
    HashMap classes,revclasses;
    HashMap fields,revfields;
    HashMap methods,revmethods;
    HashMap callgraph;
    public FastScan() {
	classes=new HashMap();
	fields=new HashMap();
	methods=new HashMap();
	revclasses=new HashMap();
	revfields=new HashMap();
	revmethods=new HashMap();
	callgraph=new HashMap();
	initialize();
    }

    public static class FieldEntry {
       	String classname;
	String fieldname;
	String fielddesc;
	public FieldEntry(String classname, String fieldname, String fielddesc) {
	    this.classname=classname;
	    this.fieldname=fieldname;
	    this.fielddesc=fielddesc;
	}
	public int hashCode() {
	    return classname.hashCode()^fieldname.hashCode();
	}
	public boolean equals(Object o) {
	    try {
		FieldEntry fe2=(FieldEntry) o;
		if (fe2.classname.equals(classname)&&
		    fe2.fieldname.equals(fieldname))
		    return true;
	    } catch (Exception e) {;}
	    return false;
	}
    }
    
    private void initialize() {
	try {
	    FileReader fclass=new FileReader("fs-class");
	    int count=0;
	    while(true) {
		String str=nexttoken(fclass);
		if (str==null) break;
		classes.put(str, new Integer(count));
		revclasses.put(new Integer(count++),str);
	    }
	    fclass.close();

	    count=0;
	    FileReader ffields=new FileReader("fs-field");
	    while(true) {
		String str=nexttoken(ffields);
		if (str==null) break;
		String fieldname=nexttoken(ffields);
		String descriptor=nexttoken(ffields);
		FieldEntry fullfieldname=new FieldEntry(str,fieldname, descriptor);
		fields.put(fullfieldname, new Integer(count));
		revfields.put(new Integer(count++),fullfieldname);
	    }
	    ffields.close();

	    count=0;
	    FileReader fmethods=new FileReader("fs-method");
	    while(true) {
		String str=nexttoken(fmethods);
		if (str==null) break;
		methods.put(str,new Integer(count));
		revmethods.put(new Integer(count++),str);
	    }
	    fmethods.close();

	    FileReader fcall=new FileReader("fs-callgraph");
	    while(true) {
		String str=nexttoken(fcall);
		if (str==null) break;
		String caller=nexttoken(fcall);
		if (callgraph.containsKey(str))
		    ((Set)callgraph.get(str)).add(caller);
		else {
		    Set callers=new HashSet();
		    callers.add(caller);
		    callgraph.put(str,callers);
		}
	    }
	    fcall.close();

	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }


    String nexttoken(java.io.InputStreamReader isr) {
	String string="";
	int c=0;
	boolean looped=false;
	while(true) {
	    try {
		c=isr.read();
	    } catch (IOException e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	    if (c==-1)
		return null;
	    if ((c==' ')||(c=='\n')) {
		if (!looped) {
		    looped=true;
		    continue;
		}
		return string;
	    }
	    string=string+new String(new char[]{(char)c});
	    looped=true;
	}
    }
}
