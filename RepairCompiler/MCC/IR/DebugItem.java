package MCC.IR;

public class DebugItem {
    int depth;
    int constraintnum;
    int conjunctionnum;

    public DebugItem(int d,int num) {
	depth=d;
	constraintnum=num;
	conjunctionnum=-1;
    }

    public DebugItem(int d,int num,int conj) {
	depth=d;
	constraintnum=num;
	conjunctionnum=conj;
    }

}
