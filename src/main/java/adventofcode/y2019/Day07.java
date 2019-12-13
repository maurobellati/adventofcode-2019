package adventofcode.y2019;

import static adventofcode.y2019.Base.inputForDay;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.collect.Lists.newArrayList;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import lombok.AllArgsConstructor;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/*
--- Day 7: Amplification Circuit ---

Based on the navigational maps, you're going to need to send more power to your ship's thrusters to reach Santa in time. To do this, you'll need to configure a series of amplifiers already installed on the ship.

There are five amplifiers connected in series; each one receives an input signal and produces an output signal. They are connected such that the first amplifier's output leads to the second amplifier's input, the second amplifier's output leads to the third amplifier's input, and so on. The first amplifier's input value is 0, and the last amplifier's output leads to your ship's thrusters.

    O-------O  O-------O  O-------O  O-------O  O-------O
0 ->| Amp A |->| Amp B |->| Amp C |->| Amp D |->| Amp E |-> (to thrusters)
    O-------O  O-------O  O-------O  O-------O  O-------O

The Elves have sent you some Amplifier Controller Software (your puzzle input), a program that should run on your existing Intcode computer. Each amplifier will need to run a copy of the program.

When a copy of the program starts running on an amplifier, it will first use an input instruction to ask the amplifier for its current phase setting (an integer from 0 to 4). Each phase setting is used exactly once, but the Elves can't remember which amplifier needs which phase setting.

The program will then call another input instruction to get the amplifier's input signal, compute the correct output signal, and supply it back to the amplifier with an output instruction. (If the amplifier has not yet received an input signal, it waits until one arrives.)

Your job is to find the largest output signal that can be sent to the thrusters by trying every possible combination of phase settings on the amplifiers. Make sure that memory is not shared or reused between copies of the program.

For example, suppose you want to try the phase setting sequence 3,1,2,4,0, which would mean setting amplifier A to phase setting 3, amplifier B to setting 1, C to 2, D to 4, and E to 0.
Then, you could determine the output signal that gets sent from amplifier E to the thrusters with the following steps:

    Start the copy of the amplifier controller software that will run on amplifier A. At its first input instruction, provide it the amplifier's phase setting, 3. At its second input instruction, provide it the input signal, 0. After some calculations, it will use an output instruction to indicate the amplifier's output signal.
    Start the software for amplifier B. Provide it the phase setting (1) and then whatever output signal was produced from amplifier A. It will then produce a new output signal destined for amplifier C.
    Start the software for amplifier C, provide the phase setting (2) and the value from amplifier B, then collect its output signal.
    Run amplifier D's software, provide the phase setting (4) and input value, and collect its output signal.
    Run amplifier E's software, provide the phase setting (0) and input value, and collect its output signal.

The final output signal from amplifier E would be sent to the thrusters. However, this phase setting sequence may not have been the best one; another sequence might have sent a higher signal to the thrusters.

Here are some example programs:

    Max thruster signal 43210 (from phase setting sequence 4,3,2,1,0):

    3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0

    Max thruster signal 54321 (from phase setting sequence 0,1,2,3,4):

    3,23,3,24,1002,24,10,24,1002,23,-1,23,
    101,5,23,23,1,24,23,23,4,23,99,0,0

    Max thruster signal 65210 (from phase setting sequence 1,0,4,3,2):

    3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,
    1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0

Try every combination of phase settings on the amplifiers. What is the highest signal that can be sent to the thrusters?

*/
class Day07 {

  @AllArgsConstructor
  static class Amplifier {
    private final Computer computer;

    Integer run(final Integer phaseSetting, final Integer inputSignal) {
      var outputs = computer.execute(newArrayList(phaseSetting, inputSignal));
      return outputs.get(0);
    }
  }

  static class AmplifierSerie {
    private final List<Amplifier> amplifiers;

    AmplifierSerie(final String program, final Integer count) {
      amplifiers = range(0, count).mapToObj(i -> new Amplifier(Computer.parse(program)))
                                  .collect(toList());
    }

    Integer execute(final List<Integer> phaseSettings) {
      checkArgument(phaseSettings.size() == amplifiers.size());

      var signal = 0;
      for (var i = 0; i < phaseSettings.size(); i++) {
        var amplifier = amplifiers.get(i);
        signal = amplifier.run(phaseSettings.get(i), signal);
      }
      return signal;
    }
  }

  static class Test {
    static Stream<Arguments> examples() {
      return Stream.of(
        arguments("3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0", asList(4, 3, 2, 1, 0), 43210),
        arguments("3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0", asList(0, 1, 2, 3, 4), 54321),
        arguments("3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0", asList(1, 0, 4, 3, 2), 65210)
      );
    }

    @ParameterizedTest
    @MethodSource("examples")
    void maxSignal(final String program, final List<Integer> phaseSettings, final Integer expectedMaxSignal) {
      assertThat(findMaxSignal(program, 5)).isEqualTo(expectedMaxSignal);
    }

    @ParameterizedTest
    @MethodSource("examples")
    void signal(final String program, final List<Integer> phaseSettings, final Integer expectedMaxSignal) {
      var amplifierSerie = new AmplifierSerie(program, phaseSettings.size());
      var actual = amplifierSerie.execute(phaseSettings);
      assertThat(actual).isEqualTo(expectedMaxSignal);
    }
  }

  public static void main(String[] args) {
    var program = inputForDay(7).get(0);
    out.println(part1(program));
    //    out.println(part2(program));
  }

  private static Integer findMaxSignal(final String program, final int count) {
    var amplifierSerie = new AmplifierSerie(program, count);
    var phases = newArrayList(0, 1, 2, 3, 4);
    return Permutations.permutations(phases).mapToInt(amplifierSerie::execute).max().orElseThrow();
  }

  private static Integer part1(final String program) {
    return findMaxSignal(program, 5);
  }

  private static Collection<Integer> part2(final String program) {
    throw new IllegalStateException();
  }
}
