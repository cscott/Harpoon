package MCC.IR;

import java.util.*;
import java.math.BigInteger;
import MCC.State;

public class SemanticChecker {

    private static final boolean CREATE_MISSING = true;

    public State state;

    Vector vConstraints;
    Vector vRules;

    SymbolTableStack sts;

    SymbolTable stSets;
    SymbolTable stRelations;
    SymbolTable stTypes;
    SymbolTable stGlobals;

    StructureTypeDescriptor dCurrentType;

    IRErrorReporter er;
    
    public SemanticChecker () {
        dCurrentType = null;
	stTypes = null;
	er = null;
    }

    public boolean check(State state, IRErrorReporter er) {

	this.state = state;
	State.currentState = state;

	if (er == null) {
	    throw new IRException("IRBuilder.build: Received null ErrorReporter");
	} else {
	    this.er = er;
	}

	if (state.ptStructures == null) {
	    throw new IRException("IRBuilder.build: Received null ParseNode");
	}

        state.vConstraints = new Vector();
        vConstraints = state.vConstraints;

        state.vRules = new Vector();
        vRules = state.vRules;

 	state.stTypes = new SymbolTable();
	stTypes = state.stTypes;

 	state.stSets = new SymbolTable();
	stSets = state.stSets;

 	state.stRelations = new SymbolTable();
	stRelations = state.stRelations;

        state.stGlobals = new SymbolTable();
        stGlobals = state.stGlobals;

        sts = new SymbolTableStack();
 	
	// add int and bool to the types list
	stTypes.add(ReservedTypeDescriptor.BIT);
	stTypes.add(ReservedTypeDescriptor.BYTE);
	stTypes.add(ReservedTypeDescriptor.SHORT);
	stTypes.add(ReservedTypeDescriptor.INT);

        stSets.add(new ReservedSetDescriptor("int", ReservedTypeDescriptor.INT));
        stSets.add(new ReservedSetDescriptor("token", ReservedTypeDescriptor.INT));

	boolean ok = true; 

        er.setFilename(state.infile + ".struct");
        if (!parse_structures(state.ptStructures)) {
            ok = false;
        }

        er.setFilename(state.infile + ".space");
        if (!parse_space(state.ptSpace)) {
            ok = false;
        }
        
        er.setFilename(state.infile + ".constraints");
        if (!parse_constraints(state.ptConstraints)) {
            ok = false;
        }

        er.setFilename(state.infile + ".model");
        if (!parse_rules(state.ptModel)) {
            ok = false;
        }

        return ok;
    }

    /********************** HELPER FUNCTIONS ************************/ 

    /**
     * special case lookup that returns null if no such type exists 
     */
    private TypeDescriptor lookupType(String typename) {
        return lookupType(typename, false);
    }

    /**
     * does a look up in the types symbol table. if the type is
     * not found than a missing type descriptor is returned
     */
    private TypeDescriptor lookupType(String typename, boolean createmissing) {
        if (stTypes.get(typename) != null) {
            // the type exists, so plug in the descriptor directly 
            return (TypeDescriptor) stTypes.get(typename);              
        } else if (createmissing) {
            return new MissingTypeDescriptor(typename);
        } else {
            return null;
        }       
    }

    /**
     * reserve a name 
     */
    private VarDescriptor reserveName(ParseNode pn) {
        assert pn != null;
        String varname = pn.getTerminal();
        assert varname != null;
                
        /* do semantic check and if valid, add it to symbol table
           and add it to the quantifier as well */
        if (sts.peek().contains(varname)) {
            /* Semantic Error: redefinition */
            er.report(pn, "Redefinition of '" + varname + "'");
            return null;
        } else {
            VarDescriptor vd = new VarDescriptor(varname);
            sts.peek().add(vd);
            return vd;
        }
    }

    /**
     * special case lookup that returns null if no such set exists 
     */
    private SetDescriptor lookupSet(String setname) {
        return lookupSet(setname, false);
    }

    /**
     * does a look up in the set's symbol table. if the set is
     * not found than a missing set descriptor is returned
     */
    private SetDescriptor lookupSet(String setname, boolean createmissing) {
        if (stSets.get(setname) != null) {
            // the set exists, so plug in the descriptor directly 
            return (SetDescriptor) stSets.get(setname);              
        } else if (createmissing) {
            return new MissingSetDescriptor(setname);
        } else {
            return null;
        }       
    }
    
    /**
     * does a look up in the set's symbol table. if the set is
     * not found than a missing set descriptor is returned
     */
    private RelationDescriptor lookupRelation(String relname) {
        if (stRelations.get(relname) != null) {
            // the relation exists, so plug in the descriptor directly 
            return (RelationDescriptor) stRelations.get(relname);              
        } else {
            return null;
        }       
    }
    
    
    private static int count = 0;
    private boolean precheck(ParseNode pn, String label) {	        
	if (pn == null) {
            er.report(pn, "IE: Expected '" + label + "', got null");
            assert false;
	    return false;
	}

	if (! pn.getLabel().equals(label)) {
	    er.report(pn, "IE: Expected '" + label + "', got '" + pn.getLabel() + "'");
            assert false;
	    return false;
	}

        if (state.verbose >= 2) {
            System.err.println("visiting*" + (count++) + ": " + label);
        }

	return true;
    }

    /********************* PARSING FUNCTIONS ************************/ 

