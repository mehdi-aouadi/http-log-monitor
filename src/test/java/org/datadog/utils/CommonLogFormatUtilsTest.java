package org.datadog.utils;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.datadog.parser.ParseException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import static org.datadog.utils.CommonLogFormatUtils.retrieveIntValue;
import static org.datadog.utils.CommonLogFormatUtils.retrieveSection;

@RunWith(JUnitParamsRunner.class)
public class CommonLogFormatUtilsTest {

  @Test
  @Parameters(value = {"123", "-123"})
  public void retrieveInt(String value) throws ParseException {
    int result = retrieveIntValue(value, true);
    assertEquals(Integer.parseInt(value), result);
    result = retrieveIntValue(value, false);
    assertEquals(Integer.parseInt(value), result);
  }

  @Test
  public void retrieveIntAuthorizedMissingValue() throws ParseException {
    int result = retrieveIntValue("-", true);
    assertEquals(0, result);
  }

  @Test(expected = ParseException.class)
  public void retrieveIntUnauthorizedMissingValue() throws ParseException {
    retrieveIntValue("-", false);
  }

  @Test(expected = ParseException.class)
  @Parameters(value = {"notANumber", "12 3", "1e5", "1_000"})
  public void retrieveIntNotNumber(String value) throws ParseException {
    retrieveIntValue(value, true);
  }

  @Test
  @Parameters(value = {
      "/pages/create",
      "/pages/create/",
      "/pages/create/image.gif"
  })
  public void retrieveSectionCorrectUrlTest(String resourceUrl) throws ParseException {
    assertEquals("pages", retrieveSection(resourceUrl));
  }

  @Test(expected = ParseException.class)
  @Parameters(value = {
      "/pages",
      "/pages/",
      "pages",
      "pages/",
      "pages//",
      "//pages",
      "//pages/",
      "pages/create",
      "//pages/create",
      ""
  })
  public void retrieveSectionNotPresentTest(String resourceUrl) throws ParseException {
    retrieveSection(resourceUrl);
  }

}
