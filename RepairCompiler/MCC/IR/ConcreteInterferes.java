package MCC.IR;
import java.util.*;

public class ConcreteInterferes {
    Termination termination;

    public ConcreteInterferes(Termination t) {
	this.termination=t;
    }

    /** Returns true if the data structure updates performed by mun
     * may increase (or decrease if satisfy=false) the scope of the
     * model definition rule r. */

    public boolean interferes(MultUpdateNode mun, Rule r, boolean satisfy) {
	
	// A rule that adds something to a set can't be falsified by
	// an update that is only performed if the set is empty
	if (!initialinterferes(mun,r,satisfy)) 
	    return false;

	for(int i=0;i<mun.numUpdates();i++) {
	    UpdateNode un=mun.getUpdate(i);
	    for (int j=0;j<un.numUpdates();j++) {
		Updates update=un.getUpdate(j);
		//Abstract updates don't have concrete interference1
		if (update.isAbstract()) 
		    continue;

		DNFRule drule=satisfy?r.getDNFNegGuardExpr():r.getDNFGuardExpr();
		Descriptor updated_des=update.getDescriptor();
		assert updated_des!=null;

		/* Test to see if the update only effects new
		   objects and we're only testing for falsifying
		   model definition rules. */
		
		if ((!satisfy)&&updateonlytonewobject(mun,un,update))
		    continue;


		// See if the update interferes with the inclusion
		// condition for the rule
		if (r.getInclusion().usesDescriptor(updated_des)) {
		    boolean ok=false;

		    /* If the update is for this rule, and the effect
                       is the intended effect, and the update only
                       effects one binding, then the abstract repair
                       node already models the action of this
                       update. */

		    if ((un.getRule()==r)&& 
			(((mun.op==MultUpdateNode.ADD)&&satisfy)
			 ||((mun.op==MultUpdateNode.REMOVE)&&!satisfy)
			 ||(mun.op==MultUpdateNode.MODIFY))) {
			Inclusion inclusion=r.getInclusion();
			if (inclusion instanceof RelationInclusion) {
			    RelationInclusion ri=(RelationInclusion)inclusion;
			    if ((!testdisjoint(update,r,ri.getLeftExpr()))&&
				(!testdisjoint(update,r,ri.getRightExpr())))
				ok=true;
			} else if (inclusion instanceof SetInclusion) {
			    SetInclusion si=(SetInclusion)inclusion;
			    if (!testdisjoint(update,r,si.getExpr()))
				ok=true;
			} else throw new Error();
		    }

		    if ((un.getRule()==r)&&
			((mun.op==MultUpdateNode.ADD)&&!satisfy)&&
			modifiesremoves(mun,un,r)) {
			Inclusion inclusion=r.getInclusion();
			if (inclusion instanceof RelationInclusion) {
			    RelationInclusion ri=(RelationInclusion)inclusion;
			    if ((!testdisjoint(update,r,ri.getLeftExpr()))&&
				(!testdisjoint(update,r,ri.getRightExpr())))
				ok=true;	 /* Update is specific to
						    given binding of the rule,
						    and adds are only performed
						    if the removal is desired or
						    the tuple doesn't exist.*/
			}
		    }

		    if (!ok) {
			if (satisfy) {
			    /** Check to see if the update definitely falsifies r, thus
			     * can't accidentally satisfy it r. */
			    if (interferesinclusion(un, update, r)) 
				return true;
			} else
			    return true; /* Interferes with inclusion condition */
		    }
		}
		
		for(int k=0;k<drule.size();k++) {
		    RuleConjunction rconj=drule.get(k);
		    for(int l=0;l<rconj.size();l++) {
			DNFExpr dexpr=rconj.get(l);
			/* See if update interferes w/ dexpr */
			if ((un.getRule()==r)&&
			    (((mun.op==MultUpdateNode.ADD)&&satisfy)
			     ||((mun.op==MultUpdateNode.REMOVE)&&!satisfy)
			     ||(mun.op==MultUpdateNode.MODIFY))) {
			    if (!testdisjoint(update,r,dexpr.getExpr()))
				continue; /* Update is specific to
                                             given binding of the
                                             rule, and effect is the
                                             intended one, so we're
                                             okay */
			}
			if ((un.getRule()==r)&&
			    ((mun.op==MultUpdateNode.ADD)&&!satisfy)&&
			    modifiesremoves(mun,un,r)) {
			    if (!testdisjoint(update,r,dexpr.getExpr()))
				continue; /* Update is specific to
                                             given binding of the
                                             rule, and adds are only
                                             performed if the removal
                                             is desired or the tuple
                                             doesn't exist.*/
			}

			if (interferes(update,dexpr))
			    return true;
		    }
		}	    
	    }
	}
	return false;
    }


