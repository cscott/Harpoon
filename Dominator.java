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
    public String toString() {
	if (global==null) {
	    return lvname+" ["+sourcename+"]"+location+"<p>\n";
	} else {
	    return global+"<p>\n";
	}
    }
}


