class View {
    RoleUniverse [] universes;
    RoleI rolei;

    public View(RoleI rolei,RoleUniverse[] universes) {	
	this.rolei=rolei;
	this.universes=new RoleUniverse[universes.length];
	for(int i=0;i<universes.length;i++)
	    this.universes[i]=universes[i];
    }

    public RoleCombination project(Role alluniverses) {
	int [] roles=new int[universes.length];
	for(int i=0;i<universes.length;i++) {
	    roles[i]=alluniverses.project(universes[i]).rolenumber;
	}
	return new RoleCombination(rolei,roles);
    }
}
