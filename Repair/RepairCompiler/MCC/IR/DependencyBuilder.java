package MCC.IR;

import MCC.State;
import java.util.*;

public class DependencyBuilder {

    Hashtable constraintnodes = new Hashtable(); 
    Hashtable rulenodes = new Hashtable();
    State state;

    public DependencyBuilder(State state) {
        this.state = state;
    }

    public void calculate() {
	/* reinitialize (clear) nodes */
        constraintnodes = new Hashtable();
        rulenodes = new Hashtable();

        /* load up the rules and constraints */
        Vector rules = state.vRules;        
        Vector constraints = state.vConstraints;

        /* build up graph rulenodes (not edges yet) */
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = (Rule) rules.elementAt(i);
            assert rule != null;
            assert rule.getLabel() != null;

            Inclusion inclusion = rule.getInclusion();
            Iterator targets = inclusion.getTargetDescriptors().iterator();
            String additionallabel = new String();

            /* #ATTN#: is this meant to be while, not if? */
            /* perhaps there is only one descriptor for targets */
            if (targets.hasNext()) {
                Descriptor d = (Descriptor)targets.next();
                additionallabel = "\\n" + d.getSymbol();
            } 
            
            GraphNode gn = new GraphNode(rule.getLabel(), rule.getLabel() + additionallabel, rule);
            rulenodes.put(rule.getLabel(), gn);
        } 

        /* build up graph constraintnodes (not edges yet) */
        for (int i = 0; i < constraints.size(); i++) {
            Constraint constraint = (Constraint) constraints.elementAt(i);
            assert constraint != null;
            assert constraint.getLabel() != null;
            GraphNode gn = new GraphNode(constraint.getLabel(), constraint);
            gn.setDotNodeParameters("shape=box");
            constraintnodes.put(constraint.getLabel(), gn);
        } 

        /* calculate rule->rule dependencies */        
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = (Rule) rules.elementAt(i);
            GraphNode rulenode = (GraphNode) rulenodes.get(rule.getLabel());
            Set requiredsymbols = rule.getRequiredDescriptors();
            requiredsymbols.addAll(rule.getInclusion().getRequiredDescriptors());

            for (int j = 0; j < rules.size(); j++) {

                if (j == i) {
                    continue; 
                }
                
                Rule otherrule = (Rule) rules.elementAt(j);
                Inclusion inclusion = otherrule.getInclusion();
                Iterator targets = inclusion.getTargetDescriptors().iterator();
                GraphNode otherrulenode = (GraphNode) rulenodes.get(otherrule.getLabel());

                while (targets.hasNext()) {
                    Descriptor d = (Descriptor) targets.next();

                    if (requiredsymbols.contains(d)) { /* rule->rule dependency */
                        otherrulenode.addEdge(new GraphNode.Edge(d.getSymbol(), rulenode));
                    }
                }
            }
        }

        /* build constraint->rule dependencies */
        for (int i = 0; i < constraints.size(); i++) {           
            Constraint constraint = (Constraint) constraints.elementAt(i);
            GraphNode constraintnode = (GraphNode) constraintnodes.get(constraint.getLabel());
            Set requiredsymbols = constraint.getRequiredDescriptorsFromLogicStatement();
            Set requiredquantifiers = constraint.getRequiredDescriptorsFromQuantifiers();
 
            for (int j = 0; j < rules.size(); j++) {                
                Rule otherrule = (Rule) rules.elementAt(j);
                Inclusion inclusion = otherrule.getInclusion();
                Iterator targets = inclusion.getTargetDescriptors().iterator();
                GraphNode otherrulenode = (GraphNode) rulenodes.get(otherrule.getLabel());

                while (targets.hasNext()) {
                    Descriptor d = (Descriptor) targets.next();

                    if (requiredsymbols.contains(d)) { /* logic->rule dependency */
                        GraphNode.Edge edge = new GraphNode.Edge(d.getSymbol(), constraintnode);
                        //edge.setDotNodeParameters("style=bold");
                        otherrulenode.addEdge(edge);
                    }

                    if (requiredquantifiers.contains(d)) { /* quantifier-> dependency */
                        GraphNode.Edge edge = new GraphNode.Edge(d.getSymbol(), constraintnode);
                        edge.setDotNodeParameters("style=dotted");
                        otherrulenode.addEdge(edge);
                    }
                }
            }
        }

        /* store results in state */
        state.rulenodes = rulenodes;
        state.constraintnodes = constraintnodes;
    }
}
