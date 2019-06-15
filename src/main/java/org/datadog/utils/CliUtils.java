package org.datadog.utils;

import java.nio.file.Paths;

import com.google.common.annotations.VisibleForTesting;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.datadog.cli.ApplicationOptions;


@Slf4j
public class CliUtils {

  static final String FILE_PATH_LONG_OPTION = "log-file-path";
  static final String REFRESH_FREQUENCY_LONG_OPTION = "refresh-frequency";
  static final String HITS_THRESHOLD_LONG_OPTION = "hits-threshold";
  static final String THRESHOLD_CYCLES_LONG_OPTION = "threshold-cycles";

  static final String FILE_PATH_SHORT_OPTION = "f";
  static final String REFRESH_FREQUENCY_SHORT_OPTION = "r";
  static final String HITS_THRESHOLD_SHORT_OPTION = "t";
  static final String THRESHOLD_CYCLES_SHORT_OPTION = "c";

  /**
   * Validates the {@link org.datadog.HttpLogMonitoringApplication} options.
   * @param commandLine The {@link CommandLine} created from the user command line.
   * @return an {@link ApplicationOptions} with the valid option value
   *         and the default values otherwise.
   */
  public static ApplicationOptions validateArguments(CommandLine commandLine) {
    ApplicationOptions defaults = ApplicationOptions.builder().build();
    String filePath = commandLine.getOptionValue(FILE_PATH_LONG_OPTION);
    if (filePath != null) {
      if (!Paths.get(filePath).toFile().isFile()) {
        log.error("{} no such file.", filePath);
        System.out.println(String.format("%s no such file", filePath));
        throw new IllegalArgumentException(filePath + " no such file.");
      }
    } else {
      filePath = defaults.getFilePath();
    }

    int refreshFrequency = retrieveIntegerOption(defaults.getRefreshFrequency(),
        commandLine,
        REFRESH_FREQUENCY_LONG_OPTION,
        1);
    int trafficThreshold = retrieveIntegerOption(
        defaults.getTrafficThreshold(),
        commandLine,
        HITS_THRESHOLD_LONG_OPTION,
        1);
    int thresholdDuration = retrieveIntegerOption(
        defaults.getThresholdAlertCycles(),
        commandLine,
        THRESHOLD_CYCLES_LONG_OPTION,
        1);

    return ApplicationOptions.builder()
        .filePath(filePath)
        .refreshFrequency(refreshFrequency)
        .trafficThreshold(trafficThreshold)
        .thresholdAlertCycles(thresholdDuration)
        .build();
  }

  /**
   * Parses an {@link String} array of options to a {@link CommandLine}.
   * @param args a {@link String} array of option values.
   * @return a {@link CommandLine} created with the option values.
   * @throws ParseException if there are any problems encountered
   *      while parsing the command line tokens
   */
  public static CommandLine parseArguments(String[] args) throws ParseException {
    Options options = getOptions();
    CommandLineParser parser = new DefaultParser();
    return parser.parse(options, args);
  }

  @VisibleForTesting
  static Options getOptions() {
    ApplicationOptions defaults = ApplicationOptions.builder().build();
    Options options = new Options();
    options.addOption(FILE_PATH_SHORT_OPTION, FILE_PATH_LONG_OPTION, true,
        "The log file absolute path, default " + defaults.getFilePath());
    options.addOption(REFRESH_FREQUENCY_SHORT_OPTION, REFRESH_FREQUENCY_LONG_OPTION, true,
        "The reporting refresh frequency in seconds, default "
            + defaults.getRefreshFrequency());
    options.addOption(HITS_THRESHOLD_SHORT_OPTION, HITS_THRESHOLD_LONG_OPTION, true,
        "Hits threshold on average during the monitoring duration, default "
            + defaults.getTrafficThreshold());
    options.addOption(THRESHOLD_CYCLES_SHORT_OPTION, THRESHOLD_CYCLES_LONG_OPTION, true,
        "Number of refresh cycles after which traffic threshold must be checked, default "
            + defaults.getThresholdAlertCycles());
    return options;
  }

  private static int retrieveIntegerOption(
      int defaultValue,
      CommandLine commandLine,
      String optionName,
      int minValue) {
    int option = defaultValue;
    String optionValue = commandLine.getOptionValue(optionName);
    if (optionValue != null) {
      try {
        option = Integer.parseInt(optionValue);
        if (option < minValue) {
          log.error("{} must be >= {}", optionName, minValue);
          System.out.println(optionName + " must be >= " + minValue);
          printApplicationHelp();
          throw new IllegalArgumentException(
              String.format("Invalid %s option value. Must be >= %d", option, minValue)
          );
        }
      } catch (NumberFormatException numberFormatException) {
        log.error(optionName + " option {} is not an integer.", optionValue, numberFormatException);
        System.out.println(
            String.format("%s option %s is not a valid integer.", optionName, optionValue)
        );
        printApplicationHelp();
      }
    }

    return option;
  }

  /**
   * Prints the application usage help manual.
   */
  public static void printApplicationHelp() {
    Options options = getOptions();
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(150);
    formatter.printHelp("./http-monitor.sh", options, true);
  }

}
