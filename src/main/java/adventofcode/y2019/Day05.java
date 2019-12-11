package adventofcode.y2019;

import static adventofcode.y2019.Base.inputForDay;
import static adventofcode.y2019.Base.splitAndMap;
import static adventofcode.y2019.Day05.Computer.ParamMode.ADDRESS;
import static adventofcode.y2019.Day05.Computer.ParamMode.VALUE;
import static adventofcode.y2019.Day05.Computer.ParamMode.mode;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Queues.newArrayDeque;
import static java.lang.StrictMath.pow;
import static java.lang.String.format;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.newArrayList;

import com.google.common.base.MoreObjects;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Collection;
import java.util.List;
import java.util.Queue;

/*
--- Day 5: Sunny with a Chance of Asteroids ---

You're starting to sweat as the ship makes its way toward Mercury. The Elves suggest that you get the air conditioner working by upgrading your ship computer to support the Thermal Environment Supervision Terminal.

The Thermal Environment Supervision Terminal (TEST) starts by running a diagnostic program (your puzzle input). The TEST diagnostic program will run on your existing Intcode computer after a few modifications:

First, you'll need to add two new instructions:

    Opcode 3 takes a single integer as input and saves it to the position given by its only parameter. For example, the instruction 3,50 would take an input value and store it at address 50.
    Opcode 4 outputs the value of its only parameter. For example, the instruction 4,50 would output the value at address 50.

Programs that use these instructions will come with documentation that explains what should be connected to the input and output. The program 3,0,4,0,99 outputs whatever it gets as input, then halts.

Second, you'll need to add support for parameter modes:

Each parameter of an instruction is handled based on its parameter mode. Right now, your ship computer already understands parameter mode 0, position mode, which causes the parameter to be interpreted as a position - if the parameter is 50, its value is the value stored at address 50 in memory. Until now, all parameters have been in position mode.

Now, your ship computer will also need to handle parameters in mode 1, immediate mode. In immediate mode, a parameter is interpreted as a value - if the parameter is 50, its value is simply 50.

Parameter modes are stored in the same value as the instruction's opcode. The opcode is a two-digit number based only on the ones and tens digit of the value, that is, the opcode is the rightmost two digits of the first value in an instruction. Parameter modes are single digits, one per parameter, read right-to-left from the opcode: the first parameter's mode is in the hundreds digit, the second parameter's mode is in the thousands digit, the third parameter's mode is in the ten-thousands digit, and so on. Any missing modes are 0.

For example, consider the program 1002,4,3,4,33.

The first instruction, 1002,4,3,4, is a multiply instruction - the rightmost two digits of the first value, 02, indicate opcode 2, multiplication. Then, going right to left, the parameter modes are 0 (hundreds digit), 1 (thousands digit), and 0 (ten-thousands digit, not present and therefore zero):

ABCDE
 1002

DE - two-digit opcode,      02 == opcode 2
 C - mode of 1st parameter,  0 == position mode
 B - mode of 2nd parameter,  1 == immediate mode
 A - mode of 3rd parameter,  0 == position mode,
                                  omitted due to being a leading zero

This instruction multiplies its first two parameters. The first parameter, 4 in position mode, works like it did before - its value is the value stored at address 4 (33). The second parameter, 3 in immediate mode, simply has value 3. The result of this operation, 33 * 3 = 99, is written according to the third parameter, 4 in position mode, which also works like it did before - 99 is written to address 4.

Parameters that an instruction writes to will never be in immediate mode.

Finally, some notes:

    It is important to remember that the instruction pointer should increase by the number of values in the instruction after the instruction finishes. Because of the new instructions, this amount is no longer always 4.
    Integers can be negative: 1101,100,-1,4,0 is a valid program (find 100 + -1, store the result in position 4).

The TEST diagnostic program will start by requesting from the user the ID of the system to test by running an input instruction - provide it 1, the ID for the ship's air conditioner unit.

It will then perform a series of diagnostic tests confirming that various parts of the Intcode computer, like parameter modes, function correctly. For each test, it will run an output instruction indicating how far the result of the test was from the expected value, where 0 means the test was successful. Non-zero outputs mean that a function is not working correctly; check the instructions that were run before the output instruction to see which one failed.

Finally, the program will output a diagnostic code and immediately halt. This final output isn't an error; an output followed immediately by a halt means the program finished. If all outputs were zero except the diagnostic code, the diagnostic program ran successfully.

After providing 1 to the only input instruction and passing all the tests, what diagnostic code does the program produce?

 */
class Day05 {
  @ToString
  static class Computer {
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

    private Queue<Integer> inputs;

    @Getter
    private List<Integer> outputs;

    @Setter
    private Integer programCounter;

    private List<Integer> runningMemory;

