class Reference {
    String classname;
    String fieldname;
    int duplicates;
    int role;
    /* Normal Field reference constructor*/
    public Reference(String classname, String fieldname, int duplicates) {
	this.classname=classname;
	this.fieldname=fieldname;
	this.duplicates=duplicates;
	this.role=-1;
    }
    /* Normal Array reference Constructor*/
    public Reference(String classname, int duplicates) {
	this.classname=classname;
	this.fieldname=null;
	this.duplicates=duplicates;
	this.role=-1;
    }

    /*Field of object constructor*/
    public Reference(int role, String fieldname) {
	this.classname=null;
	this.fieldname=fieldname;
	this.duplicates=0;
	this.role=role;
    }

    public Reference(String classname,int role,int duplicates) {
	this.classname=classname;
	this.fieldname=null;
	this.duplicates=duplicates;
	this.role=role;
    }
    public String toString() {
	if (role==-1) {
	    if (fieldname==null)
		return classname+" "+duplicates+" times.<p>\n";
	    else
		return classname+"."+fieldname+" "+duplicates+" times.<p>\n";
	} else {
	    if (role==0) {
		if (fieldname==null)
		    return classname+" "+duplicates+" times.<p>\n";
		else
		    return fieldname+"<p>\n";
	    } else {
		if (fieldname==null)
		    return classname+" R"+role+" "+duplicates+" times.<p>\n";
		else
		    return  fieldname+" R"+role+"<p>\n";

	    }
	}
    }
    
}

