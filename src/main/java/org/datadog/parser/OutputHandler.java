package org.datadog.parser;

/**
 * Handles an output of type {@link E}.
 */
public interface OutputHandler<E> {

  /**
   * Processes a parsing output of type {@link E}.
   * @param output the parsing output to process.
   */
  void process(E output);

}