    private boolean parse_rules(ParseNode pn) {
        if (!precheck(pn, "rules")) {
            return false;
        }

        boolean ok = true;
        ParseNodeVector rules = pn.getChildren();
        
        for (int i = 0; i < rules.size(); i++) {
            ParseNode rule = rules.elementAt(i);
            if (!parse_rule(rule)) {
                ok = false;
            }
        }
               
        /* type check */       
        Iterator ruleiterator = state.vRules.iterator();
        
        while (ruleiterator.hasNext()) {
            Rule rule = (Rule) ruleiterator.next();            
            Expr guard = rule.getGuardExpr();
            final SymbolTable rulest = rule.getSymbolTable();
            SemanticAnalyzer sa = new SemanticAnalyzer() {
                    public IRErrorReporter getErrorReporter() { return er; }
                    public SymbolTable getSymbolTable() { return rulest; }
                };
            TypeDescriptor guardtype = guard.typecheck(sa);
            
            if (guardtype == null) {
                ok = false;
            } else if (guardtype != ReservedTypeDescriptor.INT) {
                er.report(null, "Type of guard must be 'int' not '" + guardtype.getSymbol() + "'");
                ok = false;
            }
            
            if (!rule.getInclusion().typecheck(sa)) {
                ok = false;
            }           
            
            Iterator quantifiers = rule.quantifiers();
            
            while (quantifiers.hasNext()) {
                Quantifier q = (Quantifier) quantifiers.next();
                
                if (q instanceof ForQuantifier && !((ForQuantifier)q).typecheck(sa)) {
                    ok = false;
                }
            }              
        }        

        /* do any post checks ?? */

        return ok;
    }

    private boolean parse_rule(ParseNode pn) {
        if (!precheck(pn, "rule")) {
            return false;
        }

        boolean ok = true;
        Rule rule = new Rule();
        
        /* get rule type */
        boolean isstatic = pn.getChild("static") != null;
        boolean isdelay = pn.getChild("delay") != null;
        rule.setStatic(isstatic);
        rule.setDelay(isdelay);

        /* set up symbol table for constraint */
        assert sts.empty();
        sts.push(stGlobals);
        sts.push(rule.getSymbolTable());

        /* optional quantifiers */
        if (pn.getChild("quantifiers") != null) {
            ParseNodeVector quantifiers = pn.getChild("quantifiers").getChildren();
            
            for (int i = 0; i < quantifiers.size(); i++) {
                ParseNode qn = quantifiers.elementAt(i);
                Quantifier quantifier = parse_quantifier(qn);

                if (quantifier == null) {
                    ok = false;
                } else {
                    rule.addQuantifier(quantifier);
                }
            }
        }
        
        /* get guard expr */
        Expr guard = parse_expr(pn.getChild("expr"));

        if (guard == null) {
            ok = false;
        } else {
            rule.setGuardExpr(guard);
        }

        /* inclusion constraint */
        Inclusion inclusion = parse_inclusion(pn.getChild("inclusion"));
        
        if (inclusion == null) {
            ok = false;
        } else {
            rule.setInclusion(inclusion);
        }
        
        /* pop symbol table stack */
        SymbolTable st = sts.pop();
        sts.pop(); /* pop off globals */

        /* make sure the stack we pop is our rule s.t. */
        assert st == rule.getSymbolTable(); 
        assert sts.empty();

        /* add rule to global set */
        vRules.addElement(rule);

        return ok;
    }

    private Inclusion parse_inclusion(ParseNode pn) {
        if (!precheck(pn, "inclusion")) {
            return null;
        }

        if (pn.getChild("set") != null) {
            ParseNode set = pn.getChild("set");
            Expr expr = parse_expr(set.getChild("expr"));
            
            if (expr == null) {
                return null;
            }

            String setname = set.getChild("name").getTerminal();
            assert setname != null;
            SetDescriptor sd = lookupSet(setname);
            
            if (sd == null) {
                er.report(set.getChild("name"), "Undefined set '" + setname + "'");
                return null;
            }

            return new SetInclusion(expr, sd);
        } else if (pn.getChild("relation") != null) {
            ParseNode relation = pn.getChild("relation");
            Expr leftexpr = parse_expr(relation.getChild("left").getChild("expr"));
            Expr rightexpr = parse_expr(relation.getChild("right").getChild("expr"));
            
            if ((leftexpr == null) || (rightexpr == null)) {
                return null;
            }

            String relname = relation.getChild("name").getTerminal();
            assert relname != null;
            RelationDescriptor rd = lookupRelation(relname);

            if (rd == null) {
                er.report(relation.getChild("name"), "Undefined relation '" + relname + "'");
                return null;
            }

            return new RelationInclusion(leftexpr, rightexpr, rd);
        } else {
            throw new IRException();
        }
    }

    private boolean parse_constraints(ParseNode pn) {
        if (!precheck(pn, "constraints")) {
            return false;
        }

        boolean ok = true;
        ParseNodeVector constraints = pn.getChildren();

        for (int i = 0; i < constraints.size(); i++) {
            ParseNode constraint = constraints.elementAt(i);
            assert constraint.getLabel().equals("constraint");
            if (!parse_constraint(constraint)) {
                ok = false;
            }
        }

        /* do any post checks... (type constraints, etc?) */

        Iterator consiterator = state.vConstraints.iterator();

        while (consiterator.hasNext()) {
            Constraint cons = (Constraint) consiterator.next();

            final SymbolTable consst = cons.getSymbolTable();
            SemanticAnalyzer sa = new SemanticAnalyzer() {
                    public IRErrorReporter getErrorReporter() { return er; }
                    public SymbolTable getSymbolTable() { return consst; }
                };

            TypeDescriptor constype = cons.getLogicStatement().typecheck(sa);

            if (constype == null) {
		System.out.println("Failed attempting to type constraint");
                ok = false;
            } else if (constype != ReservedTypeDescriptor.INT) {
                er.report(null, "Type of guard must be 'int' not '" + constype.getSymbol() + "'");
                ok = false;
            }
	}

	return ok;
    }

