import java.net.*;
import java.io.*;
import java.util.*;

class Fields {
    RoleI rolei;
    Set includefields;

    public Fields(RoleI rolei) {
	this.rolei=rolei;
    }

    public void readfieldfile() {
	includefields=new HashSet();
	try {
	    FileReader fr=new FileReader("fields");
	    while(true) {
		String classname=(String) rolei.nexttoken(fr);
		if (classname==null)
		    break;
		String fieldname=(String) rolei.nexttoken(fr);
		String fielddesc=(String) rolei.nexttoken(fr);
		includefields.add(new FastScan.FieldEntry(classname,fieldname, fielddesc));
		
	    }
	    fr.close();
	} catch (FileNotFoundException e) {
	    Iterator fieldit=rolei.classinfo.fields.keySet().iterator();
	    while(fieldit.hasNext()) {
		includefields.add(fieldit.next());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    public void writefieldfile() {
	Iterator fieldit=includefields.iterator();
	try {
	    FileOutputStream fos=new FileOutputStream("fields");
	    PrintStream ps=new PrintStream(fos); 
	    while(fieldit.hasNext()) {
		FastScan.FieldEntry field=(FastScan.FieldEntry)fieldit.next();
		ps.println(field.classname+" "+field.fieldname+" "+field.fielddesc);
	    }
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void buildfieldspage() {
	synchronized(rolei) {
	    try {
		FileOutputStream fos=new FileOutputStream("fields.html");
		PrintStream ps=new PrintStream(fos);
		rolei.menuline(ps);
		FastScan classinfo=rolei.classinfo;
		Set fieldset=classinfo.fields.keySet();
		Iterator fieldit=fieldset.iterator();
		while(fieldit.hasNext()) {
		    FastScan.FieldEntry field=(FastScan.FieldEntry)fieldit.next();
		    String included=includefields.contains(field)?"Included <a href=\"rm-F"+classinfo.fields.get(field)+",0\">Remove</a>":"Not Included <a href=\"rm-F"+classinfo.fields.get(field)+",1\">Add</a>";
		    ps.println(field.classname+"."+field.fieldname+" "+included+"<p>");
		}
		ps.close();
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	}
    }
}
