class Reference {
    String classname;
    String fieldname;
    int duplicates;
    int role;
    RoleI rolei;

    /* Normal Field reference constructor*/
    public Reference(RoleI rolei,String classname, String fieldname, int duplicates) {
	this.classname=classname;
	this.fieldname=fieldname;
	this.duplicates=duplicates;
	this.role=-1;
	this.rolei=rolei;
    }
    /* Normal Array reference Constructor*/
    public Reference(RoleI rolei,String classname, int duplicates) {
	this.classname=classname;
	this.fieldname=null;
	this.duplicates=duplicates;
	this.role=-1;
	this.rolei=rolei;
    }

    /*Field of object constructor*/
    public Reference(RoleI rolei, int role, String fieldname) {
	this.classname=null;
	this.fieldname=fieldname;
	this.duplicates=0;
	this.role=role;
	this.rolei=rolei;
    }

    public Reference(RoleI rolei, String classname,int role,int duplicates) {
	this.classname=classname;
	this.fieldname=null;
	this.duplicates=duplicates;
	this.role=role;
	this.rolei=rolei;
    }
    
    public Reference desc() {
	return new Reference(rolei, classname, fieldname, 0);
    }

    public boolean realequals(Reference robj) {
	if (robj!=null)
	    if ((classname!=null&&classname.equals(robj.classname))||
		(classname==null&&robj.classname==null))
		if ((fieldname!=null&&fieldname.equals(robj.fieldname))||
		    (fieldname==null&&robj.fieldname==null))
		    if (duplicates==robj.duplicates)
			if (role==robj.role)
			    return true;
	return false;
    }

    public boolean equals(Object obj) {
	if (obj instanceof Reference) {
	    Reference robj=(Reference) obj;
	    if ((classname!=null&&classname.equals(robj.classname))||
		(classname==null&&robj.classname==null))
		if ((fieldname!=null&&fieldname.equals(robj.fieldname))||
		    (fieldname==null&&robj.fieldname==null))
		    return true;
	}
	return false;
    }
    
    public Reference project(RoleUniverse univ) {
	if (role>0) {/* exclude -1 or 0 */
	    Role r=(Role) rolei.roletable.get(new Integer(role));
	    //	    System.out.println(role);
	    Role rp=r.project(univ);
	    if (rp.rolenumber!=role) {
		Reference newr=new Reference(rolei, classname, fieldname,duplicates);
		newr.role=rp.rolenumber;
		return newr;
	    }
	}
	return this;
    }

    public int hashCode() {
	int hashcode=0;
	if (classname!=null)
	    hashcode^=classname.hashCode();
	if (fieldname!=null)
	    hashcode^=fieldname.hashCode();
	return hashcode; 
    }

    public String toString() {
	if (role==-1) {
	    if (fieldname==null)
		return classname+" "+duplicates+" times.<p>\n";
	    else {
		return classname+"."+fieldname+" "+duplicates+" times.<p>\n";
	    }
	} else {
	    if (role==0) {
		if (fieldname==null)
		    return classname+" "+duplicates+" times.<p>\n";
		else
		    return fieldname+"<p>\n";
	    } else {
		if (fieldname==null)
		    return classname+"<a href=\"rm-L"+role+"\">"+((Role)rolei.roletable.get(new Integer(role))).shortname()+"</a> "+duplicates+" times.<p>\n";
		else
		    return  fieldname+"<a href=\"rm-L"+role+"\">"+((Role)rolei.roletable.get(new Integer(role))).shortname()+"</a><p>\n";

	    }
	}
    }
    
}

