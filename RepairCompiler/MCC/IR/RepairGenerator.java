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
    static VarDescriptor oldmodel=null;
    static VarDescriptor newmodel=null;
    static VarDescriptor worklist=null;
    static VarDescriptor repairtable=null;
    static VarDescriptor goodflag=null;
    Rule currentrule=null;
    Hashtable updatenames;
    HashSet usedupdates;
    Termination termination;
    Set removed;

    public RepairGenerator(State state, Termination t) {
        this.state = state;
	updatenames=new Hashtable();
	usedupdates=new HashSet();
	termination=t;
	removed=t.removedset;
	Repair.repairgenerator=this;
    }

    private void name_updates() {
	int count=0;
	for(Iterator it=termination.updatenodes.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode) it.next();
	    TermNode tn=(TermNode) gn.getOwner();
	    MultUpdateNode mun=tn.getUpdate();
	    for (int i=0;i<mun.numUpdates();i++) {
		UpdateNode un=mun.getUpdate(i);
		String name="update"+String.valueOf(count++);
		updatenames.put(un,name);
	    }
	}
    }

    public void generate(OutputStream outputrepair, OutputStream outputaux,OutputStream outputhead, String st) {
        this.outputrepair = new java.io.PrintWriter(outputrepair, true); 
        this.outputaux = new java.io.PrintWriter(outputaux, true); 
        this.outputhead = new java.io.PrintWriter(outputhead, true); 
        headername=st;
	name_updates();

        generate_tokentable();
        generate_hashtables();
	generate_stateobject();
	generate_call();
	generate_worklist();
        generate_rules();/*
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
	cr.outputline(vdstate.getSafeSymbol()+"->doanalysis();");
	globals=state.stGlobals.descriptors();
	while (globals.hasNext()) {
	    VarDescriptor vd=(VarDescriptor) globals.next();
	    cr.outputline("*(("+vd.getType().getGenerateType().getSafeSymbol()+"*) &"+vd.getSafeSymbol()+")="+vdstate.getSafeSymbol()+"->"+vd.getSafeSymbol()+";");
	}
	cr.outputline("delete "+vdstate.getSafeSymbol()+";");
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
            craux.outputline("delete "+set.getSafeSymbol() + "_hash;");
        } 
        
        /* destroy relations */
        relations = state.stRelations.descriptors();
        
        /* first pass create all the hash tables */
        while (relations.hasNext()) {
            RelationDescriptor relation = (RelationDescriptor) relations.next();
            
            if (relation.testUsage(RelationDescriptor.IMAGE)) {
                craux.outputline("delete "+relation.getSafeSymbol() + "_hash;");
            }

            if (relation.testUsage(RelationDescriptor.INVIMAGE)) {
                craux.outputline("delete " + relation.getSafeSymbol() + ";");
            } 
        }
        craux.outputline("}");
    }

    private void generate_worklist() {
        CodeWriter crhead = new StandardCodeWriter(outputhead);
        CodeWriter craux = new StandardCodeWriter(outputaux);
	oldmodel=VarDescriptor.makeNew("oldmodel");
	newmodel=VarDescriptor.makeNew("newmodel");
	worklist=VarDescriptor.makeNew("worklist");
	goodflag=VarDescriptor.makeNew("goodflag");
	repairtable=VarDescriptor.makeNew("repairtable");
	crhead.outputline("void doanalysis();");
	craux.outputline("void "+name +"_state::doanalysis() {");
	craux.outputline(name+ " * "+oldmodel.getSafeSymbol()+"=0;");
        craux.outputline("WorkList * "+worklist.getSafeSymbol()+" = new WorkList();");
	craux.outputline("RepairHash * "+repairtable.getSafeSymbol()+"=0;");
	craux.outputline("while (1) {");
	craux.outputline("int "+goodflag.getSafeSymbol()+"=1;");
	craux.outputline(name+ " * "+newmodel.getSafeSymbol()+"=new "+name+"();");
    }
    
    private void generate_teardown() {
	CodeWriter cr = new StandardCodeWriter(outputaux);        
	cr.outputline("delete "+worklist.getSafeSymbol()+";");
    }

    private void generate_rules() {
	/* first we must sort the rules */
        Iterator allrules = state.vRules.iterator();
        Vector emptyrules = new Vector(); // rules with no quantifiers
        Vector worklistrules = new Vector(); // the rest of the rules
	RelationDescriptor.prefix = newmodel.getSafeSymbol()+"->";
	SetDescriptor.prefix = newmodel.getSafeSymbol()+"->";

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
                CodeWriter cr = new StandardCodeWriter(outputaux) {
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
		currentrule=rule;
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

        CodeWriter cr2 = new StandardCodeWriter(outputaux);        

        cr2.outputline("while ("+goodflag.getSafeSymbol()+"&&"+worklist.getSafeSymbol()+"->hasMoreElements())");
        cr2.startblock();
	VarDescriptor idvar=VarDescriptor.makeNew("id");
        cr2.outputline("int "+idvar.getSafeSymbol()+"="+worklist.getSafeSymbol()+"->getid();");
        
        String elseladder = "if";

        Iterator iterator_rules = worklistrules.iterator();
        while (iterator_rules.hasNext()) {

            Rule rule = (Rule) iterator_rules.next();
            int dispatchid = rule.getNum();

            {
                final SymbolTable st = rule.getSymbolTable();
                CodeWriter cr = new StandardCodeWriter(outputaux) {
                        public SymbolTable getSymbolTable() { return st; }
                    };

                cr.indent();
                cr.outputline(elseladder + " ("+idvar.getSafeSymbol()+" == " + dispatchid + ")");
                cr.startblock();
		VarDescriptor typevar=VarDescriptor.makeNew("type");
		VarDescriptor leftvar=VarDescriptor.makeNew("left");
		VarDescriptor rightvar=VarDescriptor.makeNew("right");
		cr.outputline("int "+typevar.getSafeSymbol()+"="+worklist.getSafeSymbol()+"->gettype();");
		cr.outputline("int "+leftvar.getSafeSymbol()+"="+worklist.getSafeSymbol()+"->getlvalue();");
		cr.outputline("int "+rightvar.getSafeSymbol()+"="+worklist.getSafeSymbol()+"->getrvalue();");
                cr.outputline("// build " + rule.getLabel());


		for (int j=0;j<rule.numQuantifiers();j++) {
		    Quantifier quantifier = rule.getQuantifier(j);
		    quantifier.generate_open(cr, typevar.getSafeSymbol(),j,leftvar.getSafeSymbol(),rightvar.getSafeSymbol());
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
		currentrule=rule;
                rule.getInclusion().generate(cr);
                cr.endblock();

		for (int j=0;j<rule.numQuantifiers();j++) {
		    cr.endblock();
		}

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
                
                CodeWriter cr = new StandardCodeWriter(outputaux) {
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
        outputaux.println("// if (__Success) { printf(\"all tests passed\"); }");
    }    



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

    private boolean need_compensation(Rule r) {
	GraphNode gn=(GraphNode)termination.scopefalsify.get(r);
	for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
	    GraphNode.Edge edge=(GraphNode.Edge)edgeit.next();
	    GraphNode gn2=edge.getTarget();
	    if (!removed.contains(gn2)) {
		TermNode tn2=(TermNode)gn2.getOwner();
		if (tn2.getType()==TermNode.CONSEQUENCE)
		    return false;
	    }
	}
	return true;
    }

    private UpdateNode find_compensation(Rule r) {
	GraphNode gn=(GraphNode)termination.scopefalsify.get(r);
	for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
	    GraphNode.Edge edge=(GraphNode.Edge)edgeit.next();
	    GraphNode gn2=edge.getTarget();
	    if (!removed.contains(gn2)) {
		TermNode tn2=(TermNode)gn2.getOwner();
		if (tn2.getType()==TermNode.UPDATE) {
		    MultUpdateNode mun=tn2.getUpdate();
		    for(int i=0;i<mun.numUpdates();i++) {
			UpdateNode un=mun.getUpdate(i);
			if (un.getRule()==r)
			    return un;
		    }
		}
	    }
	}
	throw new Error("No Compensation Update could be found");
    }

    public void generate_dispatch(CodeWriter cr, RelationDescriptor rd, String leftvar, String rightvar) {
	boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
	boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);

	if (!(usageinvimage||usageimage)) /* not used at all*/
	    return;

        cr.outputline("// RELATION DISPATCH ");
	cr.outputline("if ("+oldmodel.getSafeSymbol()+"&&");
	if (usageimage)
	    cr.outputline("!"+oldmodel.getSafeSymbol() +"->"+rd.getJustSafeSymbol()+"_hash->contains("+leftvar+","+rightvar+"))");
	else
	    cr.outputline("!"+oldmodel.getSafeSymbol() +"->"+rd.getJustSafeSymbol()+"_hashinv->contains("+rightvar+","+leftvar+"))");
	cr.startblock(); {
	    /* Adding new item */
	    /* Perform safety checks */
	    cr.outputline("if ("+repairtable.getSafeSymbol()+"&&");
	    cr.outputline(repairtable.getSafeSymbol()+"->containsrelation("+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+"))");
	    cr.startblock(); {
		/* Have update to call into */
		VarDescriptor funptr=VarDescriptor.makeNew("updateptr");
		String parttype="";
		for(int i=0;i<currentrule.numQuantifiers();i++) {
		    if (currentrule.getQuantifier(i) instanceof RelationQuantifier)
			parttype=parttype+", int, int";
		    else
			parttype=parttype+", int";
		}
		cr.outputline("void (*"+funptr.getSafeSymbol()+") ("+name+"_state *,"+name+"*,RepairHash *"+parttype+")=");
		cr.outputline("(void (*) ("+name+"_state *,"+name+"*,RepairHash *"+parttype+")) "+repairtable.getSafeSymbol()+"->getrelation("+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+");");
		String methodcall="("+funptr.getSafeSymbol()+") (this,"+oldmodel.getSafeSymbol()+","+repairtable.getSafeSymbol();
		for(int i=0;i<currentrule.numQuantifiers();i++) {
		    Quantifier q=currentrule.getQuantifier(i);
		    if (q instanceof SetQuantifier) {
			SetQuantifier sq=(SetQuantifier) q;
			methodcall+=","+sq.getVar().getSafeSymbol();
		    } else if (q instanceof RelationQuantifier) {
			RelationQuantifier rq=(RelationQuantifier) q;
			methodcall+=","+rq.x.getSafeSymbol();
			methodcall+=","+rq.y.getSafeSymbol();
		    } else if (q instanceof ForQuantifier) {
			ForQuantifier fq=(ForQuantifier) q;
			methodcall+=","+fq.getVar().getSafeSymbol();
		    }
		}
		methodcall+=");";
		cr.outputline(methodcall);
		cr.outputline(goodflag.getSafeSymbol()+"=0;");
		cr.outputline("continue;");
	    }
	    cr.endblock();
	    /* Build standard compensation actions */
	    if (need_compensation(currentrule)) {
		UpdateNode un=find_compensation(currentrule);
		String name=(String)updatenames.get(un);
		usedupdates.add(un); /* Mark as used */
		String methodcall=name+"(this,"+oldmodel.getSafeSymbol()+","+repairtable.getSafeSymbol();
		for(int i=0;i<currentrule.numQuantifiers();i++) {
		    Quantifier q=currentrule.getQuantifier(i);
		    if (q instanceof SetQuantifier) {
			SetQuantifier sq=(SetQuantifier) q;
			methodcall+=","+sq.getVar().getSafeSymbol();
		    } else if (q instanceof RelationQuantifier) {
			RelationQuantifier rq=(RelationQuantifier) q;
			methodcall+=","+rq.x.getSafeSymbol();
			methodcall+=","+rq.y.getSafeSymbol();
		    } else if (q instanceof ForQuantifier) {
			ForQuantifier fq=(ForQuantifier) q;
			methodcall+=","+fq.getVar().getSafeSymbol();
		    }
		}
		methodcall+=");";
		cr.outputline(methodcall);
		cr.outputline(goodflag.getSafeSymbol()+"=0;");
		cr.outputline("continue;");
	    }
	}
	cr.endblock();

        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
	cr.outputline("int " + addeditem + ";");
	if (rd.testUsage(RelationDescriptor.IMAGE)) {
	    cr.outputline(addeditem + " = " + rd.getSafeSymbol() + "_hash->add((int)" + leftvar + ", (int)" + rightvar + ");");
	}
	
	if (rd.testUsage(RelationDescriptor.INVIMAGE)) {
	    cr.outputline(addeditem + " = " + rd.getSafeSymbol() + "_hashinv->add((int)" + rightvar + ", (int)" + leftvar + ");");
	}
	
	cr.outputline("if (" + addeditem + ")");
	cr.startblock();

        Vector dispatchrules = getrulelist(rd);
        
        if (dispatchrules.size() == 0) {
            cr.outputline("// nothing to dispatch");
	    cr.endblock();
            return;
        }
       
        for(int i = 0; i < dispatchrules.size(); i++) {
            Rule rule = (Rule) dispatchrules.elementAt(i);
	    if (rule.getGuardExpr().getRequiredDescriptors().contains(rd)) {
		/* Guard depends on this relation, so we recomput everything */
		cr.outputline(worklist.getSafeSymbol()+"->add("+rule.getNum()+",-1,0,0);");
	    } else {
		for (int j=0;j<rule.numQuantifiers();j++) {
		    Quantifier q=rule.getQuantifier(j);
		    if (q.getRequiredDescriptors().contains(rd)) {
			/* Generate add */
			cr.outputline(worklist.getSafeSymbol()+"->add("+rule.getNum()+","+j+","+leftvar+","+rightvar+");");
		    }
		}
	    }
        }
	cr.endblock();
    }


    public void generate_dispatch(CodeWriter cr, SetDescriptor sd, String setvar) {
               
        cr.outputline("// SET DISPATCH ");

	cr.outputline("if ("+oldmodel.getSafeSymbol()+"&&");
	cr.outputline("!"+oldmodel.getSafeSymbol() +"->"+sd.getJustSafeSymbol()+"_hash->contains("+setvar+"))");
	cr.startblock(); {
	    /* Adding new item */
	    /* Perform safety checks */
	    cr.outputline("if ("+repairtable.getSafeSymbol()+"&&");
	    cr.outputline(repairtable.getSafeSymbol()+"->containsset("+sd.getNum()+","+currentrule.getNum()+","+setvar+"))");
	    cr.startblock(); {
		/* Have update to call into */
		VarDescriptor funptr=VarDescriptor.makeNew("updateptr");
		String parttype="";
		for(int i=0;i<currentrule.numQuantifiers();i++) {
		    if (currentrule.getQuantifier(i) instanceof RelationQuantifier)
			parttype=parttype+", int, int";
		    else
			parttype=parttype+", int";
		}
		cr.outputline("void (*"+funptr.getSafeSymbol()+") ("+name+"_state *,"+name+"*,RepairHash *"+parttype+")=");
		cr.outputline("(void (*) ("+name+"_state *,"+name+"*,RepairHash *"+parttype+")) "+repairtable.getSafeSymbol()+"->getset("+sd.getNum()+","+currentrule.getNum()+","+setvar+");");
		String methodcall="("+funptr.getSafeSymbol()+") (this,"+oldmodel.getSafeSymbol()+","+
			      repairtable.getSafeSymbol();
		for(int i=0;i<currentrule.numQuantifiers();i++) {
		    Quantifier q=currentrule.getQuantifier(i);
		    if (q instanceof SetQuantifier) {
			SetQuantifier sq=(SetQuantifier) q;
			methodcall+=","+sq.getVar().getSafeSymbol();
		    } else if (q instanceof RelationQuantifier) {
			RelationQuantifier rq=(RelationQuantifier) q;
			methodcall+=","+rq.x.getSafeSymbol();
			methodcall+=","+rq.y.getSafeSymbol();
		    } else if (q instanceof ForQuantifier) {
			ForQuantifier fq=(ForQuantifier) q;
			methodcall+=","+fq.getVar().getSafeSymbol();
		    }
		}
		methodcall+=");";
		cr.outputline(methodcall);
		cr.outputline(goodflag.getSafeSymbol()+"=0;");
		cr.outputline("continue;");
	    }
	    cr.endblock();
	    /* Build standard compensation actions */
	    if (need_compensation(currentrule)) {
		UpdateNode un=find_compensation(currentrule);
		String name=(String)updatenames.get(un);
		usedupdates.add(un); /* Mark as used */

		String methodcall=name+"(this,"+oldmodel.getSafeSymbol()+","+
			      repairtable.getSafeSymbol();
		for(int i=0;i<currentrule.numQuantifiers();i++) {
		    Quantifier q=currentrule.getQuantifier(i);
		    if (q instanceof SetQuantifier) {
			SetQuantifier sq=(SetQuantifier) q;
			methodcall+=","+sq.getVar().getSafeSymbol();
		    } else if (q instanceof RelationQuantifier) {
			RelationQuantifier rq=(RelationQuantifier) q;
			methodcall+=","+rq.x.getSafeSymbol();
			methodcall+=","+rq.y.getSafeSymbol();
		    } else if (q instanceof ForQuantifier) {
			ForQuantifier fq=(ForQuantifier) q;
			methodcall+=","+fq.getVar().getSafeSymbol();
		    }
		}
		methodcall+=");";
		cr.outputline(methodcall);
		cr.outputline(goodflag.getSafeSymbol()+"=0;");
		cr.outputline("continue;");
	    }
	}
	cr.endblock();

        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
	cr.outputline("int " + addeditem + " = 1;");
	cr.outputline(addeditem + " = " + sd.getSafeSymbol() + "_hash->add((int)" + setvar +  ", (int)" + setvar + ");");
	cr.startblock();
        Vector dispatchrules = getrulelist(sd);

        if (dispatchrules.size() == 0) {
            cr.outputline("// nothing to dispatch");
	    cr.endblock();
            return;
        }

        for(int i = 0; i < dispatchrules.size(); i++) {
            Rule rule = (Rule) dispatchrules.elementAt(i);
	    if (SetDescriptor.expand(rule.getGuardExpr().getRequiredDescriptors()).contains(sd)) {
		/* Guard depends on this relation, so we recompute everything */
		cr.outputline(worklist.getSafeSymbol()+"->add("+rule.getNum()+",-1,0,0);");
	    } else {
		for (int j=0;j<rule.numQuantifiers();j++) {
		    Quantifier q=rule.getQuantifier(j);
		    if (SetDescriptor.expand(q.getRequiredDescriptors()).contains(sd)) {
			/* Generate add */
			cr.outputline(worklist.getSafeSymbol()+"->add("+rule.getNum()+","+j+","+setvar+",0);");
		    }
		}
	    }
	}
	cr.endblock();
    }

}



