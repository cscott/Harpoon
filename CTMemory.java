package javax.realtime;

public class CTMemory extends ScopedMemory {
  public CTMemory(long size) {
	 super(size);
  }

  public String toString() {
    return "CTMemory: " + super.toString();
  }
}