    static private boolean modifiesremoves(MultUpdateNode mun,UpdateNode un, Rule r) {
	AbstractRepair ar=mun.getRepair();
	boolean inverted=ar.getPredicate().getPredicate().inverted();

	if (ar.getType()!=AbstractRepair.MODIFYRELATION)
	    return false;
	RelationInclusion ri=(RelationInclusion)r.getInclusion();
	Expr e=inverted?ri.getRightExpr():ri.getLeftExpr();
	while(e instanceof CastExpr) {
	    e=((CastExpr)e).getExpr();
	}
	if (!(e instanceof VarExpr))
	    return false;
	VarExpr ve=(VarExpr)e;
	if (ve.isValue())
	    return false;
	return true;
    }
    
    static private boolean updateonlytonewobject(MultUpdateNode mun, UpdateNode un, Updates updates) {
	AbstractRepair ar=mun.getRepair();
	if ((ar!=null)&&(ar.getType()==AbstractRepair.ADDTOSET||ar.getType()==AbstractRepair.ADDTORELATION))
	    for(int i=0;i<un.numUpdates();i++) {
		Updates u=un.getUpdate(i);
		if (u.getType()==Updates.POSITION&&
		    ar.isNewObject(u.getRightPos()==0)) {
		    Expr newleftexpr=u.getLeftExpr();
		    Expr leftexpr=updates.getLeftExpr();
		    boolean foundfield=false;
		    while(true) {
			if (leftexpr.equals(null,newleftexpr)) {
			    if (foundfield)
				return true;
			    else
				break;
			} else if (leftexpr instanceof DotExpr) {
			    if (!foundfield) {
				foundfield=true;
			    } else {
				if (((DotExpr)leftexpr).isPtr())
				    break; //if its not a pointer, we're still in the structure
			    }
			    leftexpr=((DotExpr)leftexpr).getExpr();
			} else if (leftexpr instanceof CastExpr) {
			    leftexpr=((CastExpr)leftexpr).getExpr();
			} else
			    break;
		    }
		}
	    }
	
	return false;
    }

    /** This method tries to show that if the Update update from the
     *  UpdateNode un changes the value of the inclusion constraint
     * that it falsifies the guard of model definition rule. */
    
    static private boolean interferesinclusion(UpdateNode un, Updates update, Rule r) {
	Descriptor updated_des=update.getDescriptor();
	Inclusion inclusion=r.getInclusion();
	if (inclusion instanceof RelationInclusion) {
	    RelationInclusion ri=(RelationInclusion)inclusion;
	    if (ri.getLeftExpr().usesDescriptor(updated_des)
		&&searchinterfere(un,update,r,ri.getLeftExpr()))
		return true;
	    if (ri.getRightExpr().usesDescriptor(updated_des)
		&&searchinterfere(un,update,r,ri.getRightExpr()))
		return true;
	} else if (inclusion instanceof SetInclusion) {
	    SetInclusion si=(SetInclusion)inclusion;
	    if (si.getExpr().usesDescriptor(updated_des)
		&&searchinterfere(un,update,r,si.getExpr()))
		return true;
	} else throw new Error();
	return false;
    }

    /** This method finds all instances of a field or global that an
     * update may modify, and builds a variable correspondance */

