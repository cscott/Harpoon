class Role {
    int rolenumber;
    RoleI rolei;
    String classname;
    boolean contained;
    Dominator[] dominators;
    Reference[] rolefieldlist, rolearraylist,nonnullfields, nonnullarrays;
    IdentityRelation[] identityrelations;
    String [] invokedmethods;
    public Role(RoleI rolei, int rolenumber, String classname, boolean contained, Dominator[] dominators,
		Reference[] rolefieldlist, Reference[] rolearraylist,
		IdentityRelation[] identityrelations, Reference[] nonnullfields,
		Reference[] nonnullarrays, String[] invokedmethods) {
	this.rolei=rolei;
	this.rolenumber=rolenumber;
	this.classname=classname;
	this.contained=contained;
	this.dominators=dominators;
	this.rolefieldlist=rolefieldlist;
	this.rolearraylist=rolearraylist;
	this.identityrelations=identityrelations;
	this.nonnullfields=nonnullfields;
	this.nonnullarrays=nonnullarrays;
	this.invokedmethods=invokedmethods;
    }
    public String toString() {
	String string="";
	string+="Role: R"+rolenumber+"<p>";
	string+=" Class: <a href=\""+rolei.classinfo.classes.get(classname)+".html\">"+classname+"</a><p>";
	if (contained)
	    string+=" State Merged<p>";
	if (dominators.length!=0)
	    string+="  Dominated by:<p>";
	for (int i=0;i<dominators.length;i++)
	    string+=dominators[i];
	if (rolefieldlist.length!=0)
	    string+="  Pointed to by the fields:<p>";
	for (int i=0;i<rolefieldlist.length;i++)
	    string+=rolefieldlist[i];
	if (rolearraylist.length!=0)
	    string+="  Pointed to by the arrays:<p>";
	for (int i=0;i<rolearraylist.length;i++)
	    string+=rolearraylist[i];
	if (nonnullfields.length!=0)
	    string+="  Non-null fields:<p>";
	for (int i=0;i<nonnullfields.length;i++)
	    string+=nonnullfields[i];
	if (nonnullarrays.length!=0)
	    string+="  Non-null elements:<p>";
	for (int i=0;i<nonnullarrays.length;i++)
	    string+=nonnullarrays[i];
	if (invokedmethods.length!=0)
	    string+="   Methods invoked on:<p>";
	for (int i=0;i<invokedmethods.length;i++)
	    string+=invokedmethods[i];
	return string;
    }
}
