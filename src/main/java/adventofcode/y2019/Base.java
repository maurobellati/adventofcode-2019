package adventofcode.y2019;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;
import static java.lang.System.lineSeparator;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Splitter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public abstract class Base {

  private static final Splitter CSV_SPLITTER = Splitter.on(",").omitEmptyStrings().trimResults();
  private final List<String> inputLines;

  Base(final List<String> inputLines) {
    this.inputLines = inputLines;
  }

  static List<String> inputForDay(final Integer day) {
    try {
      return readLines(getResource(Base.class, String.format("day-%s-input.txt", day)), UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to read the input", e);
    }
  }

  static List<String> parseCsv(final String input) {
    return CSV_SPLITTER.splitToList(input);
  }

  static List<String> lines(final String input) {
    return Splitter.on(lineSeparator()).trimResults().omitEmptyStrings().splitToList(input);
  }

  static <T> List<T> splitAndMap(String aString, String splitOn, Function<String, T> mapper) {
    return Arrays.stream(aString.split(splitOn))
                 .map(String::strip)
                 .filter(it -> !it.isBlank())
                 .map(mapper)
                 .collect(toList());
  }

  List<String> inputList() {
    return inputLines;
  }

  Stream<String> inputStream() {
    return inputLines.stream();
  }
}
