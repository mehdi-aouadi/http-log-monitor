package org.datadog.watcher;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Arrays;
import java.util.Collections;

import org.datadog.parser.OutputHandler;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FileWatcherTest {

  @Mock
  private WatchService watchServiceMock;
  @Mock
  private Path pathMock;
  @Mock
  private Path absolutePathMock;
  @Mock
  private Path parentPathMock;
  @Mock
  private FileSystem fileSystemMock;
  @Mock
  private FileSystemProvider fileSystemProviderMock;
  @Mock
  private File fileMock;
  @Mock
  private WatchKey watchKeyMock;
  @Mock
  private WatchEvent<Path> pathChangedEvent;
  @Mock
  private OutputHandler<String> outputHandlerMock;

  private FileWatcherImpl fileWatcherUnderTest;

  private final String firstLineToSkip = "First line to skip";
  private final String secondLineToSkip = "Second line to skip";
  private final String firstLogLine = "First Log Line";
  private final String secondLogLine = "Second Log Line";
  private final String thirdLogLine = "Third Log Line";
  private final String fourthLogLine = "Fourth Log Line";
  private final String fifthLogLine = "Fifth Log Line";
  private final String sixthLogLine = "Sixth Log Line";

  private final String initialFileContent = firstLineToSkip +
      System.getProperty("line.separator") +
      secondLineToSkip +
      System.getProperty("line.separator");

  private final String firstFileAddition = this.initialFileContent +
      this.firstLogLine +
      System.getProperty("line.separator") +
      this.secondLogLine +
      System.getProperty("line.separator") +
      this.thirdLogLine +
      System.getProperty("line.separator");

  private final String SecondFileAddition = this.firstFileAddition +
      this.fourthLogLine +
      System.getProperty("line.separator") +
      this.fifthLogLine +
      System.getProperty("line.separator") +
      this.sixthLogLine +
      System.getProperty("line.separator");

  @Before
  public void init() throws IOException, InterruptedException {
    when(this.pathMock.toAbsolutePath()).thenReturn(this.absolutePathMock);
    when(this.absolutePathMock.getParent()).thenReturn(this.parentPathMock);
    when(this.pathMock.getFileSystem()).thenReturn(this.fileSystemMock);
    when(this.fileSystemMock.provider()).thenReturn(this.fileSystemProviderMock);
    when(this.pathMock.toFile()).thenReturn(this.fileMock);
    when(this.fileMock.length()).thenReturn(
        (long) (this.firstLineToSkip.length() + this.secondLineToSkip.length() + 2 * System.getProperty("line.separator").length()),
        (long) (this.firstLineToSkip.length() + this.secondLineToSkip.length() + 2 * System.getProperty("line.separator").length()),
        (long) (this.firstLineToSkip.length() + this.secondLineToSkip.length() + 2 * System.getProperty("line.separator").length())
            + (long) (this.firstLogLine.length() + this.secondLogLine.length() + this.thirdLogLine.length() + 3 * System.getProperty("line.separator").length())
    );
    when(this.pathMock.getFileName()).thenReturn(this.pathMock);

    InputStream initialInputStream = createInputStream(this.initialFileContent);

    InputStream firstUpdatedInputStream = createInputStream(this.firstFileAddition);

    InputStream secondUpdatedInputStream = createInputStream(this.SecondFileAddition);

    when(fileSystemProviderMock.newInputStream(pathMock)).thenReturn(initialInputStream,
        firstUpdatedInputStream, secondUpdatedInputStream);

    this.fileWatcherUnderTest = new FileWatcherImpl(watchServiceMock, pathMock, outputHandlerMock);

    verify(parentPathMock).register(watchServiceMock, StandardWatchEventKinds.ENTRY_MODIFY);

    when(this.pathChangedEvent.context()).thenReturn(this.pathMock);
    when(this.watchKeyMock.pollEvents()).thenReturn(Collections.singletonList(pathChangedEvent));
    when(this.watchServiceMock.take()).thenReturn(this.watchKeyMock);

  }

  @Test
  public void fileWatcherTest() {
    this.fileWatcherUnderTest.watchFile();
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.outputHandlerMock, times(0)).process(argumentCaptor.capture());

    reset(this.outputHandlerMock);

    this.fileWatcherUnderTest.watchFile();
    argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.outputHandlerMock, times(3)).process(argumentCaptor.capture());

    assertTrue(argumentCaptor.getAllValues().contains(firstLogLine));
    assertTrue(argumentCaptor.getAllValues().contains(secondLogLine));
    assertTrue(argumentCaptor.getAllValues().contains(thirdLogLine));

    reset(this.outputHandlerMock);

    this.fileWatcherUnderTest.watchFile();
    argumentCaptor = ArgumentCaptor.forClass(String.class);
    verify(this.outputHandlerMock, times(3)).process(argumentCaptor.capture());

    assertTrue(argumentCaptor.getAllValues().contains(fourthLogLine));
    assertTrue(argumentCaptor.getAllValues().contains(fifthLogLine));
    assertTrue(argumentCaptor.getAllValues().contains(sixthLogLine));

  }

  private InputStream createInputStream(String string) {
    return new ByteArrayInputStream(string.getBytes());
  }

}
