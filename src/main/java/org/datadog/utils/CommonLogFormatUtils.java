package org.datadog.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.datadog.parser.ParseException;

/**
 * Common Log Format handling utilities class.
 */
public class CommonLogFormatUtils {

  private static final String SECTION_GROUP = "section";
  private static final Pattern SECTION_PATTERN = Pattern.compile(
      "^/(?<" + SECTION_GROUP + ">[^/]+)/.+$");

  /**
   * Retrieves an int value from a Common Log Format field.
   * @param number a {@link String} int representation
   * @param canBeMissing indicates if the value can be missing and represented by a hyphen "-".
   * @return 0 if no value is provided (represented by a hyphen "-"), the int value otherwise.
   * @throws ParseException if the number is not a valid int representation and is not a hyphen "-".
   */
  public static int retrieveIntValue(String number, boolean canBeMissing) throws ParseException {
    if (canBeMissing && number.equals("-")) {
      return 0;
    } else {
      try {
        return Integer.parseInt(number);
      } catch (NumberFormatException numberFormatException) {
        throw new ParseException(
            String.format("Invalid int value %s", number),
            numberFormatException);
      }      
    }    
  }

  /**
   * Retrieves a {@link String} value from a Common Log Format field and nullify it if not present
   *  (represented by a hyphen "-").
   * @param value a {@link String} representation of the value to retrieve.
   * @return null if the value is a hyphen "-", the {@link String} value otherwise.
   */
  public static String retrieveString(String value) {
    return value.equals("-") ? null : value;
  }

  /**
   * Retrieves the web site section from a resource url.
   * A section is defined as being what's before the second '/' in a URL.
   *  i.e. the section for "/pages/create' is "pages")
   * @param resource
   * @return
   * @throws ParseException
   */
  public static String retrieveSection(String resource) throws ParseException {
    Matcher sectionMatcher = SECTION_PATTERN.matcher(resource);
    if (sectionMatcher.find()) {
      return sectionMatcher.group(SECTION_GROUP);
    } else {
      throw new ParseException(
          String.format("Invalid resource url. Unable to retrieve section from %s", resource)
      );
    }
  }

}
