package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
// OR if we decide we stay with pesimism
public final class LongDoneContinuation extends LongContinuation implements VoidResultContinuation {

    long result;
    Throwable throwable;
    boolean isResume;

    public LongDoneContinuation(long result) {
	this.result= result;
	this.isResume=true;
	Scheduler.addReady(this);
    }

    public LongDoneContinuation(Throwable throwable) {
	this.throwable=throwable;
	this.isResume=false;
	Scheduler.addReady(this);
    }

    public void resume() {
	if (isResume)
	    next.resume(result);
	else
	    next.exception(throwable);
    }
    // input:  optimistic continuation (can be null)
    // output: pesimistic continuation (can't be null)
    // depressing, huh
    static public LongContinuation pesimistic(LongContinuation c)
    {
	return (!c.done)? c : new LongDoneContinuation(c.result);
    }
    
    public void exception(Throwable t) {
    }
}