    static private boolean searchinterfere(UpdateNode un, Updates update, Rule r, Expr inclusionexpr) {
	Descriptor updated_des=update.getDescriptor();
	if (updated_des instanceof FieldDescriptor) {
	    /* Build variable correspondance */
	    HashSet set=new HashSet();
	    inclusionexpr.findmatch(updated_des,set);
	    
	    for(Iterator matchit=set.iterator();matchit.hasNext();) {
		DotExpr dotexpr=(DotExpr)matchit.next();
		DotExpr updateexpr=(DotExpr)update.getLeftExpr();
		while(true) {
		    if (dotexpr.getField()!=updateexpr.getField())
			return true;
		    Expr de=dotexpr.getExpr();
		    Expr ue=updateexpr.getExpr();
		    if ((de instanceof DotExpr)&&(ue instanceof DotExpr)) {
			dotexpr=(DotExpr)de;
			updateexpr=(DotExpr)ue;
		    } else if ((de instanceof VarExpr)&&(ue instanceof VarExpr)) {
			VarDescriptor dvd=((VarExpr)de).getVar();
			VarDescriptor uvd=((VarExpr)ue).getVar();
			if (interferesinclusion(un,r,dvd,uvd))
			    return true;
			else
			    break;
		    } else
			return true;
		}
	    }
	} else if (updated_des instanceof VarDescriptor) {
	    /* We have a VarDescriptor - no correspondance necessary */
	    VarDescriptor vd=(VarDescriptor)updated_des;
	    if (interferesinclusion(un,r,vd,vd))
		return true;
	} else throw new Error();
	return false;
    }

    /** This method tries to show that if dvd=uvd, then the update un
     *  must falsify the rule r. */

    static private boolean interferesinclusion(UpdateNode un, Rule r, VarDescriptor dvd, VarDescriptor uvd) {
	DNFRule negrule=r.getDNFNegGuardExpr();
	HashMap remap=new HashMap();
	remap.put(dvd,uvd);

	for(int i=0;i<negrule.size();i++) {
	    RuleConjunction rconj=negrule.get(i);
	    boolean good=true;
	    for(int j=0;j<rconj.size();j++) {
		DNFExpr dexpr=rconj.get(j);
		if (dexpr.getExpr() instanceof OpExpr) {
		    OpExpr expr=(OpExpr)dexpr.getExpr();
		    Expr lexpr=expr.getLeftExpr();
		    Expr rexpr=expr.getRightExpr();

		    boolean varok=true;
		    Set vars=rexpr.freeVars();
		    if (vars!=null) 
			for(Iterator it=vars.iterator();it.hasNext();) {
			    VarDescriptor vd=(VarDescriptor) it.next();
			    if (!vd.isGlobal()) {
				/* VarDescriptor isn't a global */
				if (dvd!=vd) {
				    varok=false;
				    break;
				}
			    }
			}
		    
		    if (!varok)
			continue;

		    Opcode op=expr.getOpcode();
		    op=Opcode.translateOpcode(dexpr.getNegation(),op);

		    boolean match=false;
		    for(int k=0;k<un.numUpdates();k++) {
			Updates update=un.getUpdate(k);
			if(update.isExpr()) {
			    Set uvars=update.getRightExpr().freeVars();
			    boolean freevarok=true;
			    if (uvars!=null)
			    for(Iterator it=uvars.iterator();it.hasNext();) {
				VarDescriptor vd=(VarDescriptor) it.next();
				if (!vd.isGlobal()) {
				    /* VarDescriptor isn't a global */
				    if (uvd!=vd) {
					freevarok=false;
					break;
				    }
				}
			    }
			    if (!freevarok)
				continue;

			    Opcode op2=update.getOpcode();
			    if ((op2==op)||
				((op2==Opcode.GT)&&(op==Opcode.GE))||
				((op2==Opcode.LT)&&(op==Opcode.LE))||
				((op2==Opcode.EQ)&&((op==Opcode.GE)||(op==Opcode.LE)))) {
				/* Operations match*/
				if (lexpr.equals(remap,update.getLeftExpr())&&
				    rexpr.equals(remap,update.getRightExpr())) {
				    match=true;
				    break;
				}				    
			    }
			} 
		    }
		    if (!match) {
			good=false;
			break;
		    }
		} else {
		    /* TODO: Check to see if there is an abstract repair */
		    good=false;
		    break; /* try next conjunction */
		}
	    }
	    if (good)
		return false;
	}
	return true;
    }

    /** This method checks whether the update effects only the
     *  intended binding for the model definition rule. */

    private boolean testdisjoint(Updates u, Rule r, Expr e) {
	// find all exprs that may be be effected by update
	Set exprs=e.useDescriptor(u.getDescriptor());
	for(Iterator eit=exprs.iterator();eit.hasNext();) {
	    Expr e2=(Expr)eit.next();
	    if (testdisjointness(u,r,e2))
		return true;
	}
	return false;
    }

    /** This method tries to show that the modification only effects
     * one binding of the model definition rule, and thus has no
     * unintended side effects. */

