package org.datadog.parser;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.datadog.log.CommonLogFormatEntry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@RunWith(JUnitParamsRunner.class)
public class CommonLogFormatParserTest {

  private CommonLogFormatParserImpl clfParser;

  @Before
  public void init() {
    clfParser = new CommonLogFormatParserImpl();
  }

  @Test
  public void completeCorrectFullClfLine() throws ParseException {
    String commonLogFormatLine = "127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326";
    CommonLogFormatEntry actual = clfParser.parse(commonLogFormatLine);
    CommonLogFormatEntry expected = CommonLogFormatEntry.builder()
        .host("127.0.0.1")
        .userRfcId("user-identifier")
        .userId("frank")
        .logDateTime(ZonedDateTime.of(2000, 10, 10, 13, 55, 36, 0, ZoneOffset.ofHours(-7)))
        .method("GET")
        .resource("/apache_pb.gif")
        .protocol("HTTP/1.0")
        .status(200)
        .size(2326)
        .build();
    Assert.assertEquals(expected, actual);
  }

  @Test
  public void correctClfLineWithAuthorizedMissingValues() throws ParseException {
    String commonLogFormatLine = "127.0.0.1 - - [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 -";
    CommonLogFormatEntry actual = clfParser.parse(commonLogFormatLine);
    CommonLogFormatEntry expected = CommonLogFormatEntry.builder()
        .host("127.0.0.1")
        .userRfcId(null)
        .userId(null)
        .logDateTime(ZonedDateTime.of(2000, 10, 10, 13, 55, 36, 0, ZoneOffset.ofHours(-7)))
        .method("GET")
        .resource("/apache_pb.gif")
        .protocol("HTTP/1.0")
        .status(200)
        .size(0)
        .build();
    Assert.assertEquals(expected, actual);
  }

  @Test(expected = ParseException.class)
  public void clfLineWithMissingStatusCode() throws ParseException {
    String commonLogFormatLine = "127.0.0.1 - - [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" - -";
    clfParser.parse(commonLogFormatLine);
  }

  @Test(expected = ParseException.class)
  @Parameters(value = {
      "user-identifier frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326",
      "127.0.0.1 user-identifier frank [10/10/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 2326",
      "127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36] \"GET /apache_pb.gif HTTP/1.0\" 200 2326",
      "127.0.0.1 user-identifier frank 10/Oct/2000:13:55:36 -0700 \"GET /apache_pb.gif HTTP/1.0\" 2326",
      "127.0.0.1 user-identifier frank \"GET /apache_pb.gif HTTP/1.0\" 200 2326",
      "127.0.0.1 user-identifier frank [10/Oct/2000 -0700] \"/apache_pb.gif HTTP/1.0\" 200 2326",
      "127.0.0.1 user-identifier frank [10/Oct/2000 -0700] \"GET HTTP/1.0\" 200 2326",
      "127.0.0.1 user-identifier frank [10/Oct/2000 -0700] \"GET /apache_pb.gif\" 200 2326",
      "127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] GET /apache_pb.gif HTTP/1.0 200 2326",
      "127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 2326",
      "127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200",
      "127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" notAnInt 2326",
      "127.0.0.1 user-identifier frank [10/Oct/2000:13:55:36 -0700] \"GET /apache_pb.gif HTTP/1.0\" 200 notAnInt",
      ""
  })
  public void badClfLine(String logLine) throws ParseException {
    clfParser.parse(logLine);
  }
}
