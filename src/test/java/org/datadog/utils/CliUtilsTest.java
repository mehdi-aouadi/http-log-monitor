package org.datadog.utils;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.datadog.cli.ApplicationOptions;
import org.junit.Test;

import static org.datadog.utils.CliUtils.*;
import static org.junit.Assert.*;

public class CliUtilsTest {

  @Test
  public void getOptionsTest() {

    Options options = CliUtils.getOptions();
    Option option = options.getOption(FILE_PATH_LONG_OPTION);
    assertNotNull(option);
    assertEquals(option.getOpt(), FILE_PATH_SHORT_OPTION);
    assertEquals(option.getLongOpt(), FILE_PATH_LONG_OPTION);

    option = options.getOption(FILE_PATH_SHORT_OPTION);
    assertNotNull(option);
    assertEquals(option.getOpt(), FILE_PATH_SHORT_OPTION);
    assertEquals(option.getLongOpt(), FILE_PATH_LONG_OPTION);

    option = options.getOption(REFRESH_FREQUENCY_LONG_OPTION);
    assertNotNull(option);
    assertEquals(option.getOpt(), REFRESH_FREQUENCY_SHORT_OPTION);
    assertEquals(option.getLongOpt(), REFRESH_FREQUENCY_LONG_OPTION);

    option = options.getOption(REFRESH_FREQUENCY_SHORT_OPTION);
    assertNotNull(option);
    assertEquals(option.getOpt(), REFRESH_FREQUENCY_SHORT_OPTION);
    assertEquals(option.getLongOpt(), REFRESH_FREQUENCY_LONG_OPTION);

    option = options.getOption(HITS_THRESHOLD_LONG_OPTION);
    assertNotNull(option);
    assertEquals(option.getOpt(), HITS_THRESHOLD_SHORT_OPTION);
    assertEquals(option.getLongOpt(), HITS_THRESHOLD_LONG_OPTION);

    option = options.getOption(HITS_THRESHOLD_SHORT_OPTION);
    assertNotNull(option);
    assertEquals(option.getOpt(), HITS_THRESHOLD_SHORT_OPTION);
    assertEquals(option.getLongOpt(), HITS_THRESHOLD_LONG_OPTION);

    option = options.getOption(THRESHOLD_CYCLES_LONG_OPTION);
    assertNotNull(option);
    assertEquals(option.getOpt(), THRESHOLD_CYCLES_SHORT_OPTION);
    assertEquals(option.getLongOpt(), THRESHOLD_CYCLES_LONG_OPTION);

    option = options.getOption(THRESHOLD_CYCLES_SHORT_OPTION);
    assertNotNull(option);
    assertEquals(option.getOpt(), THRESHOLD_CYCLES_SHORT_OPTION);
    assertEquals(option.getLongOpt(), THRESHOLD_CYCLES_LONG_OPTION);

  }

  @Test
  public void parseArgumentsTest() throws ParseException {
    String filePath = "/dir/tempFile";
    String refresh = "30";
    CommandLine commandLine = parseArguments(
        new String[] {
            "-" + FILE_PATH_SHORT_OPTION + "=" + filePath,
            "-" + REFRESH_FREQUENCY_LONG_OPTION + "=" + refresh
        }
    );
    assertEquals(commandLine.getOptions().length, 2);
    assertNotNull(commandLine.getOptionValue(FILE_PATH_SHORT_OPTION));
    assertNotNull(commandLine.getOptionValue(FILE_PATH_LONG_OPTION));
    assertEquals(commandLine.getOptionValue(FILE_PATH_LONG_OPTION),
        commandLine.getOptionValue(FILE_PATH_SHORT_OPTION));
    assertEquals(commandLine.getOptionValue(FILE_PATH_SHORT_OPTION), filePath);

    assertNotNull(commandLine.getOptionValue(REFRESH_FREQUENCY_SHORT_OPTION));
    assertNotNull(commandLine.getOptionValue(REFRESH_FREQUENCY_LONG_OPTION));
    assertEquals(commandLine.getOptionValue(REFRESH_FREQUENCY_LONG_OPTION),
        commandLine.getOptionValue(REFRESH_FREQUENCY_SHORT_OPTION));
    assertEquals(commandLine.getOptionValue(REFRESH_FREQUENCY_SHORT_OPTION), refresh);
  }

  @Test
  public void validateDefaultOptionsTest() throws ParseException {

    CommandLine commandLine = parseArguments(
        new String[] {}
    );
    ApplicationOptions applicationOptions = validateArguments(commandLine);
    assertEquals(ApplicationOptions.builder().build(),
        applicationOptions);
  }

  @Test
  public void validatePartialOptionsTest() throws ParseException {
    int refresh = 30;
    int threshold = 200;
    CommandLine commandLine = parseArguments(
        new String[] {
            "-" + HITS_THRESHOLD_SHORT_OPTION + "=" + threshold,
            "-" + REFRESH_FREQUENCY_LONG_OPTION + "=" + refresh
        }
    );
    ApplicationOptions applicationOptions = validateArguments(commandLine);
    assertEquals(ApplicationOptions.builder().refreshFrequency(refresh).trafficThreshold(threshold).build(),
        applicationOptions);
  }

}
