package com.trunk.java.utils.clock;

import java.util.TimeZone;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import fj.data.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.joda.time.DateTimeZone.UTC;
import static org.testng.Assert.assertEquals;
import static com.trunk.java.utils.clock.JodaClock.recalibrateLocalTimezoneToDifferentTimezone;

public class JodaClockTest {

  public static final DateTimeZone SYDNEY = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Australia/Sydney"));

  @Test
  public void shouldReturnCurrentDateTimeUsingDefaultTimezone() {
    // given
    JodaClock jc = new JodaClock();

    // when
    final DateTime now = jc.now();

    // then
    assertThat(now.toString("z"), is(DateTimeZone.getDefault().getShortName(0)));
  }

  @Test
  public void shouldReturnCurrentDateTimeUsingSpecifiedTimezone() {
    // given
    DateTimeZone bangkok = DateTimeZone.forID("Asia/Bangkok");
    JodaClock jc = new JodaClock(bangkok);

    // when
    final DateTime now = jc.now();

    // then
    assertThat(now.toString("z"), is(bangkok.getShortName(0)));
  }

  @Test
  public void shouldMakeDateTimeAppearWithLocalTimeZoneAndSydneyLocalTime() throws Exception {
    Stream<DateTime> input = Stream.arrayStream(
      DateTime.parse("2016-01-01T00:00:00").withZoneRetainFields(UTC),
      DateTime.parse("2016-01-01T00:05:00").withZoneRetainFields(SYDNEY)
    ).map(d -> recalibrateLocalTimezoneToDifferentTimezone(d, SYDNEY));

    Stream<DateTime> expectedOutput = Stream.arrayStream(
      DateTime.parse("2016-01-01T00:00:00").withZoneRetainFields(UTC).withZone(SYDNEY).withZoneRetainFields(UTC),
      DateTime.parse("2016-01-01T00:05:00").withZoneRetainFields(SYDNEY)
    );
    assertEquals(input, expectedOutput);
  }
}