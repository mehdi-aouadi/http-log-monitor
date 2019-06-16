package org.datadog.utils;

public class GuiFormatUtils {

  /**
   * Converts byte size to human readable size with unit.
   * @param bytes Number of bytes.
   * @return a {@link String} containing the human readable representation.
   */
  public static String humanReadableByteCount(long bytes) {
    int unit = 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    int exp = (int) (Math.log(bytes) / Math.log(unit));
    String pre = ("KMGTPE").charAt(exp - 1) + ("i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }
}