    private boolean testdisjointness(Updates u, Rule r, Expr e) {
	// Note: rule of u must be r

	Expr update_e=u.getLeftExpr();
	HashSet quantifierset=new HashSet();

	if (termination.analyzeQuantifiers(r,quantifierset))
	    return false; /* Can't accidentally interfere with other bindings if there aren't any! */

	boolean firstfield=true;
	

	while(true) {
	    if (update_e instanceof CastExpr)
		update_e=((CastExpr)update_e).getExpr();
	    else if (e instanceof CastExpr)
		e=((CastExpr)e).getExpr();
	    else if ((update_e instanceof DotExpr)&&(e instanceof DotExpr)) {
		DotExpr de1=(DotExpr)update_e;
		DotExpr de2=(DotExpr)e;
		if (de1.isValue(null)&&!firstfield)
		    return true; /* Could have aliasing from this */
		if (de1.getField()!=de2.getField())
		    return true; /* Different fields: not comparible */
		firstfield=false;

		Expr index1=de1.getIndex();
		Expr index2=de2.getIndex();
		if (index1!=null) {
		    assert index2!=null;
		    if ((index1 instanceof VarExpr)&&(index2 instanceof VarExpr)) {
			VarDescriptor vd1=((VarExpr)index1).getVar();
			VarDescriptor vd2=((VarExpr)index2).getVar();
			if ((vd1==vd2)&&!vd1.isGlobal()) {
			    quantifierset.add(getQuantifier(r,vd1));
			    if (termination.analyzeQuantifiers(r,quantifierset))
				return false; /* State is disjoint from any other example */
			}
		    }
		}
		update_e=de1.getExpr();
		e=de2.getExpr();
	    } else if ((update_e instanceof VarExpr)&&(e instanceof VarExpr)) {
		VarDescriptor vd1=((VarExpr)update_e).getVar();
		VarDescriptor vd2=((VarExpr)e).getVar();
		if ((vd1==vd2)&&!vd1.isGlobal()) {
		    quantifierset.add(getQuantifier(r,vd1));
		    if (termination.analyzeQuantifiers(r,quantifierset))
			return false; /* State is disjoint from any other example */
		}
		return true;
	    } else return true;
	}
    }

    /** This method returns the quantifier that defines the quantifier
     * variable vd. */
    static private Quantifier getQuantifier(Quantifiers qs, VarDescriptor vd) {
	for (int i=0;i<qs.numQuantifiers();i++) {
	    Quantifier q=qs.getQuantifier(i);
	    if (q instanceof SetQuantifier) {
		SetQuantifier sq=(SetQuantifier)q;
		if (sq.getVar()==vd)
		    return sq;
	    } else if (q instanceof RelationQuantifier) {
		RelationQuantifier rq=(RelationQuantifier)q;
		if ((rq.x==vd)||(rq.y==vd))
		    return rq;
	    } else if (q instanceof ForQuantifier) {
		ForQuantifier fq=(ForQuantifier)q;
		if (fq.getVar()==vd)
		    return fq;
	    }
	}
	return null;
    }
    
    /** This function checks to see if an update is only performed if
     * a given set (or image set produced by a relation) is empty, and
     * the algorithm is computing whether the update may falsify a
     * rule that adds something to the set */

