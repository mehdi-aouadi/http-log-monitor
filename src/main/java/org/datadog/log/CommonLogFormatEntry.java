package org.datadog.log;

import java.time.ZonedDateTime;

import lombok.Builder;
import lombok.Value;

/**
 * Represents a common log format entry.
 * This class is immutable.
 */
@Value
@Builder
public class CommonLogFormatEntry {

  /**
   * The IP address of the client (remote host) which made the request to the server.
   */
  private final String host;

  /**
   * The RFC 1413 identity of the client.
   */
  private final String userRfcId;

  /**
   * The userid of the person requesting the document as determined by HTTP authentication.
   */
  private final String userId;

  /**
   *  The date, time, and time zone that the server finished processing the request..
   */
  private final ZonedDateTime logDateTime;

  /**
   * The HTTP method used by the client (GET, POST, PUT, PATCH, DELETE ...)
   */
  private final String method;

  /**
   * The resource requested by the client.
   */
  private final String resource;

  /**
   * The protocol used by the client to perform the request.
   */
  private final String protocol;

  /**
   * The status code that the server sends back to the client.
   */
  private final int status;

  /**
   * The size of the object returned to the client, not including the response headers,
   *  measured in bytes.
   */
  private final int size;

}
