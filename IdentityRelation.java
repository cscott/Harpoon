class IdentityRelation{
    String fieldname1;
    String fieldname2;

    public IdentityRelation(String fieldname1,String fieldname2) {
	this.fieldname1=fieldname1;
	this.fieldname2=fieldname2;
    }
    public String toString() {
	return fieldname1+"."+fieldname2;
    }

    public int hashCode() {
	return fieldname1.hashCode()^fieldname2.hashCode();
    }

    public boolean equals(Object obj) {
	if (obj instanceof IdentityRelation) {
	    IdentityRelation ir=(IdentityRelation) obj;
	    if (fieldname1.equals(ir.fieldname1)&&
		fieldname2.equals(ir.fieldname2))
		return true;
	}
	return false;
    }
}
