package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
// OR if we decide we stay with pesimism
public final class LongDoneContinuation extends LongContinuation implements VoidResultContinuation {

    long result;

    public LongDoneContinuation(long result) {
	this.result= result;
	Scheduler.addReady(this);
    }

    public void resume() {
	next.resume(result);
    }

    // input:  optimistic continuation (can be null)
    // output: pesimistic continuation (can't be null)
    // depressing, huh
    static public LongContinuation pesimistic(LongContinuation c)
    {
	return c!=null? c : new LongDoneContinuation(LongContinuation.result);
    }
    //BCD start
    public void exception(Throwable t) {
    }
	    
    private Continuation link;
    public final void setLink(Continuation c) { link= c; }
    public final Continuation getLink() { return link; }
    //BCD end
}
