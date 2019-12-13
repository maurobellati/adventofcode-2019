package adventofcode.y2019;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.LongStream;
import java.util.stream.Stream;

public final class Permutations {
  private Permutations() {
  }

  public static long factorial(int n) {
    if (n > 20 || n < 0) { throw new IllegalArgumentException(n + " is out of range"); }
    return LongStream.rangeClosed(2, n).reduce(1, (x, y) -> x * y);
  }

  @SafeVarargs
  @SuppressWarnings("varargs")
  public static <T> Stream<List<T>> of(T... items) {
    return permutations(Arrays.asList(items));
  }

  public static <T> List<T> permutation(final long permutationIndex, final List<T> items) {
    return permutation(permutationIndex,
                       new LinkedList<>(requireNonNull(items)),
                       new ArrayList<>(items.size()));
  }

  public static <T> Stream<List<T>> permutations(final T[] items) {
    return permutations(Arrays.asList(items));
  }

  public static <T> Stream<List<T>> permutations(final List<T> items) {
    return LongStream.range(0, factorial(items.size()))
                     .mapToObj(index -> permutation(index, items));
  }

  private static <T> List<T> permutation(final long permutationIndex, final List<T> in, final List<T> out) {
    var index = permutationIndex;
    while (true) {
      if (in.isEmpty()) {
        return out;
      }
      var subFactorial = factorial(in.size() - 1);
      out.add(in.remove((int) (index / subFactorial)));
      index = (int) (index % subFactorial);
    }
  }

}
