package adventofcode.y2019;

import static adventofcode.y2019.Base.inputForDay;
import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.System.lineSeparator;
import static java.lang.System.out;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import com.google.common.base.Splitter;
import lombok.Value;
import lombok.experimental.Wither;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/*
--- Day 8: Space Image Format ---

The Elves' spirits are lifted when they realize you have an opportunity to reboot one of their Mars rovers, and so they are curious if you would spend a brief sojourn on Mars. You land your ship near the rover.

When you reach the rover, you discover that it's already in the process of rebooting! It's just waiting for someone to enter a BIOS password. The Elf responsible for the rover takes a picture of the password (your puzzle input) and sends it to you via the Digital Sending Network.

Unfortunately, images sent via the Digital Sending Network aren't encoded with any normal encoding; instead, they're encoded in a special Space Image Format. None of the Elves seem to remember why this is the case. They send you the instructions to decode it.

Images are sent as a series of digits that each represent the color of a single pixel. The digits fill each row of the image left-to-right, then move downward to the next row, filling rows top-to-bottom until every pixel of the image is filled.

Each image actually consists of a series of identically-sized layers that are filled in this way. So, the first digit corresponds to the top-left pixel of the first layer, the second digit corresponds to the pixel to the right of that on the same layer, and so on until the last digit, which corresponds to the bottom-right pixel of the last layer.

For example, given an image 3 pixels wide and 2 pixels tall, the image data 123456789012 corresponds to the following image layers:

Layer 1: 123
         456

Layer 2: 789
         012

The image you received is 25 pixels wide and 6 pixels tall.

To make sure the image wasn't corrupted during transmission, the Elves would like you to find the layer that contains the fewest 0 digits.
On that layer, what is the number of 1 digits multiplied by the number of 2 digits?

--- Part Two ---

Now you're ready to decode the image. The image is rendered by stacking the layers and aligning the pixels with the same positions in each layer.
The digits indicate the color of the corresponding pixel: 0 is black, 1 is white, and 2 is transparent.

The layers are rendered with the first layer in front and the last layer in back.
So, if a given position has a transparent pixel in the first and second layers, a black pixel in the third layer, and a white pixel in the fourth layer, the final image would have a black pixel at that position.

For example, given an image 2 pixels wide and 2 pixels tall, the image data 0222112222120000 corresponds to the following image layers:

Layer 1: 02
         22

Layer 2: 11
         22

Layer 3: 22
         12

Layer 4: 00
         00

Then, the full image can be found by determining the top visible pixel in each position:

    The top-left pixel is black because the top layer is 0.
    The top-right pixel is white because the top layer is 2 (transparent), but the second layer is 1.
    The bottom-left pixel is white because the top two layers are 2, but the third layer is 1.
    The bottom-right pixel is black because the only visible pixel in that position is 0 (from layer 4).

So, the final image looks like this:

01
10

What message is produced after decoding your image?

*/
class Day08 {
  @Value
  static class Layer {
    private static final Integer TRANSPARENT = 2;
    private static final String[] PIXEL_COLORS = {
      "■", // Black
      "□", // White
      " "  // Transparent
    };

    final int heigth;

    @Wither
    final List<Integer> pixels;

    final int width;

    @Override
    public String toString() {
      final var result = new StringBuilder();
      result.append("Layer:").append(width).append('x').append(heigth).append(lineSeparator());
      for (var i = 0; i < size(); i++) {
        result.append(PIXEL_COLORS[getPixelAt(i)]);
        if ((i + 1) % width == 0) {
          result.append(lineSeparator());
        }
      }
      return result.toString();
    }

    Integer getPixelAt(final Integer index) {
      return pixels.get(index);
    }

    Layer mergeBack(final Layer other) {
      checkArgument(size() == other.size());

      var mergedPixels = IntStream.range(0, size())
                                  .mapToObj(i -> merge(getPixelAt(i), other.getPixelAt(i)))
                                  .collect(toList());

      return withPixels(mergedPixels);
    }

    long pixelCountWithValue(final Integer value) {
      return pixels.stream().filter(it -> it.equals(value)).count();
    }

    int size() {
      return heigth * width;
    }

    private Integer merge(final Integer frontPixel, final Integer backPixel) {
      return frontPixel.equals(TRANSPARENT) ? backPixel : frontPixel;
    }

  }

  public static void main(String[] args) {
    var program = inputForDay(8).get(0);
    out.println(part1(program)); // 2760
    out.println(part2(program)); // AGUEB
  }

  private static Stream<Layer> imageLayers(final String input, final int width, final int heigth) {
    var size = width * heigth;
    return Splitter.fixedLength(size).splitToList(input).stream()
                   .map(string -> string.codePoints().mapToObj(Character::getNumericValue).collect(toList()))
                   .map(pixels -> new Layer(heigth, pixels, width));
  }

  private static long part1(final String input) {
    var layer = imageLayers(input, 25, 6).min(comparing(it -> it.pixelCountWithValue(0)))
                                         .orElseThrow();
    return layer.pixelCountWithValue(1) * layer.pixelCountWithValue(2);
  }

  private static Layer part2(final String input) {
    return imageLayers(input, 25, 6)
      .reduce(Layer::mergeBack)
      .orElseThrow();
  }

}
