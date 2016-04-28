package com.trunk.joda.clock;

import org.joda.time.DateTime;
import org.testng.annotations.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class DateTimeComparisonTest {

  @Test
  public void shouldReturnTrueWhenCallingIsBeforeOrEquals_IfDateTimeIsBeforeAnotherDateTime() {
    // given
    DateTime first = new DateTime();
    DateTime second = first.plusDays(1);

    // when
    final boolean result = DateTimeComparison.isBeforeOrEquals(first, second);

    // then
    assertThat(result, is(true));
  }

  @Test
  public void shouldReturnTrueWhenCallingIsBeforeOrEquals_IfDateTimeIsEqualAnotherDateTime() {
    // given
    DateTime first = new DateTime();
    DateTime second = first;

    // when
    final boolean result = DateTimeComparison.isBeforeOrEquals(first, second);

    // then
    assertThat(result, is(true));
  }

  @Test
  public void shouldReturnFalseWhenCallingIsBeforeOrEquals_IfDateTimeIsAfterAnotherDateTime() {
    // given
    DateTime first = new DateTime();
    DateTime second = first.minusDays(1);

    // when
    final boolean result = DateTimeComparison.isBeforeOrEquals(first, second);

    // then
    assertThat(result, is(false));
  }

  @Test
  public void shouldReturnTrueWhenCallingIsAfterOrEquals_IfDateTimeIsBeforeAnotherDateTime() {
    // given
    DateTime first = new DateTime();
    DateTime second = first.minusDays(1);

    // when
    final boolean result = DateTimeComparison.isAfterOrEquals(first, second);

    // then
    assertThat(result, is(true));
  }

  @Test
  public void shouldReturnTrueWhenCallingIsAfterOrEquals_IfDateTimeIsEqualAnotherDateTime() {
    // given
    DateTime first = new DateTime();
    DateTime second = first;

    // when
    final boolean result = DateTimeComparison.isAfterOrEquals(first, second);

    // then
    assertThat(result, is(true));
  }

  @Test
  public void shouldReturnFalseWhenCallingIsAfterOrEquals_IfDateTimeIsAfterAnotherDateTime() {
    // given
    DateTime first = new DateTime();
    DateTime second = first.plusDays(1);

    // when
    final boolean result = DateTimeComparison.isAfterOrEquals(first, second);

    // then
    assertThat(result, is(false));
  }
}
