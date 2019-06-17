package org.datadog;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.WatchService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;
import org.datadog.alerts.AlertsManager;
import org.datadog.cli.ApplicationOptions;
import org.datadog.gui.ConsoleGui;
import org.datadog.modules.ApplicationModule;
import org.datadog.parser.CommonLogFormatHandlerImpl;
import org.datadog.parser.OutputHandler;
import org.datadog.parser.Parser;
import org.datadog.statitics.TrafficStatisticsManager;
import org.datadog.watcher.FileWatcherImpl;

import static org.datadog.utils.CliUtils.parseArguments;
import static org.datadog.utils.CliUtils.validateArguments;
import static org.datadog.utils.CliUtils.printApplicationHelp;

/**
 * Main Application class.
 *
 */
@Slf4j
public class HttpLogMonitoringApplication {

  /**
   * Launches a {@link HttpLogMonitoringApplication} in the following steps.
   * <ul>
   *   <li>Creates a default {@link ApplicationOptions} with default values.</li>
   *   <li>Parses the user entered option values.</li>
   *   <li>Validates the user entered options values and keeps default
   *   ones if an option is not present.</li>
   *   <li>Initializes a {@link OutputHandler}.</li>
   *   <li>Initializes a {@link TrafficStatisticsManager}.</li>
   *   <li>Initializes a {@link AlertsManager}.</li>
   *   <li>Initializes a {@link org.datadog.watcher.ResourceWatcher} in a separate thread.</li>
   *   <li>Initializes a {@link ConsoleGui} in a separate thread.</li>
   * </ul>
   * @param args The monitoring option values as described in {@link ApplicationOptions}
   */
  public static void main(String[] args) {
    ApplicationOptions applicationOptions = ApplicationOptions.builder().build();
    try {
      CommandLine commandLine = parseArguments(args);
      try {
        applicationOptions = validateArguments(commandLine);
      } catch (Exception exception) {
        log.error("Error validating the option values.", exception);
        System.out.println("Invalid option value(s).");
        printApplicationHelp();
        System.exit(1);
      }
      log.info("Initializing a HTTP Log Monitor with the following options {}", applicationOptions);
    } catch (ParseException exception) {
      log.error("Error when parsing application options.", exception);
      System.out.println("Error when parsing application options.");
      printApplicationHelp();
      System.exit(1);
    }

    Injector injector = Guice.createInjector(new ApplicationModule());

    EventBus eventBus = injector.getInstance(EventBus.class);

    OutputHandler<String> stringOutputHandler = new CommonLogFormatHandlerImpl(
        injector.getInstance(Parser.class),
        eventBus);

    TrafficStatisticsManager trafficStatisticsManager = new TrafficStatisticsManager(
        eventBus,
        applicationOptions.getRefreshFrequency()
    );

    eventBus.register(trafficStatisticsManager);

    AlertsManager alertsManager = new AlertsManager(eventBus,
        applicationOptions.getThresholdRefreshCycles(),
        applicationOptions.getTrafficThreshold());

    eventBus.register(alertsManager);

    String filePath = applicationOptions.getFilePath();
    new Thread(() -> new FileWatcherImpl(
        injector.getInstance(WatchService.class),
        Paths.get(filePath),
        stringOutputHandler
    ).run(), "file-watcher-thread").start();

    new Thread(() -> {
      ConsoleGui gui = new ConsoleGui();
      eventBus.register(gui);
      try {
        gui.start(() -> System.exit(0));
      } catch (IOException e) {
        log.error(e.getMessage(), e);
        System.exit(1);
      }
    }, "ui-thread").start();
  }

}
