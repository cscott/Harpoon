import java.net.*;
import java.io.*;
import java.util.*;

class RoleI {
    HashMap transitiontable;
    HashMap viewtransitiontable;
    HashMap roletable;
    HashMap reverseroletable;
    int maxrole;
    FastScan classinfo;
    Set atomic;
    HashMap policymap;
    boolean containers;
    Set diagram;
    HashMap containedmapping;
    Fields fields;
    Methods methods;
    Set excludedclasses;
    View view;
    HashMap rolecombmap;
    HashMap revrolecombmap;
    int maxrolecomb;
    RoleUniverse constructuniverse;
    ArrayList universes;
    HashMap rolenames;

    HashMap dommap;
    HashMap revdommap;
    int domcount;

    HashMap fldreffrommap;
    HashMap revfldreffrommap;
    int fldreffromcount;

    HashMap arrreffrommap;
    HashMap revarrreffrommap;
    int arrreffromcount;

    HashMap identmap;
    HashMap revidentmap;
    int identcount;

    HashMap fldreftomap;
    HashMap revfldreftomap;
    int fldreftocount;

    HashMap arrreftomap;
    HashMap revarrreftomap;
    int arrreftocount;

    HashMap methodsmap;
    HashMap revmethodsmap;
    int methodscount;
	
    RoleDisplay roledisp;

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
	rolenames=new HashMap();
	universes=new ArrayList();
	RoleUniverse completeuniverse=new RoleUniverse();
	universes.add(completeuniverse);
	view=new View(this,new RoleUniverse[] {completeuniverse}, new RoleUniverse[] {});
	roledisp=new RoleDisplay();
	view.attach(roledisp);

	maxrolecomb=0;
	rolecombmap=new HashMap();
	revrolecombmap=new HashMap();
	constructuniverse=new RoleUniverse();

	maxrole=0;
	containers=false;
	initializefastscan();
	readtransitions();
	readroles();
	readatomic();
	readclasses();
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

    void readclasses() {
	excludedclasses=new HashSet();
	FileReader fr=null;
	try {
	    fr=new FileReader("classes");
	    while(true) {
		String exclass=nexttoken(fr);
		if (exclass==null)
		    break;
		excludedclasses.add(exclass);
	    }
	    fr.close();
	} catch (FileNotFoundException e) {
	    System.out.println("Classes file not found");
	} catch (Exception e) {
	    System.out.println(e);
	    System.exit(-1);
	}
    }

