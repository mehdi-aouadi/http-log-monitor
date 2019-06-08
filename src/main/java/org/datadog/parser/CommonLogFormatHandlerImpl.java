package org.datadog.parser;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.sun.istack.internal.NotNull;
import lombok.NonNull;
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
  private EventBus eventBus;

  @Inject
  public CommonLogFormatHandlerImpl(@NotNull Parser parser, @NonNull EventBus eventBus) {
    this.parser = parser;
    this.eventBus = eventBus;
  }

  /**
   *
   * @param line
   */
  @Override
  public void process(String line) {
    try {
      CommonLogFormatEntry commonLogFormatEntry = this.parser.parse(line);
      this.eventBus.post(commonLogFormatEntry);
    } catch (ParseException parseException) {
      LOGGER.error("Invalid Common Log Format. Line : {}", line, parseException);
    }
  }
}
