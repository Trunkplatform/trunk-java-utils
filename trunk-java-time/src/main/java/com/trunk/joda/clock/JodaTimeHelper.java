package com.trunk.joda.clock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class JodaTimeHelper {
  static final public DateTime MIN = new DateTime(0000, 1, 1, 0, 0, 0, DateTimeZone.UTC );
  static final public DateTime MAX = new DateTime( 9999, 1, 1, 0, 0, 0, DateTimeZone.UTC );

  /**
   * Correct the time in the DateTime so that the time is adjusted to the equivalent in the
   * DateTimeZone, while retaining the DateTime's original time zone.
   * <p>
   *   eg {@code recalibrateLocalTimeZoneToNewTimeZone('2016-01-01 12:00:00+0300', '-0100')}
   *   will result in {@code '2016-01-01 08:00:00+0300'}
   * </p>
   * <p>
   *   This is used to correct dates parsed without a timezone.
   * </p>
   */
  public static DateTime recalibrateLocalTimeZoneToNewTimeZone(DateTime d, DateTimeZone timeZone) {
    DateTimeZone originalTimeZone = d.getZone();
    return d.withZone(timeZone)
      .withZoneRetainFields(originalTimeZone);
  }
}
