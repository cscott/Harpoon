package MCC.IR;

import java.io.*;
import java.util.*;
import MCC.State;

public class RepairGenerator {

    State state;
    java.io.PrintWriter outputrepair = null;
    java.io.PrintWriter outputaux = null;
    java.io.PrintWriter outputhead = null;
    String name="foo";
    String headername;
    public RepairGenerator(State state) {
        this.state = state;
    }

    public void generate(OutputStream outputrepair, OutputStream outputaux,OutputStream outputhead, String st) {
        this.outputrepair = new java.io.PrintWriter(outputrepair, true); 
        this.outputaux = new java.io.PrintWriter(outputaux, true); 
        this.outputhead = new java.io.PrintWriter(outputhead, true); 
        headername=st;

        generate_tokentable();
        generate_hashtables();
	generate_stateobject();
	generate_call();
	/*        generate_worklist();
        generate_rules();
        generate_checks();
        generate_teardown();*/

    }

    private void generate_call() {
        CodeWriter cr = new StandardCodeWriter(outputrepair);        
	VarDescriptor vdstate=VarDescriptor.makeNew("repairstate");
	cr.outputline(name+"_state * "+vdstate.getSafeSymbol()+"=new "+name+"_state();");
	

	Iterator globals=state.stGlobals.descriptors();
	while (globals.hasNext()) {
	    VarDescriptor vd=(VarDescriptor) globals.next();
	    cr.outputline(vdstate.getSafeSymbol()+"->"+vd.getSafeSymbol()+"=("+vd.getType().getGenerateType().getSafeSymbol()+")"+vd.getSafeSymbol()+";");
	}
	/* Insert repair here */

	globals=state.stGlobals.descriptors();
	while (globals.hasNext()) {
	    VarDescriptor vd=(VarDescriptor) globals.next();
	    cr.outputline("*(("+vd.getType().getGenerateType().getSafeSymbol()+"*) &"+vd.getSafeSymbol()+")="+vdstate.getSafeSymbol()+"->"+vd.getSafeSymbol()+";");
	}
	
	cr.outputline("delete ("+vdstate.getSafeSymbol()+");");
    }
    private void generate_tokentable() {

        CodeWriter cr = new StandardCodeWriter(outputrepair);        
        Iterator tokens = TokenLiteralExpr.tokens.keySet().iterator();        

        cr.outputline("");
        cr.outputline("// Token values");
        cr.outputline("");

        while (tokens.hasNext()) {
            Object token = tokens.next();
            cr.outputline("// " + token.toString() + " = " + TokenLiteralExpr.tokens.get(token).toString());            
        }

        cr.outputline("");
        cr.outputline("");
    }

    private void generate_stateobject() {
        CodeWriter crhead = new StandardCodeWriter(outputhead);
	crhead.outputline("class "+name+"_state {");
	crhead.outputline("public:");
	Iterator globals=state.stGlobals.descriptors();
	while (globals.hasNext()) {
	    VarDescriptor vd=(VarDescriptor) globals.next();
	    crhead.outputline(vd.getType().getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+";");
	}
    }

    private void generate_hashtables() {
        CodeWriter craux = new StandardCodeWriter(outputaux);
        CodeWriter crhead = new StandardCodeWriter(outputhead);
        crhead.outputline("#include \"SimpleHash.h\"");
	crhead.outputline("class "+name+" {");
	crhead.outputline("public:");
	crhead.outputline(name+"();");
	crhead.outputline("~"+name+"();");
        craux.outputline("#include \""+headername+"\"");

        craux.outputline(name+"::"+name+"() {");
        craux.outputline("// creating hashtables ");
        
        /* build sets */
        Iterator sets = state.stSets.descriptors();
        
        /* first pass create all the hash tables */
        while (sets.hasNext()) {
            SetDescriptor set = (SetDescriptor) sets.next();
	    crhead.outputline("SimpleHash* " + set.getSafeSymbol() + "_hash;");
            craux.outputline(set.getSafeSymbol() + "_hash = new SimpleHash();");
        } 
        
        /* second pass build relationships between hashtables */
        sets = state.stSets.descriptors();
        
        while (sets.hasNext()) {
            SetDescriptor set = (SetDescriptor) sets.next();
            Iterator subsets = set.subsets();
            
            while (subsets.hasNext()) {
                SetDescriptor subset = (SetDescriptor) subsets.next();                
                craux.outputline(subset.getSafeSymbol() + "_hash->addParent(" + set.getSafeSymbol() + "_hash);");
            }
        } 

        /* build relations */
        Iterator relations = state.stRelations.descriptors();
        
        /* first pass create all the hash tables */
        while (relations.hasNext()) {
            RelationDescriptor relation = (RelationDescriptor) relations.next();
            
            if (relation.testUsage(RelationDescriptor.IMAGE)) {
                crhead.outputline("SimpleHash* " + relation.getSafeSymbol() + "_hash;");
                craux.outputline(relation.getSafeSymbol() + "_hash = new SimpleHash();");
            }

            if (relation.testUsage(RelationDescriptor.INVIMAGE)) {
                crhead.outputline("SimpleHash* " + relation.getSafeSymbol() + "_hashinv;");
                craux.outputline(relation.getSafeSymbol() + "_hashinv = new SimpleHash();");
            } 
        }

        craux.outputline("}");
        crhead.outputline("};");
        craux.outputline(name+"::~"+name+"() {");
        craux.outputline("// deleting hashtables");

        /* build destructor */
        sets = state.stSets.descriptors();
        
        /* first pass create all the hash tables */
        while (sets.hasNext()) {
            SetDescriptor set = (SetDescriptor) sets.next();
            craux.outputline("delete("+set.getSafeSymbol() + "_hash);");
        } 
        
        /* destroy relations */
        relations = state.stRelations.descriptors();
        
        /* first pass create all the hash tables */
        while (relations.hasNext()) {
            RelationDescriptor relation = (RelationDescriptor) relations.next();
            
            if (relation.testUsage(RelationDescriptor.IMAGE)) {
                craux.outputline("delete("+relation.getSafeSymbol() + "_hash);");
            }

            if (relation.testUsage(RelationDescriptor.INVIMAGE)) {
                craux.outputline("delete(" + relation.getSafeSymbol() + ");");
            } 
        }
        craux.outputline("}");
    }