    Computer(final List<Integer> memory) {
      initialMemory = newArrayList(memory);
      runningMemory = newArrayList(memory);
      inputs = newArrayDeque();
      outputs = newArrayList();
      programCounter = 0;
    }

    private static Computer parse(final String input) {
      return new Computer(parseMemory(input));
    }

    private static List<Integer> parseMemory(final String input) {
      return splitAndMap(input, ",", Integer::parseInt);
    }

    List<Integer> getRunningMemory() {
      return unmodifiableList(runningMemory);
    }

    Integer input() {
      return inputs.remove();
    }

    void output(Integer value) {
      outputs.add(value);
    }

    private String dump() {
      return MoreObjects.toStringHelper(this)
                        .add("pc", programCounter)
                        .add("inputs", inputs)
                        .add("outputs", outputs)
                        .add("memory", runningMemory)
                        .toString();
    }

    private void emitOutput() {
      log("output: %s", param(1));
      output(param(1));
      incrementProgramCounter(2);
    }

    private Collection<Integer> execute() {
      return execute(emptyList());
    }

    private Collection<Integer> execute(final Collection<Integer> inputs) {
      this.inputs = newArrayDeque(inputs);
      outputs = newArrayList();
      runningMemory = newArrayList(initialMemory);
      programCounter = 0;

      while (hasNextInstruction()) {
        switch (opCode()) {
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
            storeInput();
            break;
          case 4:
            emitOutput();
            break;
          default:
            throw new IllegalStateException(format("OpCode %d not recognized. state=%s", opCode(), this));
        }
      }
      return getOutputs();
    }

    private ParamMode getMode(final int index) {
      return mode(paramCodes(), index);
    }

    private boolean hasNextInstruction() {
      return programCounter != PROGRAM_COUNTER_END;
    }

    private void incrementProgramCounter(final int step) {
      programCounter += step;
    }

    private void log(String format, Object... args) {
      out.println(format(format, args));
    }

    private void multiply() {
      log("%s * %s", param(1), param(2));
      writeAt(valueAt(paramIndex(3)), param(1) * param(2));
      incrementProgramCounter(4);
    }

    private int opCode() {
      return valueAt(programCounter) % 100;
    }

    private Integer param(final int index) {
      return param(index, getMode(index));
    }

    private Integer param(final int index, final ParamMode mode) {
      return mode == VALUE ? valueAt(paramIndex(index)) : valueAtAddress(paramIndex(index));
    }

    private int paramCodes() {
      return valueAt(programCounter) / 100;
    }

    private int paramIndex(final int index) {
      return programCounter + index;
    }

    private void storeInput() {
      writeAt(valueAt(paramIndex(1)), input());
      incrementProgramCounter(2);
    }

    private void sum() {
      log("%s + %s", param(1), param(2));
      writeAt(valueAt(paramIndex(3)), param(1) + param(2));
      incrementProgramCounter(4);
    }

    private void terminate() {
      programCounter = PROGRAM_COUNTER_END;
    }

    private Integer valueAt(final Integer index) {
      return runningMemory.get(index);
    }

    private Integer valueAtAddress(final int index) {
      return valueAt(valueAt(index));
    }

    private void writeAt(final int index, final Integer value) {
      out.printf("set[%s] = %s%n", index, value);
      runningMemory.set(index, value);
    }
  }

  static class Test {
    @org.junit.jupiter.api.Test
    void parseParamMode() {
      assertThat(mode(10, 1)).isEqualTo(ADDRESS);
      assertThat(mode(10, 2)).isEqualTo(VALUE);
      assertThat(mode(10, 3)).isEqualTo(ADDRESS);
    }

    @org.junit.jupiter.api.Test
    void program_copyInputToOutput() {
      var computer = Computer.parse("3,0,4,0,99");
      var value = 7;
      assertThat(computer.execute(asList(value))).containsOnly(value);
    }

    @org.junit.jupiter.api.Test
    void program_negativeParams() {
      var computer = Computer.parse("1101,100,-1,4,0");
      computer.execute();
      assertThat(computer.getRunningMemory()).containsExactly(1101, 100, -1, 4, 99);
    }

    @org.junit.jupiter.api.Test
    void program_paramModes() {
      var computer = Computer.parse("1002,4,3,4,33");
      computer.execute();
      assertThat(computer.getRunningMemory()).containsExactly(1002, 4, 3, 4, 99);
    }
  }

  public static void main(String[] args) {
    var program = inputForDay(5).get(0);
    out.println(part1(program)); // 5346030
    //    out.println(part2(program));
  }

  private static Collection<Integer> part1(final String program) {
    return Computer.parse(program).execute(asList(1));
  }

  private static long part2(final String program) {
    throw new IllegalStateException();
  }
}
