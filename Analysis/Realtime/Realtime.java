package harpoon.Analysis.Realtime;
import harpoon.ClassFile.Relinker;
import harpoon.ClassFile.HClass;
import harpoon.Util.Util;

// This class is for Realtime JAVA extensions.

public class Realtime {
  public static boolean REALTIME_JAVA = false; // Is Realtime JAVA support turned on?

  public static void objectSetup(Relinker linker) {
    if (REALTIME_JAVA) {
      linker.forName("java.lang.Object").getMutator().addDeclaredField("memoryArea",
                                                                       linker.forName("realtime.MemoryArea"));
      HClass oldThread = linker.forName("java.lang.Thread");
      HClass oldThreadCopy = ((Relinker)linker).createMutableClass("java.lang.ThreadCopy", oldThread);
      HClass newThread = linker.forName("realtime.RealtimeThread");
      ((Relinker)linker).relink(oldThread, newThread);
      newThread.getMutator().setSuperclass(oldThreadCopy);
      //System.out.println(newThread.getName());
      //System.out.println(newThread.getSuperclass().getName());
      //System.out.println(newThread.getSuperclass().getSuperclass().getName());
      Util.assert(!newThread.getName().equals(newThread.getSuperclass().getName()),
                  "RealtimeThread should not inherit from itself.");
    }
  }

}
