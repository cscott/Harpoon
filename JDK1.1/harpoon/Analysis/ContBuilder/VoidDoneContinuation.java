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
	    
    private Continuation link;
    public final void setLink(Continuation c) { link= c; }
    public final Continuation getLink() { return link; }
    //BCD end
}


