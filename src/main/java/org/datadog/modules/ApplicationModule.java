package org.datadog.modules;

import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new EventBusModule());
    install(new ParserModule());
  }
}