    private boolean initialinterferes(MultUpdateNode mun, Rule r, boolean satisfy) {
	AbstractRepair ar=mun.getRepair();
	if ((!satisfy)&&(ar!=null)&&(ar.getType()==AbstractRepair.ADDTOSET)) {
	    if (!r.getInclusion().getTargetDescriptors().contains(ar.getDescriptor()))
		return true;
	    boolean negated=ar.getPredicate().isNegated();
	    Predicate p=ar.getPredicate().getPredicate();
	    if (!(p instanceof ExprPredicate))
		return true;
	    ExprPredicate ep=(ExprPredicate)p;
	    if (ep.getType()!=ExprPredicate.SIZE)
		return true;
	    Opcode op=Opcode.translateOpcode(negated,ep.getOp());
   
	    if (((op==Opcode.EQ)&&(ep.rightSize()==1))|| //(=1)
		((op==Opcode.NE)&&(ep.rightSize()==0))|| //(!=0)
		((op==Opcode.GT)&&(ep.rightSize()==0))|| //(>0)
		((op==Opcode.GE)&&(ep.rightSize()==1))) //(>=1)
		return false;
	} else if ((!satisfy)&&(ar!=null)&&(ar.getType()==AbstractRepair.ADDTORELATION)) {
	    /* This test is for image sets of relations. */
	    if (!r.getInclusion().getTargetDescriptors().contains(ar.getDescriptor()))
		return true;
	    boolean negated=ar.getPredicate().isNegated();
	    Predicate p=ar.getPredicate().getPredicate();
	    if (!(p instanceof ExprPredicate))
		return true;
	    ExprPredicate ep=(ExprPredicate)p;
	    if (ep.getType()!=ExprPredicate.SIZE)
		return true;

	    Opcode op=Opcode.translateOpcode(negated,ep.getOp());
 	    if (!(((op==Opcode.EQ)&&(ep.rightSize()==1))|| //(=1)
		((op==Opcode.NE)&&(ep.rightSize()==0))|| //(!=0)
		((op==Opcode.GT)&&(ep.rightSize()==0))|| //(>0)
		((op==Opcode.GE)&&(ep.rightSize()==1)))) //(>=1)
		return true;

	    RelationInclusion ri=(RelationInclusion)r.getInclusion();
	    Expr tmpve=ep.inverted()?ri.getRightExpr():ri.getLeftExpr();
	    if (!(tmpve instanceof VarExpr))
		return true;
	    for(int i=0;i<mun.numUpdates();i++) {
		UpdateNode un=mun.getUpdate(i);
		for (int j=0;j<un.numUpdates();j++) {
		    Updates update=un.getUpdate(j);
		    //Abstract updates don't have concrete interference1
		    if (update.isAbstract()) 
			continue;
		    if (testdisjoint(update, r, r.getGuardExpr()))
			return true;
		}
	    }
	    return false;
   	}
	return true;
    }

    static private boolean interferes(Updates update, DNFExpr dexpr) {
	Descriptor descriptor=update.getDescriptor();
	/* If the DNFExpr expr doesn't use the updated descriptor,
	   there is no interference. */
	if (!dexpr.getExpr().usesDescriptor(descriptor))
	    return false;
	    
	if (update.isExpr()) {
	    /* We need to pair the variables */
	    Set vars=update.getRightExpr().freeVars();
	    Opcode op1=update.getOpcode();
	    Expr lexpr1=update.getLeftExpr();
	    Expr rexpr1=update.getRightExpr();
	    boolean good=true;
	    if (vars!=null)
		for(Iterator it=vars.iterator();it.hasNext();) {
		    VarDescriptor vd=(VarDescriptor) it.next();
		    /* VarDescriptor isn't a global */
		    if (!vd.isGlobal()) {
			if (update.getVar()!=vd) {
			    good=false;
			    break;
			}
		    }
		}
	    if (good&&(dexpr.getExpr() instanceof OpExpr)) {
		OpExpr expr=(OpExpr)dexpr.getExpr();
		Expr lexpr2=expr.getLeftExpr();
		Expr rexpr2=expr.getRightExpr();
		Opcode op2=expr.getOpcode();
		op2=Opcode.translateOpcode(dexpr.getNegation(),op2);

		VarDescriptor leftdescriptor=null;
		{
		    Expr e=lexpr2;
		    while(!(e instanceof VarExpr)) {
			for(;e instanceof DotExpr;e=((DotExpr)e).getExpr()) ;
			if (e instanceof VarExpr)
			    break;
			if (e instanceof CastExpr)
			    e=((CastExpr)e).getExpr();
			else throw new Error("Bad Expr Type:"+e.name());
		    }
		    leftdescriptor=((VarExpr)e).getVar();
		}

		vars=rexpr2.freeVars();
		if (vars!=null)
		    for(Iterator it=vars.iterator();it.hasNext();) {
			VarDescriptor vd=(VarDescriptor) it.next();
			if (!vd.isGlobal()) {
			    /* VarDescriptor isn't a global */
			    if (leftdescriptor!=vd) {
				good=false;
				break;
			    }
			}
		    }


		if (good) {
		    HashMap remap=new HashMap();
		    remap.put(update.getVar(),leftdescriptor);
		    if ((op1==op2)&&
			lexpr1.equals(remap,lexpr2)&&
			rexpr1.equals(remap,rexpr2))
			return false;
		}
	    }
	}
	return true;
    }
}
