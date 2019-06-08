package org.datadog;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.datadog.modules.ParserModule;
import org.datadog.parser.CommonLogFormatHandlerImpl;
import org.datadog.parser.Parser;

/**
 * Main Application class.
 *
 */
public class App {
  public static void main(String[] args) {
    EventBus eventBus = new EventBus("HTTP Log Monitor Event Bus");
    Injector injector = Guice.createInjector(new ParserModule());
    CommonLogFormatHandlerImpl commonLogFormatHandler =
        new CommonLogFormatHandlerImpl(injector.getInstance(Parser.class), eventBus);
  }
}
