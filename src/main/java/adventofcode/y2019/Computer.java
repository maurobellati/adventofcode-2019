package adventofcode.y2019;

import static adventofcode.y2019.Base.splitAndMap;
import static adventofcode.y2019.Computer.ParamMode.IMMEDIATE;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.lang.StrictMath.pow;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.max;
import static org.assertj.core.util.Lists.newArrayList;

import lombok.Getter;
import lombok.ToString;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.LongStream;

@ToString
class Computer {
  enum ParamMode {
    POSITION,
    IMMEDIATE,
    RELATIVE;

    private static final ParamMode[] MODES = {POSITION, IMMEDIATE, RELATIVE};

    public static ParamMode mode(final int codes, final int index) {
      checkArgument(index > 0, "should be positive");
      var paramCode = (codes / (int) pow(10, index - 1)) % 10;
      checkState(0 <= paramCode && paramCode <= MODES.length, "paramCode should be 0, 1 or 2");
      return MODES[paramCode];
    }
  }

  private static final long PROGRAM_COUNTER_END = -1L;

  private int base;
  private final Map<Long, Long> initialMemory;
  private List<Long> inputs;
  @Getter
  private List<Long> outputs;
  private boolean paused;
  private Long programCounter;

  private Map<Long, Long> runningMemory;

  private Computer(final List<Long> memory) {
    initialMemory = new HashMap<>(memory.size());
    for (var i = 0; i < memory.size(); i++) {
      initialMemory.put(Long.valueOf(i), memory.get(i));
    }
    runningMemory = new HashMap<>(initialMemory);
    programCounter = 0L;
    inputs = newArrayList();
    outputs = newArrayList();
    base = 0;
  }

  static Computer parse(final String input) {
    return new Computer(splitAndMap(input, ",", Long::parseLong));
  }

  List<Long> execute() {
    return execute(emptyList());
  }

  List<Long> execute(final Long... inputs) {
    return execute(Arrays.asList(inputs));
  }

  List<Long> execute(final Collection<Long> inputs) {
    log("execute with %s", inputs);
    this.inputs = newArrayList(inputs);
    outputs = newArrayList();

    paused = false;
    while (hasNextInstruction() && !paused) {
      Long opCode = valueAt(programCounter) % 100;
      log("");
      log("PC=%s : instruction: %s, opCode %s, base=%s", programCounter, valueAt(programCounter), opCode, base);
      switch (opCode.intValue()) {
        case 1 -> sum();
        case 2 -> multiply();
        case 3 -> readInput();
        case 4 -> writeOutput();
        case 5 -> jumpIf(paramValue(1) != 0);
        case 6 -> jumpIf(paramValue(1) == 0);
        case 7 -> lessThan();
        case 8 -> equals();
        case 9 -> setBase();
        case 99 -> terminate();
        default -> throw new IllegalStateException(format("OpCode %d not recognized. state=%s", opCode, this));
      }
    }
    return getOutputs();
  }

  List<Long> getRunningMemory() {
    var maxMemoryIndex = max(runningMemory.keySet());
    List<Long> result = newArrayList();
    LongStream.rangeClosed(0L, maxMemoryIndex).mapToObj(this::valueAt).forEachOrdered(result::add);
    return result;
  }

  boolean isRunning() {
    return hasNextInstruction();
  }

  void reset() {
    runningMemory = new HashMap<>(initialMemory);
    programCounter = 0L;
  }

  private void equals() {
    var value = paramValue(1).equals(paramValue(2)) ? 1L : 0L;
    log("equals: %s == %s => %s", paramValue(1), paramValue(2), value);
    writeAt(paramAddress(3), value);
    incrementProgramCounter(4);
  }

  private ParamMode getMode(final int index) {
    return ParamMode.mode((int) (valueAt(programCounter) / 100), index);
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
      log("PC = %s", paramValue(2));
      programCounter = paramValue(2);
    } else {
      incrementProgramCounter(3);
    }
  }

  private void lessThan() {
    var value = paramValue(1) < paramValue(2) ? 1L : 0L;
    log("less-than: %s < %s => %s", paramValue(1), paramValue(2), value);
    writeAt(paramAddress(3), value);
    incrementProgramCounter(4);
  }

  private void log(String format, Object... args) {
//    System.out.println(format(format, args));
  }

  private void multiply() {
    log("multiply: %s * %s", paramValue(1), paramValue(2));
    writeAt(paramAddress(3), paramValue(1) * paramValue(2));
    incrementProgramCounter(4);
  }

  private Long paramAddress(final int index) {
    final var result = valueAt(paramPC(index));
    return switch (getMode(index)) {
      case POSITION -> result;
      case RELATIVE -> base + result;
      case IMMEDIATE -> throw new IllegalStateException("Param is not valid for mode " + IMMEDIATE);
    };
  }

  private Long paramPC(final int index) {
    return programCounter + index;
  }

  private Long paramValue(final int index) {
    final var result = valueAt(paramPC(index));
    if (getMode(index) == IMMEDIATE) {
      return result;
    }
    return valueAt(paramAddress(index));
  }

  private void readInput() {
    if (inputs.isEmpty()) {
      log("pause wating for input");
      paused = true;
      return;
    }

    var input = inputs.remove(0);
    log("read: %s", input);
    writeAt(paramAddress(1), input);
    incrementProgramCounter(2);
  }

  private void setBase() {
    log("set-base: %s + %s = %s", base, paramValue(1), base + paramValue(1));
    base += paramValue(1);
    incrementProgramCounter(2);
  }

  private void sum() {
    log("sum: %s + %s", paramValue(1), paramValue(2));
    writeAt(paramAddress(3), paramValue(1) + paramValue(2));
    incrementProgramCounter(4);
  }

  private void terminate() {
    log("terminate");
    programCounter = PROGRAM_COUNTER_END;
  }

  private Long valueAt(final Long index) {
    return runningMemory.getOrDefault(index, 0L);
  }

  private void writeAt(final Long index, final Long value) {
    log("[%s] = %s", index, value);
    runningMemory.put(index, value);
  }

  private void writeOutput() {
    log("output: %s", paramValue(1));
    outputs.add(paramValue(1));
    incrementProgramCounter(2);
  }
}
