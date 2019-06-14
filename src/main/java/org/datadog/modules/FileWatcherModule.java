package org.datadog.modules;

import com.google.inject.AbstractModule;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

@Slf4j
public class FileWatcherModule extends AbstractModule {

  @Override
  protected void configure() {
    try {
      bind(WatchService.class).toInstance(FileSystems.getDefault().newWatchService());
    } catch (IOException exception) {
      log.error("Error during WatchService binding for Guice injection.", exception);
    }
  }
}
