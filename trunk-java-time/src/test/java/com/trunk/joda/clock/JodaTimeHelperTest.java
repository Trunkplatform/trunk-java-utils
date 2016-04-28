package com.trunk.joda.clock;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import fj.data.Stream;

import static org.joda.time.DateTimeZone.UTC;
import static org.testng.Assert.assertEquals;

public class JodaTimeHelperTest {

  public static final DateTimeZone SYDNEY = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Australia/Sydney"));

  @Test
  public void shouldMakeDateTimeAppearWithLocalTimeZoneAndSydneyLocalTime() throws Exception {
    Stream<DateTime> input = Stream.arrayStream(
      DateTime.parse("2016-01-01T00:00:00").withZoneRetainFields(UTC),
      DateTime.parse("2016-01-01T00:05:00").withZoneRetainFields(SYDNEY)
    ).map(d -> JodaTimeHelper.recalibrateLocalTimeZoneToNewTimeZone(d, SYDNEY));

    Stream<DateTime> expectedOutput = Stream.arrayStream(
      DateTime.parse("2016-01-01T00:00:00").withZoneRetainFields(UTC).withZone(SYDNEY).withZoneRetainFields(UTC),
      DateTime.parse("2016-01-01T00:05:00").withZoneRetainFields(SYDNEY)
    );
    assertEquals(input, expectedOutput);
  }

}
