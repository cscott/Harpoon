package MCC.IR;

class SizeObject {
    Descriptor setorrelation;
    boolean isRelation;
    SetDescriptor set;
    boolean isInverted;

    public SizeObject(SetDescriptor sd) {
	this.setorrelation=sd;
	this.isRelation=false;
    }

    public SizeObject(RelationDescriptor rd, SetDescriptor sd,boolean inverted) {
	this.isRelation=true;
	this.setorrelation=rd;
	this.set=sd;
	this.isInverted=inverted;
    }

    public int hashCode() {
	int hashcode=setorrelation.hashCode();
	if (set!=null)
	    hashcode^=set.hashCode();
	return hashcode;
    }

    public boolean equals(java.lang.Object o) {
	if (!(o instanceof SizeObject))
	    return false;
	SizeObject so=(SizeObject)o;
	if (so.setorrelation!=setorrelation)
	    return false;
	if (so.isRelation!=isRelation)
	    throw new Error("");
	if (isRelation) {
	    if (so.set!=set)
		return false;
	    if (so.isInverted!=isInverted)
		return false;
	}
	return true;
    }
}
