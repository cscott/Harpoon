package MCC.IR;
import MCC.State;
import java.util.*;

/** This class computes the maximum size of sets and relations */

public class ComputeMaxSize {
    State state;
    Hashtable sizemap; /* -1 means infinity */
    static int KBOUND=100;

    public ComputeMaxSize(State state) {
	this.state=state;
	sizemap=new Hashtable();
	computesizes();
	postprocess();
	printsizes();
    }
    
    /** This method compute relation and set maximum sizes */
    private void computesizes() {
	Vector rules=state.vRules;
	boolean change=true;
	Set descriptorset=new HashSet();
	descriptorset.addAll(state.stSets.getAllDescriptors());
	descriptorset.addAll(state.stRelations.getAllDescriptors());
	while(change) {
	    change=false;
	    for(Iterator dit=descriptorset.iterator();dit.hasNext();) {
		Descriptor d=(Descriptor)dit.next();
		if (d instanceof ReservedSetDescriptor)
		    continue;
		int totalstarts=0;
		int totalchains=0;
		Rule chainrule=null;
		for(int i=0;i<rules.size();i++) {
		    Rule r=(Rule)rules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(d)) {
			/* This rule may add items to this set or relation */
			int rulesize=1;
			boolean start=true;
			for(int j=0;j<r.numQuantifiers();j++) {
			    Quantifier q=r.getQuantifier(j);
			    int size=0;
			    if (q instanceof RelationQuantifier) {
				Descriptor d2=((RelationQuantifier)q).getRelation();
				if (sizemap.containsKey(d2)) {
				    size=getsize(d2);
				}
				if (d==d2) {
				    if (!start)
					size=-1;
				    else
					size=1;
				    start=false;
				}
			    } else if (q instanceof SetQuantifier) {
				Descriptor d2=((SetQuantifier)q).getSet();
				if (sizemap.containsKey(d2)) {
				    size=getsize(d2);
				}
				if (d==d2) {
				    if (!start)
					size=-1;
				    else
					size=1;
				    start=false;
				}
			    } else if (q instanceof ForQuantifier) {
				ForQuantifier fq=(ForQuantifier)q;
				boolean lowint=OpExpr.isInt(fq.lower);
				boolean highint=OpExpr.isInt(fq.upper);
				if (lowint&&highint) {
				    size=1+OpExpr.getInt(fq.upper)-OpExpr.getInt(fq.lower);
				    if (size<=0) /* Catch sneaky bounds */
					throw new Error("Funny bounds in: "+fq);
				} else size=-1;
			    } else 
				throw new Error("Unrecognized Quantifier");
			    
			    if ((rulesize!=0)&&((size==-1)||(rulesize==-1)))
				rulesize=-1;
			    else
				rulesize=rulesize*size;
			}
			
			if (start) {
			    if ((rulesize==-1)||(totalstarts==-1))
				totalstarts=-1;
			    else
				totalstarts+=rulesize;
			} else {
			    if (totalchains==0)
				chainrule=r;
			    else
				chainrule=null;
			    if ((rulesize==-1)||(totalchains==-1))
				totalchains=-1;
			    else
				totalchains+=rulesize;
			}
		    }
		}
		if (totalstarts>=KBOUND)
		    totalstarts=-1;
		if (totalchains>=KBOUND)
		    totalchains=-1;

		if (!sizemap.containsKey(d)||getstarts(d)!=totalstarts||getchains(d)!=totalchains) {
		    change=true;
		    MaxSizeObject so=new MaxSizeObject(totalstarts,totalchains,chainrule);
		    sizemap.put(d,so);
		}
	    }
   	}
    }

    void printsizes() {
	Set descriptorset=new HashSet();
	descriptorset.addAll(state.stSets.getAllDescriptors());
	descriptorset.addAll(state.stRelations.getAllDescriptors());
	for(Iterator dit=descriptorset.iterator();dit.hasNext();) {
	    Descriptor d=(Descriptor)dit.next();
	    if (d instanceof ReservedSetDescriptor)
		continue;
	    System.out.println("size("+d+")="+getsize(d));
	}
    }

    private void postprocess() {
	Vector rules=state.vRules;
	boolean change=true;
	Set descriptorset=new HashSet();
	descriptorset.addAll(state.stSets.getAllDescriptors());
	descriptorset.addAll(state.stRelations.getAllDescriptors());
	while(change) {
	    change=false;
	    for(Iterator dit=descriptorset.iterator();dit.hasNext();) {
		Descriptor d=(Descriptor)dit.next();
		if (d instanceof ReservedSetDescriptor)
		    continue;
		int totalstarts=0;
		int totalchains=0;
		Rule chainrule=null;
		for(int i=0;i<rules.size();i++) {
		    Rule r=(Rule)rules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(d)) {
			/* This rule may add items to this set or relation */
			int rulesize=1;
			boolean start=true;
			for(int j=0;j<r.numQuantifiers();j++) {
			    Quantifier q=r.getQuantifier(j);
			    int size=0;
			    if (q instanceof RelationQuantifier) {
				Descriptor d2=((RelationQuantifier)q).getRelation();
				if (sizemap.containsKey(d2)) {
				    size=getsize(d2);
				}
				if (d==d2) {
				    if (!start)
					size=-1;
				    else
					size=1;
				    start=false;
				} else {
				    if (getchainrule(d2)!=null) {
					if (isMutuallyExclusive(r, getchainrule(d2)))
					    size=getstarts(d2);
				    }
				}
			    } else if (q instanceof SetQuantifier) {
				Descriptor d2=((SetQuantifier)q).getSet();
				if (sizemap.containsKey(d2)) {
				    size=getsize(d2);
				}
				if (d==d2) {
				    if (!start)
					size=-1;
				    else
					size=1;
				    start=false;
				} else {
				    if (getchainrule(d2)!=null) {
					if (isMutuallyExclusive(r, getchainrule(d2)))
					    size=getstarts(d2);
				    }
				}
			    } else if (q instanceof ForQuantifier) {
				ForQuantifier fq=(ForQuantifier)q;
				boolean lowint=OpExpr.isInt(fq.lower);
				boolean highint=OpExpr.isInt(fq.upper);
				if (lowint&&highint) {
				    size=1+OpExpr.getInt(fq.upper)-OpExpr.getInt(fq.lower);
				    if (size<=0) /* Catch sneaky bounds */
					throw new Error("Funny bounds in: "+fq);
				} else size=-1;
			    } else 
				throw new Error("Unrecognized Quantifier");
			    
			    if ((rulesize!=0)&&((size==-1)||(rulesize==-1)))
				rulesize=-1;
			    else
				rulesize=rulesize*size;
			}
			
			if (start) {
			    if ((rulesize==-1)||(totalstarts==-1))
				totalstarts=-1;
			    else
				totalstarts+=rulesize;
			} else {
			    if (totalchains==0)
				chainrule=r;
			    else
				chainrule=null;
			    if ((rulesize==-1)||(totalchains==-1))
				totalchains=-1;
			    else
				totalchains+=rulesize;
			}
		    }
		}
		if (totalstarts>=KBOUND)
		    totalstarts=-1;
		if (totalchains>=KBOUND)
		    totalchains=-1;

		if (!sizemap.containsKey(d)||getstarts(d)!=totalstarts||getchains(d)!=totalchains) {
		    change=true;
		    MaxSizeObject so=new MaxSizeObject(totalstarts,totalchains,chainrule);
		    sizemap.put(d,so);
		}
	    }
   	}

    }

    int getstarts(Descriptor d) {
	MaxSizeObject so=(MaxSizeObject)sizemap.get(d);
	return so.maxstarts;
    }
    int getchains(Descriptor d) {
	MaxSizeObject so=(MaxSizeObject)sizemap.get(d);
	return so.numberchains;
    }
    int getsize(Descriptor d) {
	MaxSizeObject so=(MaxSizeObject)sizemap.get(d);
	if (so.maxstarts==0)
	    return 0;
	if (so.numberchains!=0)
	    return -1;
	return so.maxstarts;
    }

    private Rule getchainrule(Descriptor d) {
	MaxSizeObject so=(MaxSizeObject)sizemap.get(d);
	return so.chainrule;
    }

    public static boolean isMutuallyExclusive(Rule r1,Rule r2) {
	// Building a map between quantifier variables
	if (r1.numQuantifiers()!=r2.numQuantifiers())
	    return false;
	Set usedDescriptors=new HashSet();
	Hashtable varmap=new Hashtable();

    outerloop:
	for(int i=0;i<r1.numQuantifiers();i++) {
	    Quantifier q1=r1.getQuantifier(i);
	    if (!(q1 instanceof SetQuantifier))
		return false;
	    if (usedDescriptors.contains(((SetQuantifier)q1).getSet()))
		return false;
	    usedDescriptors.add(((SetQuantifier)q1).getSet());
	    for(int j=0;j<r2.numQuantifiers();j++) {
		Quantifier q2=r2.getQuantifier(j);
		if (!(q2 instanceof SetQuantifier))
		    return false;
		if (((SetQuantifier)q1).getSet()==((SetQuantifier)q2).getSet()) {
		    varmap.put(((SetQuantifier)q1).getVar(),((SetQuantifier)q2).getVar());
		    continue outerloop;
		}
	    }
	    return false;
	}
	DNFRule dr1=r1.getDNFGuardExpr();
	DNFRule dr2=r2.getDNFGuardExpr();
	for(int i=0;i<dr1.size();i++) {
	    for(int j=0;j<dr2.size();j++) {
		RuleConjunction rc1=dr1.get(i);
		RuleConjunction rc2=dr2.get(j);
		if (!exclusive(varmap,rc1,rc2))
		    return false;
	    }
	}
	return true;
    }
    
    private static boolean exclusive(Hashtable varmap, RuleConjunction rc1, RuleConjunction rc2) {
	for (int i=0;i<rc1.size();i++) {
	    for (int j=0;j<rc2.size();j++) {
		DNFExpr de1=rc1.get(i);
		DNFExpr de2=rc2.get(j);
		if ((de1.getNegation()!=de2.getNegation())&&
		    (de1.getExpr().equals(varmap,de2.getExpr())))
		    return true;
	    }
	}
	return false;
    }
}
class MaxSizeObject {
    int maxstarts;
    int numberchains;
    Rule chainrule;
    
    public MaxSizeObject(int start,int chain, Rule r) {
	maxstarts=start;
	numberchains=chain;
	chainrule=r;
    }
}
