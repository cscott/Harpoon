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
		    return classname+"<a href=\"rm-L"+role+"\"> R"+role+"</a> "+duplicates+" times.<p>\n";
		else
		    return  fieldname+"<a href=\"rm-L"+role+"\"> R"+role+"</a><p>\n";

	    }
	}
    }
    
}

