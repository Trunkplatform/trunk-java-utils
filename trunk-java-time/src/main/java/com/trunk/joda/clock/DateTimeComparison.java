package com.trunk.joda.clock;

import org.joda.time.DateTime;

public class DateTimeComparison {

  public static boolean isBeforeOrEquals(DateTime a, DateTime b) {
    return !a.isAfter(b);
  }

  public static boolean isAfterOrEquals(DateTime a, DateTime b) {
    return !a.isBefore(b);
  }
}
