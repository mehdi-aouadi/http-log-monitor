package org.datadog.utils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import lombok.NonNull;

import org.datadog.parser.ParseException;

/**
 * DateTime parsing utilities class.
 */
public class DateTimeUtils {

  public static final DateTimeFormatter DATE_TIME_FORMATTER
      = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");

  /**
   * Parses an strftime formatted {@link String} to a {@link ZonedDateTime}.
   * @param date strftime formatted {@link String}
   * @return a {@link ZonedDateTime} parsed from the strftime formatted {@link String}
   * @throws ParseException if the {@link String} to parse doesn't respect the strftime format
   * @throws NullPointerException if the {@link String} to parse is null
   */
  public static ZonedDateTime retrieveLogDateTime(@NonNull String date) throws ParseException {
    try {
      return ZonedDateTime.parse(date, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException dateTimeParseException) {
      throw new ParseException(
          String.format("Invalid strftime Format. Unable to parse value : %s to a ZonedDateTime.",
              date),
          dateTimeParseException);
    }
  }

}
