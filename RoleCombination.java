class RoleCombination {
    int[] roleidentifiers;
    RoleI rolei;

    public RoleCombination(RoleI rolei, int[] roleids) {
	roleidentifiers=new int[roleids.length];
	for (int i=0;i<roleids.length;i++) {
	    roleidentifiers[i]=roleids[i];
	}
	if (!rolei.rolecombmap.containsKey(this)) {
	    rolei.revrolecombmap.put(new Integer(rolei.maxrolecomb), this);
	    rolei.rolecombmap.put(this, new Integer(rolei.maxrolecomb));
	    rolei.maxrolecomb++;
	}
	this.rolei=rolei;
    }

    public String shortname() {
	String base="";
	for (int i=0;i<roleidentifiers.length;i++)
	    base=base+" "+((Role)rolei.roletable.get(new Integer(roleidentifiers[i]))).shortname();
	return base;
    }

    public int hashCode() {
	int hashcode=0;
	for (int i=0;i<roleidentifiers.length;i++) {
	    hashcode^=(roleidentifiers[i]<<(i%16));
	}
	return hashcode;
    }
    public boolean equals(Object obj) {
	if (obj instanceof RoleCombination) {
	    RoleCombination rcobj=(RoleCombination) obj;
	    if (rcobj.roleidentifiers.length==roleidentifiers.length) {
		for (int i=0;i<roleidentifiers.length;i++)
		    if (rcobj.roleidentifiers[i]!=roleidentifiers[i])
			return false;
		return true;
	    }
	}
	return false;
    }
}