    private boolean parse_constraint(ParseNode pn) {
        if (!precheck(pn, "constraint")) {
            return false;
        }

        boolean ok = true;
        Constraint constraint = new Constraint();

        /* test crash */
        boolean crash = pn.getChild("crash") != null;
        constraint.setCrash(crash);

        /* set up symbol table for constraint */
        assert sts.empty();
        sts.push(constraint.getSymbolTable());
        
        /* get quantifiers */
        if (pn.getChild("quantifiers") != null) {
            ParseNodeVector quantifiers = pn.getChild("quantifiers").getChildren();
            
            for (int i = 0; i < quantifiers.size(); i++) {
                ParseNode qn = quantifiers.elementAt(i);
                assert qn.getLabel().equals("quantifier");
                Quantifier quantifier = parse_quantifier(qn);
                if (quantifier == null) {
		    System.out.println("Failed parsing quantifier");
                    ok = false;
                } else {
                    constraint.addQuantifier(quantifier);
                }
            }
        }

        /* get body */
        LogicStatement logicexpr = parse_body(pn.getChild("body"));

        if (logicexpr == null) {
	    System.out.println("Failed parsing logical expression");
            ok = false;
        } else {
            constraint.setLogicStatement(logicexpr);
        }
        
        /* pop symbol table stack */
        SymbolTable st = sts.pop();

        /* make sure the stack we pop is our constraint s.t. */
        assert st == constraint.getSymbolTable(); 
        assert sts.empty();

        /* add to vConstraints */
        vConstraints.addElement(constraint);

        return ok;
    }

    private Quantifier parse_quantifier(ParseNode pn) {
        if (!precheck(pn, "quantifier")) {
            return null;           
        }

        if (pn.getChild("forall") != null) { /* forall element in Set */
            SetQuantifier sq = new SetQuantifier();

            /* get var */
            VarDescriptor vd = reserveName(pn.getChild("var"));
            
            if (vd == null) {
                return null;
            } 

            sq.setVar(vd);

            /* parse the set */
            SetDescriptor set = parse_set(pn.getChild("set"));
            assert set != null;
            sq.setSet(set);
	    vd.setSet(set);

            vd.setType(set.getType());

            /* return to caller */
            return sq;

        } else if (pn.getChild("relation") != null) { /* for < v1, v2 > in Relation */
            RelationQuantifier rq = new RelationQuantifier();

            /* get vars */
            VarDescriptor vd1 = reserveName(pn.getChild("left"));
            VarDescriptor vd2 = reserveName(pn.getChild("right"));
            
            if ((vd1 == null) || (vd2 == null)) {
                return null;
            }
            
            rq.setTuple(vd1, vd2);
            
            /* get relation */
            String relationname = pn.getChild("relation").getTerminal();
            assert relationname != null;
            RelationDescriptor rd = lookupRelation(relationname);

            if (rd == null) {
                return null;
            }
            
            rq.setRelation(rd);
            vd1.setType(rd.getDomain().getType());
	    vd1.setSet(rd.getDomain());
            vd2.setType(rd.getRange().getType());
	    vd2.setSet(rd.getRange());
            return rq;
        } else if (pn.getChild("for") != null) { /* for j = x to y */
            ForQuantifier fq = new ForQuantifier();
            
            /* grab var */
            VarDescriptor vd = reserveName(pn.getChild("var"));
	    
            if (vd == null) {
                return null;
            }

	    vd.setSet(lookupSet("int", false));
            vd.setType(ReservedTypeDescriptor.INT);
            fq.setVar(vd);

            /* grab lower/upper bounds */
            Expr lower = parse_expr(pn.getChild("lower").getChild("expr"));
            Expr upper = parse_expr(pn.getChild("upper").getChild("expr"));


            if ((lower == null) || (upper == null)) {
                return null;
            }
	    vd.setBounds(lower,upper);
            fq.setBounds(lower, upper);

            return fq;
        } else {
            throw new IRException("not supported yet");
        }
    }

    private LogicStatement parse_body(ParseNode pn) {
        if (!precheck(pn, "body")) {
            return null;           
        }
        
        if (pn.getChild("and") != null) {
            /* body AND body */
            LogicStatement left, right;
            left = parse_body(pn.getChild("and").getChild("left").getChild("body"));
            right = parse_body(pn.getChild("and").getChild("right").getChild("body"));
            
            if ((left == null) || (right == null)) {
                return null;
            }
            
            // what do we want to call the and/or/not body classes?
            return new LogicStatement(LogicStatement.AND, left, right);
        } else if (pn.getChild("or") != null) {
            /* body OR body */
            LogicStatement left, right;
            left = parse_body(pn.getChild("or").getChild("left").getChild("body"));
            right = parse_body(pn.getChild("or").getChild("right").getChild("body"));
            
            if ((left == null) || (right == null)) {
                return null;
            }

            return new LogicStatement(LogicStatement.OR, left, right);
        } else if (pn.getChild("not") != null) {
            /* NOT body */
            LogicStatement left = parse_body(pn.getChild("not").getChild("body"));
            
            if (left == null) {
                return null;
            }
            
            return new LogicStatement(LogicStatement.NOT, left);
        } else if (pn.getChild("predicate") != null) {
            return parse_predicate(pn.getChild("predicate"));
        } else {
            throw new IRException();
        }                        
    }

    private Predicate parse_predicate(ParseNode pn) {
        if (!precheck(pn, "predicate")) {
            return null;
        }

        if (pn.getChild("inclusion") != null) {
            ParseNode in = pn.getChild("inclusion");
            
            /* Expr */
            Expr expr = parse_expr(in.getChild("expr"));
           
            if (expr == null) { 
                return null;
            }

            /* get set expr */
            SetExpr setexpr = parse_setexpr(in.getChild("setexpr"));

            if (setexpr == null) {
                return null;
            }

            return new InclusionPredicate(expr, setexpr);
        } else if (pn.getChild("expr") != null) {
            Expr expr = parse_expr(pn.getChild("expr"));
            
            if (expr == null) {
                return null;
            }

            return new ExprPredicate(expr);
        } else {
            throw new IRException();
        }       
    }

