package org.datadog.parser;

import com.google.inject.Inject;
import com.sun.istack.internal.NotNull;
import org.datadog.log.CommonLogFormatEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link String} output handler.
 * It uses a Common Log Format Parser to process the output.
 */
public class CommonLogFormatHandlerImpl implements OutputHandler<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CommonLogFormatHandlerImpl.class);

  private Parser<CommonLogFormatEntry, String> parser;

  @Inject
  public CommonLogFormatHandlerImpl(@NotNull Parser parser) {
    this.parser = parser;
  }

  /**
   *
   * @param line
   */
  @Override
  public void process(String line) {
    try {
      //// TODO: 6/6/2019 Handle CommonLogFormatEntry
      this.parser.parse(line);
    } catch (ParseException parseException) {
      LOGGER.error("Invalid Common Log Format. Line : {}", line, parseException);
    }
  }
}
