import java.net.*;
import java.io.*;
import java.util.*;

class RoleI {
    HashMap transitiontable;
    HashMap roletable;

    public RoleI() {
	//	runanalysis();
	readtransitions();
	readroles();
	test();
    }

    String handleresponse(String filename, BufferedWriter out, HTTPResponse resp) {
	System.out.println(filename);
	int firstslash=filename.indexOf('/');
	int dash=filename.indexOf('-',firstslash+1);
	int question=filename.indexOf('?',dash+1);
	int comma=filename.indexOf(',',question+1);
	String file=filename.substring(dash+1, question)+".imap";
	int x=Integer.parseInt(filename.substring(question+1, comma));
	int y=Integer.parseInt(filename.substring(comma+1));
	Imap imap=new Imap(file);
	if (imap.parseclick(x,y)==null)
	    return "/"+filename.substring(dash+1,question)+".html";
	Integer rolenum=Integer.valueOf(imap.parseclick(x,y));
	Role role=(Role)roletable.get(rolenum);
	try {
	    FileOutputStream fos=new FileOutputStream("R"+rolenum+".html");
	    PrintStream ps=new PrintStream(fos);
	    ps.println(role);
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	return "/R"+rolenum+".html";
    }

    

    void test() {
	Set ks=transitiontable.keySet();
	Iterator it=ks.iterator();
	int i=0;
	try {
	    FileOutputStream fos=new FileOutputStream("index.html");
	    PrintStream ps=new PrintStream(fos);
	    while(it.hasNext()) {
		String clss=(String)it.next();
		System.out.println(clss);
		genpictures(clss, (new Integer(i++)).toString());
		ps.println("<a href=\""+(i-1)+".html\">"+clss+"</a><p>");
	    }
	    ps.close();
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    void runanalysis() {
	try {
	    Runtime runtime=Runtime.getRuntime();
	    Process p1=runtime.exec("FastScan");	
	    p1.waitFor();
	    Process p2=runtime.exec("RoleInference -cpolicy -w -n -r -ptemp");
	    p2.waitFor();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    void genpictures(String classname, String filename) {
	writedotfile(classname, filename);
	try {
	    Runtime runtime=Runtime.getRuntime();
	    Process p1=runtime.exec("dot -Tgif "+filename+" -o"+filename+".gif");	
	    p1.waitFor();
	    Process p2=runtime.exec("dot -Timap "+filename+" -o"+filename+".imap");	
	    p2.waitFor();
	    FileOutputStream fos=new FileOutputStream(filename+".html");
	    PrintStream ps=new PrintStream(fos);
	    ps.println("<a href=\"ri-"+filename+"\"><img src=\"/"+filename+".gif\" ismap> </a>");
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    void writedotfile(String classname,String filename) {
	Set transet=(Set) transitiontable.get(classname);
	FileOutputStream fos=null;
	try {
	    fos=new FileOutputStream(filename);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	PrintStream ps=new PrintStream(fos);
	Iterator it=transet.iterator();
	ps.println("digraph \""+classname+"\" {");
	ps.println("ratio=auto");
	Set observedroles=new HashSet();

	while(it.hasNext()) {
	    Transition tr=(Transition) it.next();
	    ps.print("R"+tr.rolefrom+" -> R"+tr.roleto+" [URL=\""+tr.rolefrom+"-"+tr.roleto+"\",fontsize=10,label=\"");
	    observedroles.add(new Integer(tr.rolefrom));
	    observedroles.add(new Integer(tr.roleto));
	    for(int i=0;i<tr.names.length;i++) {
		ps.print(tr.names[i]);
		if ((i+1)<tr.names.length)
		    ps.print(",");
	    }
	    ps.print("\"");
	    if (tr.type==1)
		ps.print(",color=blue");
	    ps.println("];");

	}
	Iterator iterator=observedroles.iterator();
	while(iterator.hasNext()) {
	    Integer role=(Integer) iterator.next();
	    ps.println("R"+role+" [URL=\""+role+"\"];");
	}

	ps.println("}");
	ps.close();
    }

    void readroles() {
	roletable=new HashMap();
	FileReader fr=null;
	try {
	    fr=new FileReader("webrole");
	} catch (FileNotFoundException e) {
	    System.out.println(e);
	    System.exit(-1);
	}
	while(true) {
	    String rolenumber=nexttoken(fr);
	    if (rolenumber.equals("~~")) {
		try {
		    fr.close();
		} catch (IOException e) {
		    System.out.println(e);
		    System.exit(-1);
		}
		return;
	    }
	    String classname=nexttoken(fr);

	    String contained=nexttoken(fr);
	    ArrayList al=new ArrayList();
	    while(true) {
		String localglobal=nexttoken(fr);
		if (localglobal.equals("~~"))
		    break;
		if (localglobal.equals("0")) {
		    al.add(new Dominator(nexttoken(fr), nexttoken(fr), nexttoken(fr)));
		} else if (localglobal.equals("1")) {
		    al.add(new Dominator(nexttoken(fr)));
		} else {
		    System.out.println("ERROR");System.exit(-1);}
	    }
	    Dominator[] dominators=(Dominator[])al.toArray(new Dominator[al.size()]);

	    al=new ArrayList();
	    while(true) {
		String ffieldname=nexttoken(fr);
		if (ffieldname.equals("~~"))
		    break;
		String fclassname=nexttoken(fr);
		String duplicates=nexttoken(fr);
		Reference ref=new Reference(fclassname, ffieldname, Integer.parseInt(duplicates));
		al.add(ref);
	    }
	    Reference[] rolefieldlist=(Reference[]) al.toArray(new Reference[al.size()]);
	    al=new ArrayList();
	    while(true) {
		String fclassname=nexttoken(fr);
		if (fclassname.equals("~~"))
		    break;
		String duplicates=nexttoken(fr);
		Reference ref=new Reference(fclassname, Integer.parseInt(duplicates));
		al.add(ref);
	    }
	    Reference[] rolearraylist=(Reference[]) al.toArray(new Reference[al.size()]);
	    al=new ArrayList();
	    while(true) {
		String ffieldname1=nexttoken(fr);
		if (ffieldname1.equals("~~"))
		    break;
		String ffieldname2=nexttoken(fr);
		IdentityRelation irel=new IdentityRelation(ffieldname1, ffieldname2);
		al.add(irel);
	    }
	    IdentityRelation[] identityrelations=(IdentityRelation[]) al.toArray(new IdentityRelation[al.size()]);

	    al=new ArrayList();
	    while(true) {
		String ffieldname=nexttoken(fr);
		if (ffieldname.equals("~~"))
		    break;
		String role=nexttoken(fr);
		Reference ref=new Reference(Integer.parseInt(role),ffieldname);
		al.add(ref);
	    }
	    Reference[] nonnullfields=(Reference[]) al.toArray(new Reference[al.size()]);
	    al=new ArrayList();
	    while(true) {
		String fclassname=nexttoken(fr);
		if (fclassname.equals("~~"))
		    break;
		String role=nexttoken(fr);
		String duplicates=nexttoken(fr);
		Reference ref=new Reference(fclassname,Integer.parseInt(role), Integer.parseInt(duplicates));
		al.add(ref);
	    }
	    Reference[] nonnullarrays=(Reference[]) al.toArray(new Reference[al.size()]); 
	    al=new ArrayList();
	    while(true) {
		String fmethodname=nexttoken(fr);
		if (fmethodname.equals("~~"))
		    break;
		al.add(fmethodname);
	    }
	    String[] invokedmethods=(String[]) al.toArray(new String[al.size()]); 
	    roletable.put(java.lang.Integer.valueOf(rolenumber),
			  new Role(Integer.parseInt(rolenumber), classname,contained.equals("1"), dominators,
				   rolefieldlist, rolearraylist, identityrelations, nonnullfields, nonnullarrays, invokedmethods));
	}
    }

    void readtransitions() {
	FileReader fr=null;
	transitiontable=new HashMap();

	try {
	    fr=new FileReader("webtransitions");
	} catch (FileNotFoundException e) {
	    System.out.println(e);
	    System.exit(-1);
	}
	while(true) {
	    String transclass=nexttoken(fr);
   	    if (transclass.equals("~~")) {
		try {
		    fr.close();
		} catch (IOException e) {
		    System.out.println(e);
		    System.exit(-1);
		}
		return;
	    }
	    Set set=new HashSet();
	    while(true) {
		String role1=nexttoken(fr);
		if (role1.equals("~~")) {
		    transitiontable.put(transclass,set);
		    break;
		}
		String role2=nexttoken(fr);
		String transitiontype=nexttoken(fr);
		ArrayList al=new ArrayList();
	    
		String transitionname=nexttoken(fr);
		al.add(transitionname);
		while(true) {
		    String nexttransition=nexttoken(fr);
		    if (nexttransition.equals("~~"))
			break;
		    al.add(nexttransition);
		}
		set.add(new Transition(Integer.parseInt(role1),Integer.parseInt(role2),Integer.parseInt(transitiontype),(String[])al.toArray(new String[al.size()])));
	    }
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
