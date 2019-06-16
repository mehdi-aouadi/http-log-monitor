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

import lombok.extern.slf4j.Slf4j;
import org.datadog.parser.OutputHandler;

/**
 * A file {@link ResourceWatcher} implemnatation.
 * It watches a file and detects any new modification.
 * The file content that already exist before the watcher is launched is skipped.
 * Only file content addition is handled.
 */
@Slf4j
public class FileWatcherImpl implements ResourceWatcher {

  private WatchService watchService;
  private Path filePath;
  private OutputHandler<String> outputHandler;
  private long fileCursor = 0L;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private BufferedReader bufferedReader;

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
      this.fileCursor = this.filePath.toFile().length();
    } catch (IOException exception) {
      log.error("Error during FileWatcher initialization.", exception);
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
          this.bufferedReader = Files.newBufferedReader(this.filePath, Charset.defaultCharset());
          this.bufferedReader.skip(this.fileCursor);
          String line;
          while ((line = this.bufferedReader.readLine()) != null) {
            this.outputHandler.process(line);
          }
          this.fileCursor = this.filePath.toFile().length();
          this.bufferedReader.close();
        }
      }
      watchKey.reset();
    } catch (IOException | InterruptedException exception) {
      log.error(exception.getMessage(), exception);
    }
  }

  @Override
  public void close() {
    this.running.set(false);
  }

}
