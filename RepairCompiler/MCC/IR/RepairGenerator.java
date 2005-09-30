package MCC.IR;

import java.io.*;
import java.util.*;
import MCC.State;
import MCC.Compiler;

public class RepairGenerator {
    State state;
    PrintWrapper outputrepair = null;
    PrintWrapper outputaux = null;
    PrintWrapper outputhead = null;
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
        this.outputrepair = new PrintWrapper(new java.io.PrintWriter(outputrepair, true));
        this.outputaux = new PrintWrapper(new java.io.PrintWriter(outputaux, true));
        this.outputhead = new PrintWrapper(new java.io.PrintWriter(outputhead, true));

        headername=st;
	name_updates();
	generatetypechecks(true);
        generate_tokentable();
	RelationDescriptor.prefix = "thisvar->";
	SetDescriptor.prefix = "thisvar->";

        generate_hashtables();
	generate_stateobject();


	/* Rewrite globals */
        CodeWriter craux = new StandardCodeWriter(this.outputaux);
	for (Iterator it=this.state.stGlobals.descriptors();it.hasNext();) {
	    VarDescriptor vd=(VarDescriptor)it.next();
	    craux.outputline("#define "+vd.getSafeSymbol()+" thisvar->"+vd.getSafeSymbol());
	}


	generate_call();
	generate_start();
        generate_rules();
	if (!Compiler.REPAIR||Compiler.GENERATEDEBUGPRINT) {
	    generate_print();
	}
        generate_checks();
        generate_teardown();
	CodeWriter crhead = new StandardCodeWriter(this.outputhead);
	craux = new StandardCodeWriter(this.outputaux);
	craux.emptyBuffer();
	craux.endblock();

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
            craux.outputline("#undef "+vd.getSafeSymbol());
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
			crhead.outputline("void "+methodname+"(struct "+name+"_state * " +ststate+",struct "+name+" * "+stmodel+", struct RepairHash * "+strepairtable+", int "+stleft+", int "+stright+");");
			craux.outputline("void "+methodname+"(struct "+name+"_state * "+ ststate+", struct "+name+" * "+stmodel+", struct RepairHash * "+strepairtable+", int "+stleft+", int "+stright+")");
		    } else {
			crhead.outputline("void "+methodname+"(struct "+name+"_state * "+ ststate+", struct "+name+" * "+stmodel+", struct RepairHash * "+strepairtable+", int "+stleft+");");
			craux.outputline("void "+methodname+"(struct "+name+"_state * "+ststate+", struct "+name+" * "+stmodel+", struct RepairHash * "+strepairtable+", int "+stleft+")");
		    }
		    craux.startblock();
		    craux.startBuffer();
		    craux.addDeclaration("int","maybe");
		    craux.outputline("maybe=0;");
		    if (Compiler.GENERATEINSTRUMENT)
			craux.outputline("updatecount++;");

		    final SymbolTable st = un.getRule().getSymbolTable();
		    CodeWriter cr = new StandardCodeWriter(outputaux) {
                        public SymbolTable getSymbolTable() { return st; }
                    };
		    un.generate(cr, false, false, stleft,stright, null,this);
		    craux.outputline("if (maybe) printf(\"REALLY BAD\");");
		    craux.emptyBuffer();
		    craux.endblock();
		    break;
		case MultUpdateNode.REMOVE: {
		    Rule r=un.getRule();
		    String methodcall="void "+methodname+"(struct "+name+"_state * "+ststate+", struct "+name+" * "+stmodel+", struct RepairHash * "+strepairtable;
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
		    craux.startBuffer();
		    craux.addDeclaration("int","maybe");
		    craux.outputline("maybe=0;");
		    if (Compiler.GENERATEINSTRUMENT)
			craux.outputline("updatecount++;");
		    final SymbolTable st2 = un.getRule().getSymbolTable();
		    CodeWriter cr2 = new StandardCodeWriter(outputaux) {
                        public SymbolTable getSymbolTable() { return st2; }
                    };
		    un.generate(cr2, true, false, null,null, null,this);
		    craux.outputline("if (maybe) printf(\"REALLY BAD\");");
		    craux.emptyBuffer();
		    craux.endblock();
		}
		    break;
		case MultUpdateNode.MODIFY: {
		    Rule r=un.getRule();
		    String methodcall="void "+methodname+"(struct "+name+"_state * "+ststate+", struct "+name+" * "+stmodel+", struct RepairHash * "+strepairtable;
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
		    craux.startBuffer();
		    craux.outputline("int maybe=0;");
		    if (Compiler.GENERATEINSTRUMENT)
			craux.outputline("updatecount++;");
		    final SymbolTable st2 = un.getRule().getSymbolTable();
		    CodeWriter cr2 = new StandardCodeWriter(outputaux) {
                        public SymbolTable getSymbolTable() { return st2; }
                    };
		    un.generate(cr2, false, true, stleft, stright, stnew, this);
		    craux.outputline("if (maybe) printf(\"REALLY BAD\");");
		    craux.emptyBuffer();
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
	cr.addDeclaration("struct "+ name+"_state *", vdstate.getSafeSymbol());
	cr.outputline(vdstate.getSafeSymbol()+"=allocate"+name+"_state();");
	Iterator globals=state.stGlobals.descriptors();
	while (globals.hasNext()) {
	    VarDescriptor vd=(VarDescriptor) globals.next();
	    cr.outputline(vdstate.getSafeSymbol()+"->"+vd.getSafeSymbol()+"=("+vd.getType().getGenerateType().getSafeSymbol()+")"+vd.getSafeSymbol()+";");
	}
	/* Insert repair here */
	cr.outputline("doanalysis("+vdstate.getSafeSymbol()+");");
	globals=state.stGlobals.descriptors();
	while (globals.hasNext()) {
	    VarDescriptor vd=(VarDescriptor) globals.next();
	    cr.outputline("*(("+vd.getType().getGenerateType().getSafeSymbol()+"*) &"+vd.getSafeSymbol()+")="+vdstate.getSafeSymbol()+"->"+vd.getSafeSymbol()+";");
	}
	cr.outputline("free"+name+"_state("+vdstate.getSafeSymbol()+");");
    }

    private void generate_tokentable() {
        CodeWriter cr = new StandardCodeWriter(outputrepair);
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

    private void generate_stateobject() {
        CodeWriter crhead = new StandardCodeWriter(outputhead);
        CodeWriter craux = new StandardCodeWriter(outputaux);
	crhead.outputline("struct "+name+"_state {");
	Iterator globals=state.stGlobals.descriptors();
	while (globals.hasNext()) {
	    VarDescriptor vd=(VarDescriptor) globals.next();
	    crhead.outputline(vd.getType().getGenerateType().getSafeSymbol()+" "+vd.getSafeSymbol()+";");
	}
        crhead.outputline("};");
	crhead.outputline("struct "+name+"_state * allocate"+name+"_state();");
	craux.outputline("struct "+name+"_state * allocate"+name+"_state()");
	craux.startblock();
	craux.outputline("return (struct "+name+"_state *) malloc(sizeof(struct "+name+"_state));");
	craux.endblock();

	crhead.outputline("void free"+name+"_state(struct "+name+"_state *);");
	craux.outputline("void free"+name+"_state(struct "+name+"_state * thisvar)");
	craux.startblock();
	craux.outputline("free(thisvar);");
	craux.endblock();

	crhead.outputline("void "+name+"_statecomputesizes(struct "+name+"_state * ,int *,int **);");
	crhead.outputline("void "+name+"_staterecomputesizes(struct "+name+"_state *);");
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

	cr.outputline("void "+name+"_statecomputesizes(struct "+name+"_state * thisvar,int *sizearray,int **numele)");
	cr.startblock();
	cr.startBuffer();
	cr.addDeclaration("int","maybe");
	cr.outputline("maybe=0;");
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
	cr.emptyBuffer();
	cr.endblock();
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
	cr.outputline("void "+name+"_staterecomputesizes(struct "+name+"_state * thisvar)");
	cr.startblock();
	cr.startBuffer();
	cr.addDeclaration("int","maybe");
	cr.outputline("maybe=0;");
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
	cr.emptyBuffer();
	cr.endblock();
    }


    private void generate_hashtables() {
        CodeWriter craux = new StandardCodeWriter(outputaux);
        CodeWriter crhead = new StandardCodeWriter(outputhead);
	crhead.outputline("#ifndef "+name+"_h");
	crhead.outputline("#define "+name+"_h");
        crhead.outputline("#include \"SimpleHash.h\"");
        crhead.outputline("#include \"instrument.h\"");
        crhead.outputline("#include <stdio.h>");
        crhead.outputline("#include <stdlib.h>");
	crhead.outputline("struct "+name+" * allocate"+name+"();");
	crhead.outputline("void free"+name+"(struct "+name+" *);");
	crhead.outputline("struct "+name+" {");
        craux.outputline("#include \""+headername+"\"");
        craux.outputline("#include \"size.h\"");
	if (Compiler.TIME) {
	    craux.outputline("#include <sys/time.h>");
	}
	if (Compiler.ALLOCATECPLUSPLUS) {
	    for(Iterator it=state.stTypes.descriptors();it.hasNext();) {
		TypeDescriptor td=(TypeDescriptor)it.next();
		if (td instanceof StructureTypeDescriptor) {
		    if (((StructureTypeDescriptor)td).size()>0) {
			FieldDescriptor fd=((StructureTypeDescriptor)td).get(0);
			if (fd.getSymbol().startsWith("_vptr_")) {
			    String vtable="_ZTV";
			    vtable+=td.getSymbol().length();
			    vtable+=td.getSymbol();
			    craux.outputline("extern void * "+vtable+";");
			}
		    }
		}
	    }
	}
        craux.outputline("struct "+ name+"* allocate"+name+"()");
	craux.startblock();
        craux.outputline("/* creating hashtables */");

        /* build sets */
        Iterator sets = state.stSets.descriptors();
        craux.addDeclaration("struct "+name+"*", "thisvar");
        craux.outputline("thisvar=(struct "+name+"*) malloc(sizeof(struct "+name+"));");

        /* first pass create all the hash tables */
        while (sets.hasNext()) {
            SetDescriptor set = (SetDescriptor) sets.next();
	    crhead.outputline("struct SimpleHash* " + set.getJustSafeSymbol() + "_hash;");
            craux.outputline(set.getSafeSymbol() + "_hash = noargallocateSimpleHash();");
        }

        /* second pass build relationships between hashtables */
        sets = state.stSets.descriptors();

        while (sets.hasNext()) {
            SetDescriptor set = (SetDescriptor) sets.next();
            Iterator subsets = set.subsets();

            while (subsets.hasNext()) {
                SetDescriptor subset = (SetDescriptor) subsets.next();
                craux.outputline("SimpleHashaddParent("+subset.getSafeSymbol() +"_hash ,"+ set.getSafeSymbol() + "_hash);");
            }
        }

        /* build relations */
        Iterator relations = state.stRelations.descriptors();

        /* first pass create all the hash tables */
        while (relations.hasNext()) {
            RelationDescriptor relation = (RelationDescriptor) relations.next();

            if (relation.testUsage(RelationDescriptor.IMAGE)) {
                crhead.outputline("struct SimpleHash* " + relation.getJustSafeSymbol() + "_hash;");
                craux.outputline(relation.getSafeSymbol() + "_hash = noargallocateSimpleHash();");
            }

            if (relation.testUsage(RelationDescriptor.INVIMAGE)) {
                crhead.outputline("struct SimpleHash* " + relation.getJustSafeSymbol() + "_hashinv;");
                craux.outputline(relation.getSafeSymbol() + "_hashinv = noargallocateSimpleHash();");
            }
        }
        craux.outputline("return thisvar;");
        craux.endblock();
        crhead.outputline("};");
        craux.outputline("void free"+name+"(struct "+ name +"* thisvar)");
	craux.startblock();
        craux.outputline("/* deleting hashtables */");

        /* build destructor */
        sets = state.stSets.descriptors();

        /* first pass create all the hash tables */
        while (sets.hasNext()) {
            SetDescriptor set = (SetDescriptor) sets.next();
            craux.outputline("freeSimpleHash("+set.getSafeSymbol() + "_hash);");
        }

        /* destroy relations */
        relations = state.stRelations.descriptors();

        /* first pass create all the hash tables */
        while (relations.hasNext()) {
            RelationDescriptor relation = (RelationDescriptor) relations.next();

            if (relation.testUsage(RelationDescriptor.IMAGE)) {
                craux.outputline("freeSimpleHash("+relation.getSafeSymbol() + "_hash);");
            }

            if (relation.testUsage(RelationDescriptor.INVIMAGE)) {
                craux.outputline("freeSimpleHash(" + relation.getSafeSymbol() + "_hashinv);");
            }
        }
        craux.outputline("free(thisvar);");
        craux.endblock();
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

	crhead.outputline("void doanalysis(struct "+name+"_state *);");
	craux.outputline("void doanalysis(struct "+name+"_state * thisvar)");
  	craux.startblock();
	craux.outputline("int highmark;"); /* This declaration is special...need it to be first */
	craux.startBuffer();

	if (Compiler.TIME) {
	    craux.outputline("struct timeval _begin_time,_end_time;");
	    craux.outputline("gettimeofday(&_begin_time,NULL);");
	}
	if (Compiler.GENERATEINSTRUMENT) {
	    craux.outputline("updatecount=0;");
	    craux.outputline("rebuildcount=0;");
	    craux.outputline("abstractcount=0;");
	}


	craux.addDeclaration("struct "+name+ " * ",oldmodel.getSafeSymbol());
	craux.outputline(oldmodel.getSafeSymbol()+"=0;");
        craux.addDeclaration("struct WorkList * ",worklist.getSafeSymbol());
        craux.outputline(worklist.getSafeSymbol()+" = allocateWorkList();");
	craux.addDeclaration("struct RepairHash * ",repairtable.getSafeSymbol());
	craux.outputline(repairtable.getSafeSymbol()+"=0;");
	craux.outputline("initializestack(&highmark);");
	craux.outputline("computesizes(thisvar);");
	craux.outputline(name+"_staterecomputesizes(thisvar);");
	craux.outputline("while (1)");
	craux.startblock();
	craux.addDeclaration("struct "+name+ " * ",newmodel.getSafeSymbol());
	craux.outputline(newmodel.getSafeSymbol()+"=allocate"+name+"();");
	craux.outputline("WorkListreset("+worklist.getSafeSymbol()+");");
	if (Compiler.GENERATEINSTRUMENT)
	    craux.outputline("rebuildcount++;");
    }

    private void generate_teardown() {
	CodeWriter cr = new StandardCodeWriter(outputaux);
	cr.endblock();
	if (Compiler.TIME) {
	    cr.outputline("gettimeofday(&_end_time,NULL);");
	    cr.outputline("printf(\"time=%ld uS\\n\",(_end_time.tv_sec-_begin_time.tv_sec)*1000000+_end_time.tv_usec-_begin_time.tv_usec);");
	}

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

	cr.outputline("/* printing sets!*/");
	cr.outputline("printf(\"\\n\\nPRINTING SETS AND RELATIONS\\n\");");

        Iterator setiterator = state.stSets.descriptors();
	while (setiterator.hasNext()) {
	    SetDescriptor sd = (SetDescriptor) setiterator.next();
	    if (sd.getSymbol().equals("int") || sd.getSymbol().equals("token")) {
		continue;
	    }

	    String setname = sd.getSafeSymbol();

	    cr.startblock();
	    cr.outputline("/* printing set " + setname+"*/");
	    cr.outputline("printf(\"\\nPrinting set " + sd.getSymbol() + " - %d elements \\n\", SimpleHashcountset("+setname+"_hash));");
	    cr.addDeclaration("struct SimpleIterator","__setiterator");
	    cr.outputline("SimpleHashiterator("+setname+"_hash,&__setiterator);");
	    cr.outputline("while (hasNext(&__setiterator))");
	    cr.startblock();
	    cr.addDeclaration("int","__setval");
	    cr.outputline("__setval = (int) next(&__setiterator);");

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

			cr.outputline("/* build " +escape(rule.toString())+"*/");
			cr.startblock();
			cr.addDeclaration("int","maybe");
			cr.outputline("maybe=0;");

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

                        int openparencount=0;
			for(Iterator invit=invariants.iterator();invit.hasNext();) {
			    Expr invexpr=(Expr)invit.next();
			    VarDescriptor tmpvd=VarDescriptor.makeNew("tmpvar");
			    VarDescriptor maybevd=VarDescriptor.makeNew("maybevar");
			    invexpr.generate(cr,tmpvd);
			    cr.addDeclaration("int ",maybevd.getSafeSymbol());
			    cr.outputline(maybevd.getSafeSymbol()+"=maybe;");
			    cr.outputline("maybe=0;");
			    ivalue.assignPair(invexpr,tmpvd,maybevd);
                            openparencount++;
                            cr.startblock();
			}
			quantifiers = rule.quantifiers();
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
			currentrule=rule;
			rule.getInclusion().generate(cr);
			cr.endblock();
			while (quantifiers.hasPrevious()) {
			    Quantifier quantifier = (Quantifier) quantifiers.previous();
			    cr.endblock();
			}
			cr.endblock();
                        while((openparencount--)>0)
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
		    cr2.outputline("WorkListadd("+worklist.getSafeSymbol()+","+rule.getNum()+",-1,0,0);");
		}

		cr2.outputline("while (WorkListhasMoreElements("+worklist.getSafeSymbol()+"))");
		cr2.startblock();
		VarDescriptor idvar=VarDescriptor.makeNew("id");
		cr2.addDeclaration("int ",idvar.getSafeSymbol());
		cr2.outputline(idvar.getSafeSymbol()+"=WorkListgetid("+worklist.getSafeSymbol()+");");

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
			cr.addDeclaration("int","maybe");
			cr.outputline("maybe=0;");
			VarDescriptor typevar=VarDescriptor.makeNew("type");
			VarDescriptor leftvar=VarDescriptor.makeNew("left");
			VarDescriptor rightvar=VarDescriptor.makeNew("right");
			cr.addDeclaration("int",typevar.getSafeSymbol());
			cr.outputline(typevar.getSafeSymbol()+"= WorkListgettype("+worklist.getSafeSymbol()+");");
			cr.addDeclaration("int",leftvar.getSafeSymbol());
			cr.outputline(leftvar.getSafeSymbol()+"= WorkListgetlvalue("+worklist.getSafeSymbol()+");");
			cr.addDeclaration("int",rightvar.getSafeSymbol());
			cr.outputline(rightvar.getSafeSymbol()+"= WorkListgetrvalue("+worklist.getSafeSymbol()+");");
			cr.outputline("/* build " +escape(rule.toString())+"*/");


			for (int j=0;j<rule.numQuantifiers();j++) {
			    Quantifier quantifier = rule.getQuantifier(j);
			    quantifier.generate_open(cr, typevar.getSafeSymbol(),j,leftvar.getSafeSymbol(),rightvar.getSafeSymbol());
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
		cr2.outputline("WorkListpop("+worklist.getSafeSymbol()+");");
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
	Iterator i;
	if (Compiler.REPAIR)
	    i=termination.constraintdependence.computeOrdering().iterator();
	else
	    i=state.vConstraints.iterator();
	for (; i.hasNext();) {
	    Constraint constraint;
	    if (Compiler.REPAIR)
		constraint= (Constraint) ((GraphNode)i.next()).getOwner();
	    else
		constraint=(Constraint)i.next();

            {
		final SymbolTable st = constraint.getSymbolTable();
		CodeWriter cr = new StandardCodeWriter(outputaux);
		cr.pushSymbolTable(constraint.getSymbolTable());

		cr.outputline("/* checking " + escape(constraint.toString())+"*/");
                cr.startblock();

                ListIterator quantifiers = constraint.quantifiers();

                while (quantifiers.hasNext()) {
                    Quantifier quantifier = (Quantifier) quantifiers.next();
                    quantifier.generate_open(cr);
                }

                cr.addDeclaration("int","maybe");
                cr.outputline("maybe = 0;");

                /* now we have to generate the guard test */

                VarDescriptor constraintboolean = VarDescriptor.makeNew("constraintboolean");
                constraint.getLogicStatement().generate(cr, constraintboolean);

                cr.outputline("if (maybe)");
                cr.startblock();
                cr.outputline("printf(\"maybe fail " +  escape(constraint.toString()) + ". \\n\");");
		//cr.outputline("exit(1);");
                cr.endblock();

                cr.outputline("else if (!" + constraintboolean.getSafeSymbol() + ")");
                cr.startblock();
                if (!Compiler.REPAIR||Compiler.GENERATEDEBUGHOOKS)
		    cr.outputline("printf(\"fail " + escape(constraint.toString()) + ". \\n\");");

		if (Compiler.REPAIR) {
		/* Do repairs */
		/* Build new repair table */

	        cr.outputline("if ("+repairtable.getSafeSymbol()+")");
		cr.outputline("freeRepairHash("+repairtable.getSafeSymbol()+");");
                cr.outputline(repairtable.getSafeSymbol()+"=noargallocateRepairHash();");

		if (Compiler.GENERATEDEBUGHOOKS)
		    cr.outputline("debughook();");
		/* Compute cost of each repair */
		VarDescriptor mincost=VarDescriptor.makeNew("mincost");
		VarDescriptor mincostindex=VarDescriptor.makeNew("mincostindex");
		Vector dnfconst=new Vector();
		dnfconst.addAll((Set)termination.conjunctionmap.get(constraint));

		if (dnfconst.size()<=1) {
		    cr.addDeclaration("int",mincostindex.getSafeSymbol());
		    cr.outputline(mincostindex.getSafeSymbol()+"=0;");
		}
		if (dnfconst.size()>1) {
		    cr.addDeclaration("int",mincostindex.getSafeSymbol());
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
			    if (k==0) {
				cr.addDeclaration("int",costvar.getSafeSymbol());
				cr.outputline(costvar.getSafeSymbol()+"=0;");
			    }
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
		cr.outputline("switch("+mincostindex.getSafeSymbol()+")");
		cr.startblock();
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
		cr.endblock();

		cr.outputline("if ("+oldmodel.getSafeSymbol()+")");
		cr.outputline("free"+name+"("+oldmodel.getSafeSymbol()+");");
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
	cr.outputline("freeRepairHash("+repairtable.getSafeSymbol()+");");
	cr.outputline("if ("+oldmodel.getSafeSymbol()+")");
	cr.outputline("free"+name+"("+oldmodel.getSafeSymbol()+");");
	cr.outputline("free"+name+"("+newmodel.getSafeSymbol()+");");
	cr.outputline("freeWorkList("+worklist.getSafeSymbol()+");");
	cr.outputline("resettypemap();");
	cr.outputline("break;");
	cr.endblock();
	cr.outputline("rebuild:");
	cr.outputline(";");
    }

    private MultUpdateNode getmultupdatenode(Conjunction conj, DNFPredicate dpred, int repairtype) {
	Set nodes=getmultupdatenodeset(conj,dpred,repairtype);
	Iterator it=nodes.iterator();
	if (it.hasNext())
	    return (MultUpdateNode)it.next();
	else
	    return null;
    }

    private Set getmultupdatenodeset(Conjunction conj, DNFPredicate dpred, int repairtype) {
	HashSet hs=new HashSet();
	GraphNode gn=(GraphNode) termination.conjtonodemap.get(conj);
	for(Iterator edgeit=gn.edges();edgeit.hasNext();) {
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
				hs.add(tn3.getUpdate());
			    }
			}
		    }
		}
	    }
	}
	return hs;
    }

    private AbstractRepair getabstractrepair(Conjunction conj, DNFPredicate dpred, int repairtype) {
	HashSet hs=new HashSet();
	MultUpdateNode mun=null;
	GraphNode gn=(GraphNode) termination.conjtonodemap.get(conj);
	for(Iterator edgeit=gn.edges();(mun==null)&&edgeit.hasNext();) {
	    GraphNode gn2=((GraphNode.Edge) edgeit.next()).getTarget();
	    TermNode tn2=(TermNode)gn2.getOwner();
	    if (tn2.getType()==TermNode.ABSTRACT) {
		AbstractRepair ar=tn2.getAbstract();
		if (((repairtype==-1)||(ar.getType()==repairtype))&&
		    ar.getPredicate()==dpred) {
		    return ar;
		}
	    }
	}
	return null;
    }


    /** Generates abstract (and concrete) repair for a comparison */

    private void generatecomparisonrepair(Conjunction conj, DNFPredicate dpred, CodeWriter cr){
	Set updates=getmultupdatenodeset(conj,dpred,AbstractRepair.MODIFYRELATION);
	AbstractRepair ar=getabstractrepair(conj,dpred,AbstractRepair.MODIFYRELATION);
	MultUpdateNode munmodify=null;
	MultUpdateNode munadd=null;
	MultUpdateNode munremove=null;
	for(Iterator it=updates.iterator();it.hasNext();) {
	    MultUpdateNode mun=(MultUpdateNode)it.next();
	    if (mun.getType()==MultUpdateNode.ADD) {
		munadd=mun;
	    } else if (mun.getType()==MultUpdateNode.REMOVE) {
		munremove=mun;
	    } else if (mun.getType()==MultUpdateNode.MODIFY) {
		munmodify=mun;
	    }
	}

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
	boolean needremoveloop=ar.mayNeedFunctionEnforcement(state)&&ar.needsRemoves(state);

	if (needremoveloop&&((munadd==null)||(munremove==null))) {
	    System.out.println("Warning:  need to have individual remove operations for"+dpred.name());
	    needremoveloop=false;
	}
	if (needremoveloop) {
	    cr.outputline("while (1)");
	    cr.startblock();
	}
   	if (!inverted) {
	    ((RelationExpr)expr.getLeftExpr()).getExpr().generate(cr,leftside);
	    expr.getRightExpr().generate(cr,newvalue);
	    cr.addDeclaration(rd.getRange().getType().getGenerateType().getSafeSymbol(),rightside.getSafeSymbol());
	    cr.outputline("SimpleHashget("+rd.getSafeSymbol()+"_hash,"+leftside.getSafeSymbol()+", &"+rightside.getSafeSymbol()+");");
	} else {
	    ((RelationExpr)expr.getLeftExpr()).getExpr().generate(cr,rightside);
	    expr.getRightExpr().generate(cr,newvalue);
	    cr.outputline(rd.getDomain().getType().getGenerateType().getSafeSymbol()+" "+leftside.getSafeSymbol()+";");
	    cr.outputline("SimpleHashget("+rd.getSafeSymbol()+"_hashinv,"+rightside.getSafeSymbol()+", &"+leftside.getSafeSymbol()+");");
	}

	opcode=Opcode.translateOpcode(negated,opcode);

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
	    cr.outputline("SimpleHashremove("+rd.getSafeSymbol()+"_hash, "+leftside.getSafeSymbol()+","+rightside.getSafeSymbol()+");");
	}
	if (usageinvimage) {
	    cr.outputline("SimpleHashremove("+rd.getSafeSymbol()+"_hashinv, "+rightside.getSafeSymbol()+","+leftside.getSafeSymbol()+");");
	}

	if (needremoveloop) {
	    if (!inverted) {
		cr.outputline("if (SimpleHashcontainskey("+rd.getSafeSymbol()+"_hash, "+leftside.getSafeSymbol()+"))");
		cr.startblock();
	    } else {
		cr.outputline("if (SimpleHashcontainskey("+rd.getSafeSymbol()+"_hashinv, "+rightside.getSafeSymbol()+"))");
		cr.startblock();
	    }
	    for(int i=0;i<state.vRules.size();i++) {
		Rule r=(Rule)state.vRules.get(i);
		if (r.getInclusion().getTargetDescriptors().contains(rd)) {
		    for(int j=0;j<munremove.numUpdates();j++) {
			UpdateNode un=munremove.getUpdate(i);
			if (un.getRule()==r) {
				/* Update for rule r */
			    String name=(String)updatenames.get(un);
			    cr.outputline("RepairHashaddrelation("+repairtable.getSafeSymbol()+","+rd.getNum()+","+r.getNum()+","+leftside.getSafeSymbol()+","+rightside.getSafeSymbol()+",(int) &"+name+");");
			}
		    }
		}
	    }
	    cr.outputline("continue;");
	    cr.endblock();
	}

	if (usageimage) {
	    if (!inverted) {
		cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hash,"+leftside.getSafeSymbol()+","+newvalue.getSafeSymbol()+");");
	    } else {
		cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hash, "+newvalue.getSafeSymbol()+","+rightside.getSafeSymbol()+");");
	    }
	}
	if (usageinvimage) {
	    if (!inverted) {
		cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hashinv, "+newvalue.getSafeSymbol()+","+leftside.getSafeSymbol()+");");
	    } else {
		cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hashinv,"+rightside.getSafeSymbol()+","+newvalue.getSafeSymbol()+");");
	    }
	}
	/* Do concrete repairs */
	if (munmodify!=null&&(!ar.mayNeedFunctionEnforcement(state))||(munadd==null)||(ar.needsRemoves(state)&&(munremove==null))) {
	    for(int i=0;i<state.vRules.size();i++) {
		Rule r=(Rule)state.vRules.get(i);
		if (r.getInclusion().getTargetDescriptors().contains(rd)) {
		    for(int j=0;j<munmodify.numUpdates();j++) {
			UpdateNode un=munmodify.getUpdate(j);
			if (un.getRule()==r) {
			    /* Update for rule r */
			    String name=(String)updatenames.get(un);
			    cr.outputline("RepairHashaddrelation2("+repairtable.getSafeSymbol()+","+rd.getNum()+","+r.getNum()+","+leftside.getSafeSymbol()+","+rightside.getSafeSymbol()+",(int) &"+name+","+newvalue.getSafeSymbol()+");");
			}
		    }
		}
	    }
	} else {
	    /* Start with scheduling removal */
	    if (ar.needsRemoves(state))
		for(int i=0;i<state.vRules.size();i++) {
		    Rule r=(Rule)state.vRules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(rd)) {
			for(int j=0;j<munremove.numUpdates();j++) {
			    UpdateNode un=munremove.getUpdate(i);
			    if (un.getRule()==r) {
				/* Update for rule r */
				String name=(String)updatenames.get(un);
				cr.outputline("RepairHashaddrelation("+repairtable.getSafeSymbol()+","+rd.getNum()+","+r.getNum()+","+leftside.getSafeSymbol()+","+rightside.getSafeSymbol()+",(int) &"+name+");");
			    }
			}
		    }
		}
	    /* Now do addition */
	    UpdateNode un=munadd.getUpdate(0);
	    String name=(String)updatenames.get(un);
	    if (!inverted) {
		cr.outputline(name+"(thisvar,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+leftside.getSafeSymbol()+","+newvalue.getSafeSymbol()+");");
	    } else {
		cr.outputline(name+"(thisvar,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+newvalue.getSafeSymbol()+","+rightside.getSafeSymbol()+");");
	    }
	}
	if (needremoveloop) {
	    cr.outputline("break;");
	    cr.endblock();
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
	cr.addDeclaration("int",change.getSafeSymbol());
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
		    ((ImageSetExpr)((SizeofExpr)expr.left).setexpr).generate_leftside(cr,rightvar);
		    cr.addDeclaration("int",leftvar.getSafeSymbol());
		    cr.outputline("SimpleHashget("+d.getSafeSymbol()+"_hashinv,(int)"+rightvar.getSafeSymbol()+", &"+leftvar.getSafeSymbol()+");");
		} else {
		    ((ImageSetExpr)((SizeofExpr)expr.left).setexpr).generate_leftside(cr,leftvar);
		    cr.addDeclaration("int",rightvar.getSafeSymbol());
		    cr.outputline("SimpleHashget("+d.getSafeSymbol()+"_hash ,(int)"+leftvar.getSafeSymbol()+", &"+rightvar.getSafeSymbol()+");");
		}
	    } else {
		cr.addDeclaration("int",leftvar.getSafeSymbol());
		cr.outputline(leftvar.getSafeSymbol()+"= SimpleHashfirstkey("+d.getSafeSymbol()+"_hash);");
	    }
	    /* Generate abstract remove instruction */
	    if (d instanceof RelationDescriptor) {
		RelationDescriptor rd=(RelationDescriptor) d;
		boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		if (usageimage)
		    cr.outputline("SimpleHashremove("+rd.getSafeSymbol()+"_hash, (int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		if (usageinvimage)
		    cr.outputline("SimpleHashremove("+rd.getSafeSymbol()+"_hashinv ,(int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
	    } else {
		cr.outputline("SimpleHashremove("+d.getSafeSymbol()+"_hash, (int)" + leftvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
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
				cr.outputline("RepairHashaddrelation("+repairtable.getSafeSymbol()+","+d.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+",(int) &"+name+");");
			    } else {
				cr.outputline("RepairHashaddset("+repairtable.getSafeSymbol()+","+d.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+",(int) &"+name+");");
			    }
			}
		    }
		}
	    }
	    cr.endblock();
	}

