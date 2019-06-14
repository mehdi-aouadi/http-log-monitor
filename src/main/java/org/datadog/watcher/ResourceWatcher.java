package org.datadog.watcher;

import java.io.Closeable;
import java.io.IOException;

/**
 * A resource watcher class that is able to detect any resource change and trigger an action.
 */
public interface ResourceWatcher extends Closeable {

  /**
   * Launches the watcher.
   * @throws IOException If an error occurs when watching the resource.
   */
  void run() throws IOException;

}
