package org.datadog.utils;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.datadog.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZonedDateTime;

import static org.datadog.utils.DateTimeUtils.retrieveLogDateTime;
import static org.datadog.utils.DateTimeUtils.DATE_TIME_FORMATTER;

import static org.junit.Assert.assertEquals;

@RunWith(JUnitParamsRunner.class)
public class DateTimeUtilsTest {

  @Test
  @Parameters(value = {
      "10/Oct/2000:13:55:36 -0700",
      "10/Oct/2000:13:55:36 -0000",
      "20/Nov/2000:13:00:00 +0800"
  })
  public void validDateTest(String value) throws ParseException {
    assertEquals(ZonedDateTime.parse(value, DATE_TIME_FORMATTER), retrieveLogDateTime(value));
  }

  @Test(expected = ParseException.class)
  @Parameters(value = {
      "10/Oct/2000:13:55:36",
      "10/10/2000:13:55:36 +0800",
      "35/Oct/2000:13:55:36 +0800",
      "Oct/10/2000:13:55:36 +0800"
  })
  public void nonValidDateTest(String value) throws ParseException {
    retrieveLogDateTime(value);
  }

}
