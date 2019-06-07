package org.datadog.watcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

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

  private Path filePath;
  private BufferedReader bufferedReader;
  private OutputHandler<String> outputHandler;
  private int cursorIndex = 0;
  private boolean run = true;

  public FileWatcherImpl(@NonNull Path filePath, @NonNull OutputHandler outputHandler) {
    this.filePath = filePath;
    this.outputHandler = outputHandler;
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
    this.run = true;
    try {
      WatchService watchService = FileSystems.getDefault().newWatchService();
      try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filePath.toFile()))) {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          cursorIndex += line.length() + System.lineSeparator().length();
        }
      }

      filePath.toAbsolutePath().getParent().register(
          watchService, StandardWatchEventKinds.ENTRY_MODIFY
      );

      while (this.run) {
        WatchKey watchKey = watchService.take();
        for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
          WatchEvent<Path> pathEvent = (WatchEvent<Path>) watchEvent;
          Path path = pathEvent.context();
          if (path.equals(filePath.getFileName())) {
            try (BufferedReader bufferedReader =
                     new BufferedReader(new FileReader(filePath.toFile()))) {
              String line;
              bufferedReader.skip(cursorIndex);
              while ((line = bufferedReader.readLine()) != null) {
                cursorIndex += line.length() + System.lineSeparator().length();
                this.outputHandler.process(line);
              }
            }
          }
        }
        watchKey.reset();
      }
    } catch (IOException | InterruptedException exception) {
      LOGGER.error(exception.getMessage(), exception);
    }
  }

  @Override
  public void stop() {
    this.run = false;
  }

  @Override
  public void close() {
    this.stop();
  }

}
