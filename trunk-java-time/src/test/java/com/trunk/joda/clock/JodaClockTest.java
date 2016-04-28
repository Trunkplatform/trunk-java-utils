package com.trunk.joda.clock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class JodaClockTest {

  @Test
  public void shouldReturnCurrentDateTimeUsingDefaultTimezone() {
    // given
    JodaClock jc = JodaClock.withDefaultTimeZone();

    // when
    final DateTime now = jc.now();

    // then
    assertThat(now.toString("z"), is(DateTimeZone.getDefault().getShortName(0)));
  }

  @Test
  public void shouldReturnCurrentDateTimeUsingSpecifiedTimezone() {
    // given
    DateTimeZone bangkok = DateTimeZone.forID("Asia/Bangkok");
    JodaClock jc = JodaClock.with(bangkok);

    // when
    final DateTime now = jc.now();

    // then
    assertThat(now.toString("z"), is(bangkok.getShortName(0)));
  }
}
