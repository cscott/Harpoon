package MCC.IR;

import MCC.State;
import java.util.*;

public class Repair {


    public static Vector getrulelist(Descriptor d) {

        Vector dispatchrules = new Vector();
        Vector rules = State.currentState.vRules;        
        
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = (Rule) rules.elementAt(i);
            Set requiredsymbols = rule.getRequiredDescriptors();
            
            // #TBD#: in general this is wrong because these descriptors may contain descriptors
            // bound in "in?" expressions which need to be dealt with in a topologically sorted
            // fashion...

            if (rule.getRequiredDescriptors().contains(d)) {
                dispatchrules.addElement(rule);
            }
        }
        return dispatchrules;
    }
    

    public static void generate_dispatch(CodeWriter cr, RelationDescriptor rd, String leftvar, String rightvar) {

        cr.outputline("// RELATION DISPATCH ");        

        Vector dispatchrules = getrulelist(rd);
        
        if (dispatchrules.size() == 0) {
            cr.outputline("// nothing to dispatch");
            return;
        }
       
        for(int i = 0; i < dispatchrules.size(); i++) {
            Rule rule = (Rule) dispatchrules.elementAt(i);
	    if (rule.getGuardExpr().getRequiredDescriptors().contains(rd)) {
		/* Guard depends on this relation, so we recomput everything */
		cr.outputline("WORKLIST->add("+rule.getNum()+",-1,0,0);");
	    } else {
		for (int j=0;j<rule.numQuantifiers();j++) {
		    Quantifier q=rule.getQuantifier(j);
		    if (q.getRequiredDescriptors().contains(rd)) {
			/* Generate add */
			cr.outputline("WORKLIST->add("+rule.getNum()+","+j+","+leftvar+","+rightvar+");");
		    }
		}
	    }
        }
    }


    public static void generate_dispatch(CodeWriter cr, SetDescriptor sd, String setvar) {
               
        cr.outputline("// SET DISPATCH ");        
 
        Vector dispatchrules = getrulelist(sd);

        if (dispatchrules.size() == 0) {
            cr.outputline("// nothing to dispatch");
            return;
        }

        for(int i = 0; i < dispatchrules.size(); i++) {
            Rule rule = (Rule) dispatchrules.elementAt(i);
	    if (SetDescriptor.expand(rule.getGuardExpr().getRequiredDescriptors()).contains(sd)) {
		/* Guard depends on this relation, so we recomput everything */
		cr.outputline("WORKLIST->add("+rule.getNum()+",-1,0,0);");
	    } else {
		for (int j=0;j<rule.numQuantifiers();j++) {
		    Quantifier q=rule.getQuantifier(j);
		    if (SetDescriptor.expand(q.getRequiredDescriptors()).contains(sd)) {
			/* Generate add */
			cr.outputline("WORKLIST->add("+rule.getNum()+","+j+","+setvar+",0);");
		    }
		}
	    }
	}
    }
}
