package adventofcode.y2019;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.intersection;
import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.abs;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/*
--- Day 3: Crossed Wires ---

The gravity assist was successful, and you're well on your way to the Venus refuelling station. During the rush back on Earth, the fuel management system wasn't completely installed, so that's next on the priority list.

Opening the front panel reveals a jumble of wires. Specifically, two wires are connected to a central port and extend outward on a grid. You trace the path each wire takes as it leaves the central port, one wire per line of text (your puzzle input).

The wires twist and turn, but the two wires occasionally cross paths. To fix the circuit, you need to find the intersection point closest to the central port. Because the wires are on a grid, use the Manhattan distance for this measurement.
While the wires do technically cross right at the central port where they both start, this point does not count, nor does a wire count as crossing with itself.

For example, if the first wire's path is R8,U5,L5,D3, then starting from the central port (o), it goes right 8, up 5, left 5, and finally down 3:

...........
...........
...........
....+----+.
....|....|.
....|....|.
....|....|.
.........|.
.o-------+.
...........

Then, if the second wire's path is U7,R6,D4,L4, it goes up 7, right 6, down 4, and left 4:

...........
.+-----+...
.|.....|...
.|..+--X-+.
.|..|..|.|.
.|.-X--+.|.
.|..|....|.
.|.......|.
.o-------+.
...........

These wires cross at two locations (marked X), but the lower-left one is closer to the central port: its distance is 3 + 3 = 6.

Here are a few more examples:

    R75,D30,R83,U83,L12,D49,R71,U7,L72
    U62,R66,U55,R34,D71,R55,D58,R83 = distance 159
    R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51
    U98,R91,D20,R16,D67,R40,U7,R15,U6,R7 = distance 135

What is the Manhattan distance from the central port to the closest intersection?

--- Part Two ---

It turns out that this circuit is very timing-sensitive; you actually need to minimize the signal delay.

To do this, calculate the number of steps each wire takes to reach each intersection; choose the intersection where the sum of both wires' steps is lowest. If a wire visits a position on the grid multiple times, use the steps value from the first time it visits that position when calculating the total value of a specific intersection.

The number of steps a wire takes is the total number of grid squares the wire has entered to get to that location, including the intersection being considered. Again consider the example from above:

...........
.+-----+...
.|.....|...
.|..+--X-+.
.|..|..|.|.
.|.-X--+.|.
.|..|....|.
.|.......|.
.o-------+.
...........

In the above example, the intersection closest to the central port is reached after 8+5+5+2 = 20 steps by the first wire and 7+6+4+3 = 20 steps by the second wire for a total of 20+20 = 40 steps.

However, the top-right intersection is better: the first wire takes only 8+5+2 = 15 and the second wire takes only 7+6+2 = 15, a total of 15+15 = 30 steps.

Here are the best steps for the extra examples from above:

    R75,D30,R83,U83,L12,D49,R71,U7,L72
    U62,R66,U55,R34,D71,R55,D58,R83 = 610 steps
    R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51
    U98,R91,D20,R16,D67,R40,U7,R15,U6,R7 = 410 steps

What is the fewest combined steps the wires must take to reach an intersection?

 */
@SuppressWarnings("FieldNamingConvention")
class Day03 extends Base {
  /**
   * Cartesian direction
   * . U .
   * L O R
   * . D .
   */
  @AllArgsConstructor
  @Getter
  private enum Direction {
    U(new Point(0, 1)),
    D(new Point(0, -1)),
    L(new Point(-1, 0)),
    R(new Point(1, 0));

    private final Point unitVector;
  }

  static class Part1Test {
    static Stream<Arguments> examples() {
      return Stream.of(
        arguments(asList("R3, U3", "U6, R3, D3"), 6),
        arguments(asList("R8, U5, L5, D3", "U7, R6, D4, L4"), 6),
        arguments(asList("R75, D30, R83, U83, L12, D49, R71, U7, L72", "U62, R66, U55, R34, D71, R55, D58, R83"), 159),
        arguments(asList("R98, U47, R26, D63, R33, U87, L62, D20, R33, U53, R51", "U98, R91, D20, R16, D67, R40, U7, R15, U6, R7"), 135)
      );
    }

