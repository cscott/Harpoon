package MCC.IR;
import java.util.*;

public class ConcreteInterferes {
    Termination termination;

    public ConcreteInterferes(Termination t) {
	this.termination=t;
    }


    static public boolean interferesinclusion(UpdateNode un, Updates update, Rule r) {
	Descriptor updated_des=update.getDescriptor();
	Inclusion inclusion=r.getInclusion();
	if (inclusion instanceof RelationInclusion) {
	    RelationInclusion ri=(RelationInclusion)inclusion;
	    if (ri.getLeftExpr().usesDescriptor(updated_des)&&interferesinclusion(un,update,r,ri.getLeftExpr()))
		return true;
	    if (ri.getRightExpr().usesDescriptor(updated_des)&&interferesinclusion(un,update,r,ri.getRightExpr()))
		return true;
	} else if (inclusion instanceof SetInclusion) {
	    SetInclusion si=(SetInclusion)inclusion;
	    if (si.getExpr().usesDescriptor(updated_des)&&interferesinclusion(un,update,r,si.getExpr()))
		return true;
	} else throw new Error();
	return false;
    }

    static public boolean interferesinclusion(UpdateNode un, Updates update, Rule r, Expr inclusionexpr) {
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

    static public boolean interferesinclusion(UpdateNode un, Rule r, VarDescriptor dvd, VarDescriptor uvd) {
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

    /** Returns true if the data structure updates performed by mun may increase (or decrease if satisfy=false)
     * the scope of the model definition rule r. */

    public boolean interferes(MultUpdateNode mun, Rule r, boolean satisfy) {
	if (!initialinterferes(mun,r,satisfy)) /* Can't falsify a rule adding something to a set on an initial addition*/
	    return false;
	for(int i=0;i<mun.numUpdates();i++) {
	    UpdateNode un=mun.getUpdate(i);
	    for (int j=0;j<un.numUpdates();j++) {
		Updates update=un.getUpdate(j);
		
		DNFRule drule=r.getDNFGuardExpr();
		if (satisfy)
		    drule=r.getDNFNegGuardExpr();


		if (!update.isAbstract()) {
		    Descriptor updated_des=update.getDescriptor();
		    assert updated_des!=null;
		    /* Update is local to this rule, and the effect is intentional */
		    /* If we're adding something, a side effect could be to falsify some other binding
		       If we're removing something, there is no similar side effect */

		    /* ISSUE: Rules need to be updated if we allow concrete expression of the form x.f.g */
		    if (r.getInclusion().usesDescriptor(updated_des)) {
			boolean ok=false;
			if ((un.getRule()==r)&&
			    (((mun.op==MultUpdateNode.ADD)&&satisfy)
			     ||((mun.op==MultUpdateNode.REMOVE)&&!satisfy)
			     ||(mun.op==MultUpdateNode.MODIFY))) {
			    Inclusion inclusion=r.getInclusion();
			    if (inclusion instanceof RelationInclusion) {
				RelationInclusion ri=(RelationInclusion)inclusion;
				if ((!interferes(update,r,ri.getLeftExpr()))&&
				    (!interferes(update,r,ri.getRightExpr())))
				    ok=true;
			    } else if (inclusion instanceof SetInclusion) {
				SetInclusion si=(SetInclusion)inclusion;
				if (!interferes(update,r,si.getExpr()))
				    ok=true;
			    } else throw new Error();
			}
			if (!ok) {
			    if (satisfy) {
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
				if (!interferes(update,r,dexpr.getExpr()))
				    continue; /* unique state - we're okay */
			    }
			    if (interferes(un,update, r,dexpr))
				return true;
			}
		    }
		}
	    }
	}
	return false;
    }

    private boolean interferes(Updates u, Rule r, Expr e) {
	Set exprs=e.useDescriptor(u.getDescriptor());
	for(Iterator eit=exprs.iterator();eit.hasNext();) {
	    Expr e2=(Expr)eit.next();
	    if (mayinterfere(u,r,e2))
		return true;
	}
	return false;
    }

    private boolean mayinterfere(Updates u, Rule r, Expr e) {
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
    
    static private boolean initialinterferes(MultUpdateNode mun, Rule r, boolean satisfy) {
	AbstractRepair ar=mun.getRepair();
	if (satisfy)
	    return true;
	if (ar==null)
	    return true;
	if (ar.getType()!=AbstractRepair.ADDTOSET)
	    return true;
	//	if (mun.op!=MultUpdateNode.ADD)  (Redundant)
	//    return true;
	if (!r.getInclusion().getTargetDescriptors().contains(ar.getDescriptor()))
	    return true;
	boolean negated=ar.getPredicate().isNegated();
	Predicate p=ar.getPredicate().getPredicate();
	if (!(p instanceof ExprPredicate))
	    return true;
	ExprPredicate ep=(ExprPredicate)p;
	if (ep.getType()!=ExprPredicate.SIZE)
	    return true;
	if ((ep.getOp()==Opcode.EQ)&&(ep.rightSize()==1)&&!negated)
	    return false;
	if ((ep.getOp()==Opcode.NE)&&(ep.rightSize()==1)&&negated)
	    return false;

	if ((ep.getOp()==Opcode.NE)&&(ep.rightSize()==0)&&!negated)
	    return false;
	if ((ep.getOp()==Opcode.EQ)&&(ep.rightSize()==0)&&negated)
	    return false;



	if ((ep.getOp()==Opcode.GT)&&(ep.rightSize()==0)&&!negated)
	    return false;
	if ((ep.getOp()==Opcode.LE)&&(ep.rightSize()==0)&&negated)
	    return false;

	if ((ep.getOp()==Opcode.GE)&&(ep.rightSize()==1)&&!negated)
	    return false;
	if ((ep.getOp()==Opcode.LT)&&(ep.rightSize()==1)&&negated)
	    return false;
	
	return true;

	
    }

    static private boolean interferes(UpdateNode un,Updates update, Rule r,DNFExpr dexpr) {
	Descriptor descriptor=update.getDescriptor();
	if (!dexpr.getExpr().usesDescriptor(descriptor))
	    return false;
	    
	/* We need to pair the variables */
	if (update.isExpr()) {
	    Set vars=update.getRightExpr().freeVars();
	    Opcode op1=update.getOpcode();
	    Expr lexpr1=update.getLeftExpr();
	    Expr rexpr1=update.getRightExpr();
	    boolean good=true;
	    if (vars!=null)
		for(Iterator it=vars.iterator();it.hasNext();) {
		    VarDescriptor vd=(VarDescriptor) it.next();
		    if (!vd.isGlobal()) {
			/* VarDescriptor isn't a global */
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

		good=true;
		vars=rexpr2.freeVars();
		VarDescriptor leftdescriptor=null;
		if (lexpr2 instanceof VarExpr)
		    leftdescriptor=((VarExpr)lexpr2).getVar();
		else if (lexpr2 instanceof DotExpr) {
		    Expr e=lexpr2;
		    do {
			for(;e instanceof DotExpr;e=((DotExpr)e).getExpr()) ;
			if (e instanceof VarExpr)
			    break;
			if (e instanceof CastExpr)
			    e=((CastExpr)e).getExpr();
			else throw new Error("Bad Expr Type:"+e.name());
		    } while (true);
		    leftdescriptor=((VarExpr)e).getVar();
		} else throw new Error("Bad Expr");
		
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
