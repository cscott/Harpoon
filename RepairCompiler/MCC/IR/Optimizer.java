package MCC.IR;

import MCC.State;
import java.util.*;

public class Optimizer {

    State state;
    public Optimizer(State s) {
        state = s;
    }

    public void optimize() {

        /* functional relation strength reduction */
        
        strengthreduction();
        
        mergerules();

        collapseconstraints();

        eliminateUnusedSets();

    }

    public class ReduceRelationVisitor {

        State state;
        RelationDescriptor relation;
        Rule rule;

        Constraint constraint;

        public ReduceRelationVisitor(RelationDescriptor rd, Rule rule, State state) {
            this.state = state;
            this.rule = rule;
            this.relation = rd;
        }

        public void rewrite(Constraint theconstraint) {
            // set class global
            constraint = theconstraint;
            constraint.logicstatement = (LogicStatement) rw(constraint.logicstatement);
        }

        Object rw(Object o) {            

            if (o instanceof LiteralExpr) {
                return o;
            } else if (o instanceof VarExpr) {
                return o;
            } else if (o instanceof OpExpr ) {
                OpExpr oe = (OpExpr) o;
                oe.left = (Expr) rw(oe.left);
                if (oe.right != null) {
                    oe.right = (Expr) rw(oe.right);
                }
                return oe;            
            } else if (o instanceof ImageSetExpr) {
                // shouldn't get here because we have to special case sizeof
                // which is the only supported region for a strength reduction
                throw new IRException("shouldn't be here");
            } else if (o instanceof SetExpr) {
                return o;
            } else if (o instanceof SizeofExpr) {
                SizeofExpr soe = (SizeofExpr) o;
                // check to see if the relation is the one we are replacing..
                // and then replace this relationexpr with another 
            
                // #TODO#: create a new EXPR which is basically an execution of a rule and a new
                // binding . for sizeofexpr itsa little different because we are only interested
                // in reporting 0 or 1
            
                SetExpr se = soe.getSetExpr();
                if (se instanceof ImageSetExpr) {
                    ImageSetExpr ise = (ImageSetExpr) se;
                    if (ise.getRelation() == relation) {
                        System.err.println("\nfound instance of sizeof for candidate");
                        soe.prettyPrint(new PrettyPrinter() { public void output(String s) { System.err.print(s); } });
                        System.err.println("");

                        // rewrite this node with a sizeoffunction IR node
                        return new SizeofFunction(ise.getVar(), relation, rule);
                    }
                }
                                
                return soe;
            } else if (o instanceof RelationExpr) {
                RelationExpr re = (RelationExpr) o;
                // check to see if the relation is the one we are replacing..
                // and then replace this relationexpr with another 
                
                // #TODO#: we'll replace this relationexpr with a new expr which 
                // takes the rule, relation and bindings... when we generate we'll
                // just insert the rule inline...

                if (re.getRelation() == relation) {                                    
                    System.err.println("\nfound instance of relationexpr for candidate");
                    re.prettyPrint(new PrettyPrinter() { public void output(String s) { System.err.print(s); } });
                    System.err.println("");
                    
                    // rewrite this node with a relationfunctionexpr IR node
                    return new RelationFunctionExpr(re.getExpr(), relation, rule);
                }

                return re;

            } else if (o instanceof RelationFunctionExpr) {
                return o;
            } else if (o instanceof SizeofFunction) {
                return o;
            } else if (o instanceof Expr) {
                // catches castexpr, dotexpr, elementofexpr, tupleofexpr
                throw new IRException("unsupported");
            } else if (o instanceof ExprPredicate) {
                ExprPredicate ep = (ExprPredicate) o;
                ep.expr = (Expr) rw(ep.expr);
                return ep;
            } else if (o instanceof InclusionPredicate) {
                throw new IRException("unsupported");
            } else if (o instanceof LogicStatement) {
                LogicStatement ls = (LogicStatement) o;
                ls.left = (LogicStatement) rw(ls.left);
                if (ls.right != null) {
                    ls.right = (LogicStatement) rw(ls.right);
                }
                return ls;
            } else {
                throw new IRException("unsupported");
            }
        }
    }

