package MCC.IR;

import MCC.State;
import java.util.*;

public class WorkList {


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

        cr.outputline("/* RELATION DISPATCH */");

        Vector dispatchrules = getrulelist(rd);

        if (dispatchrules.size() == 0) {
            cr.outputline("/* nothing to dispatch*/");
            return;
        }

        for(int i = 0; i < dispatchrules.size(); i++) {
            Rule rule = (Rule) dispatchrules.elementAt(i);
            cr.outputline("need to dispatch for " + rule.getLabel());
        }

        assert false; // unsupported

    }


    public static void generate_dispatch(CodeWriter cr, SetDescriptor sd, String setvar) {

        cr.outputline("/* SET DISPATCH */");

        Vector dispatchrules = getrulelist(sd);

        if (dispatchrules.size() == 0) {
            cr.outputline("/* nothing to dispatch*/");
            return;
        }

        for(int i = 0; i < dispatchrules.size(); i++) {
            Rule rule = (Rule) dispatchrules.elementAt(i);

            ListIterator quantifiers = rule.quantifiers();
            Vector otherq = new Vector(); // quantifiers that we need to iterate over to add workitems

            cr.outputline("/* " + rule.getLabel()+"*/");
            cr.startblock();


            // #ATTN#: this may/does not handle multiple instances of the same quantifier being bound
            // solution is probabyl to iterate over all bindings

            // find quantifier that we have bound
            String boundname = null;
            int size = 4; // starts at 4 because we have to store the ID
            while (quantifiers.hasNext()) {
                Quantifier quantifier = (Quantifier) quantifiers.next();
                if (quantifier instanceof SetQuantifier) {
                    size += 4;
                    SetQuantifier sq = (SetQuantifier) quantifier;
                    if (sq.getSet() == sd) {
                        // we have found our quantifier
                        boundname = sq.getVar().getSafeSymbol();

                        break;
                    }
                } else if (quantifier instanceof RelationQuantifier) {
                    size += 8;
                } else { // ForQuantifier
                    size += 4;
                }

                otherq.addElement(quantifier);
            }

            assert boundname != null;

            // bind bound variable
            cr.outputline("int " + boundname + " = " + setvar + ";");

            // add the rest of the quantifiers and continue to calculate size
            while (quantifiers.hasNext()) {
                Quantifier quantifier = (Quantifier) quantifiers.next();
                if (quantifier instanceof RelationQuantifier) {
                    size += 8;
                } else {
                    size += 4;
                }
            }

            ListIterator otheriterator = otherq.listIterator();
            while (otheriterator.hasNext()) {
                Quantifier quantifier = (Quantifier) otheriterator.next();
                quantifier.generate_open(cr);
                // implicitly opens bracket
            }

            cr.outputline("/* dispatching to " + rule.getLabel()+"*/");
            // #TODO#:  add code to do worklist addition

            cr.outputline("WORKITEM *wi = (WORKITEM *) malloc(" + size + ");");
            cr.outputline("wi->id = " + rule.getNum() + ";");

            // reset quantifiers
            quantifiers = rule.quantifiers();

            // list quantifier so the order's match!
            int offset = 0;
            while (quantifiers.hasNext()) {
                Quantifier quantifier = (Quantifier) quantifiers.next();
                offset = quantifier.generate_workliststore(cr, offset);
            }

            // now store in worklist!
            cr.outputline("WORKLIST->add((int) wi);");

            // close all those brackets
            while (otheriterator.hasPrevious()) {
                otheriterator.previous(); // throw away
                cr.endblock();
            }

            // end rule
            cr.endblock();

        }
    }

}
