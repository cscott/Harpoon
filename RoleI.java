import java.net.*;
import java.io.*;
import java.util.*;

class RoleI {
    HashMap transitiontable;
    HashMap roletable;
    FastScan classinfo;
    Set atomic;
    HashMap policymap;
    boolean containers;
    Set diagram;
    HashMap containedmapping;
    Fields fields;
    Methods methods;
    

    static final Integer P_NEVER=new Integer(0);
    static final Integer P_ONCEEVER=new Integer(1);
    static final Integer P_ONEATATIME=new Integer(2);
    static final Integer P_ALWAYS=new Integer(3);

    Integer defaultpolicy;

    synchronized private void initializefastscan() {
	/*	Runtime runtime=Runtime.getRuntime();
	Process p1=runtime.exec("./FastScan");	
	p1.waitFor();*/
	classinfo=new FastScan();
    }

    public RoleI() {
	//	runanalysis();
	containers=false;
	initializefastscan();
	readtransitions();
	readroles();
	readatomic();
	readpolicy();
	rebuildindex();
	rebuildgraphs(true);
	gendiagram();
	genmergeddiagram();
	fields=new Fields(this);
	fields.readfieldfile();
	fields.buildfieldspage();
	methods=new Methods(this);
	methods.readentries();
    }

    void rebuildindex() {
	Set ks=transitiontable.keySet();
	Iterator it=ks.iterator();
	try {
	    FileOutputStream fos=new FileOutputStream("index.html");
    	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (containers) {
		ps.println("Using Containers <a href=\"/rm-O0\">Turn off containers/firstpass</a><p>");
	    } else {
		ps.println("Not Using Containers <a href=\"/rm-O1\">Turn on containers</a><p>");
	    }
	    ps.println("default Container policy:"+policyname(defaultpolicy)+"<p>");
	    if (!containers) {
		if (defaultpolicy==P_ONCEEVER)
		    ps.println("<a href=\"/rm-Y2\">Change policy to oneatatime</a><p>");
		else
		    ps.println("<a href=\"/rm-Y1\">Change policy to onceever</a><p>");
	    }
	    ps.close();
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    private String getpolicy(String methodname) {
	if (policymap.containsKey(methodname)) {
	    Integer policy=(Integer) policymap.get(methodname);
	    return policyname(policy);
	} else return "default";
    }

    private String policyname(Integer policy) {
	if (policy==P_NEVER)
	    return "never";
	if (policy==P_ONCEEVER)
	    return "onceever";
	if (policy==P_ONEATATIME)
	    return "oneatatime";
	if (policy==P_ALWAYS)
	    return "always";
	System.out.println("Invalid policy in policyname!!!!!!:"+policy);
	System.exit(-1);
	return null; /*keep javac happy*/
    }

    static Integer parsepolicy(String policy) {
	if (policy.equals("never"))
	    return P_NEVER;
	if (policy.equals("onceever"))
	    return P_ONCEEVER;
	if (policy.equals("oneatatime"))
	    return P_ONEATATIME;
	if (policy.equals("always"))
	    return P_ALWAYS;
	return null;
    }

    synchronized void writepolicy() {
	try {
	    FileOutputStream fos=new FileOutputStream("policy");
	    PrintStream ps=new PrintStream(fos);		
	    ps.println("default: "+policyname(defaultpolicy));
	    Iterator iterator=policymap.keySet().iterator();
	    while (iterator.hasNext()) {
		String clss=(String) iterator.next();
		ps.println(clss+" "+policyname((Integer)policymap.get(clss)));
	    }
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    synchronized void readpolicy() {
	try {
	    policymap=new HashMap();
	    FileReader policyfile=new FileReader("policy");
	    String defaults=nexttoken(policyfile);
	    defaultpolicy=parsepolicy(nexttoken(policyfile));
	    if (!defaults.equals("default:")||defaultpolicy==null) {
		System.out.println("Bad formatted policy file");
		System.exit(-1);
	    }
    	    while(true) {
		String clss=nexttoken(policyfile);
		if (clss==null)
		    break;
		Integer policy=parsepolicy(nexttoken(policyfile));
		if (policy==null) {
		    System.out.println("Bad formatted policy file");
		    System.exit(-1);
		}
		policymap.put(clss, policy);
	    }
	    policyfile.close();
	} catch (FileNotFoundException e) {
	    System.out.println("Policy file not found");
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    synchronized void rerunanalysis() {
	runanalysis();
	readtransitions();
	readroles();
	rebuildgraphs(true);
	rebuildindex();
	gendiagram();
	genmergeddiagram();
	fields.readfieldfile();
	fields.buildfieldspage();
    }
    
    synchronized void writeatomic() {
	try {
	    FileOutputStream fos=new FileOutputStream("atomic");
	    PrintStream ps=new PrintStream(fos);		
	    Iterator iterator=atomic.iterator();
	    while (iterator.hasNext()) {
		ps.println(iterator.next());
	    }
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    void readatomic() {
	try {
	    atomic=new HashSet();
	    FileReader atomicfile=new FileReader("atomic");
	    while(true) {
		String method=nexttoken(atomicfile);
		if (method==null)
		    break;
		atomic.add(method);
	    }
	    atomicfile.close();
	} catch (FileNotFoundException e) {
	    System.out.println("Atomic file not found");
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }


    synchronized String handlepage(String filename, BufferedWriter out, HTTPResponse resp) {

	int firstslash=filename.indexOf('/');
	int dash=filename.indexOf('-',firstslash+1);
	String file=filename.substring(dash+2);
	char type=filename.charAt(dash+1);
	switch(type) {
	case 'M':
	    return buildmethodpage(file);
	case 'N':
	    {
		Integer methodnumber=Integer.valueOf(file);
		String methodname=(String) classinfo.revmethods.get(methodnumber);
		atomic.remove(methodname);
		return buildmethodpage(file);
	    }
	case 'S': {


	    int comma=file.indexOf(',');
	    int secondcomma=file.indexOf(',',comma+1);
	    Integer methodnumber=Integer.valueOf(file.substring(0,comma));
	    String methodname=(String)classinfo.revmethods.get(methodnumber);
	    String policy=file.substring(comma+1,secondcomma);
	    methods.changestate(methodname, Integer.valueOf(policy));
	    
	    buildallmethodpage();
	    return "/"+file.substring(secondcomma+1);
	    
	}

	case 'L': {
	    try {
		Integer rolenum=Integer.valueOf(file);
		Role role=(Role)roletable.get(rolenum);
		FileOutputStream fos=new FileOutputStream("R"+rolenum+".html");
		PrintStream ps=new PrintStream(fos);
		menuline(ps);
		ps.println(role);
		ps.close();
		return "/R"+rolenum+".html";
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	}
	case 'A':
	    {
		Integer methodnumber=Integer.valueOf(file);
		String methodname=(String) classinfo.revmethods.get(methodnumber);
		atomic.add(methodname);
		return buildmethodpage(file);
	    }
	case 'P':
	    {
		return buildallmethodpage();
	    }
	case 'R':
	    {
		writeatomic();
		writepolicy();
		fields.writefieldfile();
		methods.writeentries();
		rerunanalysis();
		return "/index.html";
	    }
	case 'O':
	    {
		/* toggle containers*/
		containers=file.equals("1");
		rebuildindex();
		rebuildgraphs(false);
		return "/index.html";
	    }
	case 'C':
	    {
		return classpage();
	    }
	case 'Y': {
	    if (file.equals("1"))
		defaultpolicy=P_ONCEEVER;
	    if (file.equals("2"))
		defaultpolicy=P_ONEATATIME;
	    rebuildindex();
	    return "/index.html";
	}
	case 'F': {
	    /* Change Field inclusion */
	    int comma=file.indexOf(',');
	    Integer fieldnumber=Integer.valueOf(file.substring(0,comma));
	    FastScan.FieldEntry field=(FastScan.FieldEntry)classinfo.revfields.get(fieldnumber);
	    String inclusion=file.substring(comma+1);
	    if (inclusion.equals("1")) {
		fields.includefields.add(field);
	    } else {
		fields.includefields.remove(field);
	    }
	    fields.buildfieldspage();
	    return "/fields.html";
	    
	}

	case 'Z': {
	    /* Change policy */
	    int comma=file.indexOf(',');
	    if (comma==-1) {
		Integer classnumber=Integer.valueOf(file);
		String classname=(String) classinfo.revclasses.get(classnumber);		
		policymap.remove(classname);
		genpictures(classname, classnumber.toString(),false);
		return "/"+classnumber+".html";
	    } else {
		Integer classnumber=Integer.valueOf(file.substring(0,comma));
		String classname=(String)classinfo.revclasses.get(classnumber);
		String policy=file.substring(comma+1);
		Integer p=null;
		if(policy.equals("0"))
		    p=P_NEVER;
		else if(policy.equals("1"))
		    p=P_ONCEEVER;
		else if(policy.equals("2"))
		    p=P_ONEATATIME;
		else if(policy.equals("3"))
		    p=P_ALWAYS;
		else {
		    System.out.println("Error in Z");
		    System.exit(-1);
		}
		policymap.put(classname, p);
		genpictures(classname, classnumber.toString(),false);
		return "/"+classnumber+".html";
	    }
	}
	}
	return null;
    }

    void menuline(PrintStream ps) {
	ps.println("<a href=\"rm-R\">Rerun Analysis</a>");
	ps.println("<a href=\"rm-P\">Method Page</a>");
	ps.println("<a href=\"rm-C\">Class Page</a>");
	ps.println("<a href=\"rolerelation.html\">Role Relation Diagram</a>");
	ps.println("<a href=\"rolerelationmerged.html\">Merged Role Relation Diagram</a>");
	ps.println("<a href=\"/fields.html\">Fields Page</a>");
	ps.println("<a href=\"/index.html\">Main Page</a><p>");
    }

    synchronized String buildallmethodpage() {
	Iterator methodi=classinfo.methods.keySet().iterator();
	try {
	    FileOutputStream fos=new FileOutputStream("allmethods.html");
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    while(methodi.hasNext()) {
		String methodname=(String) methodi.next();
		String info="";
		if (atomic.contains(methodname))
		    info="ATOMIC ";
		ps.println("<a href=\"rm-M"+
			   classinfo.methods.get(methodname)+
			   "\">"+Transition.htmlreadable(methodname)+"</a> "+info+"<p>");
		ps.println(methods.statestring(methodname, "allmethods.html"));
	    }
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	return "/allmethods.html";
    }
    
    synchronized String buildmethodpage(String file) {
	Integer methodnumber=Integer.valueOf(file);
	String filename="M"+file+".html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    String methodname=(String) classinfo.revmethods.get(methodnumber);
	    menuline(ps);
	    ps.println("Method: "+methodname+"<p>");
	    if (atomic.contains(methodname)) {
		ps.println("ATOMIC<p>");
		ps.println("<a href=\"rm-N"+file+"\">Make Non-atomic</a><p>");
	    }
	    else {
		ps.println("NOT ATOMIC<p>");
		ps.println("<a href=\"rm-A"+file+"\">Make Atomic</a><p>");
	    }
	    if (classinfo.staticmethods.contains(methodname)) {
		System.out.println("STATIC<p>");
	    }

	    if (classinfo.callgraph.containsKey(methodname)) {
		Iterator callers=((Set)classinfo.callgraph.get(methodname)).iterator();
		ps.println("Callers: <p>");
		while(callers.hasNext()) {
		    String caller=(String)callers.next();
		    ps.println("<a href=\"rm-M"+
			       classinfo.methods.get(caller)+
			       "\">"+Transition.htmlreadable(caller)+"</a><p>");
		}
	    } else ps.println("No Callers: <p>");
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	return "/"+filename;
    }
    

    synchronized String handleresponse(String filename, BufferedWriter out, HTTPResponse resp) {
	System.out.println(filename);
	int firstslash=filename.indexOf('/');
	int dash=filename.indexOf('-',firstslash+1);
	int question=filename.indexOf('?',dash+1);
	int comma=filename.indexOf(',',question+1);
	String fileprefix=filename.substring(dash+1, question);
	String file=fileprefix+".imap";
	int x=Integer.parseInt(filename.substring(question+1, comma));
	int y=Integer.parseInt(filename.substring(comma+1));
	Imap imap=new Imap(file);
	String parseclick=imap.parseclick(x,y);
	if (parseclick==null)
	    /* We have nothing */
	    return "/"+fileprefix+".html";
	else if (parseclick.indexOf('-')!=-1) {
	    /* We have an edge */
	    System.out.println(fileprefix);
	    String clss=(String) classinfo.revclasses.get(Integer.valueOf(fileprefix));
	    System.out.println(clss);
	    Set transitionset=(Set)transitiontable.get(clss);
	    Iterator it=transitionset.iterator();
   	    while(it.hasNext()) {
		Transition trans=(Transition) it.next();
		if (parseclick.equals(trans.rolefrom+"-"+trans.roleto)) {
		    try {
			FileOutputStream fos=new FileOutputStream(parseclick+".html");
			PrintStream ps=new PrintStream(fos);
			menuline(ps);
			for(int i=0;i<trans.names.length;i++) {
			    String methodname=Transition.classname(trans.names[i])+"."+Transition.methodname(trans.names[i])+Transition.signature(trans.names[i]);
			    System.out.println(methodname);
			    System.out.println(classinfo.methods.get(methodname));
			    String isatomic=atomic.contains(methodname)?"ATOMIC":"NOT ATOMIC";
			    ps.println("<a href=\"rm-M"+
				       classinfo.methods.get(methodname)+
				       "\">"+Transition.htmlreadable(Transition.filtername(trans.names[i]))+"</a>"+isatomic+"<p>\n");
			}
			ps.close();
		    } catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		    }
		    return "/"+parseclick+".html";
		}
	    }
	    System.out.println("Transition "+parseclick+" not found.");
	    System.exit(-1);
	} else {
	    /* We have a node */
	    Integer rolenum=Integer.valueOf(parseclick);
	    if (rolenum.intValue()==1) {
		/* Garbage Role */
		try {
		    FileOutputStream fos=new FileOutputStream("R"+rolenum+".html");
		    PrintStream ps=new PrintStream(fos);
		    menuline(ps);
		    ps.println("Garbage<p>");
		    ps.close();
		} catch (Exception e) {
		    e.printStackTrace();
		    System.exit(-1);
		}
		return "/R"+rolenum+".html";
	    }
	    Role role=(Role)roletable.get(rolenum);
	    try {
		FileOutputStream fos=new FileOutputStream("R"+rolenum+".html");
		PrintStream ps=new PrintStream(fos);
		menuline(ps);
		ps.println(role);
		ps.close();
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	    return "/R"+rolenum+".html";
	}
	return null; /* Make compiler happy*/
    }

    private void rebuildgraphs(boolean dot) {
	Set ks=transitiontable.keySet();
	Iterator it=ks.iterator();
	while(it.hasNext()) {
	    String clss=(String)it.next();
	    System.out.println(clss);
	    Integer i=(Integer) classinfo.classes.get(clss);
	    genpictures(clss, i.toString(),dot);
	}
    }

    synchronized String classpage() {
	Set ks=transitiontable.keySet();
	Iterator it=ks.iterator();
	try {
	    FileOutputStream fos=new FileOutputStream("allclasses.html");
    	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    while(it.hasNext()) {
		String clss=(String)it.next();
		Integer i=(Integer) classinfo.classes.get(clss);
		String info="default";
		if (policymap.containsKey(clss)) {
		    info=policyname((Integer)policymap.get(clss));
		}
		ps.println("<a href=\""+i+".html\">"+clss+"</a>"+info+"<p>");
	    }
	    ps.close();
	} catch (IOException e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	return "/allclasses.html";
    }

    synchronized void runanalysis() {
	try {
	    Runtime runtime=Runtime.getRuntime();
	    Process p1=containers?runtime.exec("./RoleInference -u -f -w -n -r -ptemp"):runtime.exec("./RoleInference -cpolicy -f -w -n -r -ptemp");
	    p1.waitFor();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    void genpictures(String classname, String filename) {
	genpictures(classname,filename,true);
    }

    synchronized void genpictures(String classname, String filename,boolean dot) {
	if (dot)
	    writedotfile(classname, filename);
	try {
	    if (dot) {
		Runtime runtime=Runtime.getRuntime();
		Process p1=runtime.exec("dot -Tgif "+filename+" -o"+filename+".gif");	
		p1.waitFor();
		Process p2=runtime.exec("dot -Timap "+filename+" -o"+filename+".imap");	
		p2.waitFor();
	    }
	    FileOutputStream fos=new FileOutputStream(filename+".html");
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    ps.println("Role Transition diagram for "+classname+"<p>");
	    String info="default";
	    if (policymap.containsKey(classname)) {
		info=policyname((Integer)policymap.get(classname));
	    }
	    ps.println("Container policy: "+info+"<p>");
	    if (!containers) {
		Integer classnum=(Integer)classinfo.classes.get(classname);
		ps.println("<a href=\"rm-Z"+classnum+"\">default</a>");
		ps.println("<a href=\"rm-Z"+classnum+",0\">never</a>");
		ps.println("<a href=\"rm-Z"+classnum+",1\">onceever</a>");
		ps.println("<a href=\"rm-Z"+classnum+",2\">oneatatime</a>");
		ps.println("<a href=\"rm-Z"+classnum+",3\">always</a><p>");
	    }
	    ps.println("<a href=\"ri-"+filename+"\"><img src=\"/"+filename+".gif\" ismap> </a>");
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    synchronized void writedotfile(String classname,String filename) {
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
	ps.println("size=\"40,40\"");
	Set observedroles=new HashSet();

	while(it.hasNext()) {
	    Transition tr=(Transition) it.next();
	    String color="black";
	    for(int i=0;i<tr.names.length;i++) {
		String methodname=Transition.classname(tr.names[i])+"."+Transition.methodname(tr.names[i])+Transition.signature(tr.names[i]);
		if (atomic.contains(methodname)) {
		    color="red";
		    break;
		}
	    }

	    if (tr.roleto!=1)
		ps.print("R"+tr.rolefrom+" -> R"+tr.roleto+" [URL=\""+tr.rolefrom+"-"+tr.roleto+"\",fontsize=10,fontcolor="+color+",label=\"");
	    else
		ps.print("R"+tr.rolefrom+" -> GARB [URL=\""+tr.rolefrom+"-"+tr.roleto+"\",fontsize=10,fontcolor="+color+",label=\"");
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
	    if (role.intValue()==1)
		ps.println("GARB [URL=\""+role+"\"];");
	    else
		ps.println("R"+role+" [URL=\""+role+"\"];");
	}

	ps.println("}");
	ps.close();
    }

    synchronized void readroles() {
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
		Reference ref=new Reference(this,fclassname, ffieldname, Integer.parseInt(duplicates));
		al.add(ref);
	    }
	    Reference[] rolefieldlist=(Reference[]) al.toArray(new Reference[al.size()]);
	    al=new ArrayList();
	    while(true) {
		String fclassname=nexttoken(fr);
		if (fclassname.equals("~~"))
		    break;
		String duplicates=nexttoken(fr);
		Reference ref=new Reference(this,fclassname, Integer.parseInt(duplicates));
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
		Reference ref=new Reference(this,Integer.parseInt(role),ffieldname);
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
		Reference ref=new Reference(this,fclassname,Integer.parseInt(role), Integer.parseInt(duplicates));
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
			  new Role(this, Integer.parseInt(rolenumber), classname,contained.equals("1"), dominators,
				   rolefieldlist, rolearraylist, identityrelations, nonnullfields, nonnullarrays, invokedmethods));
	}
    }

    static class Roleedge {
	int srcrole;
	int dstrole;
	boolean contained;
	String fieldname;
	public Roleedge(int srcrole, int dstrole, boolean contained, String fieldname) {
	    this.srcrole=srcrole;
	    this.dstrole=dstrole;
	    this.contained=contained;
	    this.fieldname=fieldname;
	}
	public boolean equals(java.lang.Object o) {
	    try {
		Roleedge re2=(Roleedge)o;
		if ((srcrole==re2.srcrole)&&(dstrole==re2.dstrole)&&
		    (contained==re2.contained)&&fieldname.equals(re2.fieldname))
		    return true;
		else
		    return false;
	    } catch (Exception e) {
		return false;
	    }
	}
	public int hashCode() {
	    return srcrole^dstrole^fieldname.hashCode();
	}
    }

    private void genmergeddiagram() {
	writemergeddiagram();
	try {
	    Runtime runtime=Runtime.getRuntime();
	    Process p1=runtime.exec("dot -Tgif rolerelationmerged -o rolerelationmerged.gif");	
	    p1.waitFor();
	    Process p2=runtime.exec("dot -Timap rolerelationmerged -o rolerelationmerged.imap");	
	    p2.waitFor();
	    Process p3=runtime.exec("dot -Tps rolerelationmerged -o rolerelationmerged.ps");	
	    p3.waitFor();
	    FileOutputStream fos=new FileOutputStream("rolerelationmerged.html");
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    ps.println("<a href=\"ri-rolerelationmerged\"><img src=\"/rolerelationmerged.gif\" ismap> </a><p>");
	    ps.println("<a href=\"/rolerelationmerged.ps\">PostScript Version</a>");
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}

    }

    private void gendiagram() {
	readdiagram();
	writediagram();
	try {
	    Runtime runtime=Runtime.getRuntime();
	    Process p1=runtime.exec("dot -Tgif rolerelation -o rolerelation.gif");	
	    p1.waitFor();
	    Process p2=runtime.exec("dot -Timap rolerelation -o rolerelation.imap");	
	    p2.waitFor();
	    Process p3=runtime.exec("dot -Tps rolerelation -o rolerelation.ps");	
	    p3.waitFor();
	    FileOutputStream fos=new FileOutputStream("rolerelation.html");
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    ps.println("<a href=\"ri-rolerelation\"><img src=\"/rolerelation.gif\" ismap> </a><p>");
	    ps.println("<a href=\"/rolerelation.ps\">PostScript Version</a>");
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    private void writediagram() {
	FileOutputStream fos=null;
	try {
	    fos=new FileOutputStream("rolerelation");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	PrintStream ps=new PrintStream(fos);
	Iterator it=diagram.iterator();
	ps.println("digraph \"Role Relationship Diagram\" {");
	ps.println("ratio=auto");
	ps.println("size=\"40,40\"");
	Set observedroles=new HashSet();

	while(it.hasNext()) {
	    Roleedge re=(Roleedge) it.next();
	    String color="black";
	    if (re.contained)
		color="blue";
	    ps.println("R"+re.srcrole+" -> R"+re.dstrole+" [fontsize=10,color="+color+",label=\""+re.fieldname+"\"];");
	    observedroles.add(new Integer(re.srcrole));
	    observedroles.add(new Integer(re.dstrole));
	}

	Iterator iterator=observedroles.iterator();
	while(iterator.hasNext()) {
	    Integer role=(Integer) iterator.next();
	    String color="black";
	    Role rol=(Role)roletable.get(role);
	    if (rol.contained)
		color="blue";
	    ps.println("R"+role+" [URL=\""+role+"\",color="+color+"];");
	}
	ps.println("}");
	ps.close();
    }

    Set remap(int role) {
	HashSet set=new HashSet();
	Stack todo=new Stack();
	HashSet done=new HashSet();
	Integer rolei=new Integer(role);
	todo.push(rolei);
	done.add(rolei);
	while(!todo.empty()) {
	    Integer obj=(Integer)todo.pop();
	    if (!mergeable(obj)||!containedmapping.containsKey(obj)) {
		set.add(obj);
	    } else {
		Set toadd=(Set)containedmapping.get(obj);
		Iterator it=toadd.iterator();
		while(it.hasNext()) {
		    Integer roledst=(Integer)it.next();
		    if (!done.contains(roledst)) {
			todo.push(roledst);
			done.add(roledst);
		    }
		}
	    }
	}
	return set;
    }

    private boolean mergeable(Integer rolenumber) {
	return ((Role)roletable.get(rolenumber)).contained;
    }

    private void writemergeddiagram() {
	FileOutputStream fos=null;
	try {
	    fos=new FileOutputStream("rolerelationmerged");
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	PrintStream ps=new PrintStream(fos);
	Iterator it=diagram.iterator();
	ps.println("digraph \"Merged Role Relationship Diagram\" {");
	ps.println("ratio=auto");
	ps.println("size=\"40,40\"");
	Set observedroles=new HashSet();
	Set edges=new HashSet();
	while(it.hasNext()) {
	    Roleedge re=(Roleedge) it.next();
	    String color="black";
	    if (re.contained)
		color="blue";
	    Set srcroles=remap(re.srcrole);
	    Set dstroles=remap(re.dstrole);
	    Iterator srcit=srcroles.iterator();
	    while (srcit.hasNext()) {
		Integer srcrole=(Integer)srcit.next();
		Iterator dstit=dstroles.iterator();
		while (dstit.hasNext()) {
		    Integer dstrole=(Integer)dstit.next();
		    Roleedge renew=new Roleedge(srcrole.intValue(),dstrole.intValue(), re.contained, re.fieldname);
		    if (!edges.contains(renew)) {
			edges.add(renew);
			if (re.dstrole==dstrole.intValue()) {
			    ps.println("R"+srcrole+" -> R"+dstrole+" [fontsize=10,color="+color+",label=\""+re.fieldname+"\"];");
			    observedroles.add(dstrole);
			}
			observedroles.add(srcrole);
		    }
		}
	    }
	}

	Iterator iterator=observedroles.iterator();
	while(iterator.hasNext()) {
	    Integer role=(Integer) iterator.next();
	    String color="black";
	    Role rol=(Role)roletable.get(role);
	    if (rol.contained)
		color="blue";
	    ps.println("R"+role+" [URL=\""+role+"\",color="+color+"];");
	}
	ps.println("}");
	ps.close();
    }

    private void readdiagram() {
	diagram=new HashSet();
	containedmapping=new HashMap();

	try {
	    FileReader fr=new FileReader("webdiagram");
	    while(true) {
		String srcrole=nexttoken(fr);
		if (srcrole==null) {
		    fr.close();
		    return;
		}
		String dstrole=nexttoken(fr);
		String contained=nexttoken(fr);
		String fieldname=nexttoken(fr);
		diagram.add(new Roleedge(Integer.parseInt(srcrole),
					 Integer.parseInt(dstrole), contained.equals("1"), fieldname));
		if (contained.equals("1")) {
		    if (containedmapping.containsKey(Integer.valueOf(dstrole))) {
			((Set)containedmapping.get(Integer.valueOf(dstrole))).add(Integer.valueOf(srcrole));
		    } else {
			Set set=new HashSet();
			set.add(Integer.valueOf(srcrole));
			containedmapping.put(Integer.valueOf(dstrole), set);
		    }
		}
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
    }

    synchronized void readtransitions() {
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
