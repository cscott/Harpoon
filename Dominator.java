class Dominator {
    String lvname, sourcename, location,global;
   
    public Dominator(String lvname, String sourcename, String location ) {
	this.lvname=lvname;
	this.sourcename=sourcename;
	this.location=location;
	this.global=null;
    }
    public Dominator(String global) {
	this.lvname=null;
	this.sourcename=null;
	this.location=null;
	this.global=global;
    }

    public int hashCode() {
	int hashcode=0;
	if (lvname!=null)
	    hashcode^=lvname.hashCode();
	if (sourcename!=null)
	    hashcode^=sourcename.hashCode();
	if (location!=null)
	    hashcode^=location.hashCode();
	if (global!=null)
	    hashcode^=global.hashCode();
	return hashcode;
    }

    public boolean equals(Object obj) {
	if (obj instanceof Dominator) {
	    Dominator dobj=(Dominator) obj;
	    if ((lvname!=null&&lvname.equals(dobj.lvname))||(lvname==null&&dobj.lvname==null))
		if ((sourcename!=null&&sourcename.equals(dobj.sourcename))||(sourcename==null&&dobj.sourcename==null))
		    if ((location!=null&&location.equals(dobj.location))||(location==null&&dobj.location==null))
			if ((global!=null&&global.equals(dobj.global))||(global==null&&dobj.global==null))
			    return true;
	}
	return false;
    }

    public String toString() {
	if (global==null) {
	    return lvname+" ["+sourcename+"]"+location+"<p>\n";
	} else {
	    return global+"<p>\n";
	}
    }
}