// In some cases the analysis has determined that generating removes
// is unnecessary
	if (generateadd&&munadd==null)
	    generateadd=false;

	if (generateadd) {

	    cr.outputline("for(;"+change.getSafeSymbol()+">0;"+change.getSafeSymbol()+"--)");
	    cr.startblock();
	    VarDescriptor newobject=VarDescriptor.makeNew("newobject");
	    if (d instanceof RelationDescriptor) {
		VarDescriptor otherside=VarDescriptor.makeNew("otherside");
		((ImageSetExpr)((SizeofExpr)expr.left).setexpr).generate_leftside(cr,otherside);

		RelationDescriptor rd=(RelationDescriptor)d;
		if (termination.sources.relsetSource(rd,!ep.inverted())) {
		    /* Set Source */
		    SetDescriptor sd=termination.sources.relgetSourceSet(rd,!ep.inverted());
		    VarDescriptor iterator=VarDescriptor.makeNew("iterator");
		    cr.addDeclaration(sd.getType().getGenerateType().getSafeSymbol(),newobject.getSafeSymbol());
		    cr.addDeclaration("struct SimpleIterator",iterator.getSafeSymbol());
		    cr.outputline("for(SimpleHashiterator("+sd.getSafeSymbol()+"_hash , &"+ iterator.getSafeSymbol() +"); hasNext(&"+iterator.getSafeSymbol()+");)");
		    cr.startblock();
		    if (ep.inverted()) {
			cr.outputline("if (!SimpleHashcontainskeydata("+rd.getSafeSymbol()+"_hashinv,"+iterator.getSafeSymbol()+".key(),"+otherside.getSafeSymbol()+"))");
		    } else {
			cr.outputline("if (!SimpleHashcontainskeydata("+rd.getSafeSymbol()+"_hash, "+otherside.getSafeSymbol()+","+iterator.getSafeSymbol()+".key()))");
		    }
		    cr.outputline(newobject.getSafeSymbol()+"=key(&"+iterator.getSafeSymbol()+");");
		    cr.outputline("next(&"+iterator.getSafeSymbol()+");");
		    cr.endblock();
		} else if (termination.sources.relallocSource(rd,!ep.inverted())) {
		    /* Allocation Source*/
		    termination.sources.relgenerateSourceAlloc(cr,newobject,rd,!ep.inverted());
		} else throw new Error("No source for adding to Relation");
		if (ep.inverted()) {
		    boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		    boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		    if (usageimage)
			cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hash, "+newobject.getSafeSymbol()+","+otherside.getSafeSymbol()+");");
		    if (usageinvimage)
			cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hashinv, "+otherside.getSafeSymbol()+","+newobject.getSafeSymbol()+");");

		    UpdateNode un=munadd.getUpdate(0);
		    String name=(String)updatenames.get(un);
		    cr.outputline(name+"(thisvar,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+newobject.getSafeSymbol()+","+otherside.getSafeSymbol()+");");
		} else {
		    boolean usageimage=rd.testUsage(RelationDescriptor.IMAGE);
		    boolean usageinvimage=rd.testUsage(RelationDescriptor.INVIMAGE);
		    if (usageimage)
			cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hash, "+otherside.getSafeSymbol()+","+newobject.getSafeSymbol()+");");
		    if (usageinvimage)
			cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hashinv, "+newobject.getSafeSymbol()+","+otherside.getSafeSymbol()+");");
		    UpdateNode un=munadd.getUpdate(0);
		    String name=(String)updatenames.get(un);
		    cr.outputline(name+"(thisvar, "+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+otherside.getSafeSymbol()+","+newobject.getSafeSymbol()+");");
		}
	    } else {
		SetDescriptor sd=(SetDescriptor)d;
		if (termination.sources.setSource(sd)) {
		    /* Set Source */
		    /* Set Source */
		    SetDescriptor sourcesd=termination.sources.getSourceSet(sd);
		    VarDescriptor iterator=VarDescriptor.makeNew("iterator");
		    cr.addDeclaration(sourcesd.getType().getGenerateType().getSafeSymbol(), newobject.getSafeSymbol());
		    cr.addDeclaration("struct SimpleIterator",iterator.getSafeSymbol());
		    cr.outputline("for(SimpleHashiterator("+sourcesd.getSafeSymbol()+"_hash, &"+iterator.getSafeSymbol()+"); hasNext(&"+iterator.getSafeSymbol()+");)");
		    cr.startblock();
		    cr.outputline("if (!SimpleHashcontainskey("+sd.getSafeSymbol()+"_hash, key(&"+iterator.getSafeSymbol()+")))");
		    cr.outputline(newobject.getSafeSymbol()+"=key(&"+iterator.getSafeSymbol()+");");
		    cr.outputline("next(&"+iterator.getSafeSymbol()+");");
		    cr.endblock();
		} else if (termination.sources.allocSource(sd)) {
		    /* Allocation Source*/
		    termination.sources.generateSourceAlloc(cr,newobject,sd);
		} else throw new Error("No source for adding to Set");
		cr.outputline("SimpleHashadd("+sd.getSafeSymbol()+"_hash, "+newobject.getSafeSymbol()+","+newobject.getSafeSymbol()+");");
		UpdateNode un=munadd.getUpdate(0);
		String name=(String)updatenames.get(un);
		cr.outputline(name+"(thisvar,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+newobject.getSafeSymbol()+");");
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
			cr.outputline("SimpleHashremove("+rd.getSafeSymbol()+"_hash, (int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
		    if (usageinvimage)
			cr.outputline("SimpleHashremove("+rd.getSafeSymbol()+"_hashinv, (int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		} else {
		    if (usageimage)
			cr.outputline("SimpleHashremove("+rd.getSafeSymbol()+"_hash ,(int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		    if (usageinvimage)
			cr.outputline("SimpleHashremove("+rd.getSafeSymbol()+"_hashinv, (int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
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
				    cr.outputline("RepairHashaddrelation("+repairtable.getSafeSymbol()+","+rd.getNum()+","+r.getNum()+","+rightvar.getSafeSymbol()+","+leftvar.getSafeSymbol()+",(int) &"+name+");");
				} else {
				    cr.outputline("RepairHashaddrelation("+repairtable.getSafeSymbol()+","+rd.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+",(int) &"+name+");");
				}
			    }
			}
		    }
		}
	    } else {
		SetDescriptor sd=ip.setexpr.sd;
		cr.outputline("SimpleHashremove("+sd.getSafeSymbol()+"_hash, (int)" + leftvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");

		for(int i=0;i<state.vRules.size();i++) {
		    Rule r=(Rule)state.vRules.get(i);
		    if (r.getInclusion().getTargetDescriptors().contains(sd)) {
			for(int j=0;j<mun.numUpdates();j++) {
			    UpdateNode un=mun.getUpdate(j);
			    if (un.getRule()==r) {
				/* Update for rule rule r */
				String name=(String)updatenames.get(un);
				cr.outputline("RepairHashaddset("+repairtable.getSafeSymbol()+","+sd.getNum()+","+r.getNum()+","+leftvar.getSafeSymbol()+",(int) &"+name+");");
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
			cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hash, (int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
		    if (usageinvimage)
			cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hashinv, (int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		} else {
		    if (usageimage)
			cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hash, (int)" + leftvar.getSafeSymbol() + ", (int)" + rightvar.getSafeSymbol() + ");");
		    if (usageinvimage)
			cr.outputline("SimpleHashadd("+rd.getSafeSymbol()+"_hashinv, (int)" + rightvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");
		}
		UpdateNode un=mun.getUpdate(0);
		String name=(String)updatenames.get(un);
		if (inverse) {
		    cr.outputline(name+"(thisvar,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+rightvar.getSafeSymbol()+","+leftvar.getSafeSymbol()+");");
		} else {
		    cr.outputline(name+"(thisvar,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+leftvar.getSafeSymbol()+","+rightvar.getSafeSymbol()+");");
		}
	    } else {
		SetDescriptor sd=ip.setexpr.sd;
		cr.outputline("SimpleHashadd("+sd.getSafeSymbol()+"_hash, (int)" + leftvar.getSafeSymbol() + ", (int)" + leftvar.getSafeSymbol() + ");");

		UpdateNode un=mun.getUpdate(0);
		/* Update for rule rule r */
		String name=(String)updatenames.get(un);
		cr.outputline(name+"(thisvar,"+newmodel.getSafeSymbol()+","+repairtable.getSafeSymbol()+","+leftvar.getSafeSymbol()+");");
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

        cr.outputline("/* RELATION DISPATCH */");
	if (Compiler.REPAIR) {
	    cr.outputline("if ("+oldmodel.getSafeSymbol()+"&&");
	    if (usageimage)
		cr.outputline("!SimpleHashcontainskeydata("+oldmodel.getSafeSymbol()+"->"+rd.getJustSafeSymbol() + "_hash, "+leftvar+","+rightvar+"))");
	    else
		cr.outputline("!SimpleHashcontainskeydata("+oldmodel.getSafeSymbol() +"->"+rd.getJustSafeSymbol()+"_hashinv, "+rightvar+","+leftvar+"))");

	    cr.startblock(); {
		/* Adding new item */
		/* Perform safety checks */
		cr.outputline("if ("+repairtable.getSafeSymbol()+"&&");
		cr.outputline("RepairHashcontainsrelation("+repairtable.getSafeSymbol()+","+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+"))");
		cr.startblock(); {
		    /* Have update to call into */
		    VarDescriptor mdfyptr=VarDescriptor.makeNew("modifyptr");
		    VarDescriptor ismdfyptr=VarDescriptor.makeNew("ismodifyptr");
		    cr.addDeclaration("int ",ismdfyptr.getSafeSymbol());
		    cr.outputline(ismdfyptr.getSafeSymbol()+"=RepairHashismodify("+repairtable.getSafeSymbol()+","+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+");");




		    String parttype="";
		    for(int i=0;i<currentrule.numQuantifiers();i++) {
			if (currentrule.getQuantifier(i) instanceof RelationQuantifier)
			    parttype=parttype+", int, int";
			else
			    parttype=parttype+", int";
		    }

		    VarDescriptor tmpptr=VarDescriptor.makeNew("tempupdateptr");

		    String methodcall="(thisvar,"+oldmodel.getSafeSymbol()+","+repairtable.getSafeSymbol();
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



		    cr.addDeclaration("void *",tmpptr.getSafeSymbol());
		    cr.outputline(tmpptr.getSafeSymbol()+"=");
		    cr.outputline("(void *) RepairHashgetrelation("+repairtable.getSafeSymbol()+","+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+");");
		    cr.outputline("if ("+ismdfyptr.getSafeSymbol()+")");
		    {
			VarDescriptor funptr=VarDescriptor.makeNew("updateptr");
			String methodcallprefix="("+funptr.getSafeSymbol()+") ";
			cr.startblock();
			cr.addDeclaration("int",mdfyptr.getSafeSymbol());
			cr.outputline(mdfyptr.getSafeSymbol()+"=RepairHashgetrelation2("+repairtable.getSafeSymbol()+","+rd.getNum()+","+currentrule.getNum()+","+leftvar+","+rightvar+");");
			cr.addDeclaration("void (*"+funptr.getSafeSymbol()+") (struct "+name+"_state *, struct "+name+"*, struct RepairHash *"+parttype+",int,int,int);");
			cr.outputline(funptr.getSafeSymbol()+"="+"(void (*) (struct "+name+"_state *, struct "+name+"*, struct RepairHash *"+parttype+",int,int,int)) "+tmpptr.getSafeSymbol()+";");
			cr.outputline(methodcallprefix+methodcall+","+leftvar+", "+rightvar+", "+mdfyptr.getSafeSymbol() +");");
			cr.endblock();
		    }
		    cr.outputline("else ");
		    {
			VarDescriptor funptr=VarDescriptor.makeNew("updateptr");
			String methodcallprefix="("+funptr.getSafeSymbol()+") ";
			cr.startblock();
			cr.addDeclaration("void (*"+funptr.getSafeSymbol()+") (struct "+name+"_state *, struct "+name+"*,struct RepairHash *"+parttype+");");
			cr.outputline(funptr.getSafeSymbol()+"="+"(void (*) (struct "+name+"_state *,struct "+name+"*,struct RepairHash *"+parttype+")) "+tmpptr.getSafeSymbol()+";");
			cr.outputline(methodcallprefix+methodcall+");");
			cr.endblock();
		    }
		    cr.outputline("free"+name+"("+newmodel.getSafeSymbol()+");");
		    cr.outputline("goto rebuild;");
		}
		cr.endblock();

		/* Build standard compensation actions */
		if (need_compensation(currentrule)) {
		    UpdateNode un=find_compensation(currentrule);
		    String name=(String)updatenames.get(un);
		    usedupdates.add(un); /* Mark as used */
		    String methodcall=name+"(thisvar,"+oldmodel.getSafeSymbol()+","+repairtable.getSafeSymbol();
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
		    cr.outputline("free"+name+"("+newmodel.getSafeSymbol()+");");
		    cr.outputline("goto rebuild;");
		}
	    }
	    cr.endblock();
	}

        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
        cr.startblock();
	cr.addDeclaration("int" , addeditem);
	cr.outputline(addeditem + "=0;");

	String ifstring="if (!maybe";
	if (rd.getDomain().getType() instanceof StructureTypeDescriptor)  {
	    ifstring+="&&"+leftvar;
	}

	if (rd.getRange().getType() instanceof StructureTypeDescriptor)  {
            ifstring+="&&"+rightvar;
	}

	ifstring+=")";

	if (rd.testUsage(RelationDescriptor.IMAGE)) {
	    cr.outputline(ifstring);
	    cr.outputline(addeditem + " = SimpleHashadd("+rd.getSafeSymbol()+"_hash, (int)" + leftvar + ", (int)" + rightvar+ ");");
	}

	if (rd.testUsage(RelationDescriptor.INVIMAGE)) {
	    cr.outputline(ifstring);
	    cr.outputline(addeditem + " = SimpleHashadd("+rd.getSafeSymbol()+"_hashinv, (int)" + rightvar + ", (int)" + leftvar + ");");
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
            cr.outputline("/* nothing to dispatch */");
            cr.endblock();
            return;
        }

	cr.outputline("if (" + addeditem + ")");
	cr.startblock();

        for(int i = 0; i < dispatchrules.size(); i++) {
            Rule rule = (Rule) dispatchrules.elementAt(i);
	    if (rule.getGuardExpr().getRequiredDescriptors().contains(rd)) {
		/* Guard depends on this relation, so we recomput everything */
		cr.outputline("WorkListadd("+worklist.getSafeSymbol()+","+rule.getNum()+",-1,0,0);");
	    } else {
		for (int j=0;j<rule.numQuantifiers();j++) {
		    Quantifier q=rule.getQuantifier(j);
		    if (q.getRequiredDescriptors().contains(rd)) {
			/* Generate add */
			cr.outputline("WorkListadd("+worklist.getSafeSymbol()+","+rule.getNum()+","+j+","+leftvar+","+rightvar+");");
		    }
		}
	    }
        }
        cr.endblock();
	cr.endblock();
    }


    public void generate_dispatch(CodeWriter cr, SetDescriptor sd, String setvar) {
	cr.outputline("/* SET DISPATCH */");
	if (Compiler.REPAIR) {
	    cr.outputline("if ("+oldmodel.getSafeSymbol()+"&&");
	    cr.outputline("!SimpleHashcontainskey("+oldmodel.getSafeSymbol() +"->"+sd.getJustSafeSymbol()+"_hash, "+setvar+"))");
	    cr.startblock(); {
		/* Adding new item */
		/* See if there is an outstanding update in the repairtable */
		cr.outputline("if ("+repairtable.getSafeSymbol()+"&&");
		cr.outputline("RepairHashcontainsset("+repairtable.getSafeSymbol()+","+sd.getNum()+","+currentrule.getNum()+","+setvar+"))");
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
		    cr.addDeclaration("void (*"+funptr.getSafeSymbol()+") (struct "+name+"_state *,struct "+name+"*,struct RepairHash *"+parttype+");");
		    cr.outputline(funptr.getSafeSymbol()+"=");
		    cr.outputline("(void (*) (struct "+name+"_state *,struct "+name+"*,struct RepairHash *"+parttype+")) RepairHashgetset("+repairtable.getSafeSymbol()+","+sd.getNum()+","+currentrule.getNum()+","+setvar+");");
		    String methodcall="("+funptr.getSafeSymbol()+") (thisvar,"+oldmodel.getSafeSymbol()+","+
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
		    cr.outputline("free"+name+"("+newmodel.getSafeSymbol()+");");
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

			String methodcall=name+"(thisvar,"+oldmodel.getSafeSymbol()+","+
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
			    cr.outputline("!SimpleHashcontainskey("+ oldmodel.getSafeSymbol() +"->"+sdrule.getJustSafeSymbol() +"_hash,"+setvar+"))");
			    cr.startblock();
			}
			cr.outputline(methodcall);
			cr.outputline("free"+name+"("+newmodel.getSafeSymbol()+");");
			cr.outputline("goto rebuild;");
			cr.endblock();
		    }
		    if (currentrule==itrule)
			cr.endblock();
		}
	    }
	}

        cr.startblock();
        String addeditem = (VarDescriptor.makeNew("addeditem")).getSafeSymbol();
	cr.addDeclaration("int", addeditem);
	cr.outputline(addeditem + " = 0;");
	if (sd.getType() instanceof StructureTypeDescriptor)  {
	    cr.outputline("if (!maybe&&"+setvar+")");
	} else
	    cr.outputline("if (!maybe)");
	cr.outputline(addeditem + " = SimpleHashadd("+sd.getSafeSymbol()+"_hash, (int)" + setvar +  ", (int)" + setvar + ");");
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
            cr.outputline("/* nothing to dispatch */");
	    cr.endblock();
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
		cr.outputline("WorkListadd("+worklist.getSafeSymbol()+","+rule.getNum()+",-1,0,0);");
	    } else {
		for (int j=0;j<rule.numQuantifiers();j++) {
		    Quantifier q=rule.getQuantifier(j);
		    if (SetDescriptor.expand(q.getRequiredDescriptors()).contains(sd)) {
			/* Generate add */
			cr.outputline("WorkListadd("+worklist.getSafeSymbol()+","+rule.getNum()+","+j+","+setvar+",0);");
		    }
		}
	    }
	}
	cr.endblock();
	cr.endblock();
	cr.endblock();
    }
}
