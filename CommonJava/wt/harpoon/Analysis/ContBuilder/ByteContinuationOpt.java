
/**
 * ByteContinuationOpt
 *
 *
 * Created: Fri Mar 17 15:24:21 2000
 *
 * @author bdemsky
 * @version
 */

package harpoon.Analysis.ContBuilder;
public class ByteContinuationOpt extends ByteContinuation {

    public ByteContinuationOpt(byte r) {
	result=r;
	done=true;
    }
    public void exception(java.lang.Throwable t) {}

} // ByteContinuationOpt