    private SetDescriptor parse_set(ParseNode pn) {
        if (!precheck(pn, "set")) {
            return null;
        }
    
        if (pn.getChild("name") != null) {
            String setname = pn.getChild("name").getTerminal();
            assert setname != null;
                
            if (!stSets.contains(setname)) {
                /* Semantic Error: unknown set */
                er.report(pn, "Unknown set '" + setname + "' referenced in quantifier");
                return null;
            } else {
                /* all good, get setdescriptor */
                SetDescriptor sd = (SetDescriptor) stSets.get(setname);
                assert sd != null;
                return sd;
            }            
        } else if (pn.getChild("listofliterals") != null) {            
            TokenSetDescriptor tokenset = new TokenSetDescriptor();
            ParseNodeVector token_literals = pn.getChild("listofliterals").getChildren();
            assert token_literals.size() > 0;
            
            for (int i = 0; i < token_literals.size(); i++) {
                ParseNode literal = token_literals.elementAt(i);
                assert literal.getLabel().equals("literal");
                LiteralExpr litexpr = parse_literal(literal);

                if (litexpr == null) {
                    return null;
                }
                
                if (litexpr instanceof TokenLiteralExpr || litexpr instanceof IntegerLiteralExpr) {
                    tokenset.addLiteral(litexpr);
                } else {
                    er.report(literal, "Elements of a user-defined set must be of type token or integer");
                    // return null; /* don't think we need to return null */
                }
            }               

            return tokenset;
        } else {
            throw new IRException(pn.getTerminal());
        }
    }
    
    private boolean parse_space(ParseNode pn) {
        if (!precheck(pn, "space")) {
            return false;
        }
        
        boolean ok = true;
        ParseNodeVector sets = pn.getChildren("setdefinition");
        ParseNodeVector relations = pn.getChildren("relationdefinition");

        assert pn.getChildren().size() == (sets.size() + relations.size());
        
        /* parse sets */
        for (int i = 0; i < sets.size(); i++) {
            if (!parse_setdefinition(sets.elementAt(i))) {
                ok = false;
            }
        }

        /* parse relations */
        for (int i = 0; i < relations.size(); i++) {
            if (!parse_relationdefinition(relations.elementAt(i))) {
                ok = false;
            }
        }

        // ok, all the spaces have been parsed, now we should typecheck and check
        // for cycles etc.

        // #TBD#: typecheck and check for cycles
      
        /* replace missing with actual */
        Iterator allsets = state.stSets.descriptors();
        
        while (allsets.hasNext()) {
            SetDescriptor sd = (SetDescriptor) allsets.next();
            Vector subsets = sd.getSubsets();

            for (int i = 0; i < subsets.size(); i++) {
                SetDescriptor subset = (SetDescriptor) subsets.elementAt(i);
                
                if (subset instanceof MissingSetDescriptor) {
                    SetDescriptor newsubset = lookupSet(subset.getSymbol());

                    if (newsubset == null) {
                        er.report(null, "Unknown subset '" + subset.getSymbol() + "'");
                    } else {
                        subsets.setElementAt(newsubset, i);
                    }
                }
            }
        }
        
        return ok;
    }

    private boolean parse_setdefinition(ParseNode pn) {
        if (!precheck(pn, "setdefinition")) {
            return false;
        }
        
        boolean ok = true;
        
        /* get set name */
        String setname = pn.getChild("name").getTerminal();
        assert (setname != null);

        SetDescriptor sd = new SetDescriptor(setname);
        
        /* get set type */
        String settype = pn.getChild("type").getTerminal();
        TypeDescriptor type = lookupType(settype);
        if (type == null) {
            er.report(pn, "Undefined type '" + settype + "'");
            ok = false; 
        } else {
            sd.setType(type);
        }

        /* is this a partition? */
        boolean partition = pn.getChild("partition") != null;
        sd.isPartition(partition); 

        /* if set has subsets, add them to set descriptor */
        if (pn.getChild("setlist") != null) {
            ParseNodeVector setlist = pn.getChild("setlist").getChildren();
            
            for (int i = 0; i < setlist.size(); i++) {
                String subsetname = setlist.elementAt(i).getLabel();
                sd.addSubset(lookupSet(subsetname, CREATE_MISSING));
            }            
        }

        /* add set to symbol table */
        if (stSets.contains(setname)) {
            // Semantic Check: redefinition
            er.report(pn, "Redefinition of set: " + setname);
            ok = false;
        } else {
            stSets.add(sd);
        }

        return ok;
    }

    private boolean parse_relationdefinition(ParseNode pn) {
        if (!precheck(pn, "relationdefinition")) {
            return false;
        }

        boolean ok = true;

        /* get relation name */
        String relname = pn.getChild("name").getTerminal();
        assert relname != null;

        RelationDescriptor rd = new RelationDescriptor(relname);

        /* check if static */
        boolean bStatic = pn.getChild("static") != null;
        rd.isStatic(bStatic);

        /* get domain */
        String domainsetname = pn.getChild("domain").getChild("type").getTerminal();
        assert domainsetname != null;

        /* get range */
        String rangesetname = pn.getChild("range").getChild("type").getTerminal();
        assert rangesetname != null;

        /* get domain multiplicity */	
	String domainmult;
	if (pn.getChild("domain").getChild("domainmult") != null)
	    domainmult = pn.getChild("domain").getChild("domainmult").getChild("mult").getTerminal();
        //assert domainmult != null;

        /* get range multiplicity */
	String rangemult;
	if (pn.getChild("range").getChild("domainrange") != null)
	    rangemult = pn.getChild("range").getChild("domainrange").getChild("mult").getTerminal();
        //assert rangemult != null;

        /* NOTE: it is assumed that the sets have been parsed already so that the 
           set namespace is fully populated. any missing setdescriptors for the set
           symbol table will be assumed to be errors and reported. */

        SetDescriptor domainset = lookupSet(domainsetname);
        if (domainset == null) {
            er.report(pn, "Undefined set '" + domainsetname + "' referenced in relation '" + relname + "'");
            ok = false;
        } else {
            rd.setDomain(domainset);
        }

        SetDescriptor rangeset = lookupSet(rangesetname);
        if (rangeset == null) {
            er.report(pn, "Undefined set '" + rangesetname + "' referenced in relation '" + relname + "'");
            ok = false;
        } else {
            rd.setRange(rangeset);
        }

        // #TBD#: eventually we'll use the multiplicities but now we don't... oh well

        /* lets add the relation to the global symbol table */
        if (!stRelations.contains(relname)) {
            stRelations.add(rd);
        } else {
            er.report(pn, "Redefinition of relation '" + relname + "'");
            ok = false;
        }

        return ok;
    }

