package MCC.IR;
import java.util.*;
import MCC.State;


class ExactSize {
    State state;
    private Hashtable sizemap;
    private Hashtable constraintmap;
    private SetAnalysis setanalysis;
    private Hashtable minsize;
    private Hashtable constraintensureminsize;

    public ExactSize(State state) {
	this.state=state;
	this.sizemap=new Hashtable();
        this.minsize=new Hashtable();
        this.constraintensureminsize=new Hashtable();
	this.constraintmap=new Hashtable();
	this.setanalysis=state.setanalysis;
	computesizes();
    }

    public int minSize(SetDescriptor sd) {
	SizeObject so=new SizeObject(sd);
	if (minsize.containsKey(so))
	    return ((Integer)minsize.get(so)).intValue();
	else
	    return 0;
    }

    public Constraint ensuresMinSize(SetDescriptor sd) {
        SizeObject so=new SizeObject(sd);
	return (Constraint)constraintensureminsize.get(so);
    }

    public int getsize(SetDescriptor sd) {
	SizeObject so=new SizeObject(sd);
	if (sizemap.containsKey(so))
	    return ((Integer)sizemap.get(so)).intValue();
	else
	    return -1;
    }
    public Constraint getConstraint(SetDescriptor sd) {
	SizeObject so=new SizeObject(sd);
	return (Constraint)constraintmap.get(so);
    }

    public int getsize(RelationDescriptor rd, SetDescriptor sd, boolean inverted) {
	Iterator it=setanalysis.getSuperset(sd).iterator();
	while(sd!=null) {
	    SizeObject so=new SizeObject(rd,sd,inverted);
	    if (sizemap.containsKey(so))
		return ((Integer)sizemap.get(so)).intValue();
	    sd=null;
	    if (it.hasNext())
		sd=(SetDescriptor)it.next();
	}
	return -1;
    }

    public Constraint getConstraint(RelationDescriptor rd, SetDescriptor sd, boolean inverted) {
	Iterator it=setanalysis.getSuperset(sd).iterator();
	while(sd!=null) {
	    SizeObject so=new SizeObject(rd,sd,inverted);
	    if (constraintmap.containsKey(so))
		return ((Constraint)constraintmap.get(so));
	    sd=null;
	    if (it.hasNext())
		sd=(SetDescriptor)it.next();
	}
	return null;
    }

    private void constructminsizes() {
        boolean change=true;
        HashSet usedsources=new HashSet();
        while (change) {
            change=false;
            for(int i=0;i<state.vRules.size();i++) {
                Rule r=(Rule)state.vRules.get(i);
                //model defition rule with no condition
                if ((!(r.getGuardExpr() instanceof BooleanLiteralExpr))||
                    (!((BooleanLiteralExpr)r.getGuardExpr()).getValue()))
                    continue;
                //inclusion condition needs to be safe
                if ((!(r.getInclusion() instanceof SetInclusion))||
                    (!((SetInclusion)r.getInclusion()).getExpr().isSafe()))
                    continue;
                //needs exactly 1 quantifier which is a set quantifier
                if (r.numQuantifiers()!=1||
                    (!(r.getQuantifier(0) instanceof SetQuantifier)))
                    continue;
                SetQuantifier sq=(SetQuantifier)r.getQuantifier(0);
                SetDescriptor sd=sq.getSet();
                int size=-1;
                Constraint source=null;
                if (getsize(sd)>0) {
                    size=getsize(sd);
                    source=getConstraint(sd);
                } else if (minSize(sd)>0) {
                    size=minSize(sd);
                    source=ensuresMinSize(sd);
                } else continue;
                if (size>1)
                    size=1; //would need more in depth analysis otherwise
                SetDescriptor newsd=((SetInclusion)r.getInclusion()).getSet();
                if (usedsources.contains(newsd))
                    continue; //avoid dependence cycles in our analysis
                //need to force repair to fix one of the constraints in the cycle
                int currentsize=minSize(newsd);
                if (size>currentsize) {
                    SizeObject so=new SizeObject(newsd);
                    minsize.put(so, new Integer(size));
                    constraintensureminsize.put(so,source);
                    usedsources.add(source);
		    System.out.println("Set "+newsd.toString()+" has minimum size "+size);
                    change=true;
                    //need update
                }
            }
        }
    }

