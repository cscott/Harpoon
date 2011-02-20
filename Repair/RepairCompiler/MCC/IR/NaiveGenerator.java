package MCC.IR;

import java.io.*;
import java.util.*;
import MCC.State;

public class NaiveGenerator {

    State state;
    java.io.PrintWriter output = null;

    public NaiveGenerator(State state) {
        this.state = state;
    }

    public void generate(java.io.OutputStream output) {
        this.output = new java.io.PrintWriter(output, true);

        generate_tokentable();
        generate_hashtables();
        generate_rules();
        generate_implicit_checks();
        generate_checks();

    }

    private void generate_tokentable() {

        CodeWriter cr = new StandardCodeWriter(output);
        Iterator tokens = TokenLiteralExpr.tokens.keySet().iterator();

        cr.outputline("");
        cr.outputline("/* Token values*/");
        cr.outputline("");

        while (tokens.hasNext()) {
            Object token = tokens.next();
            cr.outputline("/* " + token.toString() + " = " + TokenLiteralExpr.tokens.get(token).toString()+"*/");
        }

        cr.outputline("");
        cr.outputline("");
    }

    private void generate_hashtables() {

        CodeWriter cr = new StandardCodeWriter(output);
        cr.outputline("int __Success = 1;\n");
        cr.outputline("/* creating hashtables */");

        /* build all the hashtables */
        Hashtable hashtables = new Hashtable();

        /* build sets */
        Iterator sets = state.stSets.descriptors();

        /* first pass create all the hash tables */
        while (sets.hasNext()) {
            SetDescriptor set = (SetDescriptor) sets.next();
            cr.outputline("SimpleHash* " + set.getSafeSymbol() + "_hash = new SimpleHash();");
        }

        /* second pass build relationships between hashtables */
        sets = state.stSets.descriptors();

        while (sets.hasNext()) {
            SetDescriptor set = (SetDescriptor) sets.next();
            Iterator subsets = set.subsets();

            while (subsets.hasNext()) {
                SetDescriptor subset = (SetDescriptor) subsets.next();
                cr.outputline(subset.getSafeSymbol() + "_hash->addParent(" + set.getSafeSymbol() + "_hash);");
            }
        }

        /* build relations */
        Iterator relations = state.stRelations.descriptors();

        /* first pass create all the hash tables */
        while (relations.hasNext()) {
            RelationDescriptor relation = (RelationDescriptor) relations.next();

            if (relation.testUsage(RelationDescriptor.IMAGE)) {
                cr.outputline("SimpleHash* " + relation.getSafeSymbol() + "_hash = new SimpleHash();");
            }

            if (relation.testUsage(RelationDescriptor.INVIMAGE)) {
                cr.outputline("SimpleHash* " + relation.getSafeSymbol() + "_hashinv = new SimpleHash();");
            }
        }

        cr.outputline("");
        cr.outputline("");
    }

    private void generate_rules() {

        /* first we must sort the rules */
        GraphNode.DFS.depthFirstSearch(state.rulenodes.values());

        TreeSet topologicalsort = new TreeSet(new Comparator() {
                public boolean equals(Object obj) { return false; }
                public int compare(Object o1, Object o2) {
                    GraphNode g1 = (GraphNode) o1;
                    GraphNode g2 = (GraphNode) o2;
                    return g2.getFinishingTime() - g1.getFinishingTime();
                }
            });

        topologicalsort.addAll(state.rulenodes.values());

        /* build all the rules */
        Iterator rules = topologicalsort.iterator();

        while (rules.hasNext()) {

            GraphNode rulenode = (GraphNode) rules.next();
            Rule rule = (Rule) rulenode.getOwner();

            if (!state.vRules.contains(rule)) {
                // this is no longer a top-level rule
                continue;
            }

            {

                CodeWriter cr = new StandardCodeWriter(output);
                cr.pushSymbolTable(rule.getSymbolTable());

                cr.outputline("/* build " + rule.getLabel()+"*/");
                cr.startblock();

                ListIterator quantifiers = rule.quantifiers();

                while (quantifiers.hasNext()) {
                    Quantifier quantifier = (Quantifier) quantifiers.next();
                    quantifier.generate_open(cr);
                }

                /* pretty print! */
                cr.output("/*");
                rule.getGuardExpr().prettyPrint(cr);
                cr.outputline("*/");

                /* now we have to generate the guard test */

                VarDescriptor guardval = VarDescriptor.makeNew();
                rule.getGuardExpr().generate(cr, guardval);

                cr.outputline("if (" + guardval.getSafeSymbol() + ")");
                cr.startblock();

                /* now we have to generate the inclusion code */
                rule.getInclusion().generate(cr);
                cr.endblock();

                // close startblocks generated by DotExpr memory checks
                //DotExpr.generate_memory_endblocks(cr);

                while (quantifiers.hasPrevious()) {
                    Quantifier quantifier = (Quantifier) quantifiers.previous();
                    cr.endblock();
                }

                cr.endblock();
                cr.outputline("");
                cr.outputline("");
            }
        }
    }

    private void generate_implicit_checks() {

        /* do post checks */

        CodeWriter cr = new StandardCodeWriter(output);

        // #TBD#: these should be implicit checks added to the set of constraints
        //output.println("check multiplicity");
    }

    private void generate_checks() {

        /* do constraint checks */
        Vector constraints = state.vConstraints;

        for (int i = 0; i < constraints.size(); i++) {

            Constraint constraint = (Constraint) constraints.elementAt(i);

            {

                CodeWriter cr = new StandardCodeWriter(output);
                cr.pushSymbolTable(constraint.getSymbolTable());

                cr.outputline("/* checking " + constraint.getLabel()+"*/");
                cr.startblock();

                ListIterator quantifiers = constraint.quantifiers();

                while (quantifiers.hasNext()) {
                    Quantifier quantifier = (Quantifier) quantifiers.next();
                    quantifier.generate_open(cr);
                }

                cr.outputline("int maybe = 0;");

                /* now we have to generate the guard test */

                VarDescriptor constraintboolean = VarDescriptor.makeNew("constraintboolean");
                constraint.getLogicStatement().generate(cr, constraintboolean);

                cr.outputline("if (maybe)");
                cr.startblock();
                cr.outputline("__Success = 0;");
                cr.outputline("printf(\"maybe fail " + constraint.getNum() + ". \");");
                cr.outputline("exit(1);");
                cr.endblock();

                cr.outputline("else if (!" + constraintboolean.getSafeSymbol() + ")");
                cr.startblock();

                cr.outputline("__Success = 0;");
                cr.outputline("printf(\"fail " + constraint.getNum() + ". \");");
                cr.outputline("exit(1);");
                cr.endblock();

                while (quantifiers.hasPrevious()) {
                    Quantifier quantifier = (Quantifier) quantifiers.previous();
                    cr.endblock();
                }

                cr.endblock();
                cr.outputline("");
                cr.outputline("");
            }

        }

        output.println("/*if (__Success) { printf(\"all tests passed\"); }*/");
    }

}
