package org.datadog.parser;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.datadog.log.CommonLogFormatEntry;

/**
 * A {@link String} output handler.
 * It uses a Common Log Format Parser to process the output.
 */
@Slf4j
public class CommonLogFormatHandlerImpl implements OutputHandler<String> {

  private Parser<CommonLogFormatEntry, String> parser;
  private EventBus eventBus;

  @Inject
  public CommonLogFormatHandlerImpl(@NonNull Parser parser, @NonNull EventBus eventBus) {
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
      log.error("Invalid Common Log Format. Line : {}", line, parseException);
    }
  }
}
