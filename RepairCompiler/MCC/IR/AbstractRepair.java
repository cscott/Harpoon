package MCC.IR;
import MCC.State;

class AbstractRepair {
    public final static int ADDTOSET=1;
    public final static int REMOVEFROMSET=2;
    public final static int ADDTORELATION=3;
    public final static int REMOVEFROMRELATION=4;
    public final static int MODIFYRELATION=5;

    DNFPredicate torepair;
    int type;
    Descriptor descriptor;
    Sources sources;

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

    public boolean isNewObject(boolean isdomain) {
	if (getType()==ADDTOSET) {
	    return sources.allocSource((SetDescriptor)descriptor);
	} else if (getType()==ADDTORELATION) {
	    RelationDescriptor rd=(RelationDescriptor)descriptor;
	    return sources.relallocSource(rd,isdomain);
	} else throw new Error(type());
    }

    public SetDescriptor getDomainSet() {
	if (torepair==null)
	    return null;
	Predicate predicate=torepair.getPredicate();
	if (!(predicate.getDescriptor() instanceof RelationDescriptor))
	    return null;

	/* Have relation descriptor now */
	if (predicate instanceof InclusionPredicate) {
	    InclusionPredicate ip=(InclusionPredicate)predicate;
	    if (ip.inverted())
		return ip.expr.getSet();
	    else if (ip.setexpr instanceof ImageSetExpr) {
		ImageSetExpr ise=(ImageSetExpr)ip.setexpr;
		if (ise.isimageset)
		    return ise.getImageSetExpr().getSet();
		else
		    return ise.getVar().getSet();
	    }
	} else if (predicate instanceof ExprPredicate) {
	    ExprPredicate ep=(ExprPredicate)predicate;

	    if (ep.inverted()&&ep.getType()==ExprPredicate.SIZE)
		return sources.relgetSourceSet((RelationDescriptor)predicate.getDescriptor(),true);
	    else if (ep.inverted()&&ep.getType()==ExprPredicate.COMPARISON)
		return ((OpExpr)ep.expr).right.getSet();
	    else if (!ep.inverted()) {
		switch(ep.getType()) {
		case ExprPredicate.SIZE: 
		    {
			SizeofExpr soe=((SizeofExpr)((OpExpr)ep.expr).left);
			ImageSetExpr ise=(ImageSetExpr)soe.setexpr;
			if (ise.isimageset)
			    return ise.getImageSetExpr().getSet();
			else
			    return ise.getVar().getSet();
		    }
		case ExprPredicate.COMPARISON:
		    {
			RelationExpr re=((RelationExpr)((OpExpr)ep.expr).left);
			return re.expr.getSet();
		    }
		default:
		    throw new Error("");
		}
	    }
	} else throw new Error("Unrecognized predicate");
	return null;
    }

    public SetDescriptor getRangeSet() {
	if (torepair==null)
	    return null;
	Predicate predicate=torepair.getPredicate();
	if (!(predicate.getDescriptor() instanceof RelationDescriptor))
	    return null;

	/* Have relation descriptor now */
	if (predicate instanceof InclusionPredicate) {
	    InclusionPredicate ip=(InclusionPredicate)predicate;
	    if (!ip.inverted())
		return ip.expr.getSet();
	    else if (ip.setexpr instanceof ImageSetExpr) {
		ImageSetExpr ise=(ImageSetExpr)ip.setexpr;
		if (ise.isimageset)
		    return ise.getImageSetExpr().getSet();
		else
		    return ise.getVar().getSet();
	    }
	} else if (predicate instanceof ExprPredicate) {
	    ExprPredicate ep=(ExprPredicate)predicate;

	    if (!ep.inverted()&&ep.getType()==ExprPredicate.SIZE)
		return sources.relgetSourceSet((RelationDescriptor)predicate.getDescriptor(),false);
	    else if (!ep.inverted()&&ep.getType()==ExprPredicate.COMPARISON)
		return ((OpExpr)ep.expr).right.getSet();
	    else if (ep.inverted()) {
		switch(ep.getType()) {
		case ExprPredicate.SIZE: 
		    {
			SizeofExpr soe=((SizeofExpr)((OpExpr)ep.expr).left);
			ImageSetExpr ise=(ImageSetExpr)soe.setexpr;
			if (ise.isimageset)
			    return ise.getImageSetExpr().getSet();
			else
			    return ise.getVar().getSet();
		    }
		case ExprPredicate.COMPARISON:
		    {
			RelationExpr re=((RelationExpr)((OpExpr)ep.expr).left);
			return re.expr.getSet();
		    }
		default:
		    throw new Error("");
		}
	    }
	} else throw new Error("Unrecognized predicate");
	return null;
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


    /** Thie method tells whether the repair needs to remove objects *
     *  from the relation, or whether the model definition rules make
     *  the remove unnecessary.*/

    public boolean needsRemoves(State state) {
	assert type==MODIFYRELATION;
	SetDescriptor sd=getPredicate().getPredicate().inverted()?getRangeSet():getDomainSet();
	return !ConstraintDependence.rulesensurefunction(state,(RelationDescriptor)getDescriptor(), sd, getPredicate().getPredicate().inverted(), true);
    }

    public AbstractRepair(DNFPredicate dp,int typ, Descriptor d, Sources s) {
	torepair=dp;
	type=typ;
	descriptor=d;
	sources=s;
    }
}