    private boolean parse_structures(ParseNode pn) {
        if (!precheck(pn, "structures")) {
            return false;
        }
        
        boolean ok = true;
        ParseNodeVector structures = pn.getChildren("structure");

        for (int i = 0; i < structures.size(); i++) {
            if (!parse_structure(structures.elementAt(i))) {
                ok = false;
            }
        }

        ParseNodeVector globals = pn.getChildren("global");

        for (int i = 0; i < globals.size(); i++) {
            if (!parse_global(globals.elementAt(i))) {
                ok = false;
            }
        }

        // ok, all the structures have been parsed, now we gotta type check       

        Enumeration types = stTypes.getDescriptors();
        while (types.hasMoreElements()) {
            TypeDescriptor t = (TypeDescriptor) types.nextElement();

            if (t instanceof ReservedTypeDescriptor) {
                // we don't need to check reserved types
            } else if (t instanceof StructureTypeDescriptor) {
                
                StructureTypeDescriptor type = (StructureTypeDescriptor) t;
                TypeDescriptor subtype = type.getSuperType();

                // check that the subtype is valid
                if (subtype instanceof MissingTypeDescriptor) {
                    TypeDescriptor newtype = lookupType(subtype.getSymbol());
                    if (newtype == null) {
                        // subtype not defined anywheere
                        // #TBD#: somehow determine how we can get the original parsenode (global function?)
                        er.report(null, "Undefined subtype '" + subtype.getSymbol() + "'");
                        ok = false;
                    } else {
                        type.setSuperType(newtype);
                    }
                }

                Iterator fields = type.getFields();

                while (fields.hasNext()) {
                    FieldDescriptor field = (FieldDescriptor) fields.next();                        
                    TypeDescriptor fieldtype = field.getType();

                    assert fieldtype != null;

                    // check that the type is valid
                    if (fieldtype instanceof MissingTypeDescriptor) {
                        TypeDescriptor newtype = lookupType(fieldtype.getSymbol());
                        if (newtype == null) {
                            // type never defined
                            // #TBD#: replace new ParseNode with original parsenode
                            er.report(null, "Undefined type '" + fieldtype.getSymbol() + "'");
                            ok = false;
                        } else {
                            assert newtype != null;
                            field.setType(newtype);
                        }
                    }                        
                }

                Iterator labels = type.getLabels();

                while (labels.hasNext()) {
                    LabelDescriptor label = (LabelDescriptor) labels.next();
                    TypeDescriptor labeltype = label.getType();

                    assert labeltype != null;

                    // check that the type is valid
                    if (labeltype instanceof MissingTypeDescriptor) {
                        TypeDescriptor newtype = lookupType(labeltype.getSymbol());
                        if (newtype == null) {
                            // type never defined
                            // #TBD#: replace new ParseNode with original parsenode
                            er.report(null, "Undefined type '" + labeltype.getSymbol() + "'");
                            ok = false;
                        } else {
                            assert newtype != null;
                            label.setType(newtype);
                        }
                    }
                }
                
            } else {
                throw new IRException("shouldn't be any other typedescriptor classes");
            }
        }

        if (!ok) {
            return false;
        }

        types = stTypes.getDescriptors();

        while (types.hasMoreElements()) {
            TypeDescriptor t = (TypeDescriptor) types.nextElement();

            if (t instanceof ReservedTypeDescriptor) {
                // we don't need to check reserved types
            } else if (t instanceof StructureTypeDescriptor) {
                
                StructureTypeDescriptor type = (StructureTypeDescriptor)t;
                TypeDescriptor subtype = type.getSuperType();
                Iterator fields = type.getFields();

                while (fields.hasNext()) {
                    FieldDescriptor field = (FieldDescriptor) fields.next();                        

                    if (field instanceof ArrayDescriptor) {
                        ArrayDescriptor ad = (ArrayDescriptor) field;
                        Expr indexbound = ad.getIndexBound();
                        TypeDescriptor indextype = indexbound.typecheck(new SemanticAnalyzer() {
                                public IRErrorReporter getErrorReporter() { return er; }
                                public SymbolTable getSymbolTable() { return stGlobals; }
                            });

                        if (indextype == null) {
                            ok = false;
                        } else if (indextype != ReservedTypeDescriptor.INT) {
                            er.report(null, "'" + type.getSymbol() + "." + field.getSymbol() + "' index bounds must be type 'int' not '" + indextype.getSymbol() + "'");
                            ok = false;
                        }
                    }
                }

                Iterator labels = type.getLabels();

                while (labels.hasNext()) {
                    LabelDescriptor label = (LabelDescriptor) labels.next();
                    Expr index = label.getIndex();

                    if (index != null) {
                        TypeDescriptor indextype = index.typecheck(new SemanticAnalyzer() {
                                public IRErrorReporter getErrorReporter() { return er; }
                                public SymbolTable getSymbolTable() { return stGlobals; }
                            });
                        
                        if (indextype != ReservedTypeDescriptor.INT) {
                            er.report(null, "Label '" + type.getSymbol() + "." + label.getSymbol() + "' index must be type 'int' not '" + indextype.getSymbol() + "'");
                            ok = false;
                        }
                    }
                }
	    } else {
                throw new IRException("shouldn't be any other typedescriptor classes");
            }
        }

        // #TBD#: need to make sure that no cycles exist in any of the declarations or subtypes
        // subtypes, of course, are not real subtypes, they are merely a way to validate a 
        // cast, i believe

        return ok;
    }

