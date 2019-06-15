package org.datadog.watcher;

import java.io.ByteArrayInputStream;
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
import java.util.List;

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
  private WatchKey watchKeyMock;
  @Mock
  private WatchEvent<Path> pathChangedEvent;
  @Mock
  private OutputHandler outputHandlerMock;

  private FileWatcherImpl fileWatcherUnderTest;

  private final String firstLineToSkip = "First line to skip";
  private final String secondLineToSkip = "Second line to skip";
  private final String firstLogLine = "First Log Line";
  private final String secondLogLine = "Second Log Line";
  private final String thirdLogLine = "Third Log Line";
  private final String fourthLogLine = "Fourth Log Line";
  private final String fifthLogLine = "Fifth Log Line";
  private final String sixthLogLine = "Sixth Log Line";

  private final String initialFileContent = new StringBuilder(firstLineToSkip)
      .append(System.lineSeparator())
      .append(secondLineToSkip)
      .toString();

  private final String firstFileAddition = new StringBuilder(this.initialFileContent)
      .append(System.lineSeparator())
      .append(this.firstLogLine)
      .append(System.lineSeparator())
      .append(this.secondLogLine)
      .append(System.lineSeparator())
      .append(this.thirdLogLine)
      .toString();

  private final String SecondFileAddition = new StringBuilder(this.firstFileAddition)
      .append(System.lineSeparator())
      .append(this.fourthLogLine)
      .append(System.lineSeparator())
      .append(this.fifthLogLine)
      .append(System.lineSeparator())
      .append(this.sixthLogLine)
      .toString();

  @Before
  public void init() throws IOException, InterruptedException {
    when(this.pathMock.toAbsolutePath()).thenReturn(this.absolutePathMock);
    when(this.absolutePathMock.getParent()).thenReturn(this.parentPathMock);
    when(this.pathMock.getFileSystem()).thenReturn(this.fileSystemMock);
    when(this.fileSystemMock.provider()).thenReturn(this.fileSystemProviderMock);
    when(this.pathMock.getFileName()).thenReturn(this.pathMock);

    InputStream initialInputStream = createInputStream(this.initialFileContent);

    InputStream firstUpdatedInputStream = createInputStream(this.firstFileAddition);

    InputStream secondUpdatedInputStream = createInputStream(this.SecondFileAddition);

    when(fileSystemProviderMock.newInputStream(pathMock)).thenReturn(initialInputStream,
        firstUpdatedInputStream, secondUpdatedInputStream);

    this.fileWatcherUnderTest = new FileWatcherImpl(watchServiceMock, pathMock, outputHandlerMock);

    verify(parentPathMock).register(watchServiceMock, StandardWatchEventKinds.ENTRY_MODIFY);

    when(this.pathChangedEvent.context()).thenReturn(this.pathMock);
    when(this.watchKeyMock.pollEvents()).thenReturn(Arrays.asList(pathChangedEvent));
    when(this.watchServiceMock.take()).thenReturn(this.watchKeyMock);

  }

  @Test
  public void fileWatcherTest() throws InterruptedException, IOException {
    this.fileWatcherUnderTest.watchFile();
    ArgumentCaptor<String> argumentCaptor = ArgumentCaptor.forClass(String.class);
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