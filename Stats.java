package javax.realtime;

final class Stats {
  private static long accessChecks = 0;
  private static long newObjects = 0;
  private static long newArrayObjects = 0;
  
  final static void addCheck() {
    accessChecks++;
  }

  final static void addNewObject() {
    newObjects++;
  }

  final static void addNewArrayObjects() {
    newArrayObjects++;
  }

  final static void print() {
    System.out.println("-------------------------------------");
    System.out.println("Dynamic statistics for Realtime Java:");
    System.out.println("Number of access checks: "+accessChecks);
    System.out.println("Number of objects blessed: "+newObjects);
    System.out.println("Number of array objects blessed: "+newArrayObjects);
    System.out.println("-------------------------------------");
  }


}