    private boolean parse_global(ParseNode pn) {
        if (!precheck(pn, "global")) {
            return false;
        }

        String name = pn.getChild("name").getTerminal();
        assert name != null;

        String type = pn.getChild("type").getTerminal();
        assert type != null;
        TypeDescriptor td = lookupType(type);
        assert td != null;
        assert !(td instanceof ReservedTypeDescriptor);
        
        if (stGlobals.contains(name)) {
            /* redefinition of global */
            er.report(pn, "Redefinition of global '" + name + "'");
            return false;
        }

        stGlobals.add(new VarDescriptor(name, name, td,true));
        return true;
    }

    private boolean parse_structure(ParseNode pn) {
        if (!precheck(pn, "structure")) {
            return false;
        }

        boolean ok = true;
        String typename = pn.getChild("name").getTerminal();
        StructureTypeDescriptor type = new StructureTypeDescriptor(typename);
        
        if (pn.getChild("subtype") != null) {
            // has a subtype, lets try to resolve it
            String subtype = pn.getChild("subtype").getTerminal();

            if (subtype.equals(typename)) {
                // Semantic Error: cyclic inheritance
                er.report(pn, "Cyclic inheritance prohibited");
                ok = false;
            }

            /* lookup the type to get the type descriptor */
            type.setSuperType(lookupType(subtype, CREATE_MISSING));
        } else if (pn.getChild("subclass") != null) {
            // has a subtype, lets try to resolve it
            String subclass = pn.getChild("subclass").getTerminal();
	    
            if (subclass.equals(typename)) {
                // Semantic Error: cyclic inheritance
                er.report(pn, "Cyclic inheritance prohibited");
                ok = false;
            }

            /* lookup the type to get the type descriptor */
            type.setSuperType(lookupType(subclass, CREATE_MISSING));
            type.setSubClass(true);
        }

        // set the current type so that the recursive parses on the labels
        // and fields can add themselves automatically to the current type         
        dCurrentType = type;
        
        // parse the labels and fields
        if (!parse_labelsandfields(pn.getChild("lf"))) {
            ok = false;
        }

        if (stTypes.contains(typename)) {
            er.report(pn, "Redefinition of type '" + typename + "'");
            ok = false;
        } else {
            stTypes.add(type);
        }
        
        return ok;
    }

    private boolean parse_labelsandfields(ParseNode pn) {
        if (!precheck(pn, "lf")) {
            return false;
        }

        boolean ok = true;
     
        // check the fields first (need the field names 
        // to type check the labels)
        if (!parse_fields(pn.getChild("fields"))) {
            ok = false;
        }

        // check the labels now that all the fields are sorted
        if (!parse_labels(pn.getChild("labels"))) {
            ok = false;
        }

        return ok;
    }

    private boolean parse_fields(ParseNode pn) {
        if (!precheck(pn, "fields")) {
            return false;
        }
        
        boolean ok = true;
        
        /* NOTE: because the order of the fields is important when defining a data structure,
           and because the order is defined by the order of the fields defined in the field
           vector, its important that the parser returns the fields in the correct order */
        
        ParseNodeVector fields = pn.getChildren();

        for (int i = 0; i < fields.size(); i++) {
            ParseNode field = fields.elementAt(i);            
            FieldDescriptor fd;
            boolean reserved;
            String name = null;

            if (field.getChild("reserved") != null) {
                // reserved field
                // #TBD#: it will be necessary for reserved field descriptors to generate
                // a unique symbol for the type descriptor requires it for its hashtable
                fd = new ReservedFieldDescriptor();
                reserved = true;
            } else {
                name = field.getChild("name").getTerminal();                
                fd = new FieldDescriptor(name);
                reserved = false;
            }

            String type = field.getChild("type").getTerminal();
            boolean ptr = field.getChild("*") != null;
            fd.setPtr(ptr);

            fd.setType(lookupType(type, CREATE_MISSING));

            if (field.getChild("index") != null) {
                // field is an array, so create an array descriptor to wrap the 
                // field descriptor and then replace the top level field descriptor with
                // this array descriptor
                Expr expr = parse_expr(field.getChild("index").getChild("expr"));
                if (expr == null) {
                    // #ATTN#: do we really want to return an exception here?
                    throw new IRException("invalid index expression");
                }
                ArrayDescriptor ad = new ArrayDescriptor(fd, expr);               
                fd = ad;
            }

            // add the current field to the current type
            if (reserved == false) {
                // lets double check that we are redefining a field
                if (dCurrentType.getField(name) != null) {
                    // Semantic Error: field redefinition 
                    er.report(pn, "Redefinition of field '" + name + "'");
                    ok = false;
                } else {
                    dCurrentType.addField(fd);
                }
            } else {
                dCurrentType.addField(fd);
            }
        }
        
        return ok;
    }

