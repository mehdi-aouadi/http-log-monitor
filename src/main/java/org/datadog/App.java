package org.datadog;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.datadog.modules.ApplicationModule;
import org.datadog.modules.ParserModule;
import org.datadog.parser.CommonLogFormatHandlerImpl;
import org.datadog.parser.Parser;

/**
 * Main Application class.
 *
 */
public class App {
  public static void main(String[] args) {
    Injector injector = Guice.createInjector(new ApplicationModule());
    CommonLogFormatHandlerImpl commonLogFormatHandler =
        new CommonLogFormatHandlerImpl(injector.getInstance(Parser.class), injector.getInstance(EventBus.class));
    commonLogFormatHandler.process("127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326");
  }
}
