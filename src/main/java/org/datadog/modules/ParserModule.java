package org.datadog.modules;

import com.google.inject.AbstractModule;
import org.datadog.parser.CommonLogFormatParserImpl;
import org.datadog.parser.Parser;

/**
 * Google Guice Dependency Injection module that binds a {@link Parser} to an implementation.
 */
public class ParserModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(Parser.class).to(CommonLogFormatParserImpl.class);
  }

}