    private boolean parse_labels(ParseNode pn) {
        if (!precheck(pn, "labels")) {
            return false;
        }

        boolean ok = true;

        /* NOTE: parse_labels should be called after the fields have been parsed because any
           labels not found in the field set of the current type will be flagged as errors */

        ParseNodeVector labels = pn.getChildren();

        for (int i = 0; i < labels.size(); i++) {           
            ParseNode label = labels.elementAt(i);
            String name = label.getChild("name").getTerminal();
            LabelDescriptor ld = new LabelDescriptor(name); 

            if (label.getChild("index") != null) {
                Expr expr = parse_expr(label.getChild("index").getChild("expr"));
                if (expr == null) {
                    /* #ATTN#: do we really want to return an exception here? */
                    throw new IRException("Invalid expression");
                }
                ld.setIndex(expr);                
            } 
            
            String type = label.getChild("type").getTerminal();

            ld.setType(lookupType(type, CREATE_MISSING));
                        
            String field = label.getChild("field").getTerminal();           
            FieldDescriptor fd = dCurrentType.getField(field);

            if (fd == null) {
                /* Semantic Error: Undefined field in label */
                er.report(label, "Undefined field '" + field + "' in label");
                ok = false;
            } else {
                ld.setField(fd);
            }

            /* add label to current type */
            if (dCurrentType.getLabel(name) != null) {
                /* semantic error: label redefinition */
                er.report(pn, "Redefinition of label '" + name + "'");
                ok = false;
            } else {
                dCurrentType.addLabel(ld);            
            }
        }

        return ok;
    }

    private Expr parse_expr(ParseNode pn) {
        if (!precheck(pn, "expr")) {
            return null;
        }

        if (pn.getChild("var") != null) {
            // we've got a variable reference... we'll have to scope check it later
            // when we are completely done... there are also some issues of cyclic definitions
            return new VarExpr(pn.getChild("var").getTerminal());
        } else if (pn.getChild("literal") != null) {
            return parse_literal(pn.getChild("literal"));
        } else if (pn.getChild("operator") != null) {
            return parse_operator(pn.getChild("operator"));
        } else if (pn.getChild("relation") != null) {
            return parse_relation(pn.getChild("relation"));
        } else if (pn.getChild("sizeof") != null) {
            return parse_sizeof(pn.getChild("sizeof"));
        } else if (pn.getChild("simple_expr") != null) {
            return parse_simple_expr(pn.getChild("simple_expr"));        
        } else if (pn.getChild("elementof") != null) {
            return parse_elementof(pn.getChild("elementof"));        
        } else if (pn.getChild("tupleof") != null) {
            return parse_tupleof(pn.getChild("tupleof"));        
        } else if (pn.getChild("isvalid") != null) {
            er.report(pn, "Operation 'isvalid' is currently unsupported.");
            return null;
        } else {
            er.report(pn, "Unknown or invalid expression type '" + pn.getTerminal() + "'");
            return null;
        }            
    }

    private Expr parse_elementof(ParseNode pn) {
        if (!precheck(pn, "elementof")) {
            return null;
        }

        /* get setname */
        String setname = pn.getChild("name").getTerminal();
        assert setname != null;
        SetDescriptor sd = lookupSet(setname);

        if (sd == null) {
            er.report(pn, "Undefined set '" + setname + "'");
            return null;
        }

        /* get left side expr */
        Expr expr = parse_expr(pn.getChild("expr"));
        
        if (expr == null) {
            return null;
        }

        return new ElementOfExpr(expr, sd);
    }

    private Expr parse_tupleof(ParseNode pn) {
        if (!precheck(pn, "tupleof")) {
            return null;
        }
        
        /* get relation */
        String relname = pn.getChild("name").getTerminal();
        assert relname != null;
        RelationDescriptor rd = lookupRelation(relname);

        if (rd == null) {
            er.report(pn, "Undefined relation '" + relname + "'");
            return null;
        }

        Expr left = parse_expr(pn.getChild("left").getChild("expr"));
        Expr right = parse_expr(pn.getChild("right").getChild("expr"));

        if ((left == null) || (right == null)) {
            return null;
        }

        return new TupleOfExpr(left, right, rd);
    }

    private Expr parse_simple_expr(ParseNode pn) {
        if (!precheck(pn, "simple_expr")) {
            return null;
        }

        // only locations so far
        return parse_location(pn.getChild("location"));
    }

    private Expr parse_location(ParseNode pn) {
        if (!precheck(pn, "location")) {
            return null;
        }

        if (pn.getChild("var") != null) {
            // should be changed into a namespace check */
            return new VarExpr(pn.getChild("var").getTerminal());
        } else if (pn.getChild("cast") != null) {
            return parse_cast(pn.getChild("cast"));
        } else if (pn.getChild("dot") != null) {
            return parse_dot(pn.getChild("dot"));
        } else {
            throw new IRException();
        }
    }

    private RelationExpr parse_relation(ParseNode pn) {
        if (!precheck(pn, "relation")) {
            return null;
        }

        String relname = pn.getChild("name").getTerminal();
        boolean inverse = pn.getChild("inv") != null;
        Expr expr = parse_expr(pn.getChild("expr"));        

        if (expr == null) {
            return null;
        }
                    
        RelationDescriptor relation = lookupRelation(relname);
            
        if (relation == null) {
            /* Semantic Error: relation not definied" */
            er.report(pn, "Undefined relation '" + relname + "'");
            return null;
        }                       

        /* add usage so correct sets are created */
        relation.addUsage(inverse ? RelationDescriptor.INVIMAGE : RelationDescriptor.IMAGE);
            
        return new RelationExpr(expr, relation, inverse);
    }

    private SizeofExpr parse_sizeof(ParseNode pn) {
        if (!precheck(pn, "sizeof")) {
            return null;
        }

        /* get setexpr */
        SetExpr setexpr = parse_setexpr(pn.getChild("setexpr"));
        
        if (setexpr == null) {
            return null;
        }

        return new SizeofExpr(setexpr);
    }

