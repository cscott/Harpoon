package MCC.IR;

class AbstractRepair {
    public final static int ADDTOSET=1;
    public final static int REMOVEFROMSET=2;
    public final static int ADDTORELATION=3;
    public final static int REMOVEFROMRELATION=4;
    public final static int MODIFYRELATION=5;

    DNFPredicate torepair;
    int type;
    Descriptor descriptor;

    public String type() {
	switch(type) {
	case ADDTOSET:
	    return "AddToSet";
	case REMOVEFROMSET:
	    return "RemoveToSet";
	case ADDTORELATION:
	    return "AddToRelation";
	case REMOVEFROMRELATION:
	    return "RemoveFromRelation";
	case MODIFYRELATION:
	    return "ModifyRelation";
	default:
	    return "Unknown";
	}
    }
    
    public int getType() {
	return type;
    }

    public DNFPredicate getPredicate() {
	return torepair;
    }

    public Descriptor getDescriptor() {
	return descriptor;
    }

    public AbstractRepair(DNFPredicate dp,int typ, Descriptor d) {
	torepair=dp;
	type=typ;
	descriptor=d;
    }
}
