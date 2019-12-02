package adventofcode.y2019;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.readLines;

import com.google.common.base.Splitter;

import java.io.IOException;
import java.util.List;
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

  List<String> inputList() {
    return inputLines;
  }

  Stream<String> inputStream() {
    return inputLines.stream();
  }
}
