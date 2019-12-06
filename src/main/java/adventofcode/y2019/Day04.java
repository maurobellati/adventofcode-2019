package adventofcode.y2019;

import static java.lang.System.out;
import static java.util.Comparator.naturalOrder;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;

/*
--- Day 4: Secure Container ---

You arrive at the Venus fuel depot only to discover it's protected by a password. The Elves had written the password on a sticky note, but someone threw it out.

However, they do remember a few key facts about the password:

    It is a six-digit number.
    The value is within the range given in your puzzle input.
    Two adjacent digits are the same (like 22 in 122345).
    Going from left to right, the digits never decrease; they only ever increase or stay the same (like 111123 or 135679).

Other than the range rule, the following are true:

    111111 meets these criteria (double 11, never decreases).
    223450 does not meet these criteria (decreasing pair of digits 50).
    123789 does not meet these criteria (no double).

How many different passwords within the range given in your puzzle input meet these criteria?

--- Part Two ---

An Elf just remembered one more important detail: the two adjacent matching digits are not part of a larger group of matching digits.

Given this additional criterion, but still ignoring the range rule, the following are now true:

    112233 meets these criteria because the digits never decrease and all repeated digits are exactly two digits long.
    123444 no longer meets the criteria (the repeated 44 is part of a larger group of 444).
    111122 meets the criteria (even though 1 is repeated more than twice, it still contains a double 22).

How many different passwords within the range given in your puzzle input meet all of the criteria?

 */
@SuppressWarnings("FieldNamingConvention")
class Day04 {
  static class Part1Test {
    @ParameterizedTest
    @CsvSource({
                 "111111, true",
                 "223450, false",
                 "123789, false",
               })
    void examples(final Integer input, boolean expected) {
      assertThat(PasswordCriteria.part01(input)).isEqualTo(expected);
    }
  }

  static class Part2Test {
    @ParameterizedTest
    @CsvSource({
                 "112233, true",
                 "111122, true",
                 "123444, false",
                 "122234, false",
               })
    void examples(final Integer input, boolean expected) {
      assertThat(PasswordCriteria.part02(input)).isEqualTo(expected);
    }
  }

  private static class PasswordCriteria {
    static boolean part01(final Integer input) {
      var inputString = input.toString();
      return isSixDigitNumber(inputString) &&
        hasNonDecreasingDigits(inputString) &&
        hasAtLeastTwoConsecutiveSameDigits(inputString);
    }

    static boolean part02(final Integer input) {
      return part01(input) && hasExactlyTwoConsecutiveSameDigits(input.toString());
    }

    private static Multiset<Integer> allConsecutives(final String input) {
      Multiset<Integer> result = HashMultiset.create();

      var chars = input.toCharArray();
      var chainSize = 1;
      for (var i = 0; i < chars.length - 1; i++) {
        var twin = chars[i] == chars[i + 1];
        if (twin) {
          result.remove(chainSize);
          chainSize++;
          result.add(chainSize);
        } else {
          chainSize = 1;
        }
      }
      return result;
    }

    private static boolean hasAtLeastTwoConsecutiveSameDigits(final String input) {
      return allConsecutives(input).elementSet().stream()
                                   .min(naturalOrder()).map(it -> it >= 2)
                                   .orElse(false);
    }

    private static boolean hasExactlyTwoConsecutiveSameDigits(final String input) {
      return allConsecutives(input).contains(2);
    }

    private static boolean hasNonDecreasingDigits(final String input) {
      var chars = input.toCharArray();
      Arrays.sort(chars);
      return new String(chars).equals(input);
    }

    private static boolean isSixDigitNumber(final String input) {
      return input.length() == 6;
    }
  }

  public static void main(String args[]) {
    int min = 273025;
    int max = 767253;
    out.println(part1(min, max)); // 910
    out.println(part2(min, max)); // 791
  }

  private static long part1(int min, int max) {
    return ContiguousSet.closed(min, max).stream()
                        .filter(PasswordCriteria::part01)
                        .count();
  }

  private static long part2(int min, int max) {
    return ContiguousSet.closed(min, max).stream()
                        .filter(PasswordCriteria::part02)
                        .count();
  }
}
