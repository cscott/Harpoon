package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
public class IntDoneContinuation extends IntContinuation implements VoidResultContinuation {

    int result;

    public IntDoneContinuation(int result) {
	this.result= result;
	Scheduler.addReady(this);
    }

    public void resume() {
	next.resume(result);
    }

    // input:  optimistic continuation (can be null)
    // output: pesimistic continuation (can't be null)
    // depressing, huh
    static public IntContinuation pesimistic(IntContinuation c)
    {
	return c!=null? c : new IntDoneContinuation(IntContinuation.result);
    }
    public void exception(Throwable t) {
    }
    private Continuation link;
    public final void setLink(Continuation c) { link= c; }
    public final Continuation getLink() { return link; }
}
