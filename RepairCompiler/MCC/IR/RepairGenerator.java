package MCC.IR;

import java.io.*;
import java.util.*;
import MCC.State;
import MCC.Compiler;

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
    HashSet togenerate;
    static boolean DEBUG=false;
    Cost cost;
    ModelRuleDependence mrd;

    public RepairGenerator(State state, Termination t) {
        this.state = state;
	updatenames=new Hashtable();
	usedupdates=new HashSet();
	termination=t;
	removed=t.removedset;
	togenerate=new HashSet();
        togenerate.addAll(termination.conjunctions);
	if (Compiler.REPAIR)
	    togenerate.removeAll(removed);
        GraphNode.computeclosure(togenerate,removed);
	cost=new Cost();
	mrd=ModelRuleDependence.doAnalysis(state);
	Repair.repairgenerator=this;
    }

    private void generatetypechecks(boolean flag) {
	if (flag) {
	    DotExpr.DOTYPECHECKS=true;
	    VarExpr.DOTYPECHECKS=true;
	    DotExpr.DONULL=true;
	    VarExpr.DONULL=true;
	} else {
	    VarExpr.DOTYPECHECKS=false;
	    DotExpr.DOTYPECHECKS=false;
	    VarExpr.DONULL=true;
	    DotExpr.DONULL=true;
	}
    }


    private void name_updates() {
	int count=0;
	for(Iterator it=termination.updatenodes.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode) it.next();
	    TermNode tn=(TermNode) gn.getOwner();
	    MultUpdateNode mun=tn.getUpdate();
	    if (togenerate.contains(gn))
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
	generatetypechecks(true);
        generate_tokentable();
        generate_hashtables();
	generate_stateobject();
	generate_call();
	generate_start();
        generate_rules();
	if (!Compiler.REPAIR||Compiler.GENERATEDEBUGPRINT) {
	    generate_print();
	}
        generate_checks();
        generate_teardown();
	CodeWriter crhead = new StandardCodeWriter(this.outputhead);
	CodeWriter craux = new StandardCodeWriter(this.outputaux);
	crhead.outputline("};");
	craux.outputline("}");

	if (Compiler.GENERATEDEBUGHOOKS) {
	    crhead.outputline("void debughook();");
	    craux.outputline("void debughook() {}");
	}
	generatetypechecks(false);
	generate_computesizes();
	generatetypechecks(true);
	generate_recomputesizes();
	generatetypechecks(false);
	generate_updates();
	StructureGenerator sg=new StructureGenerator(state,this);
	sg.buildall();
	crhead.outputline("#endif");
    }

    String ststate="state";
    String stmodel="model";
    String strepairtable="repairtable";
    String stleft="left";
    String stright="right";
    String stnew="newvalue";

    private void generate_updates() {
	int count=0;
        CodeWriter crhead = new StandardCodeWriter(outputhead);        
        CodeWriter craux = new StandardCodeWriter(outputaux);        
	RelationDescriptor.prefix = "model->";
	SetDescriptor.prefix = "model->";

	/* Rewrite globals */

	for (Iterator it=this.state.stGlobals.descriptors();it.hasNext();) {
	    VarDescriptor vd=(VarDescriptor)it.next();
	    craux.outputline("#define "+vd.getSafeSymbol()+" "+ststate+"->"+vd.getSafeSymbol());
	}

	for(Iterator it=termination.updatenodes.iterator();it.hasNext();) {
	    GraphNode gn=(GraphNode) it.next();
	    TermNode tn=(TermNode) gn.getOwner();
	    MultUpdateNode mun=tn.getUpdate();
	    boolean isrelation=(mun.getDescriptor() instanceof RelationDescriptor);
	    if (togenerate.contains(gn))
	    for (int i=0;i<mun.numUpdates();i++) {
		UpdateNode un=mun.getUpdate(i);
		String methodname=(String)updatenames.get(un);
		
		switch(mun.op) {
		case MultUpdateNode.ADD:
		    if (isrelation) {
			crhead.outputline("void "+methodname+"("+name+"_state * " +ststate+","+name+" * "+stmodel+", RepairHash * "+strepairtable+", int "+stleft+", int "+stright+");");
			craux.outputline("void "+methodname+"("+name+"_state * "+ ststate+","+name+" * "+stmodel+", RepairHash * "+strepairtable+", int "+stleft+", int "+stright+")");
		    } else {
			crhead.outputline("void "+methodname+"("+name+"_state * "+ ststate+","+name+" * "+stmodel+", RepairHash * "+strepairtable+", int "+stleft+");");
			craux.outputline("void "+methodname+"("+name+"_state * "+ststate+","+name+" * "+stmodel+", RepairHash * "+strepairtable+", int "+stleft+")");
		    }
		    craux.startblock();
		    craux.outputline("int maybe=0;");
		    if (Compiler.GENERATEINSTRUMENT)
			craux.outputline("updatecount++;");

		    final SymbolTable st = un.getRule().getSymbolTable();                
		    CodeWriter cr = new StandardCodeWriter(outputaux) {
                        public SymbolTable getSymbolTable() { return st; }
                    };
		    un.generate(cr, false, false, stleft,stright, null,this);
		    craux.outputline("if (maybe) printf(\"REALLY BAD\");");
		    craux.endblock();
		    break;
		case MultUpdateNode.REMOVE: {
		    Rule r=un.getRule();
		    String methodcall="void "+methodname+"("+name+"_state * "+ststate+","+name+" * "+stmodel+", RepairHash * "+strepairtable;
		    for(int j=0;j<r.numQuantifiers();j++) {
			Quantifier q=r.getQuantifier(j);
			if (q instanceof SetQuantifier) {
			    SetQuantifier sq=(SetQuantifier) q;
			    methodcall+=","+sq.getVar().getType().getGenerateType().getSafeSymbol()+" "+sq.getVar().getSafeSymbol();
			} else if (q instanceof RelationQuantifier) {
			    RelationQuantifier rq=(RelationQuantifier) q;
			    
			    methodcall+=","+rq.x.getType().getGenerateType().getSafeSymbol()+" "+rq.x.getSafeSymbol();
			    methodcall+=","+rq.y.getType().getGenerateType().getSafeSymbol()+" "+rq.y.getSafeSymbol();
			} else if (q instanceof ForQuantifier) {
			    ForQuantifier fq=(ForQuantifier) q;
			    methodcall+=",int "+fq.getVar().getSafeSymbol();
			}
		    }
		    methodcall+=")";
		    crhead.outputline(methodcall+";");
		    craux.outputline(methodcall);
		    craux.startblock();
		    craux.outputline("int maybe=0;");
		    if (Compiler.GENERATEINSTRUMENT)
			craux.outputline("updatecount++;");
		    final SymbolTable st2 = un.getRule().getSymbolTable();
		    CodeWriter cr2 = new StandardCodeWriter(outputaux) {
                        public SymbolTable getSymbolTable() { return st2; }
                    };
		    un.generate(cr2, true, false, null,null, null,this);
		    craux.outputline("if (maybe) printf(\"REALLY BAD\");");
		    craux.endblock();
		}
		    break;
		case MultUpdateNode.MODIFY: {
		    Rule r=un.getRule();
		    String methodcall="void "+methodname+"("+name+"_state * "+ststate+","+name+" * "+stmodel+", RepairHash * "+strepairtable;
		    for(int j=0;j<r.numQuantifiers();j++) {
			Quantifier q=r.getQuantifier(j);
			if (q instanceof SetQuantifier) {
			    SetQuantifier sq=(SetQuantifier) q;
			    methodcall+=","+sq.getVar().getType().getGenerateType().getSafeSymbol()+" "+sq.getVar().getSafeSymbol();
			} else if (q instanceof RelationQuantifier) {
			    RelationQuantifier rq=(RelationQuantifier) q;
			    
			    methodcall+=", "+rq.x.getType().getGenerateType().getSafeSymbol()+" "+rq.x.getSafeSymbol();
			    methodcall+=", "+rq.y.getType().getGenerateType().getSafeSymbol()+" "+rq.y.getSafeSymbol();
			} else if (q instanceof ForQuantifier) {
			    ForQuantifier fq=(ForQuantifier) q;
			    methodcall+=", int "+fq.getVar().getSafeSymbol();
			}
		    }
		    methodcall+=", int "+stleft+", int "+stright+", int "+stnew;
		    methodcall+=")";
		    crhead.outputline(methodcall+";");
		    craux.outputline(methodcall);
		    craux.startblock();
		    craux.outputline("int maybe=0;");
		    if (Compiler.GENERATEINSTRUMENT)
			craux.outputline("updatecount++;");
		    final SymbolTable st2 = un.getRule().getSymbolTable();
		    CodeWriter cr2 = new StandardCodeWriter(outputaux) {
                        public SymbolTable getSymbolTable() { return st2; }
                    };
		    un.generate(cr2, false, true, stleft, stright, stnew, this);
		    craux.outputline("if (maybe) printf(\"REALLY BAD\");");
		    craux.endblock();
		}
		    break;

		default:
		    throw new Error("Nonimplement Update");
		}
	    }
	}
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
	crhead.outputline("void computesizes(int *,int **);");
	crhead.outputline("void recomputesizes();");
    }

    private void generate_computesizes() {
	int max=TypeDescriptor.counter;
	TypeDescriptor[] tdarray=new TypeDescriptor[max];
	for(Iterator it=state.stTypes.descriptors();it.hasNext();) {
	    TypeDescriptor ttd=(TypeDescriptor)it.next();
	    tdarray[ttd.getId()]=ttd;
	}
	final SymbolTable st = state.stGlobals;
	CodeWriter cr = new StandardCodeWriter(outputaux) {
		public SymbolTable getSymbolTable() { return st; }
	    };

	cr.outputline("void "+name+"_state::computesizes(int *sizearray,int **numele) {");
	cr.outputline("int maybe=0;");
	for(int i=0;i<max;i++) {
	    TypeDescriptor td=tdarray[i];
	    Expr size=td.getSizeExpr();
	    VarDescriptor vd=VarDescriptor.makeNew("size");
	    size.generate(cr,vd);
	    cr.outputline("sizearray["+i+"]="+vd.getSafeSymbol()+";");
	}
	for(int i=0;i<max;i++) {
	    TypeDescriptor td=tdarray[i];
	    if (td instanceof StructureTypeDescriptor) {
		StructureTypeDescriptor std=(StructureTypeDescriptor) td;
		for(int j=0;j<std.fieldlist.size();j++) {
		    FieldDescriptor fd=(FieldDescriptor)std.fieldlist.get(j);
		    if (fd instanceof ArrayDescriptor) {
			ArrayDescriptor ad=(ArrayDescriptor)fd;
			Expr index=ad.getIndexBound();
			VarDescriptor vd=VarDescriptor.makeNew("index");
			index.generate(cr,vd);
			cr.outputline("numele["+i+"]["+j+"]="+vd.getSafeSymbol()+";");
		    }
		}
	    }
	}
	cr.outputline("if (maybe) printf(\"BAD ERROR\");");
	cr.outputline("}");
    }

    private void generate_recomputesizes() {
	int max=TypeDescriptor.counter;
	TypeDescriptor[] tdarray=new TypeDescriptor[max];
	for(Iterator it=state.stTypes.descriptors();it.hasNext();) {
	    TypeDescriptor ttd=(TypeDescriptor)it.next();
	    tdarray[ttd.getId()]=ttd;
	}
	final SymbolTable st = state.stGlobals;
	CodeWriter cr = new StandardCodeWriter(outputaux) {
		public SymbolTable getSymbolTable() { return st; }
	    };
	cr.outputline("void "+name+"_state::recomputesizes() {");
	cr.outputline("int maybe=0;");
	for(int i=0;i<max;i++) {
	    TypeDescriptor td=tdarray[i];
	    Expr size=td.getSizeExpr();
	    VarDescriptor vd=VarDescriptor.makeNew("size");
	    size.generate(cr,vd);
	}
	for(int i=0;i<max;i++) {
	    TypeDescriptor td=tdarray[i];
	    if (td instanceof StructureTypeDescriptor) {
		StructureTypeDescriptor std=(StructureTypeDescriptor) td;
		for(int j=0;j<std.fieldlist.size();j++) {
		    FieldDescriptor fd=(FieldDescriptor)std.fieldlist.get(j);
		    if (fd instanceof ArrayDescriptor) {
			ArrayDescriptor ad=(ArrayDescriptor)fd;
			Expr index=ad.getIndexBound();
			VarDescriptor vd=VarDescriptor.makeNew("index");
			index.generate(cr,vd);
		    }
		}
	    }
	}
	cr.outputline("if (maybe) printf(\"BAD ERROR\");");
	cr.outputline("}");
    }


    private void generate_hashtables() {
        CodeWriter craux = new StandardCodeWriter(outputaux);
        CodeWriter crhead = new StandardCodeWriter(outputhead);
	crhead.outputline("#ifndef "+name+"_h");
	crhead.outputline("#define "+name+"_h");
        crhead.outputline("#include \"SimpleHash.h\"");
	crhead.outputline("extern \"C\" {");
        crhead.outputline("#include \"instrument.h\"");
	crhead.outputline("}");
        crhead.outputline("#include <stdio.h>");
        crhead.outputline("#include <stdlib.h>");
	crhead.outputline("class "+name+" {");
	crhead.outputline("public:");
	crhead.outputline(name+"();");
	crhead.outputline("~"+name+"();");
        craux.outputline("#include \""+headername+"\"");
        craux.outputline("#include \"size.h\"");

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
                craux.outputline("delete " + relation.getSafeSymbol() + "_hashinv;");
            } 
        }
        craux.outputline("}");
    }

    private void generate_start() {
        CodeWriter crhead = new StandardCodeWriter(outputhead);
        CodeWriter craux = new StandardCodeWriter(outputaux);
	oldmodel=VarDescriptor.makeNew("oldmodel");
	newmodel=VarDescriptor.makeNew("newmodel");
	worklist=VarDescriptor.makeNew("worklist");
	goodflag=VarDescriptor.makeNew("goodflag");
	repairtable=VarDescriptor.makeNew("repairtable");

	if (Compiler.GENERATEINSTRUMENT) {
	    craux.outputline("int updatecount;");
	    craux.outputline("int rebuildcount;");
	    craux.outputline("int abstractcount;");
	}
	
	crhead.outputline("void doanalysis();");
	craux.outputline("void "+name +"_state::doanalysis()");
  	craux.startblock();
	if (Compiler.GENERATEINSTRUMENT) {
	    craux.outputline("updatecount=0;");
	    craux.outputline("rebuildcount=0;");
	    craux.outputline("abstractcount=0;");
	}
	craux.outputline("int highmark;");
	craux.outputline("initializestack(&highmark);");
	craux.outputline("typeobject *typeobject1=gettypeobject();");
	craux.outputline("typeobject1->computesizes(this);");
	craux.outputline("recomputesizes();");
	craux.outputline(name+ " * "+oldmodel.getSafeSymbol()+"=0;");
        craux.outputline("WorkList * "+worklist.getSafeSymbol()+" = new WorkList();");
	craux.outputline("RepairHash * "+repairtable.getSafeSymbol()+"=0;");
	craux.outputline("while (1)");
	craux.startblock();
	craux.outputline(name+ " * "+newmodel.getSafeSymbol()+"=new "+name+"();");
	craux.outputline(worklist.getSafeSymbol()+"->reset();");
	if (Compiler.GENERATEINSTRUMENT)
	    craux.outputline("rebuildcount++;");
    }
    
    private void generate_teardown() {
	CodeWriter cr = new StandardCodeWriter(outputaux);        
	cr.endblock();
	if (Compiler.GENERATEINSTRUMENT) {
	    cr.outputline("printf(\"updatecount=%d\\n\",updatecount);");
	    cr.outputline("printf(\"rebuildcount=%d\\n\",rebuildcount);");
	    cr.outputline("printf(\"abstractcount=%d\\n\",abstractcount);");
	}

    }

    private void generate_print() {
	
	final SymbolTable st = new SymbolTable();

	CodeWriter cr = new StandardCodeWriter(outputaux) {
		public SymbolTable getSymbolTable() { return st; }
	    };

	cr.outputline("// printing sets!");
	cr.outputline("printf(\"\\n\\nPRINTING SETS AND RELATIONS\\n\");");

        Iterator setiterator = state.stSets.descriptors();
	while (setiterator.hasNext()) {
	    SetDescriptor sd = (SetDescriptor) setiterator.next();
	    if (sd.getSymbol().equals("int") || sd.getSymbol().equals("token")) {
		continue;
	    }

	    String setname = sd.getSafeSymbol();

	    cr.startblock();
	    cr.outputline("// printing set " + setname);
	    cr.outputline("printf(\"\\nPrinting set " + sd.getSymbol() + " - %d elements \\n\", " + setname + "_hash->count());");
	    cr.outputline("SimpleIterator __setiterator;");
	    cr.outputline("" + setname + "_hash->iterator(__setiterator);");
	    cr.outputline("while (__setiterator.hasNext())");
	    cr.startblock();
	    cr.outputline("int __setval = (int) __setiterator.next();");

	    TypeDescriptor td = sd.getType();
	    if (td instanceof StructureTypeDescriptor) {
		StructureTypeDescriptor std = (StructureTypeDescriptor) td;
		VarDescriptor vd = new VarDescriptor ("__setval", "__setval", td, false);
		std.generate_printout(cr, vd);
	    } else { // Missing type descriptor or reserved type, just print int
		cr.outputline("printf(\"<%d> \", __setval);"); 	    		
	    }


	    cr.endblock();
	    cr.endblock();
	}

	cr.outputline("printf(\"\\n\\n------------------- END PRINTING\\n\");");
    }

    Set ruleset=null;
    private void generate_rules() {
	/* first we must sort the rules */
	RelationDescriptor.prefix = newmodel.getSafeSymbol()+"->";
	SetDescriptor.prefix = newmodel.getSafeSymbol()+"->";
	System.out.println("SCC="+(mrd.numSCC()-1));
	for(int sccindex=0;sccindex<mrd.numSCC();sccindex++) {
	    ruleset=mrd.getSCC(sccindex);
	    boolean needworklist=mrd.hasCycle(sccindex);
	    
	    if (!needworklist) {
		Iterator iterator_rs = ruleset.iterator();
		while (iterator_rs.hasNext()) {
		    Rule rule = (Rule) iterator_rs.next();
		    if (rule.getnogenerate())
			continue;
		    {
			final SymbolTable st = rule.getSymbolTable();
			CodeWriter cr = new StandardCodeWriter(outputaux) {
				public SymbolTable getSymbolTable() { return st; }
			    };
			InvariantValue ivalue=new InvariantValue();
			cr.setInvariantValue(ivalue);

			cr.outputline("// build " +escape(rule.toString()));
			cr.startblock();
			cr.outputline("int maybe=0;");

			Expr ruleexpr=rule.getGuardExpr();
			HashSet invariantvars=new HashSet();
			Set invariants=ruleexpr.findInvariants(invariantvars);

			if ((ruleexpr instanceof BooleanLiteralExpr)&&
			    ((BooleanLiteralExpr)ruleexpr).getValue()) {
			    if (rule.getInclusion() instanceof SetInclusion) {
				invariants.addAll(((SetInclusion)rule.getInclusion()).getExpr().findInvariants(invariantvars));
			    } else if (rule.getInclusion() instanceof RelationInclusion) {
				invariants.addAll(((RelationInclusion)rule.getInclusion()).getLeftExpr().findInvariants(invariantvars));
				invariants.addAll(((RelationInclusion)rule.getInclusion()).getRightExpr().findInvariants(invariantvars));
			    }
			}
			ListIterator quantifiers = rule.quantifiers();			
			while (quantifiers.hasNext()) {
			    Quantifier quantifier = (Quantifier) quantifiers.next();
			    if (quantifier instanceof ForQuantifier) {
				ForQuantifier fq=(ForQuantifier)quantifier;
				invariants.addAll(fq.lower.findInvariants(invariantvars));
				invariants.addAll(fq.upper.findInvariants(invariantvars));
			    }
			}

			for(Iterator invit=invariants.iterator();invit.hasNext();) {
			    Expr invexpr=(Expr)invit.next();
			    VarDescriptor tmpvd=VarDescriptor.makeNew("tmpvar");
			    VarDescriptor maybevd=VarDescriptor.makeNew("maybevar");
			    invexpr.generate(cr,tmpvd);
			    cr.outputline("int "+maybevd.getSafeSymbol()+"=maybe;");
			    cr.outputline("maybe=0;");
			    ivalue.assignPair(invexpr,tmpvd,maybevd);
			}

			quantifiers = rule.quantifiers();
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
	    } else {
		CodeWriter cr2 = new StandardCodeWriter(outputaux);
		
		for(Iterator initialworklist=ruleset.iterator();initialworklist.hasNext();) {
		    /** Construct initial worklist set */
		    Rule rule=(Rule)initialworklist.next();
		    cr2.outputline(worklist.getSafeSymbol()+"->add("+rule.getNum()+",-1,0,0);");
		}

		cr2.outputline("while ("+worklist.getSafeSymbol()+"->hasMoreElements())");
		cr2.startblock();
		VarDescriptor idvar=VarDescriptor.makeNew("id");
		cr2.outputline("int "+idvar.getSafeSymbol()+"="+worklist.getSafeSymbol()+"->getid();");
		
		String elseladder = "if";
		
		Iterator iterator_rules = ruleset.iterator();
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
			cr.outputline("int maybe=0;");
			VarDescriptor typevar=VarDescriptor.makeNew("type");
			VarDescriptor leftvar=VarDescriptor.makeNew("left");
			VarDescriptor rightvar=VarDescriptor.makeNew("right");
			cr.outputline("int "+typevar.getSafeSymbol()+"="+worklist.getSafeSymbol()+"->gettype();");
			cr.outputline("int "+leftvar.getSafeSymbol()+"="+worklist.getSafeSymbol()+"->getlvalue();");
			cr.outputline("int "+rightvar.getSafeSymbol()+"="+worklist.getSafeSymbol()+"->getrvalue();");
			cr.outputline("// build " +escape(rule.toString()));
			
			
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
		cr2.outputline(worklist.getSafeSymbol()+"->pop();");
		cr2.endblock();
	    }
	}
    }

    public static String escape(String s) {
	String newstring="";
	for(int i=0;i<s.length();i++) {
	    char c=s.charAt(i);
	    if (c=='"')
		newstring+="\"";
	    else
		newstring+=c;
	}
	return newstring;
    }

    private void generate_checks() {

        /* do constraint checks */
	//        Vector constraints = state.vConstraints;


	//        for (int i = 0; i < constraints.size(); i++) {
	//            Constraint constraint = (Constraint) constraints.elementAt(i); 
	for (Iterator i = termination.constraintdependence.computeOrdering().iterator(); i.hasNext();) {
	    Constraint constraint = (Constraint) ((GraphNode)i.next()).getOwner();
	    
            {
		final SymbolTable st = constraint.getSymbolTable();
		CodeWriter cr = new StandardCodeWriter(outputaux);
		cr.pushSymbolTable(constraint.getSymbolTable());

		cr.outputline("// checking " + escape(constraint.toString()));
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
                cr.outputline("printf(\"maybe fail " +  escape(constraint.toString()) + ". \\n\");");
                cr.outputline("exit(1);");
                cr.endblock();

                cr.outputline("else if (!" + constraintboolean.getSafeSymbol() + ")");
                cr.startblock();
                if (!Compiler.REPAIR||Compiler.GENERATEDEBUGHOOKS)
		    cr.outputline("printf(\"fail " + escape(constraint.toString()) + ". \\n\");");
		
		if (Compiler.REPAIR) {
		/* Do repairs */
		/* Build new repair table */
	        cr.outputline("if ("+repairtable.getSafeSymbol()+")");
		cr.outputline("delete "+repairtable.getSafeSymbol()+";");
                cr.outputline(repairtable.getSafeSymbol()+"=new RepairHash();");
		
		if (Compiler.GENERATEDEBUGHOOKS)
		    cr.outputline("debughook();");
		/* Compute cost of each repair */
		VarDescriptor mincost=VarDescriptor.makeNew("mincost");
		VarDescriptor mincostindex=VarDescriptor.makeNew("mincostindex");
		Vector dnfconst=new Vector();
		dnfconst.addAll((Set)termination.conjunctionmap.get(constraint));

		if (dnfconst.size()<=1) {
		    cr.outputline("int "+mincostindex.getSafeSymbol()+"=0;");
		}
		if (dnfconst.size()>1) {
		    cr.outputline("int "+mincostindex.getSafeSymbol()+";");
		    boolean first=true;
		    for(int j=0;j<dnfconst.size();j++) {
			GraphNode gn=(GraphNode)dnfconst.get(j);
			Conjunction conj=((TermNode)gn.getOwner()).getConjunction();
			if (removed.contains(gn))
			    continue;
			
			VarDescriptor costvar;
			if (first) {
			    costvar=mincost;
                        } else
			    costvar=VarDescriptor.makeNew("cost");
			for(int k=0;k<conj.size();k++) {
			    DNFPredicate dpred=conj.get(k);
			    Predicate p=dpred.getPredicate();
			    boolean negate=dpred.isNegated();
			    VarDescriptor predvalue=VarDescriptor.makeNew("Predicatevalue");
			    p.generate(cr,predvalue);
			    if (k==0)
				cr.outputline("int "+costvar.getSafeSymbol()+"=0;");

			    if (negate)
				cr.outputline("if (maybe||"+predvalue.getSafeSymbol()+")");
			    else
				cr.outputline("if (maybe||!"+predvalue.getSafeSymbol()+")");
			    cr.outputline(costvar.getSafeSymbol()+"+="+cost.getCost(dpred)+";");
			}

			if(!first) {
			    cr.outputline("if ("+costvar.getSafeSymbol()+"<"+mincost.getSafeSymbol()+")");
			    cr.startblock();
			    cr.outputline(mincost.getSafeSymbol()+"="+costvar.getSafeSymbol()+";");
			    cr.outputline(mincostindex.getSafeSymbol()+"="+j+";");
			    cr.endblock();
			} else
			    cr.outputline(mincostindex.getSafeSymbol()+"="+j+";");
			first=false;
		    }
		}
		cr.outputline("switch("+mincostindex.getSafeSymbol()+") {");
		for(int j=0;j<dnfconst.size();j++) {
		    GraphNode gn=(GraphNode)dnfconst.get(j);
		    Conjunction conj=((TermNode)gn.getOwner()).getConjunction();

		    if (removed.contains(gn))
			continue;
		    cr.outputline("case "+j+":");
		    cr.startblock();
		    for(int k=0;k<conj.size();k++) {
			DNFPredicate dpred=conj.get(k);
			Predicate p=dpred.getPredicate();
			boolean negate=dpred.isNegated();
			VarDescriptor predvalue=VarDescriptor.makeNew("Predicatevalue");
			p.generate(cr,predvalue);
			if (negate)
			    cr.outputline("if (maybe||"+predvalue.getSafeSymbol()+")");
			else
			    cr.outputline("if (maybe||!"+predvalue.getSafeSymbol()+")");
			cr.startblock();
			if (Compiler.GENERATEINSTRUMENT)
			    cr.outputline("abstractcount++;");
			if (p instanceof InclusionPredicate)
			    generateinclusionrepair(conj,dpred, cr);
			else if (p instanceof ExprPredicate) {
			    ExprPredicate ep=(ExprPredicate)p;
			    if (ep.getType()==ExprPredicate.SIZE)
				generatesizerepair(conj,dpred,cr);
			    else if (ep.getType()==ExprPredicate.COMPARISON)
				generatecomparisonrepair(conj,dpred,cr);
			} else throw new Error("Unrecognized Predicate");
			cr.endblock();
		    }
		    /* Update model */
		    cr.endblock();
		    cr.outputline("break;");
		}
		cr.outputline("}");

		cr.outputline("if ("+oldmodel.getSafeSymbol()+")");
		cr.outputline("delete "+oldmodel.getSafeSymbol()+";");
		cr.outputline(oldmodel.getSafeSymbol()+"="+newmodel.getSafeSymbol()+";");
		cr.outputline("goto rebuild;");  /* Rebuild model and all */
		}
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
	CodeWriter cr = new StandardCodeWriter(outputaux);
	cr.startblock();
	cr.outputline("if ("+repairtable.getSafeSymbol()+")");
	cr.outputline("delete "+repairtable.getSafeSymbol()+";");
	cr.outputline("if ("+oldmodel.getSafeSymbol()+")");
	cr.outputline("delete "+oldmodel.getSafeSymbol()+";");
	cr.outputline("delete "+newmodel.getSafeSymbol()+";");
	cr.outputline("delete "+worklist.getSafeSymbol()+";");
	cr.outputline("resettypemap();");
	cr.outputline("break;");
	cr.endblock();
	cr.outputline("rebuild:");
	cr.outputline(";");	
	
    }
    
    private MultUpdateNode getmultupdatenode(Conjunction conj, DNFPredicate dpred, int repairtype) {
	MultUpdateNode mun=null;
	GraphNode gn=(GraphNode) termination.conjtonodemap.get(conj);
	for(Iterator edgeit=gn.edges();(mun==null)&&edgeit.hasNext();) {
	    GraphNode gn2=((GraphNode.Edge) edgeit.next()).getTarget();
	    TermNode tn2=(TermNode)gn2.getOwner();
	    if (tn2.getType()==TermNode.ABSTRACT) {
		AbstractRepair ar=tn2.getAbstract();
		if (((repairtype==-1)||(ar.getType()==repairtype))&&
		    ar.getPredicate()==dpred) {
		    for(Iterator edgeit2=gn2.edges();edgeit2.hasNext();) {
			GraphNode gn3=((GraphNode.Edge) edgeit2.next()).getTarget();
			if (!removed.contains(gn3)) {
			    TermNode tn3=(TermNode)gn3.getOwner();
			    if (tn3.getType()==TermNode.UPDATE) {
				mun=tn3.getUpdate();
				break;
			    }
			}
		    }
		}
	    }
	}
	return mun;
    }

    /** Generates abstract (and concrete) repair for a comparison */

    private void generatecomparisonrepair(Conjunction conj, DNFPredicate dpred, CodeWriter cr){
	MultUpdateNode munmodify=getmultupdatenode(conj,dpred,AbstractRepair.MODIFYRELATION);
	MultUpdateNode munremove=getmultupdatenode(conj,dpred,AbstractRepair.REMOVEFROMRELATION);
	MultUpdateNode munadd=getmultupdatenode(conj,dpred,AbstractRepair.ADDTORELATION);
	ExprPredicate ep=(ExprPredicate)dpred.getPredicate();
	RelationDescriptor rd=(RelationDescriptor)ep.getDescriptor();
	boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
	boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
	boolean inverted=ep.inverted();
	boolean negated=dpred.isNegated();
	OpExpr expr=(OpExpr)ep.expr;
	Opcode opcode=expr.getOpcode();
	VarDescriptor leftside=VarDescriptor.makeNew("leftside");
	VarDescriptor rightside=VarDescriptor.makeNew("rightside");
	VarDescriptor newvalue=VarDescriptor.makeNew("newvalue");
	if (!inverted) {
	    ((RelationExpr)expr.getLeftExpr()).getExpr().generate(cr,leftside);
	    expr.getRightExpr().generate(cr,newvalue);
	    cr.outputline(rd.getRange().getType().getGenerateType().getSafeSymbol()+" "+rightside.getSafeSymbol()+";");
	    cr.outputline(rd.getSafeSymbol()+"_hash->get("+leftside.getSafeSymbol()+","+rightside.getSafeSymbol()+");");
	} else {
	    ((RelationExpr)expr.getLeftExpr()).getExpr().generate(cr,rightside);
	    expr.getRightExpr().generate(cr,newvalue);
	    cr.outputline(rd.getDomain().getType().getGenerateType().getSafeSymbol()+" "+leftside.getSafeSymbol()+";");
	    cr.outputline(rd.getSafeSymbol()+"_hashinv->get("+rightside.getSafeSymbol()+","+leftside.getSafeSymbol()+");");
	}
	if (negated)
	    if (opcode==Opcode.GT) {
		opcode=Opcode.LE;
	    } else if (opcode==Opcode.GE) {
		opcode=Opcode.LT;
	    } else if (opcode==Opcode.LT) {
		opcode=Opcode.GE;
	    } else if (opcode==Opcode.LE) {
		opcode=Opcode.GT;
	    } else if (opcode==Opcode.EQ) {
		opcode=Opcode.NE;
	    } else if (opcode==Opcode.NE) {
		opcode=Opcode.EQ;
	    } else {
		throw new Error("Unrecognized Opcode");
	    }

	if (opcode==Opcode.GT) {
	    cr.outputline(newvalue.getSafeSymbol()+"++;");
	} else if (opcode==Opcode.GE) {
	    /* Equal */
	} else if (opcode==Opcode.LT) {
	    cr.outputline(newvalue.getSafeSymbol()+"--;");
	} else if (opcode==Opcode.LE) {
	    /* Equal */
	} else if (opcode==Opcode.EQ) {
	    /* Equal */
	} else if (opcode==Opcode.NE) { /* search for FLAGNE if this is changed*/
	    cr.outputline(newvalue.getSafeSymbol()+"++;");
	} else {
	    throw new Error("Unrecognized Opcode");
	}
	/* Do abstract repairs */
	if (usageimage) {
	    cr.outputline(rd.getSafeSymbol()+"_hash->remove("+leftside.getSafeSymbol()+","+rightside.getSafeSymbol()+");");
	    if (!inverted) {
		cr.outputline(rd.getSafeSymbol()+"_hash->add("+leftside.getSafeSymbol()+","+newvalue.getSafeSymbol()+");");
	    } else {
		cr.outputline(rd.getSafeSymbol()+"_hash->add("+newvalue.getSafeSymbol()+","+rightside.getSafeSymbol()+");");
	    }
	}
	if (usageinvimage) {
	    cr.outputline(rd.getSafeSymbol()+"_hashinv->remove("+rightside.getSafeSymbol()+","+leftside.getSafeSymbol()+");");
	    if (!inverted) {
		cr.outputline(rd.getSafeSymbol()+"_hashinv->add("+newvalue.getSafeSymbol()+","+leftside.getSafeSymbol()+");");
	    } else {
		cr.outputline(rd.getSafeSymbol()+"_hashinv->add("+rightside.getSafeSymbol()+","+newvalue.getSafeSymbol()+");");
	    }
	}
	/* Do concrete repairs */
	if (munmodify!=null) {
	    for(int i=0;i<state.vRules.size();i++) {
		Rule r=(Rule)state.vRules.get(i);
		if (r.getInclusion().getTargetDescriptors().contains(rd)) {
		    for(int j=0;j<munmodify.numUpdates();j++) {
			UpdateNode un=munmodify.getUpdate(j);
			if (un.getRule()==r) {
			    /* Update for rule r */
			    String name=(String)updatenames.get(un);
			    cr.outputline(repairtable.getSafeSymbol()+"->addrelation("+rd.getNum()+","+r.getNum()+","+leftside.getSafeSymbol()+","+rightside.getSafeSymbol()+",(int) &"+name+","+newvalue.getSafeSymbol()+");");
			}
		    }
		}
	    }

	} else {
	    /* Start with scheduling removal */
	    for(int i=0;i<state.vRules.size();i++) {
		Rule r=(Rule)state.vRules.get(i);
		if (r.getInclusion().getTargetDescriptors().contains(rd)) {
		    for(int j=0;j<munremove.numUpdates();j++) {
			UpdateNode un=munremove.getUpdate(i);
			if (un.getRule()==r) {
			    /* Update for rule r */
			    String name=(String)updatenames.get(un);
			    cr.outputline(repairtable.getSafeSymbol()+"->addrelation("+rd.getNum()+","+r.getNum()+","+leftside.getSafeSymbol()+","+rightside.getSafeSymbol()+",(int) &"+name+");");
			}
		    }
		}
	    }
	    /* Now do addition */
	    UpdateNode un=munadd.getUpdate(0);
	    String name=(String)updatenames.get(un);
	    if (!inverted) {
		cr.outputline(name+"(this,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+leftside.getSafeSymbol()+","+newvalue.getSafeSymbol()+");");
	    } else {
		cr.outputline(name+"(this,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+newvalue.getSafeSymbol()+","+rightside.getSafeSymbol()+");");
	    }
	}
    }

    public void generatesizerepair(Conjunction conj, DNFPredicate dpred, CodeWriter cr) {
	ExprPredicate ep=(ExprPredicate)dpred.getPredicate();
	OpExpr expr=(OpExpr)ep.expr;
	Opcode opcode=expr.getOpcode();
	opcode=Opcode.translateOpcode(dpred.isNegated(),opcode);

	MultUpdateNode munremove;

	MultUpdateNode munadd;
	if (ep.getDescriptor() instanceof RelationDescriptor) {
	    munremove=getmultupdatenode(conj,dpred,AbstractRepair.REMOVEFROMRELATION);
	    munadd=getmultupdatenode(conj,dpred,AbstractRepair.ADDTORELATION);
	} else {
	    munremove=getmultupdatenode(conj,dpred,AbstractRepair.REMOVEFROMSET);
	    munadd=getmultupdatenode(conj,dpred,AbstractRepair.ADDTOSET);
	}
	int size=ep.rightSize();
	VarDescriptor sizevar=VarDescriptor.makeNew("size");
	((OpExpr)expr).left.generate(cr, sizevar);
	VarDescriptor change=VarDescriptor.makeNew("change");
	cr.outputline("int "+change.getSafeSymbol()+";");
	boolean generateadd=false;
	boolean generateremove=false;
	if (opcode==Opcode.GT) {
	    cr.outputline(change.getSafeSymbol()+"="+String.valueOf(size+1)+"-"+sizevar.getSafeSymbol()+";");
	    generateadd=true;
	    generateremove=false;
	} else if (opcode==Opcode.GE) {
	    cr.outputline(change.getSafeSymbol()+"="+String.valueOf(size)+"-"+sizevar.getSafeSymbol()+";");
	    generateadd=true;
	    generateremove=false;
	} else if (opcode==Opcode.LT) {
	    cr.outputline(change.getSafeSymbol()+"="+String.valueOf(size-1)+"-"+sizevar.getSafeSymbol()+";");
	    generateadd=false;
	    generateremove=true;
	} else if (opcode==Opcode.LE) {
	    cr.outputline(change.getSafeSymbol()+"="+String.valueOf(size)+"-"+sizevar.getSafeSymbol()+";");
	    generateadd=false;
	    generateremove=true;
	} else if (opcode==Opcode.EQ) {
	    cr.outputline(change.getSafeSymbol()+"="+String.valueOf(size)+"-"+sizevar.getSafeSymbol()+";");
	    if (size==0)
		generateadd=false;
	    else 
		generateadd=true;
	    generateremove=true;
	} else if (opcode==Opcode.NE) {
	    cr.outputline(change.getSafeSymbol()+"="+String.valueOf(size+1)+"-"+sizevar.getSafeSymbol()+";");
	    generateadd=true;
	    generateremove=false;
	} else {
	    throw new Error("Unrecognized Opcode");
	}

// In some cases the analysis has determined that generating removes
// is unnecessary
	if (generateremove&&munremove==null) 
	    generateremove=false;

	Descriptor d=ep.getDescriptor();
	if (generateremove) {
	    cr.outputline("for(;"+change.getSafeSymbol()+"<0;"+change.getSafeSymbol()+"++)");
	    cr.startblock();
	    /* Find element to remove */
	    VarDescriptor leftvar=VarDescriptor.makeNew("leftvar");
	    VarDescriptor rightvar=VarDescriptor.makeNew("rightvar");
	    if (d instanceof RelationDescriptor) {
		if (ep.inverted()) {
		    rightvar=((ImageSetExpr)((SizeofExpr)((OpExpr)expr).left).setexpr).getVar();
		    cr.outputline("int "+leftvar.getSafeSymbol()+";");
		    cr.outputline(d.getSafeSymbol()+"_hashinv->get((int)"+rightvar.getSafeSymbol()+","+leftvar.getSafeSymbol()+");");
		} else {
		    leftvar=((ImageSetExpr)((SizeofExpr)((OpExpr)expr).left).setexpr).getVar();
		    cr.outputline("int "+rightvar.getSafeSymbol()+"=0;");
		    cr.outputline(d.getSafeSymbol()+"_hash->get((int)"+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+");");
		}
	    } else {
		cr.outputline("int "+leftvar.getSafeSymbol()+"="+d.getSafeSymbol()+"_hash->firstkey();");
	    }
	    /* Generate abstract remove instruction */
	    if (d instanceof RelationDescriptor) {
		RelationDescriptor rd=(RelationDescriptor) d;
		boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		if (usageimage)
		    cr.outputline(rd.getSafeSymbol() + "_hash->remove((int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		if (usageinvimage)
		    cr.outputline(rd.getSafeSymbol() + "_hashinv->remove((int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
	    } else {
		cr.outputline(d.getSafeSymbol() + "_hash->remove((int)" + leftvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
	    }
	    /* Generate concrete remove instruction */
	    for(int i=0;i<state.vRules.size();i++) {
		Rule r=(Rule)state.vRules.get(i);
		if (r.getInclusion().getTargetDescriptors().contains(d)) {
		    for(int j=0;j<munremove.numUpdates();j++) {
			UpdateNode un=munremove.getUpdate(j);
			if (un.getRule()==r) {
				/* Update for rule rule r */
			    String name=(String)updatenames.get(un);
			    if (d instanceof RelationDescriptor) {
				cr.outputline(repairtable.getSafeSymbol()+"->addrelation("+d.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+",(int) &"+name+");");
			    } else {
				cr.outputline(repairtable.getSafeSymbol()+"->addset("+d.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+",(int) &"+name+");");
			    }
			}
		    }
		}
	    }
	    cr.endblock();
	}
	if (generateadd) {

	    cr.outputline("for(;"+change.getSafeSymbol()+">0;"+change.getSafeSymbol()+"--)");
	    cr.startblock();
	    VarDescriptor newobject=VarDescriptor.makeNew("newobject");
	    if (d instanceof RelationDescriptor) {
		VarDescriptor otherside=((ImageSetExpr)((SizeofExpr)((OpExpr)ep.expr).left).setexpr).vd;
		RelationDescriptor rd=(RelationDescriptor)d;
		if (termination.sources.relsetSource(rd,!ep.inverted())) {
		    /* Set Source */
		    SetDescriptor sd=termination.sources.relgetSourceSet(rd,!ep.inverted());
		    VarDescriptor iterator=VarDescriptor.makeNew("iterator");
		    cr.outputline(sd.getType().getGenerateType().getSafeSymbol() +" "+newobject.getSafeSymbol()+";");
		    cr.outputline("SimpleIterator "+iterator.getSafeSymbol()+";");
		    cr.outputline("for("+sd.getSafeSymbol()+"_hash->iterator("+ iterator.getSafeSymbol() +");"+iterator.getSafeSymbol()+".hasNext();)");
		    cr.startblock();
		    if (ep.inverted()) {
			cr.outputline("if (!"+rd.getSafeSymbol()+"_hashinv->contains("+iterator.getSafeSymbol()+".key(),"+otherside.getSafeSymbol()+"))");
		    } else {
			cr.outputline("if (!"+rd.getSafeSymbol()+"_hash->contains("+otherside.getSafeSymbol()+","+iterator.getSafeSymbol()+".key()))");
		    }
		    cr.outputline(newobject.getSafeSymbol()+"="+iterator.getSafeSymbol()+".key();");
		    cr.outputline(iterator.getSafeSymbol()+".next();");
		    cr.endblock();
		} else if (termination.sources.relallocSource(rd,!ep.inverted())) {
		    /* Allocation Source*/
		    termination.sources.relgenerateSourceAlloc(cr,newobject,rd,!ep.inverted());
		} else throw new Error("No source for adding to Relation");
		if (ep.inverted()) {
		    boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		    boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		    if (usageimage)
			cr.outputline(rd.getSafeSymbol()+"_hash->add("+newobject.getSafeSymbol()+","+otherside.getSafeSymbol()+");");
		    if (usageinvimage)
			cr.outputline(rd.getSafeSymbol()+"_hashinv->add("+otherside.getSafeSymbol()+","+newobject.getSafeSymbol()+");");

		    UpdateNode un=munadd.getUpdate(0);
		    String name=(String)updatenames.get(un);
		    cr.outputline(name+"(this,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+newobject.getSafeSymbol()+","+otherside.getSafeSymbol()+");");
		} else {
		    boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		    boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		    if (usageimage)
			cr.outputline(rd.getSafeSymbol()+"_hash->add("+otherside.getSafeSymbol()+","+newobject.getSafeSymbol()+");");
		    if (usageinvimage)
			cr.outputline(rd.getSafeSymbol()+"_hashinv->add("+newobject.getSafeSymbol()+","+otherside.getSafeSymbol()+");");
		    UpdateNode un=munadd.getUpdate(0);
		    String name=(String)updatenames.get(un);
		    cr.outputline(name+"(this,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+otherside.getSafeSymbol()+","+newobject.getSafeSymbol()+");");
		}
	    } else {
		SetDescriptor sd=(SetDescriptor)d;
		if (termination.sources.setSource(sd)) {
		    /* Set Source */
		    /* Set Source */
		    SetDescriptor sourcesd=termination.sources.getSourceSet(sd);
		    VarDescriptor iterator=VarDescriptor.makeNew("iterator");
		    cr.outputline(sourcesd.getType().getGenerateType().getSafeSymbol() +" "+newobject.getSafeSymbol()+";");
		    cr.outputline("SimpleIterator "+iterator.getSafeSymbol()+";");
		    cr.outputline("for("+sourcesd.getSafeSymbol()+"_hash->iterator("+iterator.getSafeSymbol()+");"+iterator.getSafeSymbol()+".hasNext();)");
		    cr.startblock();
		    cr.outputline("if (!"+sd.getSafeSymbol()+"_hash->contains("+iterator.getSafeSymbol()+".key()))");
		    cr.outputline(newobject.getSafeSymbol()+"="+iterator.getSafeSymbol()+".key();");
		    cr.outputline(iterator.getSafeSymbol()+".next();");
		    cr.endblock();
		} else if (termination.sources.allocSource(sd)) {
		    /* Allocation Source*/
		    termination.sources.generateSourceAlloc(cr,newobject,sd);
		} else throw new Error("No source for adding to Set");
		cr.outputline(sd.getSafeSymbol()+"_hash->add("+newobject.getSafeSymbol()+","+newobject.getSafeSymbol()+");");
		UpdateNode un=munadd.getUpdate(0);
		String name=(String)updatenames.get(un);
		cr.outputline(name+"(this,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+newobject.getSafeSymbol()+");");
	    }
	    cr.endblock();
	}
    }

    public void generateinclusionrepair(Conjunction conj, DNFPredicate dpred, CodeWriter cr){
	InclusionPredicate ip=(InclusionPredicate) dpred.getPredicate();
	boolean negated=dpred.isNegated();
	MultUpdateNode mun=getmultupdatenode(conj,dpred,-1);
	VarDescriptor leftvar=VarDescriptor.makeNew("left");
	ip.expr.generate(cr, leftvar);

	if (negated) {
	    if (ip.setexpr instanceof ImageSetExpr) {
		ImageSetExpr ise=(ImageSetExpr) ip.setexpr;
		VarDescriptor rightvar=ise.getVar();
		boolean inverse=ise.inverted();
		RelationDescriptor rd=ise.getRelation();
		boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		if (inverse) {
		    if (usageimage)
			cr.outputline(rd.getSafeSymbol() + "_hash->remove((int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
		    if (usageinvimage)
			cr.outputline(rd.getSafeSymbol() + "_hashinv->remove((int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		} else {
		    if (usageimage)
			cr.outputline(rd.getSafeSymbol() + "_hash->remove((int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		    if (usageinvimage)
			cr.outputline(rd.getSafeSymbol() + "_hashinv->remove((int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
		}
		for(int i=0;i<state.vRules.size();i++) {
		    Rule r=(Rule)state.vRules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(rd)) {
			for(int j=0;j<mun.numUpdates();j++) {
			    UpdateNode un=mun.getUpdate(j);
			    if (un.getRule()==r) {
				/* Update for rule rule r */
				String name=(String)updatenames.get(un);
				if (inverse) {
				    cr.outputline(repairtable.getSafeSymbol()+"->addrelation("+rd.getNum()+","+r.getNum()+","+rightvar.getSafeSymbol()+","+leftvar.getSafeSymbol()+",(int) &"+name+");");
				} else {
				    cr.outputline(repairtable.getSafeSymbol()+"->addrelation("+rd.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+",(int) &"+name+");");
				}
			    }
			}
		    }
		}
	    } else {
		SetDescriptor sd=ip.setexpr.sd;
		cr.outputline(sd.getSafeSymbol() + "_hash->remove((int)" + leftvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");

		for(int i=0;i<state.vRules.size();i++) {
		    Rule r=(Rule)state.vRules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(sd)) {
			for(int j=0;j<mun.numUpdates();j++) {
			    UpdateNode un=mun.getUpdate(j);
			    if (un.getRule()==r) {
				/* Update for rule rule r */
				String name=(String)updatenames.get(un);
				cr.outputline(repairtable.getSafeSymbol()+"->addset("+sd.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+",(int) &"+name+");");
			    }
			}
		    }
		}
	    }
	} else {
	    /* Generate update */
	    if (ip.setexpr instanceof ImageSetExpr) {
		ImageSetExpr ise=(ImageSetExpr) ip.setexpr;
		VarDescriptor rightvar=ise.getVar();
		boolean inverse=ise.inverted();
		RelationDescriptor rd=ise.getRelation();
		boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		if (inverse) {
		    if (usageimage)
			cr.outputline(rd.getSafeSymbol() + "_hash->add((int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
		    if (usageinvimage)
			cr.outputline(rd.getSafeSymbol() + "_hashinv->add((int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		} else {
		    if (usageimage)
			cr.outputline(rd.getSafeSymbol() + "_hash->add((int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		    if (usageinvimage)
			cr.outputline(rd.getSafeSymbol() + "_hashinv->add((int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
		}
		UpdateNode un=mun.getUpdate(0);
		String name=(String)updatenames.get(un);
		if (inverse) {
		    cr.outputline(name+"(this,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+rightvar.getSafeSymbol()+","+leftvar.getSafeSymbol()+");");
		} else {
		    cr.outputline(name+"(this,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+");");
		}
	    } else {
		SetDescriptor sd=ip.setexpr.sd;
		cr.outputline(sd.getSafeSymbol() + "_hash->add((int)" + leftvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");

		UpdateNode un=mun.getUpdate(0);
		/* Update for rule rule r */
		String name=(String)updatenames.get(un);
		cr.outputline(name+"(this,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+leftvar.getSafeSymbol()+");");
	    }
	}
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
	if (!Compiler.REPAIR)
	    return false;
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
	if (Compiler.REPAIR) {
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
		    VarDescriptor mdfyptr=VarDescriptor.makeNew("modifyptr");
		    VarDescriptor ismdfyptr=VarDescriptor.makeNew("ismodifyptr");
		    cr.outputline("int "+ismdfyptr.getSafeSymbol()+"="+repairtable.getSafeSymbol()+"->ismodify("+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+");");



		    
		    String parttype="";
		    for(int i=0;i<currentrule.numQuantifiers();i++) {
			if (currentrule.getQuantifier(i) instanceof RelationQuantifier)
			    parttype=parttype+", int, int";
			else
			    parttype=parttype+", int";
		    }
		    VarDescriptor funptr=VarDescriptor.makeNew("updateptr");
		    VarDescriptor tmpptr=VarDescriptor.makeNew("tempupdateptr");
		    
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
		    
		    
		    
		    cr.outputline("void *"+tmpptr.getSafeSymbol()+"=");
		    cr.outputline("(void *) "+repairtable.getSafeSymbol()+"->getrelation("+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+");");
		    cr.outputline("if ("+ismdfyptr.getSafeSymbol()+")");
		    {
			cr.startblock();
			cr.outputline("int "+mdfyptr.getSafeSymbol()+"="+repairtable.getSafeSymbol()+"->getrelation2("+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+");");
			cr.outputline("void (*"+funptr.getSafeSymbol()+") ("+name+"_state *,"+name+"*,RepairHash *"+parttype+",int,int,int)="+"(void (*) ("+name+"_state *,"+name+"*,RepairHash *"+parttype+",int,int,int)) "+tmpptr.getSafeSymbol()+";");
			cr.outputline(methodcall+","+leftvar+", "+rightvar+", "+mdfyptr.getSafeSymbol() +");");
			cr.endblock();
		    }
		    cr.outputline("else ");
		    {
			cr.startblock();
			cr.outputline("void (*"+funptr.getSafeSymbol()+") ("+name+"_state *,"+name+"*,RepairHash *"+parttype+")="+"(void (*) ("+name+"_state *,"+name+"*,RepairHash *"+parttype+")) "+tmpptr.getSafeSymbol()+";");
			cr.outputline(methodcall+");");
			cr.endblock();
		    }
		    cr.outputline("delete "+newmodel.getSafeSymbol()+";");
		    cr.outputline("goto rebuild;");
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
		    cr.outputline("delete "+newmodel.getSafeSymbol()+";");
		    cr.outputline("goto rebuild;");
		}
	    }
	    cr.endblock();
	}

        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
	cr.outputline("int " + addeditem + "=0;");

	String ifstring="if (!maybe&&";
	boolean dogenerate=false;
	if (rd.getDomain().getType() instanceof StructureTypeDescriptor)  {
	    dogenerate=true;
	    ifstring+=leftvar;
	}

	if (rd.getRange().getType() instanceof StructureTypeDescriptor)  {
	    if (dogenerate)
		ifstring+="&&"+rightvar;
	    else
		ifstring+=rightvar;
	    dogenerate=true;
	}

	ifstring+=")";

	if (rd.testUsage(RelationDescriptor.IMAGE)) {
	    cr.outputline(ifstring);
	    cr.outputline(addeditem + " = " + rd.getSafeSymbol() + "_hash->add((int)" + leftvar + ", (int)" + rightvar+ ");");
	}
	
	if (rd.testUsage(RelationDescriptor.INVIMAGE)) {
	    cr.outputline(ifstring);
	    cr.outputline(addeditem + " = " + rd.getSafeSymbol() + "_hashinv->add((int)" + rightvar + ", (int)" + leftvar + ");");
	}
	

        Vector dispatchrules = getrulelist(rd);
        
	Set toremove=new HashSet();
	for(int i=0;i<dispatchrules.size();i++) {
	    Rule r=(Rule)dispatchrules.get(i);
	    if (!ruleset.contains(r))
		toremove.add(r);
	}
	dispatchrules.removeAll(toremove);
        if (dispatchrules.size() == 0) {
            cr.outputline("// nothing to dispatch");
            return;
        }

	cr.outputline("if (" + addeditem + ")");
	cr.startblock();
       
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
	if (Compiler.REPAIR) {
	    cr.outputline("if ("+oldmodel.getSafeSymbol()+"&&");
	    cr.outputline("!"+oldmodel.getSafeSymbol() +"->"+sd.getJustSafeSymbol()+"_hash->contains("+setvar+"))");
	    cr.startblock(); {
		/* Adding new item */
		/* See if there is an outstanding update in the repairtable */
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
		    cr.outputline("delete "+newmodel.getSafeSymbol()+";");
		    cr.outputline("goto rebuild;");
		}
		cr.endblock();
		/* Build standard compensation actions */
		Vector ruleset=new Vector();
		ruleset.add(currentrule);
		if (state.implicitruleinv.containsKey(currentrule))
		    ruleset.addAll((Set)state.implicitruleinv.get(currentrule));
		for(int i=0;i<ruleset.size();i++) {
		    Rule itrule=(Rule)ruleset.get(i);
		    
		    if (need_compensation(itrule)) {
			UpdateNode un=find_compensation(itrule);
			String name=(String)updatenames.get(un);
			usedupdates.add(un); /* Mark as used */
			
			String methodcall=name+"(this,"+oldmodel.getSafeSymbol()+","+
			    repairtable.getSafeSymbol();
			for(int j=0;j<currentrule.numQuantifiers();j++) {
			    Quantifier q=currentrule.getQuantifier(j);
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
			if (currentrule!=itrule) {
			    SetDescriptor sdrule=((SetInclusion)itrule.getInclusion()).getSet();
			    cr.outputline("if ("+oldmodel.getSafeSymbol()+"&&");
			    cr.outputline("!"+oldmodel.getSafeSymbol() +"->"+sdrule.getJustSafeSymbol()+"_hash->contains("+setvar+"))");
			    cr.startblock();
			}
			cr.outputline(methodcall);
			cr.outputline("delete "+newmodel.getSafeSymbol()+";");
			cr.outputline("goto rebuild;");
			cr.endblock();
		    }
		    if (currentrule==itrule)
			cr.endblock();
		}
	    }
	}

        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
	cr.outputline("int " + addeditem + " = 0;");
	if (sd.getType() instanceof StructureTypeDescriptor)  {
	    cr.outputline("if (!maybe&&"+setvar+")");
	} else
	    cr.outputline("if (!maybe)");
	cr.outputline(addeditem + " = " + sd.getSafeSymbol() + "_hash->add((int)" + setvar +  ", (int)" + setvar + ");");
	cr.startblock();
        Vector dispatchrules = getrulelist(sd);

	Set toremove=new HashSet();
	for(int i=0;i<dispatchrules.size();i++) {
	    Rule r=(Rule)dispatchrules.get(i);
	    if (!ruleset.contains(r))
		toremove.add(r);
	}
	dispatchrules.removeAll(toremove);

        if (dispatchrules.size() == 0) {
            cr.outputline("// nothing to dispatch");
	    cr.endblock();
            return;
        }
	/* Add item to worklist if new */
	cr.outputline("if ("+addeditem+")");
	cr.startblock();
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
	cr.endblock();
    }
}
