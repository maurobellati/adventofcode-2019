package adventofcode.y2019;

import static adventofcode.y2019.Base.inputForDay;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Stream.iterate;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/*
--- Day 6: Universal Orbit Map ---

You've landed at the Universal Orbit Map facility on Mercury. Because navigation in space often involves transferring between orbits, the orbit maps here are useful for finding efficient routes between, for example, you and Santa. You download a map of the local orbits (your puzzle input).

Except for the universal Center of Mass (COM), every object in space is in orbit around exactly one other object. An orbit looks roughly like this:

                  \
                   \
                    |
                    |
AAA--> o            o <--BBB
                    |
                    |
                   /
                  /

In this diagram, the object BBB is in orbit around AAA. The path that BBB takes around AAA (drawn with lines) is only partly shown. In the map data, this orbital relationship is written AAA)BBB, which means "BBB is in orbit around AAA".

Before you use your map data to plot a course, you need to make sure it wasn't corrupted during the download. To verify maps, the Universal Orbit Map facility uses orbit count checksums - the total number of direct orbits (like the one shown above) and indirect orbits.

Whenever A orbits B and B orbits C, then A indirectly orbits C. This chain can be any number of objects long: if A orbits B, B orbits C, and C orbits D, then A indirectly orbits D.

For example, suppose you have the following map:

COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L

Visually, the above map of orbits looks like this:

        G - H       J - K - L
       /           /
COM - B - C - D - E - F
               \
                I

In this visual representation, when two objects are connected by a line, the one on the right directly orbits the one on the left.

Here, we can count the total number of orbits as follows:

    D directly orbits C and indirectly orbits B and COM, a total of 3 orbits.
    L directly orbits K and indirectly orbits J, E, D, C, B, and COM, a total of 7 orbits.
    COM orbits nothing.

The total number of direct and indirect orbits in this example is 42.

What is the total number of direct and indirect orbits in your map data?

 */
class Day06 {

  static class Test {
    @org.junit.jupiter.api.Test
    void part01() {
      var input = asList("COM)B",
                         "B)C",
                         "C)D",
                         "D)E",
                         "E)F",
                         "B)G",
                         "G)H",
                         "D)I",
                         "E)J",
                         "J)K",
                         "K)L");

      assertThat(part1(buildMap(input))).isEqualTo(42);
    }

    @org.junit.jupiter.api.Test
    void part02() {
      var input = asList("COM)B",
                         "B)C",
                         "C)D",
                         "D)E",
                         "E)F",
                         "B)G",
                         "G)H",
                         "D)I",
                         "E)J",
                         "J)K",
                         "K)L",
                         "K)YOU",
                         "I)SAN");

      assertThat(part2(buildMap(input))).isEqualTo(4);
    }

  }

  public static void main(String[] args) {
    var map = buildMap(inputForDay(6));
    out.println(part1(map)); // 278744
    out.println(part2(map)); // 475
  }

  private static Map<String, String> buildMap(final List<String> input) {
    return input.stream().map(it -> it.split("\\)"))
                .collect(toMap(parts -> parts[1], parts -> parts[0]));
  }

  private static Stream<String> computeOrbitChainFrom(final String start, final Map<String, String> map) {
    return iterate(start, it -> map.containsKey(it) || map.containsValue(it), map::get);
  }

  private static Long computeOrbitDistanceBetween(final Map<String, String> map, final String x, final String y) {
    var xChain = computeOrbitChainFrom(x, map).collect(toList());
    var yChain = computeOrbitChainFrom(y, map).collect(toList());

    return xChain.stream().filter(yChain::contains)
                 .mapToLong(it -> xChain.indexOf(it) + yChain.indexOf(it))
                 .findFirst().orElseThrow();
  }

  private static Long computeTotalOrbitsCount(final Map<String, String> map) {
    return map.keySet().stream()
              .mapToLong(key -> computeOrbitDistanceBetween(map, key, "COM"))
              .sum();
  }

  private static Long part1(final Map<String, String> map) {
    return computeTotalOrbitsCount(map);
  }

  private static Long part2(final Map<String, String> map) {
    return computeOrbitDistanceBetween(map, "YOU", "SAN") - 2;
  }
}
