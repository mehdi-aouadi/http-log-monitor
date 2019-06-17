package org.datadog.utils;

import java.time.Duration;
import java.time.Instant;
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
   * Parses an "dd/MMM/yyyy:HH:mm:ss Z" formatted {@link String} to a {@link ZonedDateTime}.
   * @param date {@link String} date time representation
   * @return a {@link ZonedDateTime} parsed from the "dd/MMM/yyyy:HH:mm:ss Z"
   *     formatted {@link String}
   * @throws ParseException if the {@link String} to parse doesn't is not in the
   *     "dd/MMM/yyyy:HH:mm:ss Z" format
   * @throws NullPointerException if the {@link String} to parse is null
   */
  public static ZonedDateTime retrieveLogDateTime(@NonNull String date) throws ParseException {
    try {
      return ZonedDateTime.parse(date, DATE_TIME_FORMATTER);
    } catch (DateTimeParseException dateTimeParseException) {
      throw new ParseException(
          String.format(
              "Invalid Date Time Format. Date Time format must "
                  + "be \"dd/MMM/yyyy:HH:mm:ss Z\". Unable to parse "
                  + "value : %s to a ZonedDateTime.",
              date),
          dateTimeParseException);
    }
  }

  /**
   * Generates a human readable {@link String} representation od a duration
   * between two {@link Instant}.
   * @param from {@link Instant} duration start.
   * @param to  {@link Instant} duration end.
   * @return a {@link String} human readable duration.
   */
  public static String toPrettyDuration(Instant from, Instant to) {
    return Duration.between(from, to).withNanos(0).toString()
        .substring(2)
        .replaceAll("(\\d[HMS])(?!$)", "$1 ")
        .toLowerCase();
  }

}