    private CastExpr parse_cast(ParseNode pn) {
        if (!precheck(pn, "cast")) {
            return null;
        }

        /* get type */
        String typename = pn.getChild("type").getTerminal();
        assert typename != null;
        TypeDescriptor type = lookupType(typename);

        if (type == null) {
            /* semantic error: undefined type in cast */
            er.report(pn, "Undefined type '" + typename + "' in cast operator");
            return null;
        } 

        /* get expr */
        Expr expr = parse_simple_expr(pn.getChild("simple_expr"));
        
        if (expr == null) {
            return null;
        } 

        return new CastExpr(type, expr);
    }

    private SetExpr parse_setexpr(ParseNode pn) {
        if (!precheck(pn, "setexpr")) {
            return null;
        }

        // #TBD#: setexpr and parse_relation seem to be cousins... is there a reduction/refactor possible?

        if (pn.getChild("set") != null) {
            String setname = pn.getChild("set").getTerminal();
            assert setname != null;
            SetDescriptor sd = lookupSet(setname);

            if (sd == null) {
                er.report(pn, "Unknown or undefined set '" + setname + "'");             
                return null;
            } else {                         
                return new SetExpr(sd);
            }
        } else if (pn.getChild("dot") != null) {
            VarDescriptor vd = parse_quantifiervar(pn.getChild("dot").getChild("quantifiervar"));
            RelationDescriptor relation = lookupRelation(pn.getChild("dot").getChild("relation").getTerminal());
            relation.addUsage(RelationDescriptor.IMAGE);
            return new ImageSetExpr(vd, relation);
        } else if (pn.getChild("dotinv") != null) {
            VarDescriptor vd = parse_quantifiervar(pn.getChild("dotinv").getChild("quantifiervar"));
            RelationDescriptor relation = lookupRelation(pn.getChild("dotinv").getChild("relation").getTerminal());
            relation.addUsage(RelationDescriptor.INVIMAGE);
            return new ImageSetExpr(ImageSetExpr.INVERSE, vd, relation);
        } else {
            throw new IRException();
        }
    }

    private VarDescriptor parse_quantifiervar(ParseNode pn) {
        if (!precheck(pn, "quantifiervar")) {
            return null;
        }

        /* get var */
        String varname = pn.getTerminal();
        assert varname != null;
        
        /* NOTE: quantifier var's are only found in the constraints and
           model definitions... therefore we can do a semantic check here 
           to make sure that the variables exist in the symbol table */

        /* NOTE: its assumed that the symbol table stack is appropriately
           set up with the parent quantifier symbol table */
        assert !sts.empty();          

        /* do semantic check and if valid, add it to symbol table
           and add it to the quantifier as well */
        if (sts.peek().contains(varname)) {
	    VarDescriptor vdold=(VarDescriptor)sts.peek().get(varname);
	    return vdold;
	    /*	   Dan was creating a new VarDescriptor...This seems
		   like the wrong thing to do.  We'll just lookup the
		   other one.
		   --------------------------------------------------
		   VarDescriptor vd=new VarDescriptor(varname);
		   vd.setSet(vdold.getSet()); return vd;*/
        } else {
            /* Semantic Error: undefined variable */
            er.report(pn, "Undefined variable '" + varname + "'");
            return null;
        }
    }
    
    private LiteralExpr parse_literal(ParseNode pn) {
        if (!precheck(pn, "literal")) {
            return null;
        }

        if (pn.getChild("boolean") != null) {
            if (pn.getChild("boolean").getChild("true") != null) {
                return new BooleanLiteralExpr(true);
            } else {
                return new BooleanLiteralExpr(false);
            } 
        } else if (pn.getChild("decimal") != null) {            
            String integer = pn.getChild("decimal").getTerminal();

            /* Check for integer literal overflow */
            BigInteger intLitBI = new BigInteger(integer);
            BigInteger intMax = new BigInteger("" + Integer.MAX_VALUE);
            BigInteger intMin = new BigInteger("" + Integer.MIN_VALUE);
            int value;

            if (intLitBI.compareTo(intMin) < 0) {
                value = Integer.MIN_VALUE;
                er.warn(pn, "Integer literal overflow");
            } else if (intLitBI.compareTo(intMax) > 0) {
                value = Integer.MAX_VALUE;
                er.warn(pn, "Integer literal overflow");
            } else {
                /* no truncation needed */
                value = Integer.parseInt(integer);
            }

            return new IntegerLiteralExpr(value);
        } else if (pn.getChild("token") != null) {
            return new TokenLiteralExpr(pn.getChild("token").getTerminal());
        } else if (pn.getChild("string") != null) {
            throw new IRException("string unsupported");
        } else if (pn.getChild("char") != null) {
            throw new IRException("char unsupported");
        } else {
            throw new IRException("unknown literal expression type.");
        }
    }

    private OpExpr parse_operator(ParseNode pn) {
        if (!precheck(pn, "operator")) {
            return null; 
        }

        String opname = pn.getChild("op").getTerminal();
        Opcode opcode = Opcode.decodeFromString(opname);

        if (opcode == null) {
            er.report(pn, "Unsupported operation: " + opname);
            return null;
        }
        
        Expr left = parse_expr(pn.getChild("left").getChild("expr"));
        Expr right = null;
        
        if (pn.getChild("right") != null) {
            right = parse_expr(pn.getChild("right").getChild("expr"));
        }

        if (left == null) {           
            return null;
        }

        if (right == null && opcode != Opcode.NOT) {
            er.report(pn, "Two arguments required.");
            return null;
        }

        return new OpExpr(opcode, left, right);
    }

    private DotExpr parse_dot(ParseNode pn) {
        if (!precheck(pn, "dot")) {
            return null;
        }

        Expr left = parse_simple_expr(pn.getChild("simple_expr"));

        if (left == null) {
            return null;
        }

        String field = pn.getChild("field").getTerminal();

        Expr index = null;

        if (pn.getChild("index") != null) {
            index = parse_expr(pn.getChild("index").getChild("expr"));
            
            if (index == null) {
                return null;
            }
        }

        return new DotExpr(left, field, index);        
    }

}

