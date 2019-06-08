package org.datadog.parser;

import lombok.ToString;

/**
 * Thrown to indicates that a method cannot perform an input parsing to an output format.
 */
@ToString
public class ParseException extends Exception {

  public ParseException(String message) {
    super(message);
  }

  public ParseException(String message, Throwable cause) {
    super(message, cause);
  }

}
