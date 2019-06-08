package org.datadog.modules;

import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

public class EventBusModule extends AbstractModule {

  private final EventBus eventBus = new EventBus("HTTP Log Monitor Event Bus");

  @Override
  protected void configure() {
    bind(EventBus.class).toInstance(eventBus);
    bindListener(Matchers.any(), new TypeListener() {
      public <I> void hear(TypeLiteral<I> typeLiteral, TypeEncounter<I> typeEncounter) {
        typeEncounter.register((InjectionListener<I>) i -> eventBus.register(i));
      }
    });
  }

}
