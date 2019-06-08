package org.datadog.parser;

/**
 * A generic parsing class that parses an input of type {@link U} to an output of type {@link T}.
 * @param <T> The type of the parsing result.
 */
public interface Parser<T, U> {

  /**
   * Parses an input of type {@link String} to an output of type {@link T}.
   * @param input The input of type {@link U} to parse.
   * @return {@link T} as a parsing result.
   * @throws ParseException if the input can not be parsed to the output type.
   */
  T parse(U input) throws ParseException;

}
