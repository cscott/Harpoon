package MCC.IR;

class ScopeNode {
    Rule rule;
    boolean satisfy;

    public ScopeNode(Rule r,boolean satisfy) {
	rule=r;
	this.satisfy=satisfy;
    }
}
