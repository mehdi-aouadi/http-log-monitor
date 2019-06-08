package org.datadog.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.NonNull;
import org.datadog.log.CommonLogFormatEntry;

import static org.datadog.utils.DateTimeUtils.retrieveLogDateTime;

import static org.datadog.utils.CommonLogFormatUtils.retrieveIntValue;
import static org.datadog.utils.CommonLogFormatUtils.retrieveString;

/**
 * A Common Log Format {@link Parser} implementation.
 * It is based on a Common Log Format {@link Pattern} to parse the log input
 * to a {@link CommonLogFormatEntry}
 * @see CommonLogFormatEntry
 */
public class CommonLogFormatParserImpl implements Parser<CommonLogFormatEntry, String> {

  private static final String HOST_GROUP = "host";
  private static final String USER_RFC_ID_GROUP = "userRfcId";
  private static final String USER_ID_GROUP = "userId";
  private static final String DATE_TIME_GROUP = "dateTime";
  private static final String METHOD_GROUP = "method";
  private static final String RESOURCE_GROUP = "resource";
  private static final String PROTOCOL_GROUP = "protocol";
  private static final String STATUS_GROUP = "status";
  private static final String SIZE_GROUP = "size";

  private static final Pattern COMMON_LOG_FORMAT_PATTERN = Pattern.compile(
      "^(?<" + HOST_GROUP + ">\\S+) "
          + "(?<" + USER_RFC_ID_GROUP + ">\\S+) "
          + "(?<" + USER_ID_GROUP + ">\\S+) "
          + "\\[(?<" + DATE_TIME_GROUP + ">[^]]+)] "
          + "\"(?<" + METHOD_GROUP + ">[A-Z]+) "
          + "(?<" + RESOURCE_GROUP + ">[^ " + "\"]+) ?"
          + "(?<" + PROTOCOL_GROUP + ">[^\"]+)?\" "
          + "(?<" + STATUS_GROUP + ">[0-9]{3})"
          + " (?<" + SIZE_GROUP + ">[0-9]+|-)$");

  /**
   * Parses a {@link String} to a {@link CommonLogFormatEntry} using a {@link Pattern}.
   * @param input The input to parse.
   * @return a {@link CommonLogFormatEntry}.
   * @throws ParseException if the input {@link String} can not be parsed
   *     to a {@link CommonLogFormatEntry}.
   */
  @Override
  public CommonLogFormatEntry parse(@NonNull String input) throws ParseException {
    Matcher commonLogFormatMatcher = COMMON_LOG_FORMAT_PATTERN.matcher(input);
    if (commonLogFormatMatcher.find()) {
      return CommonLogFormatEntry.builder()
          .host(commonLogFormatMatcher.group(HOST_GROUP))
          .userRfcId(retrieveString(commonLogFormatMatcher.group(USER_RFC_ID_GROUP)))
          .userId(retrieveString(commonLogFormatMatcher.group(USER_ID_GROUP)))
          .logDateTime(retrieveLogDateTime(commonLogFormatMatcher.group(DATE_TIME_GROUP)))
          .method(commonLogFormatMatcher.group(METHOD_GROUP))
          .resource(commonLogFormatMatcher.group(RESOURCE_GROUP))
          .protocol(commonLogFormatMatcher.group(PROTOCOL_GROUP))
          .status(retrieveIntValue(commonLogFormatMatcher.group(STATUS_GROUP), false))
          .size(retrieveIntValue(commonLogFormatMatcher.group(SIZE_GROUP), true))
          .build();
    } else {
      throw new ParseException(
          String.format("Invalid Common Log Format. Unable to parse log input : %s.", input)
      );
    }
  }
}