    public void strengthreduction() {
        
        // ok... need to find all relations that are quantified over... 
        // because these cannot be made into relational functions

        HashSet forbiddenrelations = new HashSet();
        Vector rules = state.vRules;
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = (Rule) rules.elementAt(i);
            forbiddenrelations.addAll(rule.getQuantifierDescriptors());
        }
        
        // forbiddenrelations now contains all of the descriptors that
        // are quantified over... strictly we could allow a relation
        // to be quantified over by replacing the relation with a set
        // quantifier and rewriting occurrences of the right hand side
        // with the function applied to the left hand side (the set
        // element)... but we won't.  it is also not clear what the
        // set of values should be on the left hand side... it is only
        // well defined for true function, or rather could only be
        // done efficiently by true functions because the set of s in
        // S where s.R is not null is not S in general.

        // ok enough rambling, uh... find candidate relations... these will be as follows... 
        // 1. they'll not be used as an inverse
        // 2. they'll be generated by a rule of the form [forall x in S], guard => <x, x.field> in R

        // ok... lets get a list of quantifiers used as inverses. we
        // are going to loop through the constraints and then we are
        // going to write some code that searches through the
        // expression tree for inverses... when we find one we are
        // going to add it to a set of inversed relations.... which we
        // will add to our set of forbidden descriptors... then we'll
        // search through the rules and look at the target descriptors
        // and find relations that are not on the forbidden list

        Vector constraints = state.vConstraints;
        for (int i = 0; i < constraints.size(); i++) {
            Constraint constraint = (Constraint) constraints.elementAt(i);

            // ok... no we need to look for inversed relations... 
            Set inversedrelations = constraint.getLogicStatement().getInversedRelations();
            forbiddenrelations.addAll(inversedrelations);           
        }

        // now we need to search through the rules and look at the target 
        // descriptors... we are looking for relations that are not on 
        // the forbidden list... these we will add to our candidate list...
        // we can ignore rules that don't have a single quantifier and
        // we want rules of the form [fa s in S], guard => <s, s.field> in 

        HashMap candidaterelations = new HashMap();

        for (int i = 0; i < rules.size(); i++) {
            Rule rule = (Rule) rules.elementAt(i);
            
            if (rule.quantifiers.size() == 1) {
                // one quantifier!
                Quantifier quantifier = (Quantifier) rule.quantifiers.elementAt(0);
                if (quantifier instanceof SetQuantifier) {
                    // its a set!
                    SetQuantifier sq = (SetQuantifier) quantifier;
                    VarDescriptor setvar = sq.getVar();
                    
                    // now we got the set var... because there can't be any funny
                    // business in the guard (we don't allow "in?" for these 
                    // optimizations) we can go ahead and look at the inclusion...
                    // hopefully its a relationinclusion and then we'll inspect
                    // some more!

                    Inclusion inclusion = rule.getInclusion();
                    
                    if (inclusion instanceof RelationInclusion) {
                        // ok... lets make sure the left hand element is the
                        // setdescriptors' var descriptor
                        RelationInclusion ri = (RelationInclusion) inclusion;

                        // lets make sure this relationdescriptor is not in our forbidden list
                        
                        if (!forbiddenrelations.contains(ri.getRelation())) {
                            // not forbidden!                        

                            // lets make sure its of the form <s, s.field>
                            Expr expr = ri.getLeftExpr();

                            // lets make sure its a 
                            if (expr instanceof VarExpr) {
                                VarExpr ve = (VarExpr) expr;
                                // lets make sure that ve points to our setvar
                                if (ve.getVar() == setvar) {
                                    // ok, this relation is of the form 
                                    // [forall s in S], guard => <s, ??> in R

                                    Expr rightexpr = ri.getRightExpr();
                                    if (rightexpr instanceof DotExpr) {
                                        DotExpr de = (DotExpr) rightexpr;
                                        Expr innerexpr = de.getExpr();
                                        if (expr instanceof VarExpr) {
                                            VarExpr rightvar = (VarExpr) expr;
                                            if (rightvar.getVar() == setvar) {
                                                // ok... we have found a candidate
                                                // lets add the relation and the rule that builds it to the candidate list
                                                candidaterelations.put(ri.getRelation(), rule);
                                            }
                                        }                                        
                                    }
                                }
                            }
                        }                                    
                    }
                }
            }
        }
         
