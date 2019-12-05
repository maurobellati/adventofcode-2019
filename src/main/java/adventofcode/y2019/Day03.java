package adventofcode.y2019;

import static com.google.common.collect.Sets.newHashSet;
import static java.lang.Math.abs;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.List;
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

  Day03(final List<String> inputLines) {
    super(inputLines);
  }

  public static void main(String args[]) {
    out.println(new Day03(inputForDay(3)).part1());
    //    out.println(new Day03(inputForDay(2)).part2());
  }

  private static Point findClosestTo(final Collection<Point> points, final Point target) {
    return points.stream()
                 .min(comparing(it -> Point.manhattanDistance(it, target)))
                 .orElseThrow();
  }

  Integer part1() {
    var wires = inputStream().map(it -> splitAndMap(it, ",", Instruction::parse)).collect(toList());
    var closestIntersection = findClosestTo(intersect(wires), Point.ORIGIN);
    return Point.manhattanDistance(Point.ORIGIN, closestIntersection);
  }

  Integer part2() {
    throw new IllegalStateException("Unable to find a valid solution");
  }

  private Set<Point> intersect(final List<List<Instruction>> wires) {
    return wires.stream()
                .map(this::wirePath)
                .reduce(Sets::intersection).orElseThrow();
  }

  private Set<Point> wirePath(final List<Instruction> wire) {
    Set<Point> result = newHashSet();
    var p = Point.ORIGIN;
    for (var instruction : wire) {
      var path = p.pathTowards(instruction.getDirection(), instruction.getDistance());
      result.addAll(path.collect(toSet()));
      p = p.move(instruction);
    }
    return result;
  }

}