    @ParameterizedTest
    @MethodSource
    void examples(final List<String> input, Integer expected) {
      assertThat(new Day03(input).part1()).isEqualTo(expected);
    }
  }

  static class Part2Test {
    static Stream<Arguments> examples() {
      return Stream.of(
        arguments(asList("R3, U3", "U6, R3, D3"), 18),
        arguments(asList("R8, U5, L5, D3", "U7, R6, D4, L4"), 30),
        arguments(asList("R75, D30, R83, U83, L12, D49, R71, U7, L72", "U62, R66, U55, R34, D71, R55, D58, R83"), 610),
        arguments(asList("R98, U47, R26, D63, R33, U87, L62, D20, R33, U53, R51", "U98, R91, D20, R16, D67, R40, U7, R15, U6, R7"), 410)
      );
    }

    @ParameterizedTest
    @MethodSource
    void examples(final List<String> input, Integer expected) {
      assertThat(new Day03(input).part2()).isEqualTo(expected);
    }
  }

  @Value
  @AllArgsConstructor
  private static class Instruction {
    private Direction direction;
    private Integer distance;

    static Instruction parse(final String input) {
      return new Instruction(Direction.valueOf(input.substring(0, 1).toUpperCase()), Integer.parseInt(input.substring(1)));
    }

    Point toPoint() {
      return direction.getUnitVector().scale(distance);
    }
  }

  @Value
  private static class Point {
    static final Point ORIGIN = new Point(0, 0);

    private int x;
    private int y;

    private static Integer manhattanDistance(final Point a, final Point b) {
      return abs(a.getX() - b.getX()) + abs(a.getY() - b.getY());
    }

    Point move(final Direction direction) {
      return move(direction.getUnitVector().scale(1));
    }

    Point move(final Instruction instruction) {
      return move(instruction.toPoint());
    }

    Stream<Point> pathTowards(final Direction direction, final int distance) {
      return Stream.iterate(this, it -> it.move(direction))
                   .skip(1)
                   .limit(distance);
    }

    Point scale(final Integer factor) {
      return new Point(x * factor, y * factor);
    }

    private Point move(final Point delta) {
      return new Point(x + delta.getX(), y + delta.getY());
    }
  }

  @Value
  private static class Wire {
    final List<Point> points;

    Wire(final List<Point> points) {
      this.points = unmodifiableList(points);
    }

    static Wire build(final Point origin, final Collection<Instruction> instructions) {
      List<Point> result = newArrayList();
      var p = origin;
      for (var instruction : instructions) {
        var wireSegment = p.pathTowards(instruction.getDirection(), instruction.getDistance());
        result.addAll(wireSegment.collect(toList()));
        p = p.move(instruction);
      }
      return new Wire(result);
    }

    Integer distanceOf(final Point p) {
      return Optional.of(points.indexOf(p) + 1)
                     .filter(it -> it > 0)
                     .orElseThrow(() -> new IllegalStateException(p + " not found"));
    }

    Set<Point> intersect(final Wire other) {
      return intersection(newHashSet(getPoints()), newHashSet(other.getPoints()));
    }
  }

  @Value
  @AllArgsConstructor
  private static class WirePoint {
    private Point point;
    private int step;
  }

  private final Wire wire0;
  private final Wire wire1;

  Day03(final List<String> inputLines) {
    super(inputLines);
    wire0 = wire(0);
    wire1 = wire(1);
  }

  public static void main(String args[]) {
    out.println(new Day03(inputForDay(3)).part1()); // 1084
    out.println(new Day03(inputForDay(3)).part2()); // 9240
  }

  Integer part1() {
    return wire0.intersect(wire1)
                .stream()
                .mapToInt(it -> Point.manhattanDistance(it, Point.ORIGIN))
                .min()
                .orElseThrow(() -> new IllegalStateException("No intersection"));
  }

  Integer part2() {
    return wire0.intersect(wire1)
                .stream()
                .mapToInt(it -> wire0.distanceOf(it) + wire1.distanceOf(it))
                .min()
                .orElseThrow(() -> new IllegalStateException("No intersection"));
  }

  private Collection<Instruction> instructions(final int index) {
    return splitAndMap(inputList().get(index), ",", Instruction::parse);
  }

  private Wire wire(final int index) {
    return Wire.build(Point.ORIGIN, instructions(index));
  }

}
