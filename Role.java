import java.util.*;

class Role {
    int rolenumber; /* 1 is the magical garbage object*/
    RoleI rolei;
    String classname;
    boolean contained;
    Dominator[] dominators;
    Reference[] rolefieldlist, rolearraylist,nonnullfields, nonnullarrays;
    IdentityRelation[] identityrelations;
    String [] invokedmethods;
    
    public Role(RoleI rolei, int rolenumber, String classname, boolean contained, Dominator[] dominators,
		Reference[] rolefieldlist, Reference[] rolearraylist,
		IdentityRelation[] identityrelations, Reference[] nonnullfields,
		Reference[] nonnullarrays, String[] invokedmethods) {
	this.rolei=rolei;
	this.rolenumber=rolenumber;
	this.classname=classname;
	this.contained=contained;
	this.dominators=dominators;
	this.rolefieldlist=rolefieldlist;
	this.rolearraylist=rolearraylist;
	this.identityrelations=identityrelations;
	this.nonnullfields=nonnullfields;
	this.nonnullarrays=nonnullarrays;
	this.invokedmethods=invokedmethods;
	if (rolenumber==-1) {
	    if (rolei.reverseroletable.containsKey(this)) {
		this.rolenumber=((Integer)rolei.reverseroletable.get(this)).intValue();
		
	    } else {
		rolei.maxrole++;
		this.rolenumber=rolei.maxrole;
		rolei.reverseroletable.put(this,new java.lang.Integer(this.rolenumber));
		rolei.roletable.put(new java.lang.Integer(this.rolenumber),this);
	    }
	}
    }

    public String shortname() {
	if (rolei.rolenames.containsKey(new Integer(this.rolenumber)))
	    return (String) rolei.rolenames.get(new Integer(this.rolenumber));
	if (rolenumber==1)
	    return "Garbage";
	if (classname==null) /* ignoring classname */
	    return "R"+rolenumber;
	int lastslash=classname.lastIndexOf('/')+1;
	String ourclassname=classname.substring(lastslash);
	if (classname.charAt(0)=='['&&lastslash!=0)
	    ourclassname="["+ourclassname;
	if (dominators.length==0&&
	    rolefieldlist.length==0&&
	    rolearraylist.length==0&&
	    nonnullfields.length==0&&
	    nonnullarrays.length==0&&
	    invokedmethods.length==0&&
	    identityrelations.length==0)
	    return "Initial"+ourclassname;
	/* Tree elements processing */
	int state=0;
	for (int i=0;i<nonnullfields.length;i++) {
	    if (nonnullfields[i].fieldname.equalsIgnoreCase("left")||
		nonnullfields[i].fieldname.equalsIgnoreCase("right"))
		state|=1;
	    else
		state|=2;
	}

	for (int i=0;i<rolefieldlist.length;i++) {
	    if (rolefieldlist[i].fieldname.equalsIgnoreCase("left")||
		rolefieldlist[i].fieldname.equalsIgnoreCase("right"))
		state|=1;
	    else
		state|=2;
	}
	
	if ((state&1)==1) {
	    if (dominators.length==0&&
		rolearraylist.length==0&&
		nonnullarrays.length==0&&
		invokedmethods.length==0)
		state|=4;
	    	    
	    String base=null;
	    if (state==5)
		base=ourclassname;
	    else
		base=ourclassname+" "+rolenumber;
	    
	    if ((state&1)==1) {
		boolean leftused=false, rightused=false;
		for (int i=0;i<rolefieldlist.length;i++) {
		    if (rolefieldlist[i].fieldname.equalsIgnoreCase("left")&&!leftused) {
			base="left "+base;leftused=true;
		    }
		    if (rolefieldlist[i].fieldname.equalsIgnoreCase("right")&&!rightused) {
			base="right "+base;rightused=true;
		    }
		}
		leftused=false; rightused=false;
		for (int i=0;i<nonnullfields.length;i++) {
		    if (nonnullfields[i].fieldname.equalsIgnoreCase("left")&&!leftused) {
			if (!rightused)
			    base= base + " w/ ";
			else
			    base= base + " & ";
			base=base + "left";leftused=true;
		    }
		    if (nonnullfields[i].fieldname.equalsIgnoreCase("right")&&!rightused) {
			if (!leftused)
			    base= base + " w/ ";
			else
			    base= base + " & ";
			base=base + " right";rightused=true;
		    }
		}
	    }
	    
	    return base;
	} else if (dominators.length==0&&
	    rolefieldlist.length<=1&&
	    rolearraylist.length==0&&
	    nonnullfields.length<=2&&
	    nonnullarrays.length==0&&
	    invokedmethods.length==0&&
	    identityrelations.length==0) {
	    String start=ourclassname;
	    if (rolefieldlist.length==1)
		start=rolefieldlist[0].fieldname+" "+start;
	    if (nonnullfields.length==0)
		return start;
	    else if (nonnullfields.length==1)
		return start+" w/ "+nonnullfields[0].fieldname;
	    else
		return start+" w/ "+nonnullfields[0].fieldname+" & "+nonnullfields[1].fieldname;
	} else if (dominators.length==1&&
	    rolefieldlist.length==0&&
	    rolearraylist.length==0&&
	    nonnullfields.length<=2&&
	    nonnullarrays.length==0&&
	    invokedmethods.length==0&&
	    identityrelations.length==0) {
	    String start=ourclassname;

	    start=dominators[0].shortname()+" "+start;

	    if (nonnullfields.length==0)
		return start;
	    else if (nonnullfields.length==1)
		return start+" w/ "+nonnullfields[0].fieldname;
	    else
		return start+" w/ "+nonnullfields[0].fieldname+" & "+nonnullfields[1].fieldname; 
	} else {
	    return ourclassname+" "+rolenumber;
	}
    }

