package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
// OR if we decide we stay with pesimism
public final class ShortDoneContinuation extends ShortContinuation implements VoidResultContinuation {

    short result;
    Throwable throwable;
    boolean isResume;

    public ShortDoneContinuation(short result) {
	this.result= result;
	this.isResume=true;
	Scheduler.addReady(this);
    }

    public ShortDoneContinuation(Throwable throwable) {
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


    //BCD start
    public void exception(Throwable t) {
    }
	    
    //BCD end
}


