package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
// OR if we decide we stay with pesimism
public final class VoidDoneContinuation extends VoidContinuation implements VoidResultContinuation {

    Throwable throwable;
    boolean isResume;

    public VoidDoneContinuation() {
	this.isResume=true;
	Scheduler.addReady(this);
    }

    public VoidDoneContinuation(boolean schedule) {
	this.isResume=true;
	if (schedule)
	    Scheduler.addReady(this);
    }

    public VoidDoneContinuation(Throwable throwable) {
	this.throwable=throwable;
	this.isResume=false;
	Scheduler.addReady(this);
    }

    // input:  optimistic continuation (can be null)
    // output: pesimistic continuation (can't be null)
    // depressing, huh
    static public VoidContinuation pesimistic(VoidContinuation c)
    {
	return (!c.done)? c : new VoidDoneContinuation();
    }

    public void resume() {
	if (next!=null) {
	    if (isResume)
		next.resume();
	    else
		next.exception(throwable);
	} else {
	    if (!isResume) {
		if (throwable instanceof Error)
		   throw (Error)throwable;
		else if (throwable instanceof RuntimeException)
		   throw (RuntimeException)throwable;
		else {
		   throwable.printStackTrace();
		   System.out.println(throwable);
		}
	    }
	}
    }


    //BCD start
    public void exception(Throwable t) {
    }
	    
    //BCD end
}