    private void generate_worklist() {
        CodeWriter cr = new StandardCodeWriter(outputrepair);
        cr.outputline("WORKLIST = new WorkList();");
    }
    
    private void generate_teardown() {
        CodeWriter cr = new StandardCodeWriter(outputrepair);        
        cr.outputline("delete WORKLIST;");
    }

    private void generate_rules() {
        
        /* first we must sort the rules */
        Iterator allrules = state.vRules.iterator();

        Vector emptyrules = new Vector(); // rules with no quantifiers
        Vector worklistrules = new Vector(); // the rest of the rules

        while (allrules.hasNext()) {
            Rule rule = (Rule) allrules.next();
            ListIterator quantifiers = rule.quantifiers();

            boolean noquantifiers = true;
            while (quantifiers.hasNext()) {
                Quantifier quantifier = (Quantifier) quantifiers.next();
                if (quantifier instanceof ForQuantifier) {
                    // ok, because integers exist already!
                } else {
                    // real quantifier
                    noquantifiers = false;
                    break;
                }
            }

            if (noquantifiers) {
                emptyrules.add(rule);
            } else {
                worklistrules.add(rule);
            }
        }
       
        Iterator iterator_er = emptyrules.iterator();
        while (iterator_er.hasNext()) {

            Rule rule = (Rule) iterator_er.next();            

            {
                final SymbolTable st = rule.getSymbolTable();                
                CodeWriter cr = new StandardCodeWriter(outputrepair) {
                        public SymbolTable getSymbolTable() { return st; }
                    };
                
                cr.outputline("// build " + rule.getLabel());
                cr.startblock();

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
                
                cr.outputline("if (" + guardval.getSafeSymbol() + ")");
                cr.startblock();

                /* now we have to generate the inclusion code */
                rule.getInclusion().generate(cr);
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

        CodeWriter cr2 = new StandardCodeWriter(outputrepair);        

        cr2.outputline("WORKLIST->reset();");
        cr2.outputline("while (WORKLIST->hasMoreElements())");
        cr2.startblock();
        cr2.outputline("WORKITEM *wi = (WORKITEM *) WORKLIST->nextElement();");
        
        String elseladder = "if";

        Iterator iterator_rules = worklistrules.iterator();
        while (iterator_rules.hasNext()) {
            
            Rule rule = (Rule) iterator_rules.next();            
            int dispatchid = rule.getNum();

            {
                final SymbolTable st = rule.getSymbolTable();                
                CodeWriter cr = new StandardCodeWriter(outputrepair) {
                        public SymbolTable getSymbolTable() { return st; }
                    };

                cr.indent();
                cr.outputline(elseladder + " (wi->id == " + dispatchid + ")");
                cr.startblock();

                cr.outputline("// build " + rule.getLabel());

                ListIterator quantifiers = rule.quantifiers();

                int count = 0;
                while (quantifiers.hasNext()) {
                    Quantifier quantifier = (Quantifier) quantifiers.next();
                    count = quantifier.generate_worklistload(cr, count );                    
                }
                        
                /* pretty print! */
                cr.output("//");
                rule.getGuardExpr().prettyPrint(cr);
                cr.outputline("");

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

                cr.endblock(); // end else-if WORKLIST ladder

                elseladder = "else if";
            }
        }

        cr2.outputline("else");
        cr2.startblock();
        cr2.outputline("printf(\"VERY BAD !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\\n\\n\");");
        cr2.outputline("exit(1);");
        cr2.endblock();

        // end block created for worklist
        cr2.endblock();

    }

    private void generate_checks() {

        /* do constraint checks */
        Vector constraints = state.vConstraints;

        for (int i = 0; i < constraints.size(); i++) {

            Constraint constraint = (Constraint) constraints.elementAt(i); 

            {

                final SymbolTable st = constraint.getSymbolTable();
                
                CodeWriter cr = new StandardCodeWriter(outputrepair) {
                        public SymbolTable getSymbolTable() { return st; }
                    };
                
                cr.outputline("// checking " + constraint.getLabel());
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
                cr.outputline("printf(\"maybe fail " + (i+1) + ". \");");
                cr.outputline("exit(1);");
                cr.endblock();

                cr.outputline("else if (!" + constraintboolean.getSafeSymbol() + ")");
                cr.startblock();

                cr.outputline("__Success = 0;");
                cr.outputline("printf(\"fail " + (i+1) + ". \");");
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

        outputrepair.println("// if (__Success) { printf(\"all tests passed\"); }");
    }    

}



