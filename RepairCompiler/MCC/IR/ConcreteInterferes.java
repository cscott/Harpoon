package MCC.IR;

class ConcreteInterferes {
    static public boolean interferes(MultUpdateNode mun, Rule r, boolean satisfy) {
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
		    if (r.getInclusion().usesDescriptor(updated_des))
			return true; /* Interferes with inclusion condition */
		    
		    for(int k=0;k<drule.size();k++) {
			RuleConjunction rconj=drule.get(k);
			for(int l=0;l<rconj.size();l++) {
			    DNFExpr dexpr=rconj.get(l);
			    /* See if update interfers w/ dexpr */
			    
			    if (!dexpr.getExpr().usesDescriptor(updated_des))
				continue; /* No use of the descriptor */
			    
			    return true;
			}
		    }
		}
	    }
	}
	return false;
    }
}