        // ok... we now have candidaterelations... however, we aren't done... we need to make sure 
        // that they are built from a single rule, because, otherwise, i'll be confused.
        
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = (Rule) rules.elementAt(i);
            if (rule.getInclusion() instanceof RelationInclusion) {
                RelationInclusion ri = (RelationInclusion) rule.getInclusion();
                RelationDescriptor rd = ri.getRelation();
                if (candidaterelations.containsKey(rd)) {
                    // we need to make sure the rules match
                    Rule buildrule = (Rule) candidaterelations.get(rd);
                    if (buildrule != rule) {
                        // uh oh... relation is candidate but is build
                        // by multiple rules... we don't support this.
                        // remove it from the list of candidates
                        candidaterelations.remove(rd);
                    }
                }                    
            }                
        }
        
        // ok... now we have some true candidates... 
        // candidates are relations and there rules that build them... basically whenever we see an 
        // instance of this relation we are going to replace it with this relation
        
        Iterator candidates;
        
        // alright, lets find some places to replace these candidates!!!
        
        candidates = candidaterelations.keySet().iterator();
        while (candidates.hasNext()) {
            RelationDescriptor rd = (RelationDescriptor) candidates.next();                
            Rule rule = (Rule) candidaterelations.get(rd);

            System.err.println(rd.toString() + " is a candidate for STRENGTH REDUCTION!");
            
            // ok ... lets seek out and destroy! we only need to look in constraints because 
            // candidates can't appear as quantifiers 

            ReduceRelationVisitor rrv = new ReduceRelationVisitor(rd, rule, state);

            ListIterator listofconstraints = state.vConstraints.listIterator();
            while (listofconstraints.hasNext()) {
                Constraint constraint = (Constraint) listofconstraints.next();
                
                // um... ok.. we have a constraint and we want to see if there are any places
                // to replace the constraint...!!!  ok... so basically what are the options
                // well we allow anything really but the two things that should be swapped are 
                // relationexpr's with our relation and sizeof's... i think that covers everything
                // ... ok...

                // so because our IR blows, we are going to have to traverse the IR and make 
                // changes to the IR... this is fine.. we should write a visitor... 

                rrv.rewrite(constraint);                
            }            
        }
        
        
        // we need to remove the rules that build these relations
        candidates = candidaterelations.values().iterator();
        while (candidates.hasNext()) {
            state.vRules.remove((Rule) candidates.next());
        }                        
        
    }
      
    public void mergerules() {
        
        /*
          1. recognize that if you have an *unique* inclusion "... => x in S" and
          a set of model rules of the form "[forall y in S]" that do not
          contain any references to other sets (in? operator) then you can move
          those quantified rules into the original inclusion
        */

        // lets find unique inclusions! (inclusions are unique if there set identifier is unique)
        
        HashMap includedsets = new HashMap();
        ListIterator rules = state.vRules.listIterator();

        while (rules.hasNext()) {
            Rule rule = (Rule) rules.next();
            Inclusion inclusion = rule.getInclusion();
            
            if (inclusion instanceof SetInclusion) {
                SetInclusion si = (SetInclusion) inclusion;
                SetDescriptor sd = si.getSet();
                
                if (includedsets.containsKey(sd)) {
                    // already contained... set to empty
                    includedsets.put(sd, null);
                } else {
                    includedsets.put(sd, rule);
                }
                
            }
        }

        HashMap uniques = new HashMap(); // this a vector of pairs (or in java... vector pairs)
        Iterator allsd = includedsets.keySet().iterator();
        while (allsd.hasNext()) {
            SetDescriptor sd = (SetDescriptor) allsd.next();
            Object o = includedsets.get(sd);

            if (o != null) { // a unique set!
                uniques.put(sd, (Rule) o);
            }
        }

        // ok now we have a vector "unique set" of all of the 
        // set descriptors with unique inclusion rules
        // NOW lets find all of the rules whose quantifiers 
        // have single set quantifiers for each of these set
        // descriptors
        Iterator uniquesetiterator = uniques.keySet().iterator();
        while (uniquesetiterator.hasNext()) {
            SetDescriptor sd = (SetDescriptor) uniquesetiterator.next();
            Rule uniquerule = (Rule) uniques.get(sd);
            
            // ok... search for candidate rules...
            Vector candidaterules = new Vector();
            ListIterator morerules = state.vRules.listIterator();
            while (morerules.hasNext()) {
                Rule rule = (Rule) morerules.next();
                ListIterator quantifiers = rule.quantifiers();

                if (rule == uniquerule) continue;

                if (quantifiers.hasNext()) {
                    // first quantifier
                    Quantifier quantifier = (Quantifier) quantifiers.next();
                    if (quantifier instanceof SetQuantifier) {
                        SetQuantifier sq = (SetQuantifier) quantifier;
                        if (sq.getSet() == sd) {
                            // match! lets make sure this is the sole quantifier
                            if (!quantifiers.hasNext()) {
                                // no more quantifiers! bingo, match
                                candidaterules.addElement(rule);
                            }                
                        }
                    }
                }
            }
                 
            // at this point we have a vector "candidaterules" which is composed
            // of rules which have a first quantifier, which is a set quantifier
            // which has the same setdescriptor as one that is unique and has no
            // further quantifiers (sole quantifier)

            // we need to now move these rules into the inclusion of "unique rule"
            // we'll do by creating a meta-inclusion to replace the current 
            // inclusion ... the meta-inclusion will include the current inclusion
            // to generate the rule's target set as well as the "candidate rules"

            if (candidaterules.size() > 0) {
                MetaInclusion meta = new MetaInclusion();            
                Inclusion oldinclusion = uniquerule.getInclusion();
                meta.setInclusion(oldinclusion);
                meta.addRules(candidaterules);
                uniquerule.setInclusion(meta);            
                
                // no we must remove these rules from the global list so they are generated twice
                state.vRules.removeAll(candidaterules);
            }
        }        
    }

    public void collapseconstraints() {
        
        /* empty
           we are going to look at constraints of the form "[forall s in S], logicstatement"
           where logicstatement does not make any reference to any descriptors besides "s"           
        */

        HashMap candidates = new HashMap();
        Iterator constraints = state.vConstraints.iterator();
        while (constraints.hasNext()) {
            Constraint constraint = (Constraint) constraints.next();
            
            // ok ... we are looping through the constraints trying to find ones 
            // with empty required descriptors in the logicstatement and single setquantifiers

            if (constraint.getLogicStatement().getRequiredDescriptors().isEmpty()) {
                // haha, no requried descriptors... this is what we want!
                if (constraint.quantifiers.size() == 1) {
                    // good.
                    Quantifier q = (Quantifier) constraint.quantifiers.elementAt(0);
                    if (q instanceof SetQuantifier) {
                        SetQuantifier sq = (SetQuantifier) q;
                        SetDescriptor sd = sq.getSet();
                        candidates.put(constraint, sd);
                        continue;
                    }
                }
            }
        }

        // ok... "candidates" now has a set of constraints that have the following 
        // properties: 1) don't have any model descriptors referenced in there body
        // 2) have a single set quantifier

        // now we will loop through our rules and find any rules that produce said 
        // set descriptors

        // we can reuse the constraint iterator
        constraints = candidates.keySet().iterator();
        while (constraints.hasNext()) {
            Constraint constraint = (Constraint) constraints.next();
            SetDescriptor sd = (SetDescriptor) candidates.get(constraint);

            // now we loop through all of the rules and find rules that
            // and items to the set "sd"

            Iterator rules = state.vRules.iterator();
            while (rules.hasNext()) {
                Rule rule = (Rule) rules.next();
                mergeConstraintIntoRule(constraint, sd, rule);
            }
        }

        // no we must remove the candidates from the list of constraints
        // in state.vConstraints so they don't get built (redundant!)
        state.vConstraints.removeAll(candidates.keySet());

        
    }       

    void mergeConstraintIntoRule(Constraint constraint, SetDescriptor sd, Rule rule) {
        
        if (rule.getInclusion() instanceof SetInclusion) {
            SetInclusion si = (SetInclusion) rule.getInclusion();
            if (si.getSet() == sd) {
                // we have a match... lets convert this inclusion to a meta-inclusion
                MetaInclusion meta = new MetaInclusion();
                meta.setInclusion(si);
                meta.addConstraint(constraint);
                rule.setInclusion(meta);
            }
        } else if (rule.getInclusion() instanceof MetaInclusion) {

            MetaInclusion meta = (MetaInclusion) rule.getInclusion();
            
            if (meta.getInclusion() instanceof SetInclusion) {
                SetInclusion si = (SetInclusion) meta.getInclusion();
                if (si.getSet() == sd) {
                    // we have a match, we need to add this constraint to the current 
                    // meta inclusion
                    meta.addConstraint(constraint);
                }
            }
             
            // we need to recurse on the sub rules
            Iterator subrules = meta.rules.iterator();
            while (subrules.hasNext()) {
                mergeConstraintIntoRule(constraint, sd, (Rule)subrules.next());
            }
            
        }
    }

    public void eliminateUnusedSets() {

        // ok. so we know that rules with metainclusions have setquantifiers and that all 
        // sub rules and sub constraints use that set. however they don't need that inclusion
        // constraint... 

        // so we are going to loop through the constraints and the relations and we are going to keep 
        // track of all the required model descriptors for the top-level rules/constraints...

        // we are then going to go through all of the rules and look at the inclusion constraints 
        // if the inclusion constraint is adding to a ste that is not on the required list then
        // we are going to flag that setinclusion with a "donotstore" flag which will prevent it 
        // from adding to its respective sets

        // there is one complication... if the inclusion is in a meta inclusion then its possible that
        // the values being added to the set are not unique. ... there are two possibilities, this matters, 
        // or it doesn't matter. ok... it doesn't matter if the rules below aren't optimized by removing 
        // redundancy checks... ok... actually... i don't think it ever matters. in the worst case you'll 
        // do a lot more work than necessary because the sub rules and sub constraints could represent a lot
        // of work that you don't need to do because there is say , for example, only three distinct items in 
        // 1,000,000 instances...

        // ok... with that aside aside, we continue.

        // loop through constraints and relations and get the set of requireddescriptors

        HashSet required = new HashSet();

        Iterator allrules = state.vRules.iterator();
        while (allrules.hasNext()) {
            required.addAll( ((Rule) allrules.next()).getRequiredDescriptors());
        }
        
        Iterator allconstraints = state.vConstraints.iterator();
        while (allconstraints.hasNext()) {
            required.addAll( ((Constraint) allconstraints.next()).getRequiredDescriptors());
        }

        // ok... now the set "required" has a list of all the descriptors that do not need to be built.
        // lets now loop through the rules and look at the inclusion constraints . if a rule has an setinculsion
        // whose set is not on the list than the rule is removed. if a rule has a metainclusion whose set inclusion's
        // set is not on the list the setinclusion is marked "dostore= false"

        // reset
        allrules = state.vRules.iterator();
        while (allrules.hasNext()) {
            Rule rule = (Rule) allrules.next();
            
            if (rule.getInclusion() instanceof SetInclusion) {
                SetInclusion si = (SetInclusion) rule.getInclusion();
                if (!required.contains(si.getSet())) {
                    System.err.println("removing " + rule.getLabel());
                    // we don't need this set so remove the rule
                    allrules.remove();
                    continue;
                }
            } else if (rule.getInclusion() instanceof MetaInclusion) {
                MetaInclusion meta = (MetaInclusion) rule.getInclusion();
                // we are guanarneed taht the sub inclsion on a meta inclusion is a setinclusion
                SetInclusion si = (SetInclusion) meta.getInclusion();
                if (!required.contains(si.getSet())) {
                    // don't need this set, but need its value
                    si.dostore = false;
                    System.err.println("removing set construction for " + si.getSet().toString() + " in " + rule.getLabel());
                }
            }            
        }
    }

}

