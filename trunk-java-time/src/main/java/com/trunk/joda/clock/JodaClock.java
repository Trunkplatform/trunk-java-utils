package com.trunk.joda.clock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * The default implementation of {@link Clock} that
 * wraps {@link DateTime#now()}. It supports using the
 * default system time zone or the given time zone
 */
public abstract class JodaClock implements Clock {

  public static JodaClock withDefaultTimeZone() {
    return new DefaultJodaClock();
  }

  public static JodaClock with(DateTimeZone dateTimeZone) {
    return new TzJodaClock(dateTimeZone);
  }

  private static class DefaultJodaClock extends JodaClock {
    @Override
    public DateTime now() {
      return DateTime.now();
    }
  }

  private static class TzJodaClock extends JodaClock {
    private final DateTimeZone dateTimeZone;

    public TzJodaClock(DateTimeZone dateTimeZone) {
      this.dateTimeZone = dateTimeZone;
    }

    @Override
    public DateTime now() {
      return DateTime.now().withZone(dateTimeZone);
    }
  }
}
