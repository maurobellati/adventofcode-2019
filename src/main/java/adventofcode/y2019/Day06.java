package adventofcode.y2019;

import static adventofcode.y2019.Base.inputForDay;
import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

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

      assertThat(computeTotalOrbitCount(buildMap(input))).isEqualTo(42);
    }

  }

  public static void main(String[] args) {
    var map = buildMap(inputForDay(6));
    out.println(part1(map)); // 278744
    //    out.println(part2(input));
  }

  private static Map<String, String> buildMap(final List<String> input) {
    return input.stream().map(it -> it.split("\\)"))
                .collect(toMap(parts -> parts[1], parts -> parts[0]));
  }

  private static Integer computeTotalOrbitCount(final Map<String, String> map) {
    return map.keySet().stream()
              .mapToInt(key -> countOribitFor(key, map))
              .sum();
  }

  private static int countOribitFor(final String key, final Map<String, String> map) {
    if (!map.containsKey(key)) {
      return 0;
    }
    return 1 + countOribitFor(map.get(key), map);
  }

  private static Integer part1(final Map<String, String> map) {
    return computeTotalOrbitCount(map);
  }

  private static Integer part2(final List<String> map) {
    return 0;
  }
}