    public Role project(RoleUniverse univ) {
	if (this.rolenumber==1)
	    return this; /*Garbage role*/
	if (univ.cache.containsKey(new Integer(this.rolenumber)))
	    return (Role) rolei.roletable.get(univ.cache.get(new Integer(this.rolenumber)));

	String newclassname=univ.classmatters?classname:null;

	if ((newclassname!=null)&&univ.singleroleclass.contains(newclassname)) {
	    Role r=new Role(rolei, -1, newclassname, contained, new Dominator[0],
			    new Reference[0], new Reference[0],
			    new IdentityRelation[0], new Reference[0], new Reference[0], new String[0]);
	    return (Role) rolei.roletable.get(new Integer(r.rolenumber));	    	}
	
	ArrayList al=new ArrayList();	
	for(int i=0;i<dominators.length;i++)
	    if (!univ.restrictdominators||
		univ.alloweddominators.contains(dominators[i]))
		al.add(dominators[i]);
	Dominator[] newdominators=(Dominator[])al.toArray(new Dominator[al.size()]);
	
	al=new ArrayList();
	for(int i=0;i<rolefieldlist.length;i++)
	    if (!univ.restrictfieldreferenceto||
		univ.allowedfieldto.contains(rolefieldlist[i]))
		al.add(rolefieldlist[i]);
	Reference[] newrolefieldlist=(Reference[])al.toArray(new Reference[al.size()]);
	
	al=new ArrayList();
	for(int i=0;i<rolearraylist.length;i++)
	    if (!univ.restrictarrayreferenceto||
		univ.allowedarrayto.contains(rolearraylist[i]))
		al.add(rolearraylist[i]);
	Reference[] newrolearraylist=(Reference[])al.toArray(new Reference[al.size()]);

	al=new ArrayList();
	for(int i=0;i<identityrelations.length;i++)
	    if (!univ.restrictidentity||
		univ.allowedidentityfields.contains(identityrelations[i].fieldname1))
		al.add(identityrelations[i]);
	IdentityRelation[] newidentityrelations=(IdentityRelation[])al.toArray(new IdentityRelation[al.size()]);
	
	al=new ArrayList();
	for(int i=0;i<nonnullfields.length;i++) {
	    if (!univ.restrictfieldreferencefrom||
		univ.allowedfieldfrom.contains(new Reference(rolei, classname, nonnullfields[i].fieldname, 0)))
		al.add(nonnullfields[i].project(univ));
	}
	Reference[] newnonnullfields=(Reference[]) al.toArray(new Reference[al.size()]);
	
	al=new ArrayList();
	for(int i=0;i<nonnullarrays.length;i++) {
	    if (!univ.restrictarrayreferencefrom||
		univ.allowedarrayfrom.contains(nonnullarrays[i]))
		al.add(nonnullarrays[i].project(univ));
	}
	Reference[] newnonnullarrays=(Reference[]) al.toArray(new Reference[al.size()]);

	al=new ArrayList();
	for(int i=0;i<invokedmethods.length;i++) {
	    if (!univ.restrictinvokedmethods||
		univ.allowedinvokedmethods.contains(invokedmethods[i]))
		al.add(invokedmethods[i]);
	}
	String[] newinvokedmethods=(String[])al.toArray(new String[al.size()]);

	Role r=new Role(rolei, -1, newclassname, contained, newdominators,
			newrolefieldlist, newrolearraylist,
			newidentityrelations, newnonnullfields, newnonnullarrays, newinvokedmethods);
	univ.cache.put(new Integer(this.rolenumber), new Integer(r.rolenumber));
	return (Role) rolei.roletable.get(new Integer(r.rolenumber));
    }

