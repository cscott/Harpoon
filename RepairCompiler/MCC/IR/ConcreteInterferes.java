package MCC.IR;

class ConcreteInterferes {
    static public boolean interferes(MultUpdateNode mun, Rule r, boolean satisfy) {
	for(int i=0;i<mun.numUpdates();i++) {
	    UpdateNode un=mun.getUpdate(i);
	    for (int j=0;j<un.numUpdates();j++) {
		Updates update=un.getUpdate(j);
		Descriptor des=update.getDescriptor();
		DNFRule drule=r.getDNFGuardExpr();
		for(int k=0;k<drule.size();k++) {
		    RuleConjunction rconj=drule.get(k);
		    for(int l=0;l<rconj.size();l++) {
			DNFExpr dexpr=rconj.get(l);
			
		    }
		}
	    }
	}
	return false;
    }


}
