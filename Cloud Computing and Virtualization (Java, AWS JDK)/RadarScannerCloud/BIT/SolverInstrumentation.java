package BIT;

import BIT.highBIT.ClassInfo;
import BIT.highBIT.Routine;

import java.io.File;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** Solver Instrumentalist and responsible for the corresponding metrics. */
public class SolverInstrumentation {

  private static final Map<Long, SolverMetrics> solverMetricsMap = new HashMap<>();

  /**
   * Instruments the Class Files contained in the provided Directory. The Metric used is the number
   * of Routine Calls.
   *
   * @param args CommandLine arguments containing: Class files directory, solver directory.
   */
  public static void main(String[] args) {
    String className = String.format("BIT/%s", SolverInstrumentation.class.getSimpleName());
    if (args.length != 2) {
      System.out.printf("Usage [%s]: <Class Files Directory> <Solver Directory>%n", className);
      return;
    }

    for (String filename : Objects.requireNonNull((new File(args[0])).list())) {
      if (filename.endsWith(".class")) {
        System.out.printf("Instrumenting '%s'!%n", filename);
        ClassInfo classInfo =
            new ClassInfo(
                String.format("%s%s%s", args[0], System.getProperty("file.separator"), filename));
        Enumeration routines = classInfo.getRoutines().elements();

        while (routines.hasMoreElements()) {
          Routine routine = (Routine) routines.nextElement();
          routine.addBefore(className, "routineCallCount", 0);
        }

        classInfo.write(
            String.format("%s%s%s", args[1], System.getProperty("file.separator"), filename));
      }
    }
  }

  /**
   * Gets the SolverMetrics of the given request.
   *
   * @return Metrics of the Solver executing in a given thread.
   */
  private static synchronized SolverMetrics getSolverMetrics() {
    long tid = Thread.currentThread().getId();
    SolverMetrics solverMetrics = solverMetricsMap.get(tid);
    if (solverMetrics == null) {
      solverMetrics = new SolverMetrics();
      solverMetricsMap.put(tid, solverMetrics);
    }
    return solverMetrics;
  }

  /**
   * Pops the SolverMetrics of the given request.
   *
   * @return Metrics of the Solver executing in a given Thread.
   */
  public static synchronized SolverMetrics popSolverMetrics() {
    return solverMetricsMap.remove(Thread.currentThread().getId());
  }

  /**
   * Counts the number of executed routines.
   *
   * @param ignored (Unused) Merely to comply with the BIT callback specification.
   */
  public static synchronized void routineCallCount(int ignored) {
    getSolverMetrics().routineCallCount();
  }

  /**
   * Stores SolverMetrics of a given request. Containing auxiliary methods for the
   * SolverInstrumentation class.
   */
  public static class SolverMetrics {

    private long routineCallCounter = 0;

    public void routineCallCount() {
      this.routineCallCounter++;
    }

    public long getRoutineCallCounter() {
      return this.routineCallCounter;
    }

    @Override
    public String toString() {
      return String.format(
          "SolverMetrics For Thread: %d%n" + "> Routine Call Counter: %d%n",
          Thread.currentThread().getId(), this.getRoutineCallCounter());
    }
  }
}
