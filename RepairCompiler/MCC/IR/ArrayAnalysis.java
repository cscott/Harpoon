package MCC.IR;
import java.util.*;
import MCC.State;

public class ArrayAnalysis {
    Termination termination;
    State state;

    public ArrayAnalysis(State state, Termination t) {
	this.state=state;
	this.termination=t;
	this.set=new Hashtable();
	this.leftrelation=new Hashtable();
	this.rightrelation=new Hashtable();
	
	assert termination.exactsize!=null;
	doAnalysis();
    }

    Hashtable set;
    Hashtable leftrelation;
    Hashtable rightrelation;

    public AccessPath getSet(SetDescriptor sd) {
	if (set.containsKey(sd))
	    return (AccessPath)set.get(sd);
	return null;
    }

    
    public AccessPath getDomain(RelationDescriptor rd) {
	if (leftrelation.containsKey(rd))
	    return (AccessPath)leftrelation.get(rd);
	return null;
    }

    public AccessPath getRange(RelationDescriptor rd) {
	if (rightrelation.containsKey(rd))
	    return (AccessPath)rightrelation.get(rd);
	return null;
    }

    private void doAnalysis() {
	Vector rules=state.vRules;
	for(int i=0;i<rules.size();i++) {
	    Rule r=(Rule)rules.get(i);
	    Inclusion inc=r.getInclusion();
	    if (inc instanceof SetInclusion) {
		SetInclusion si=(SetInclusion)inc;

		AccessPath oldap=set.containsKey(si.getSet())?(AccessPath)set.get(si.getSet()):null;
		AccessPath newap=analyzeExpr(r,si.getExpr());
		if (oldap==null) {
		    set.put(si.getSet(),newap);
		} else {
		    if (!oldap.equal(newap))
			set.put(si.getSet(),AccessPath.NONE);
		}
	    } else if (inc instanceof RelationInclusion) {
		RelationInclusion ri=(RelationInclusion)inc;
		
		AccessPath oldapl=leftrelation.containsKey(ri.getRelation())?(AccessPath)leftrelation.get(ri.getRelation()):null;
		AccessPath newapl=analyzeExpr(r,ri.getLeftExpr());
		if (oldapl==null) {
		    leftrelation.put(ri.getRelation(),newapl);
		} else {
		    if (!oldapl.equal(newapl))
			leftrelation.put(ri.getRelation(),AccessPath.NONE);
		}

		AccessPath oldapr=rightrelation.containsKey(ri.getRelation())?(AccessPath)rightrelation.get(ri.getRelation()):null;
		AccessPath newapr=analyzeExpr(r,ri.getRightExpr());
		if (oldapr==null) {
		    rightrelation.put(ri.getRelation(),newapr);
		} else {
		    if (!oldapr.equal(newapr))
			rightrelation.put(ri.getRelation(),AccessPath.NONE);
		}
	    } else throw new Error();
    	}
    }

    public AccessPath analyzeExpr(Rule r,Expr e) {
	Vector dotvector=new Vector();
	Expr ptr=e;
	while (ptr instanceof CastExpr)
	    ptr=((CastExpr)ptr).getExpr();

	while(true) {
	    /* Does something other than a dereference? */

	    if (!(ptr instanceof DotExpr)) {
		return AccessPath.NONE; 
	    }

	    DotExpr de=(DotExpr)ptr;
	    dotvector.add(de);
	    ptr=de.left;
	    while (ptr instanceof CastExpr)
		ptr=((CastExpr)ptr).getExpr();

	    if (ptr instanceof VarExpr) {
		VarExpr ve=(VarExpr)ptr;
		VarDescriptor vd=ve.getVar();
		AccessPath ap=new AccessPath();
		if (vd.isGlobal()) {
		    ap.startVar(vd);
		} else {
		    for(int i=0;i<r.numQuantifiers();i++) {
			Quantifier q=r.getQuantifier(i);
			if ((q instanceof SetQuantifier)&&
			    ((SetQuantifier)q).getVar()==vd) {
			    SetDescriptor sd=((SetQuantifier)q).getSet();
			    int size=termination.exactsize.getsize(sd);
			    if (size==1) {
				ap.startSet(sd);
				break;
			    } else {
				return AccessPath.NONE;
			    }
			}
		    }
		    if (!ap.setStart) {
			return AccessPath.NONE;
		    }
		}
		/* Starting point finished - parse dereferences */
		boolean foundarray=false;
		for(int j=dotvector.size()-1;j>=0;j--) {
		    DotExpr de2=(DotExpr) dotvector.get(j);
		    FieldDescriptor fd=de2.getField();
		    if (fd instanceof ArrayDescriptor) {
			foundarray=true;
			if (((ArrayDescriptor)fd).getField().getPtr()) {
			    return AccessPath.NONE;
			}
		    } else {
			if (foundarray&&fd.getPtr()) {
			    return AccessPath.NONE;
			}
		    }
		    ap.addField(fd);
		}
		return ap;
	    }
	}
    }

    public static class AccessPath {
	public static final AccessPath NONE=new AccessPath();
	
	public AccessPath() {
	    path=new Vector();
	}
	boolean setStart;
	SetDescriptor startset;
	VarDescriptor startvar;

	public boolean isSet() {
	    return setStart;
	}

	public SetDescriptor getSet() {
	    return startset;
	}

	public VarDescriptor getVar() {
	    return startvar;
	}


	public void startSet(SetDescriptor sd) {
	    this.startset=sd;
	    setStart=true;
	}

	public void startVar(VarDescriptor vd) {
	    assert vd.isGlobal();
	    this.startvar=vd;
	    setStart=false;
	}

	Vector path;
	public void addField(FieldDescriptor fd) {
	    path.add(fd);
	}

	public int numFields() {
	    return path.size();
	}

	public FieldDescriptor getField(int i) {
	    return (FieldDescriptor)path.get(i);
	}

	public String toString() {
	    String st="";
	    if (setStart)
		st+=this.startset;
	    else
		st+=this.startvar;

	    for(int i=0;i<numFields();i++) {
		st+="."+getField(i);
	    }
	    return st;
	}

	public boolean equal(AccessPath ap) {
	    if (this==ap)
		return true;
	    if (setStart&&this.startset!=ap.startset)
		return false;
	    if ((!setStart)&&this.startvar!=ap.startvar)
		return false;
	    if (this.path.size()!=ap.path.size())
		return false;
	    for(int i=0;i<this.path.size();i++) {
		if (this.path.get(i)!=ap.path.get(i))
		    return false;
	    }
	    return true;
	}
    }
}
