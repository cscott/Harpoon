package harpoon.Analysis.ContBuilder;


// adapter class. to be eliminated when we switch to optimism
// OR if we decide we stay with pesimism
public final class DoubleDoneContinuation extends DoubleContinuation implements VoidResultContinuation {

    double result;
    Throwable throwable;
    boolean isResume;

    public DoubleDoneContinuation(double result) {
	this.result= result;
	this.isResume=true;
	Scheduler.addReady(this);
    }

    public DoubleDoneContinuation(Throwable throwable) {
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