    void writeclasses() {
	try {
	    FileOutputStream fos=new FileOutputStream("classes");
	    PrintStream ps=new PrintStream(fos);
	    Iterator it=excludedclasses.iterator();
	    while (it.hasNext())
		ps.println(it.next());
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
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
	case '=':
	    int dash2=filename.indexOf('-');
	    rolenames.put(Integer.valueOf(file.substring(0,dash2)),file.substring(dash2+1));
	    rebuildgraphs(true);
	    gendiagram();
	    genmergeddiagram();
	    return "/index.html";
	case 'B':
	    char type2=filename.charAt(dash+2);
	    int include=0;
	    switch(type2) {
	    case 'A':
		return buildviewpage();
	    case 'B':
		include++; /*use case C code now*/
	    case 'C':
		include++;
	    case 'L':
		Integer universe=Integer.valueOf(filename.substring(dash+3));
		ArrayList auniv=new ArrayList(java.util.Arrays.asList(view.universes));
		ArrayList aunivd=new ArrayList(java.util.Arrays.asList(view.display));
		if (include==1) {
		    auniv.add(universes.get(universe.intValue()));
		} else if (include==2) {
		    int iremove=auniv.indexOf(universes.get(universe.intValue()));
		    if (iremove>=0)
			auniv.remove(iremove);
		    int iremoved=aunivd.indexOf(universes.get(universe.intValue()));
		    if (iremoved>=0)
			aunivd.remove(iremoved);

		} else if (include==0) {
		    aunivd.add(universes.get(universe.intValue()));
		}
		view=new View(this, (RoleUniverse[]) auniv.toArray(new RoleUniverse[auniv.size()]), (RoleUniverse[]) aunivd.toArray(new RoleUniverse[aunivd.size()]));
		return buildviewpage();
	    case 'D':
		roledisp=new RoleDisplay();
		view.attach(roledisp);
		rebuildgraphs(true);
		gendiagram();
		genmergeddiagram();
		return buildviewpage();
	    }
	case 'Q':
	    type2=filename.charAt(dash+2);
	    switch(type2) {
	    case 'A':
		return builduniversepage();
	    case 'C':
		if (filename.charAt(dash+3)=='1')
		    constructuniverse.setclass(true);
		else
		    constructuniverse.setclass(false);
		return builduniversepage();
	    case 'D':
		char type3=filename.charAt(dash+3);
		switch(type3) {
		case '0':
		    /* Reference to Arrays*/
		    return referencetoarray();
		case '1':
		    return referencetofield();
		case '2':
		    return referencefromarray();
		case '3':
		    return referencefromfield();
		case '4':
		    return identity();
		case '5':
		    return invokedmethods();
		case '6':
		    return dominators();
		case '7':
		    return univclasspage();
		}
	    case 'E':
		type3=filename.charAt(dash+3);
		switch(type3) {
		case '0':
		    constructuniverse.restrictarrayreferenceto=true;
		    if (constructuniverse.allowedarrayto==null)
			constructuniverse.allowedarrayto=new HashSet();
		    return referencetoarray();
		case '1':
		    constructuniverse.restrictfieldreferenceto=true;
		    if (constructuniverse.allowedfieldto==null)
			constructuniverse.allowedfieldto=new HashSet();
		    return referencetofield();
		case '2':
		    constructuniverse.restrictarrayreferencefrom=true;
		    if (constructuniverse.allowedarrayfrom==null)
			constructuniverse.allowedarrayfrom=new HashSet();
		    return referencefromarray();
		case '3':
		    constructuniverse.restrictfieldreferencefrom=true;
		    if (constructuniverse.allowedfieldfrom==null)
			constructuniverse.allowedfieldfrom=new HashSet();
		    return referencefromfield();
		case '4':
		    constructuniverse.restrictidentity=true;
		    if (constructuniverse.allowedidentityfields==null)
			constructuniverse.allowedidentityfields=new HashSet();
		    return identity();
		case '5':
		    constructuniverse.restrictinvokedmethods=true;
		    if (constructuniverse.allowedinvokedmethods==null)
			constructuniverse.allowedinvokedmethods=new HashSet();
		    return invokedmethods();
		case '6':
		    constructuniverse.restrictdominators=true;
		    if (constructuniverse.alloweddominators==null)
			constructuniverse.alloweddominators=new HashSet();
		    return dominators();
		    
		}
	    case 'F':
		type3=filename.charAt(dash+3);
		switch(type3) {
		case '0':
		    constructuniverse.restrictarrayreferenceto=false;
		    return referencetoarray();
		case '1':
		    constructuniverse.restrictfieldreferenceto=false;
		    return referencetofield();
		case '2':
		    constructuniverse.restrictarrayreferencefrom=false;
		    return referencefromarray();
		case '3':
		    constructuniverse.restrictfieldreferencefrom=false;
		    return referencefromfield();
		case '4':
		    constructuniverse.restrictidentity=false;
		    return identity();
		case '5':
		    constructuniverse.restrictinvokedmethods=false;
		    return invokedmethods();
		case '6':
		    constructuniverse.restrictdominators=false;
		    return dominators();
		}
	    case 'R':
		type3=filename.charAt(dash+3);
		switch(type3) {
		case '0':
		    constructuniverse.allowedarrayto.remove(((Reference)revarrreftomap.get(Integer.valueOf(filename.substring(dash+4)))).desc());
		    return referencetoarray();
		case '1':
		    constructuniverse.allowedfieldto.remove(((Reference)revfldreftomap.get(Integer.valueOf(filename.substring(dash+4)))).desc());
		    return referencetofield();
		case '2':
		    constructuniverse.allowedarrayfrom.remove(((Reference)revarrreffrommap.get(Integer.valueOf(filename.substring(dash+4)))).desc());
		    return referencefromarray();
		case '3':
		    constructuniverse.allowedfieldfrom.remove(((Reference)revfldreffrommap.get(Integer.valueOf(filename.substring(dash+4)))).desc());
		    return referencefromfield();
		case '4':
		    constructuniverse.allowedidentityfields.remove(revidentmap.get(Integer.valueOf(filename.substring(dash+4))));
		    return identity();
		case '5':
		    constructuniverse.allowedinvokedmethods.remove(revmethodsmap.get(Integer.valueOf(filename.substring(dash+4))));
		    return invokedmethods();
		case '6':
		    constructuniverse.alloweddominators.remove(revdommap.get(Integer.valueOf(filename.substring(dash+4))));
		    return dominators();
		case '7':
		    constructuniverse.singleroleclass.remove(classinfo.revclasses.get(Integer.valueOf(filename.substring(dash+4))));
		    return univclasspage();
		}
	    case 'I':
		type3=filename.charAt(dash+3);
		switch(type3) {
		case '0':
		    constructuniverse.allowedarrayto.add(((Reference)revarrreftomap.get(Integer.valueOf(filename.substring(dash+4)))).desc());
		    return referencetoarray();
		case '1':
		    constructuniverse.allowedfieldto.add(((Reference)revfldreftomap.get(Integer.valueOf(filename.substring(dash+4)))).desc());
		    return referencetofield();
		case '2':
		    constructuniverse.allowedarrayfrom.add(((Reference)revarrreffrommap.get(Integer.valueOf(filename.substring(dash+4)))).desc());
		    return referencefromarray();
		case '3':
		    constructuniverse.allowedfieldfrom.add(((Reference)revfldreffrommap.get(Integer.valueOf(filename.substring(dash+4)))).desc());
		    return referencefromfield();
		case '4':
		    constructuniverse.allowedidentityfields.add(revidentmap.get(Integer.valueOf(filename.substring(dash+4))));
		    return identity();
		case '5':
		    constructuniverse.allowedinvokedmethods.add(revmethodsmap.get(Integer.valueOf(filename.substring(dash+4))));
		    return invokedmethods();
		case '6':
		    constructuniverse.alloweddominators.add(revdommap.get(Integer.valueOf(filename.substring(dash+4))));
		    return dominators();
		case '7':
		    constructuniverse.singleroleclass.add(classinfo.revclasses.get(Integer.valueOf(filename.substring(dash+4))));
		    return univclasspage();
		}
	    case 'S':
		universes.add(constructuniverse);
		constructuniverse=new RoleUniverse();
		return builduniversepage();
	    }
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
		writeclasses();
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
	case 'X': {
	    /* Change policy */
	    int comma=file.indexOf(',');
	    Integer classnumber=Integer.valueOf(file.substring(0,comma));
	    String classname=(String)classinfo.revclasses.get(classnumber);
	    String policy=file.substring(comma+1);
	    if(policy.equals("0"))
		excludedclasses.remove(classname);
	    else if(policy.equals("1"))
		excludedclasses.add(classname);
	    else {
		System.out.println("Error in X");
		System.exit(-1);
	    }
	    genpictures(classname, classnumber.toString(),false);
	    return "/"+classnumber+".html";
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
	ps.println("<a href=\"rm-BA\">Manage View</a>");
	ps.println("<a href=\"rm-QA\">Edit Role Universe</a>");
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

    synchronized String buildviewpage() {
	String filename="view.html"; 
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    for(int i=0;i<universes.size();i++) {
		int included=0;
		for (int j=0;j<view.universes.length;j++) {
		    if (view.universes[j]==universes.get(i)) {
			included=1;
			break;
		    }
		}
		for (int j=0;j<view.display.length;j++) {
		    if (view.display[j]==universes.get(i)) {
			included=2;
			break;
		    }
		}
		if (included!=0) {
		    if (included==1)
			ps.println("Universe "+i+" Included in Cross Product<p>");
		    else
			ps.println("Universe "+i+" Included in List<p>");
		    ps.println("<a href=\"rm-BB"+i+"\">Exclude</a><p>");
		} else {
		    ps.println("Universe "+i+" Excluded from View<p>");
		    ps.println("<a href=\"rm-BC"+i+"\">Include in Cross Product</a><p>");
		    ps.println("<a href=\"rm-BL"+i+"\">Include in List</a><p>");
		}
	    }
	    ps.println("<a href=\"rm-BD\">Rebuild Graphs with new View</a><p>");
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return "/"+filename;
    }

    synchronized String univclasspage() {
	String filename="classpage.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    HashMap classmap=classinfo.classes;
	    Iterator classit=classmap.keySet().iterator();
	    while(classit.hasNext()) {
		String classstr=(String)classit.next();
		if (constructuniverse.singleroleclass.contains(classstr)) {
		    ps.println("Single Role Class: "+classstr+" <a href=\"rm-QR7"+
			       classmap.get(classstr)+"\">Allow multiple roles</a><p>");
		} else {
		    ps.println("Multiple Role Class: "+classstr+" <a href=\"rm-QI7"+
			       classmap.get(classstr)+"\">Force single role</a><p>");
		}
	    }
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	return "/"+filename;
    }
    
    synchronized String builduniversepage() {
	String filename="universe.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (constructuniverse.classmatters) {
		ps.println("Class Matters<p>");
		ps.println("<a href=\"rm-QC0\">Ignore Class</a><p>");
	    } else {
		ps.println("Class Ignored<p>");
		ps.println("<a href=\"rm-QC1\">Class Matters</a><p>");
	    }

	    ps.println("<a href=\"rm-QD0\">Reference to Arrays</a><p>");
	    ps.println("<a href=\"rm-QD1\">Reference to Fields</a><p>");	    
	    ps.println("<a href=\"rm-QD2\">Reference from Arrays</a><p>");
	    ps.println("<a href=\"rm-QD3\">Reference from Fields</a><p>");

	    ps.println("<a href=\"rm-QD4\">Identity Relations</a><p>");
	    ps.println("<a href=\"rm-QD5\">Invoked Methods</a><p>");

	    ps.println("<a href=\"rm-QD6\">Dominators</a><p>");
	    ps.println("<a href=\"rm-QD7\">Classes</a><p>");

	    ps.println("<a href=\"rm-QS\">Save Universe as "+universes.size()+"</a><p>");
 
	    ps.println("");
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return "/"+filename;
    }

    synchronized String referencetofield() {
	String filename="reftofield.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (constructuniverse.restrictfieldreferenceto) {
		ps.println("<a href=\"rm-QF1\">Unrestrict Field References</a><p>");
		Iterator remiter=constructuniverse.allowedfieldto.iterator();
		while(remiter.hasNext()) {
		    Reference ref=(Reference)remiter.next();
		    ps.println("<a href=\"rm-QR1"+fldreftomap.get(ref)+"\">Remove "+ref.classname+"."+ref.fieldname+"</a><p>");
		}
		Iterator additer=fldreftomap.keySet().iterator();
		while(additer.hasNext()) {
		    Reference ref=(Reference)additer.next();
		    if (!constructuniverse.allowedfieldto.contains(ref))
			ps.println("<a href=\"rm-QI1"+fldreftomap.get(ref)+"\">Add "+ref.classname+"."+ref.fieldname+"</a><p>");
		}
	    } else {
		ps.println("<a href=\"rm-QE1\">Restrict Field References to list</a><p>");
	    }
	    
	    
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return "/"+filename;
    }

    synchronized String referencetoarray() {
	String filename="reftoarray.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (constructuniverse.restrictarrayreferenceto) {
		ps.println("<a href=\"rm-QF0\">Unrestrict Array References</a><p>");
		Iterator remiter=constructuniverse.allowedarrayto.iterator();
		while(remiter.hasNext()) {
		    Reference ref=(Reference)remiter.next();
		    ps.println("<a href=\"rm-QR0"+arrreftomap.get(ref)+"\">Remove "+ref.classname+"</a><p>");
		}
		Iterator additer=arrreftomap.keySet().iterator();
		while(additer.hasNext()) {
		    Reference ref=(Reference)additer.next();
		    if (!constructuniverse.allowedarrayto.contains(ref))
			ps.println("<a href=\"rm-QI0"+arrreftomap.get(ref)+"\">Add "+ref.classname+"</a><p>");
		}
	    } else {
		ps.println("<a href=\"rm-QE0\">Restrict Array References to list</a><p>");
	    }
	    
	    
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return "/"+filename;
    }