    private void computesizes() {
	for(Iterator it=state.stSets.descriptors();it.hasNext();) {
	    SetDescriptor sd=(SetDescriptor)it.next();
	    for(int i=0;i<state.vConstraints.size();i++) {
		Constraint c=(Constraint)state.vConstraints.get(i);
		if (c.numQuantifiers()!=0)
		    continue;
		DNFConstraint dconst=c.dnfconstraint;
		int oldsize=-1;
		boolean matches=true;
		for(int j=0;j<dconst.size();j++) {
		    Conjunction conj=dconst.get(j);
		    boolean goodmatch=false;
		    for(int k=0;k<conj.size();k++) {
			DNFPredicate dpred=conj.get(k);
			if (!dpred.isNegated()) {
			    Predicate p=dpred.getPredicate();
			    if (p instanceof ExprPredicate) {
				ExprPredicate ep=(ExprPredicate)p;
				if (ep.getType()==ExprPredicate.SIZE&&
				    ep.getOp()==Opcode.EQ&&
				    ep.getDescriptor()==sd&&
				    ep.isRightInt()) {
				    if (j==0) {
					oldsize=ep.rightSize();
					goodmatch=true;
					break;
				    } else {
					if (oldsize==ep.rightSize()) {
					    goodmatch=true;
					    break;
					}
				    }
				}
			    }
			}
		    }
		    if (!goodmatch) {
			matches=false;
			break; //this constraint won't work
		    }
		}
		if (matches) {
		    System.out.println("Set "+sd.toString()+" has size "+oldsize);
		    SizeObject so=new SizeObject(sd);
		    sizemap.put(so,new Integer(oldsize));
		    constraintmap.put(so,c);
		}
	    }
	}

	for(Iterator it=state.stRelations.descriptors();it.hasNext();) {
	    RelationDescriptor rd=(RelationDescriptor)it.next();
	    for(int i=0;i<state.vConstraints.size();i++) {
		Constraint c=(Constraint)state.vConstraints.get(i);
		if (c.numQuantifiers()!=1||!(c.getQuantifier(0) instanceof SetQuantifier))
		    continue;
		SetQuantifier q=(SetQuantifier) c.getQuantifier(0);

		DNFConstraint dconst=c.dnfconstraint;
		int oldsize=-1;
		boolean matches=true;
		boolean inverted=false;
		for(int j=0;j<dconst.size();j++) {
		    Conjunction conj=dconst.get(j);
		    boolean goodmatch=false;
		    for(int k=0;k<conj.size();k++) {
			DNFPredicate dpred=conj.get(k);
			if (!dpred.isNegated()) {
			    Predicate p=dpred.getPredicate();
			    if (p instanceof ExprPredicate) {
				ExprPredicate ep=(ExprPredicate)p;
				if (ep.getType()==ExprPredicate.SIZE&&
				    ep.getOp()==Opcode.EQ&&
				    ep.getDescriptor()==rd&&
				    ep.isRightInt()&&
				    ((ImageSetExpr)((SizeofExpr)((OpExpr)ep.expr).left).getSetExpr()).getVar()==q.getVar()) {
				    if (j==0) {
					oldsize=ep.rightSize();
					goodmatch=true;
					inverted=ep.inverted();
					break;
				    } else {
					if (oldsize==ep.rightSize()&&inverted==ep.inverted()) {
					    goodmatch=true;
					    break;
					}
				    }
				}
			    }
			}
		    }
		    if (!goodmatch) {
			matches=false;
			break; //this constraint won't work
		    }
		}
		if (matches) {
		    System.out.println("Set "+rd.toString()+" has size "+oldsize);
		    SizeObject so=new SizeObject(rd,q.getSet(),inverted);
		    sizemap.put(so,new Integer(oldsize));
		    constraintmap.put(so,c);
		}
	    }
	}
        constructminsizes();
    }
}
