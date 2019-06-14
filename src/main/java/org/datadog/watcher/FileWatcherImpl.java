package org.datadog.watcher;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import lombok.NonNull;

import org.datadog.parser.OutputHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A file {@link ResourceWatcher} implemnatation.
 * It watches a file and detects any new modification.
 * The file content that already exist before the watcher is launched is skipped.
 * Only file content addition is handled.
 */
public class FileWatcherImpl implements ResourceWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileWatcherImpl.class);

  private WatchService watchService;
  private Path filePath;
  private OutputHandler<String> outputHandler;
  private int cursorIndex = 0;
  private final AtomicBoolean running = new AtomicBoolean(false);

  @Inject
  public FileWatcherImpl(@NonNull WatchService watchService,
                         @NonNull Path filePath,
                         @NonNull OutputHandler outputHandler) {
    this.watchService = watchService;
    this.filePath = filePath;
    this.outputHandler = outputHandler;

    try {
      filePath.toAbsolutePath().getParent().register(
          this.watchService, StandardWatchEventKinds.ENTRY_MODIFY
      );

      try (BufferedReader bufferedReader
               = Files.newBufferedReader(this.filePath, Charset.defaultCharset())) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          this.cursorIndex += line.length() + System.lineSeparator().length();
        }
      }

    } catch (IOException exception) {
      LOGGER.error("Error during FileWatcher initialization.", exception);
    }
  }

  /**
   * Launches a {@link WatchService} and register the file's parent directory for events of type
   *  {@link StandardWatchEventKinds.ENTRY_MODIFY}.
   *  The events are filtered according to the filename. Only the file modications are handler.
   *  A first step consists of couting the existing characters already present in the file before
   *    registering the file for watching the modificatiion events.
   *  At each modification event the existing characters are skipped and only the new added lines
   *    are processed.
   *
   */
  @Override
  public void run() {
    this.running.set(true);
    while (this.running.get()) {
      watchFile();
    }
  }

  @VisibleForTesting
  void watchFile() {
    try {
      WatchKey watchKey = this.watchService.take();
      for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
        WatchEvent<Path> pathEvent = (WatchEvent<Path>) watchEvent;
        Path path = pathEvent.context();
        if (path.equals(this.filePath.getFileName())) {
          try (BufferedReader bufferedReader =
                   Files.newBufferedReader(this.filePath, Charset.defaultCharset())) {
            String line;
            bufferedReader.skip(this.cursorIndex);
            while ((line = bufferedReader.readLine()) != null) {
              this.cursorIndex += line.length() + System.lineSeparator().length();
              this.outputHandler.process(line);
            }
          }
        }
      }
      watchKey.reset();
    } catch (IOException | InterruptedException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  @Override
  public void close() {
    this.running.set(false);
  }

}
