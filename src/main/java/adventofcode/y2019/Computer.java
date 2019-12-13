package adventofcode.y2019;

import static adventofcode.y2019.Base.splitAndMap;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.StrictMath.pow;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.assertj.core.util.Lists.newArrayList;

import lombok.Getter;
import lombok.ToString;

import java.util.Collection;
import java.util.List;

@ToString
class Computer {
  enum ParamMode {
    ADDRESS,
    VALUE;

    private static final List<ParamMode> MODES = newArrayList(ADDRESS, VALUE);

    public static ParamMode mode(final int codes, final int index) {
      checkArgument(index > 0, "should be positive");
      var paramCode = (codes / (int) pow(10, index - 1)) % 10;
      checkState(0 <= paramCode && paramCode <= 1, "paramCode should be 0 or 1");
      return MODES.get(paramCode);
    }
  }

  private static final int PROGRAM_COUNTER_END = -1;

  private final List<Integer> initialMemory;
  private List<Integer> inputs;
  @Getter
  private List<Integer> outputs;
  private boolean paused;
  private Integer programCounter;

  private List<Integer> runningMemory;

  private Computer(final List<Integer> memory) {
    initialMemory = newArrayList(memory);
    runningMemory = newArrayList(memory);
    inputs = newArrayList();
    outputs = newArrayList();
    programCounter = 0;
  }

  static Computer parse(final String input) {
    return new Computer(parseMemory(input));
  }

  private static List<Integer> parseMemory(final String input) {
    return splitAndMap(input, ",", Integer::parseInt);
  }

  List<Integer> execute() {
    return execute(emptyList());
  }

  List<Integer> execute(final Collection<Integer> inputs) {
    log("execute with %s", inputs);
    this.inputs = newArrayList(inputs);
    outputs = newArrayList();

    paused = false;
    while (hasNextInstruction() && !paused) {
      var opCode = valueAt(programCounter) % 100;
      switch (opCode) {
        case 99:
          terminate();
          break;
        case 1:
          sum();
          break;
        case 2:
          multiply();
          break;
        case 3:
          readInput();
          break;
        case 4:
          writeOutput();
          break;
        case 5:
          jumpIf(param(1) != 0);
          break;
        case 6:
          jumpIf(param(1) == 0);
          break;
        case 7:
          lessThan();
          break;
        case 8:
          equals();
          break;
        default:
          throw new IllegalStateException(format("OpCode %d not recognized. state=%s", opCode, this));
      }
    }
    return getOutputs();
  }

  List<Integer> getRunningMemory() {
    return unmodifiableList(runningMemory);
  }

  boolean isRunning() {
    return hasNextInstruction();
  }

  void reset() {
    runningMemory = newArrayList(initialMemory);
    programCounter = 0;
  }

  private void equals() {
    var value = param(1).equals(param(2)) ? 1 : 0;
    log("equals: %s == %s => %s", param(1), param(2), value);
    writeAt(valueAt(paramIndex(3)), value);
    incrementProgramCounter(4);
  }

  private ParamMode getMode(final int index) {
    return ParamMode.mode(valueAt(programCounter) / 100, index);
  }

  private boolean hasNextInstruction() {
    return programCounter != PROGRAM_COUNTER_END;
  }

  private void incrementProgramCounter(final int step) {
    log("PC += %s", step);
    programCounter += step;
  }

  private void jumpIf(final boolean condition) {
    log("jump-if: %s", condition);
    if (condition) {
      log("PC = %s", param(2));
      programCounter = param(2);
    } else {
      incrementProgramCounter(3);
    }
  }

  private void lessThan() {
    var value = param(1) < param(2) ? 1 : 0;
    log("less-than: %s < %s => %s", param(1), param(2), value);
    writeAt(valueAt(paramIndex(3)), value);
    incrementProgramCounter(4);
  }

  private void log(String format, Object... args) {
//    System.out.println(format(format, args));
  }

  private void multiply() {
    log("multiply: %s * %s", param(1), param(2));
    writeAt(valueAt(paramIndex(3)), param(1) * param(2));
    incrementProgramCounter(4);
  }

  private Integer param(final int index) {
    return param(index, getMode(index));
  }

  private Integer param(final int index, final ParamMode mode) {
    return mode == ParamMode.VALUE ? valueAt(paramIndex(index)) : valueAt(valueAt(paramIndex(index)));
  }

  private int paramIndex(final int index) {
    return programCounter + index;
  }

  private void readInput() {
    if (inputs.isEmpty()) {
      log("pause wating for input");
      paused = true;
      return;
    }

    var input = inputs.remove(0);
    log("read: %s", input);
    writeAt(valueAt(paramIndex(1)), input);
    incrementProgramCounter(2);
  }

  private void sum() {
    log("sum: %s + %s", param(1), param(2));
    writeAt(valueAt(paramIndex(3)), param(1) + param(2));
    incrementProgramCounter(4);
  }

  private void terminate() {
    log("terminate");
    programCounter = PROGRAM_COUNTER_END;
  }

  private Integer valueAt(final Integer index) {
    return runningMemory.get(index);
  }

  private void writeAt(final int index, final Integer value) {
    log("[%s] = %s", index, value);
    runningMemory.set(index, value);
  }

  private void writeOutput() {
    log("output: %s", param(1));
    outputs.add(param(1));
    incrementProgramCounter(2);
  }
}