    public int hashCode() {
	int hashcode=0;
	if (classname!=null)
	    hashcode^=classname.hashCode();
	if (contained)
	    hashcode^=1;
	for(int i=0;i<dominators.length;i++)
	    hashcode^=dominators[i].hashCode();
	for(int i=0;i<rolefieldlist.length;i++)
	    hashcode^=rolefieldlist[i].hashCode();
	for(int i=0;i<rolearraylist.length;i++)
	    hashcode^=rolearraylist[i].hashCode();
	for(int i=0;i<nonnullfields.length;i++)
	    hashcode^=nonnullfields[i].hashCode();
	for(int i=0;i<nonnullarrays.length;i++)
	    hashcode^=nonnullarrays[i].hashCode();
	for(int i=0;i<identityrelations.length;i++)
	    hashcode^=identityrelations[i].hashCode();
	for(int i=0;i<invokedmethods.length;i++)
	    hashcode^=invokedmethods[i].hashCode();
	return hashcode;
    }

    boolean arrayequal(Object []a1, Object[] a2) {
	if ((a1==null)&&(a2==null))
	    return true;
	if (a1==null)
	    return false;
	if (a1.length!=a2.length)
	    return false;
	for(int i=0;i<a1.length;i++)
	    if (!a1[i].equals(a2[i]))
		return false;
	return true;
    }

    boolean refarrayequal(Reference []a1, Reference[] a2) {
	if ((a1==null)&&(a2==null))
	    return true;
	if (a1==null)
	    return false;
	if (a1.length!=a2.length)
	    return false;
	for(int i=0;i<a1.length;i++)
	    if (!a1[i].realequals(a2[i]))
		return false;
	return true;
    }

    public boolean equals(Object obj) {
	if (obj instanceof Role) {
	    Role robj=(Role) obj;
	    if ((robj.rolenumber==1&&rolenumber!=1)||
		(robj.rolenumber!=1&&rolenumber==1))
		return false;
		
	    if ((classname!=null&&classname.equals(robj.classname))||
		(classname==null&&robj.classname==null))
		if (contained==robj.contained)
		    if (arrayequal(dominators, robj.dominators)&&
			refarrayequal(rolefieldlist, robj.rolefieldlist)&&
			refarrayequal(rolearraylist, robj.rolearraylist)&&
			refarrayequal(nonnullfields, robj.nonnullfields)&&
			refarrayequal(nonnullarrays, robj.nonnullarrays)&&
			arrayequal(identityrelations, robj.identityrelations)&&
			arrayequal(invokedmethods, robj.invokedmethods))
			return true;
	}
	return false;
    }

    public String toString() {
	String string="";
	string+="Role: R"+rolenumber+"<p>";
	string+=this.shortname()+"<p>";
	string+=" Class: <a href=\""+rolei.classinfo.classes.get(classname)+".html\">"+classname+"</a><p>";
	if (contained)
	    string+=" State Merged<p>";
	if (dominators.length!=0)
	    string+="  Dominated by:<p>";
	for (int i=0;i<dominators.length;i++)
	    string+=dominators[i];
	if (rolefieldlist.length!=0)
	    string+="  Pointed to by the fields:<p>";
	for (int i=0;i<rolefieldlist.length;i++)
	    string+=rolefieldlist[i];
	if (rolearraylist.length!=0)
	    string+="  Pointed to by the arrays:<p>";
	for (int i=0;i<rolearraylist.length;i++)
	    string+=rolearraylist[i];
	if (nonnullfields.length!=0)
	    string+="  Non-null fields:<p>";
	for (int i=0;i<nonnullfields.length;i++)
	    string+=nonnullfields[i];
	if (nonnullarrays.length!=0)
	    string+="  Non-null elements:<p>";
	for (int i=0;i<nonnullarrays.length;i++)
	    string+=nonnullarrays[i];
	if (invokedmethods.length!=0)
	    string+="   Methods invoked on:<p>";
	for (int i=0;i<invokedmethods.length;i++)
	    string+=invokedmethods[i]+"<p>";
	if (identityrelations.length!=0)
	    string+="   Identity relations:<p>";
	for (int i=0;i<identityrelations.length;i++)
	    string+=identityrelations[i]+"<p>";

	return string;
    }
}
