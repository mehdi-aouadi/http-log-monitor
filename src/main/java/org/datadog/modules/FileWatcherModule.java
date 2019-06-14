package org.datadog.modules;

import com.google.inject.AbstractModule;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.WatchService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileWatcherModule extends AbstractModule {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherModule.class);

  @Override
  protected void configure() {
    try {
      bind(WatchService.class).toInstance(FileSystems.getDefault().newWatchService());
    } catch (IOException exception) {
      LOGGER.error("Error during WatchService binding for Guice injection.", exception);
    }
  }
}
