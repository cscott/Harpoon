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

        generate_hashtables();
        generate_rules();
        generate_implicit_checks();
        generate_checks();

    }

    private void generate_hashtables() {

        CodeWriter cr = new CodeWriter() {
                
                int indent = 0;
                public void indent() { indent++; }
                public void unindent() { indent--; assert indent >= 0; }
                private void doindent() {
                    for (int i = 0; i < indent; i++) { 
                        output.print("  ");
                    }
                }
                public void outputline(String s) {
                    doindent();
                    output.println(s);
                }                                                             
                public void output(String s) { throw new IRException(); }
                public SymbolTable getSymbolTable() { throw new IRException(); }
            };
        
        cr.outputline("// creating hashtables ");
        
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
            cr.outputline("SimpleHash* " + relation.getSafeSymbol() + "_hash = new SimpleHash();");
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

            {

                final SymbolTable st = rule.getSymbolTable();
                
                CodeWriter cr = new CodeWriter() {
                        boolean linestarted = false;
                        int indent = 0;
                        public void indent() { indent++; }
                        public void unindent() { indent--; assert indent >= 0; }
                        private void doindent() {
                            for (int i = 0; i < indent; i++) { 
                                output.print("  ");
                            }
                            linestarted = true;
                        }
                        public void outputline(String s) {
                            if (!linestarted) {
                                doindent();
                            }
                            output.println(s);
                            linestarted = false;
                        }                 
                        public void output(String s) {
                            if (!linestarted) {
                                doindent();
                            }
                            output.print(s);
                            output.flush(); 
                        }
                        public SymbolTable getSymbolTable() { return st; }
                    };
                
                cr.outputline("// build " + rule.getLabel());
                cr.outputline("{");
                cr.indent();

                ListIterator quantifiers = rule.quantifiers();

                while (quantifiers.hasNext()) {
                    Quantifier quantifier = (Quantifier) quantifiers.next();                   
                    quantifier.generate_open(cr);
                }            
                        
                /* pretty print! */
                cr.output("//");
                rule.getGuardExpr().prettyPrint(cr);
                cr.outputline("");

                /* now we have to generate the guard test */
        
                VarDescriptor guardval = VarDescriptor.makeNew();
                rule.getGuardExpr().generate(cr, guardval);
                
                cr.outputline("if (" + guardval.getSafeSymbol() + ") {");

                cr.indent();

                /* now we have to generate the inclusion code */
                rule.getInclusion().generate(cr);

                cr.unindent();

                cr.outputline("}");

                while (quantifiers.hasPrevious()) {
                    Quantifier quantifier = (Quantifier) quantifiers.previous();
                    cr.unindent();                    
                    cr.outputline("}");
                }

                cr.unindent();
                cr.outputline("}");
                cr.outputline("");
                cr.outputline("");
            }
        }

    }

    private void generate_implicit_checks() {

        /* do post checks */
        //output.println("check to make sure all relations are well typed");
        //output.println("check multiplicity");

    }

    private void generate_checks() {

        /* do constraint checks */
        Vector constraints = state.vConstraints;

        for (int i = 0; i < constraints.size(); i++) {
            //output.println("check constraint " + (i + 1));
        }

        //output.println("report problems");
    }    

}
