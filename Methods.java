import java.util.*;
import java.io.*;

class Methods {
    RoleI rolei;
    HashMap statechange;

    public Methods(RoleI rolei) {
	this.rolei=rolei;
    }

    String statechange(String methodname) {
	if (statechange.containsKey(methodname)) {
	    String methodstate="State change on: ";
	    Set posset=(Set) statechange.get(methodname);
	    Iterator it=posset.iterator();
    	    while(it.hasNext())
		methodstate+="ARG: "+ it.next()+" \n";
	    return methodstate;
	} else return "State change on: ";
    }
    
    void changestate(String methodname, Integer position) {
	if (statechange.containsKey(methodname)) {
	    Set sset=(Set)statechange.get(methodname);
	    if (sset.contains(position))
		sset.remove(position);
	    else
		sset.add(position);
	} else {
	    Set sset=new HashSet();
	    sset.add(position);
	    statechange.put(methodname,sset);
	}
    }

    String statestring(String methodname, String returnpage) {
	int numargs=numberofargs(methodname);
	String statestring=statechange(methodname);
	Integer methodnum=(Integer)rolei.classinfo.methods.get(methodname);
	for(int i=0;i<numargs;i++) {
	    statestring+="<a href=\"rm-S"+methodnum+","+i+","+returnpage+"\"> ARG"+i+"</a> ";
	}
	statestring+="<p>";
	return statestring;
    }

    int numberofargs(String methodname) {
	boolean isstatic=rolei.classinfo.staticmethods.contains(methodname);
	int count=isstatic?0:1;
	int openindex=methodname.indexOf('(');
	int closeindex=methodname.indexOf(')');
	for(int i=openindex+1;i<closeindex;i++) {
	    switch(methodname.charAt(i)) {
	    case '[':
		count++;
		i++;
		if (methodname.charAt(i)=='L')
		    for(;methodname.charAt(i)!=';';i++);
		break;
	    case 'L':
		count++;
		for(;methodname.charAt(i)!=';';i++);
		break;
	    default:
	    }
	}
	return count;
    }

    public void writeentries() {
	Iterator fieldit=statechange.keySet().iterator();
	try {
	    FileOutputStream fos=new FileOutputStream("statechange");
	    PrintStream ps=new PrintStream(fos); 
	    while(fieldit.hasNext()) {
		String method=(String)fieldit.next();
		Set posset=(Set)statechange.get(method);
		Iterator pit=posset.iterator();
		while(pit.hasNext()) {
		    ps.println(method+" "+pit.next());
		}
	    }
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void readentries() {
	statechange=new HashMap();
	try {
	    FileReader fr=new FileReader("statechange");
	    while(true) {
		String methodname=(String) rolei.nexttoken(fr);
		if (methodname==null)
		    break;
		Integer position=Integer.valueOf((String) rolei.nexttoken(fr));
		if (statechange.containsKey(methodname))
		    ((Set)statechange.get(methodname)).add(position);
		else {
		    Set pset=new HashSet();
		    pset.add(position);
		    statechange.put(methodname, pset);
		}
	    }
	    fr.close();
	} catch (FileNotFoundException e) {
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }
}