    synchronized String referencefromarray() {
	String filename="reffromarray.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (constructuniverse.restrictarrayreferencefrom) {
		ps.println("<a href=\"rm-QF2\">Unrestrict Array References</a><p>");
		Iterator remiter=constructuniverse.allowedarrayfrom.iterator();
		while(remiter.hasNext()) {
		    Reference ref=(Reference)remiter.next();
		    ps.println("<a href=\"rm-QR2"+arrreffrommap.get(ref)+"\">Remove "+ref.classname+"</a><p>");
		}
		Iterator additer=arrreffrommap.keySet().iterator();
		while(additer.hasNext()) {
		    Reference ref=(Reference)additer.next();
		    if (!constructuniverse.allowedarrayfrom.contains(ref))
			ps.println("<a href=\"rm-QI2"+arrreffrommap.get(ref)+"\">Add "+ref.classname+"</a><p>");
		}
	    } else {
		ps.println("<a href=\"rm-QE2\">Restrict Array References from list</a><p>");
	    }
	    
	    
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return "/"+filename;
    }

    synchronized String referencefromfield() {
	String filename="reffromfield.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (constructuniverse.restrictfieldreferencefrom) {
		ps.println("<a href=\"rm-QF3\">Unrestrict Field References</a><p>");
		Iterator remiter=constructuniverse.allowedfieldfrom.iterator();
		while(remiter.hasNext()) {
		    Reference ref=(Reference)remiter.next();
		    ps.println("<a href=\"rm-QR3"+fldreffrommap.get(ref)+"\">Remove "+ref.classname+"."+ref.fieldname+"</a><p>");
		}
		Iterator additer=fldreffrommap.keySet().iterator();
		while(additer.hasNext()) {
		    Reference ref=(Reference)additer.next();
		    if (!constructuniverse.allowedfieldfrom.contains(ref))
			ps.println("<a href=\"rm-QI3"+fldreffrommap.get(ref)+"\">Add "+ref.classname+"."+ref.fieldname+"</a><p>");
		}
	    } else {
		ps.println("<a href=\"rm-QE3\">Restrict Field References from list</a><p>");
	    }
	    
	    
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return "/"+filename;
    }

    synchronized String identity() {
	String filename="identity.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (constructuniverse.restrictidentity) {
		ps.println("<a href=\"rm-QF4\">Unrestrict Identity</a><p>");
		Iterator remiter=constructuniverse.allowedidentityfields.iterator();
		while(remiter.hasNext()) {
		    String ref=(String)remiter.next();
		    ps.println("<a href=\"rm-QR4"+identmap.get(ref)+"\">Remove "+ref+"</a><p>");
		}
		Iterator additer=identmap.keySet().iterator();
		while(additer.hasNext()) {
		    String ref=(String)additer.next();
		    if (!constructuniverse.allowedidentityfields.contains(ref))
			ps.println("<a href=\"rm-QI4"+identmap.get(ref)+"\">Add "+ref+"</a><p>");
		}
	    } else {
		ps.println("<a href=\"rm-QE4\">Restrict Identity from list</a><p>");
	    }
	    
	    
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return "/"+filename;
    }

    synchronized String invokedmethods() {
	String filename="invokedmethods.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (constructuniverse.restrictinvokedmethods) {
		ps.println("<a href=\"rm-QF5\">Unrestrict Invoked Methods</a><p>");
		Iterator remiter=constructuniverse.allowedinvokedmethods.iterator();
		while(remiter.hasNext()) {
		    String ref=(String)remiter.next();
		    ps.println("<a href=\"rm-QR5"+methodsmap.get(ref)+"\">Remove "+ref+"</a><p>");
		}
		Iterator additer=methodsmap.keySet().iterator();
		while(additer.hasNext()) {
		    String ref=(String)additer.next();
		    if (!constructuniverse.allowedinvokedmethods.contains(ref))
			ps.println("<a href=\"rm-QI5"+methodsmap.get(ref)+"\">Add "+ref+"</a><p>");
		}
	    } else {
		ps.println("<a href=\"rm-QE5\">Restrict Methods from list</a><p>");
	    }
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}

	return "/"+filename;
    }

    synchronized String dominators() {
	String filename="dominators.html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    menuline(ps);
	    if (constructuniverse.restrictdominators) {
		ps.println("<a href=\"rm-QF6\">Unrestrict Invoked Dominators</a><p>");
		Iterator remiter=constructuniverse.alloweddominators.iterator();
		while(remiter.hasNext()) {
		    Dominator ref=(Dominator)remiter.next();
		    ps.println("<a href=\"rm-QR6"+dommap.get(ref)+"\">Remove "+ref+"</a><p>");
		}
		Iterator additer=dommap.keySet().iterator();
		while(additer.hasNext()) {
		    Dominator ref=(Dominator)additer.next();
		    if (!constructuniverse.alloweddominators.contains(ref))
			ps.println("<a href=\"rm-QI6"+dommap.get(ref)+"\">Add "+ref+"</a><p>");
		}
	    } else {
		ps.println("<a href=\"rm-QE6\">Restrict Dominators from list</a><p>");
	    }
	    ps.close();
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return "/"+filename;
    }

    synchronized String buildmethodpage(String file) {
	Integer methodnumber=Integer.valueOf(file);
	String filename="M"+file+".html";
	try {
	    FileOutputStream fos=new FileOutputStream(filename);
	    PrintStream ps=new PrintStream(fos);
	    String methodname=(String) classinfo.revmethods.get(methodnumber);
	    menuline(ps);
	    ps.println("Method: "+Transition.htmlreadable(methodname)+"<p>");
	    if (atomic.contains(methodname)) {
		ps.println("ATOMIC<p>");
		ps.println("<a href=\"rm-N"+file+"\">Make Non-atomic</a><p>");
	    }
	    else {
		ps.println("NOT ATOMIC<p>");
		ps.println("<a href=\"rm-A"+file+"\">Make Atomic</a><p>");
	    }
	    if (classinfo.staticmethods.contains(methodname)) {
		ps.println("STATIC<p>");
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
	    Set transitionset=(Set)viewtransitiontable.get(clss);
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
	    Integer rolenumc=Integer.valueOf(parseclick);
	    RoleCombination rolec=(RoleCombination) revrolecombmap.get(rolenumc);
	    try {
		FileOutputStream fos1=new FileOutputStream("RC"+rolenumc+".html");
		
		PrintStream ps1=new PrintStream(fos1);
		menuline(ps1);
		for(int i=0;i<rolec.roleidentifiers.length;i++) {
		    int rolenum=rolec.roleidentifiers[i];
		    if (rolenum==1) {
			/* Garbage Role */
			FileOutputStream fos=new FileOutputStream("R"+rolenum+".html");
			PrintStream ps=new PrintStream(fos);
			menuline(ps);
			ps.println("Garbage<p>");
			ps.close();
			ps1.println("Garbage Role<p>");
		    } else {
			Role role=(Role)roletable.get(new Integer(rolenum));
			FileOutputStream fos=new FileOutputStream("R"+rolenum+".html");
			PrintStream ps=new PrintStream(fos);
			menuline(ps);
			ps.println(role);
			ps.close();
			ps1.println("<a href=\"R"+rolenum+".html\">"+role.shortname()+"</a><p>");
		    }
		}
		ps1.println("Also has following roles:<p>");
		if (roledisp!=null) {
		    RoleDisplay.RoleDisplayEntry rde=roledisp.get(rolec);
		    for(int i=0;i<rde.sets.length;i++) {
			Iterator it=rde.sets[i].iterator();
			while(it.hasNext()) {
			    Integer rolenumber=(Integer)it.next();
			    Role role=(Role)roletable.get(rolenumber);		
			    FileOutputStream fos=new FileOutputStream("R"+rolenumber+".html");
			    PrintStream ps=new PrintStream(fos);
			    menuline(ps);
			    ps.println(role);
			    ps.close();
			    ps1.println("<a href=\"R"+rolenumber+".html\">"+role.shortname()+"</a><p>");
			    
			}
			ps1.println("---------------------------------------<p>");
		    }
		}
		ps1.close();
	    } catch (Exception e) {
		e.printStackTrace();
		System.exit(-1);
	    }
	    return "/RC"+rolenumc+".html";
	}
	return null; /* Make compiler happy*/
    }

    private void rebuildgraphs(boolean dot) {
	Set ks=transitiontable.keySet();
	Iterator it=ks.iterator();
	while(it.hasNext()) {
	    String clss=(String)it.next();
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
	    Process p1=containers?runtime.exec("./RoleInference -x -u -f -w -n -r -ptemp"):runtime.exec("./RoleInference -x -cpolicy -f -w -n -r -ptemp");
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
	    if (excludedclasses.contains(classname)) {
		Integer classnum=(Integer)classinfo.classes.get(classname);
		ps.println("Forced Single Role for Class ");
		ps.println("<a href=\"rm-X"+classnum+",0\">Allow multiple roles</a><p>");
	    } else {
		Integer classnum=(Integer)classinfo.classes.get(classname);
		ps.println("<a href=\"rm-X"+classnum+",1\">Force single role for this class</a><p>");
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
	Set oldtranset=(Set) transitiontable.get(classname);
	HashMap transmap=new HashMap();
	Iterator it=oldtranset.iterator();

	while(it.hasNext()) {
	    Transition tr=(Transition) it.next();
	    int rolefrom=tr.rolefrom;
	    int roleto=tr.roleto;
	    RoleCombination rcfrom=view.project((Role)roletable.get(new Integer(rolefrom)));
	    RoleCombination rcto=view.project((Role)roletable.get(new Integer(roleto)));
	    Transition ntr=new Transition(((Integer)rolecombmap.get(rcfrom)).intValue(),
					  ((Integer)rolecombmap.get(rcto)).intValue(), 
					  tr.type, tr.names);
	    if (transmap.containsKey(ntr)) {
		Transition otr=(Transition)transmap.get(ntr);
		String[] nstr=new String[ntr.names.length+otr.names.length];
		for(int i=0;i<otr.names.length;i++)
		    nstr[i]=otr.names[i];
		for(int i=0;i<ntr.names.length;i++)
		    nstr[i+otr.names.length]=ntr.names[i];
		otr.names=nstr;
	    } else
		transmap.put(ntr,ntr);
	}
	Set transet=transmap.keySet();

	viewtransitiontable.put(classname, transet);
	
	FileOutputStream fos=null;
	try {
	    fos=new FileOutputStream(filename);
	} catch (Exception e) {
	    e.printStackTrace();
	    System.exit(-1);
	}
	PrintStream ps=new PrintStream(fos);
        it=transet.iterator();
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

	    ps.print("R"+tr.rolefrom+" -> R"+tr.roleto+" [URL=\""+tr.rolefrom+"-"+tr.roleto+"\",fontsize=10,fontcolor="+color+",label=\"");
	    
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
	    ps.println("R"+role+" [label=\""+((RoleCombination)revrolecombmap.get(role)).shortname()+"\", URL=\""+role+"\"];");
	}

	ps.println("}");
	ps.close();
    }

    void initializemaps() {
	dommap=new HashMap();
	revdommap=new HashMap();
	domcount=0;

	fldreffrommap=new HashMap();
	revfldreffrommap=new HashMap();
	fldreffromcount=0;

	arrreffrommap=new HashMap();
	revarrreffrommap=new HashMap();
	arrreffromcount=0;

	identmap=new HashMap();
	revidentmap=new HashMap();
	identcount=0;
	
	fldreftomap=new HashMap();
	revfldreftomap=new HashMap();
	fldreftocount=0;

	arrreftomap=new HashMap();
	revarrreftomap=new HashMap();
	arrreftocount=0;

	methodsmap=new HashMap();
	revmethodsmap=new HashMap();
	methodscount=0;
    }

    synchronized void readroles() {
	roletable=new HashMap();
	reverseroletable=new HashMap();
	Role garbage=new Role(this, 1, null,false, new Dominator[0], new Reference[0], new Reference[0], new IdentityRelation[0], new Reference[0], new Reference[0], new String[0]);
	roletable.put(new Integer(1),garbage);
	reverseroletable.put(garbage, new Integer(1));
	initializemaps();
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
	    for (int i=0;i<dominators.length;i++) {
		if (!dommap.containsKey(dominators[i])) {
		    dommap.put(dominators[i],new Integer(domcount));
		    revdommap.put(new Integer(domcount),dominators[i]);
		    domcount++;
		}
	    }
	    
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
	    for (int i=0;i<rolefieldlist.length;i++) {
		if (!fldreftomap.containsKey(rolefieldlist[i])) {
		    fldreftomap.put(rolefieldlist[i],new Integer(fldreftocount));
		    revfldreftomap.put(new Integer(fldreftocount),rolefieldlist[i]);
		    fldreftocount++;
		}
	    }


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
	    for (int i=0;i<rolearraylist.length;i++) {
		if (!arrreftomap.containsKey(rolearraylist[i])) {
		    arrreftomap.put(rolearraylist[i],new Integer(arrreftocount));
		    revarrreftomap.put(new Integer(arrreftocount),rolearraylist[i]);
		    arrreftocount++;
		}
	    }


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

	    for (int i=0;i<identityrelations.length;i++) {
		if (!identmap.containsKey(identityrelations[i])) {
		    identmap.put(identityrelations[i].fieldname1, new Integer(identcount));
		    revidentmap.put(new Integer(identcount), identityrelations[i].fieldname1);
		    identcount++;
		}
	    }


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
	    for (int i=0;i<nonnullfields.length;i++) {
		Reference newref=new Reference(this, classname, nonnullfields[i].fieldname, 0);
		
		if (!fldreffrommap.containsKey(newref)) {
		    fldreffrommap.put(newref,new Integer(fldreffromcount));
		    revfldreffrommap.put(new Integer(fldreffromcount),newref);
		    fldreffromcount++;
		}
	    }


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
	    for (int i=0;i<nonnullarrays.length;i++) {
		if (!arrreffrommap.containsKey(nonnullarrays[i])) {
		    arrreffrommap.put(nonnullarrays[i],new Integer(arrreffromcount));
		    revarrreffrommap.put(new Integer(arrreffromcount),nonnullarrays[i]);
		    arrreffromcount++;
		}
	    }


	    al=new ArrayList();
	    while(true) {
		String fmethodname=nexttoken(fr);
		if (fmethodname.equals("~~"))
		    break;
		String fparamname=nexttoken(fr);
		String fparamnum=nexttoken(fr);
		al.add(fmethodname+" "+fparamname+" "+fparamnum);
	    }
	    String[] invokedmethods=(String[]) al.toArray(new String[al.size()]); 
	    for(int i=0;i<invokedmethods.length;i++) {
		if (!methodsmap.containsKey(invokedmethods[i])) {
		    methodsmap.put(invokedmethods[i], new Integer(methodscount));
		    revmethodsmap.put(new Integer(methodscount), invokedmethods[i]);
		    methodscount++;
		}
	    }


	    Role r=new Role(this, Integer.parseInt(rolenumber), classname,contained.equals("1"), dominators,
			    rolefieldlist, rolearraylist, identityrelations, nonnullfields, nonnullarrays, invokedmethods);
	    roletable.put(java.lang.Integer.valueOf(rolenumber),r);
	    reverseroletable.put(r,java.lang.Integer.valueOf(rolenumber));
	    if (java.lang.Integer.valueOf(rolenumber).intValue()>maxrole)
		maxrole=java.lang.Integer.valueOf(rolenumber).intValue();
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
	Set edges=new HashSet();

	while(it.hasNext()) {
	    Roleedge re=(Roleedge) it.next();
	    String color="black";
	    if (re.contained)
		color="blue";
	    int srcrole=((Integer)rolecombmap.get(view.project((Role)roletable.get(new Integer(re.srcrole))))).intValue();
	    int dstrole=((Integer)rolecombmap.get(view.project((Role)roletable.get(new Integer(re.dstrole))))).intValue();

	    Roleedge renew=new Roleedge(srcrole,dstrole, re.contained, re.fieldname);
	    if (!edges.contains(renew)) {
		edges.add(renew);
		ps.println("R"+renew.srcrole+" -> R"+renew.dstrole+" [fontsize=10,color="+color+",label=\""+renew.fieldname+"\"];");
		observedroles.add(new Integer(renew.srcrole));
		observedroles.add(new Integer(renew.dstrole));
	    }
	}

	Iterator iterator=observedroles.iterator();
	while(iterator.hasNext()) {
	    Integer role=(Integer) iterator.next();
	    String color="black";
	    //gotta get some role to see if this combination is contained...
	    RoleCombination rc1=(RoleCombination)revrolecombmap.get(role);
	    if (rc1.roleidentifiers.length>0) {
		Role rol=(Role)roletable.get(new Integer(rc1.roleidentifiers[0]));
		if (rol.contained)
		    color="blue";
	    }
	    ps.println("R"+role+" [label=\""+((RoleCombination)revrolecombmap.get(role)).shortname()+"\", URL=\""+role+"\",color="+color+"];");
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
	return true;
	//	return ((Role)roletable.get(rolenumber)).contained;
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
		    int srcrolec=((Integer)rolecombmap.get(view.project((Role)roletable.get(srcrole)))).intValue();
		    int dstrolec=((Integer)rolecombmap.get(view.project((Role)roletable.get(dstrole)))).intValue();
		    

		    Roleedge renew=new Roleedge(srcrolec,dstrolec, re.contained, re.fieldname);
		    if (!edges.contains(renew)) {
			edges.add(renew);
			if (re.dstrole==dstrole.intValue()) {
			    ps.println("R"+srcrolec+" -> R"+dstrolec+" [fontsize=10,color="+color+",label=\""+re.fieldname+"\"];");
			    observedroles.add(new Integer(dstrolec));
			}
			observedroles.add(new Integer(srcrolec));
		    }
		}
	    }
	}

	Iterator iterator=observedroles.iterator();
	while(iterator.hasNext()) {
	    Integer role=(Integer) iterator.next();
	    String color="black";
	    //gotta get some role to see if this combination is contained...
	    RoleCombination rc1=(RoleCombination)revrolecombmap.get(role);
	    if (rc1.roleidentifiers.length>0) {
		Role rol=(Role)roletable.get(new Integer(rc1.roleidentifiers[0]));
		if (rol.contained)
		    color="blue";
	    }
	    ps.println("R"+role+" [label=\""+((RoleCombination)revrolecombmap.get(role)).shortname()+"\", URL=\""+role+"\",color="+color+"];");
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
	viewtransitiontable=new HashMap();
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
