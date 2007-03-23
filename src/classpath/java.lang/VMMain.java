/** Our own private class: this is the special main thread of the program. */
package java.lang;

class VMMain implements Runnable {
    private String[] args;
    private VMMain(String[] args) { this.args = args; }
    public void run() {
	String[] args = this.args;  this.args = null;
	invokeMain(args);
    }
    /** This native method actually invokes the program's main method. */
    private static native void invokeMain(String[] args);
}
