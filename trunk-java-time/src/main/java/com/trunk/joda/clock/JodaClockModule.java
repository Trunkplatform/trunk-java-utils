package com.trunk.joda.clock;

import org.joda.time.DateTimeZone;

import com.google.inject.AbstractModule;

public class JodaClockModule extends AbstractModule {

  private DateTimeZone timeZone;

  public JodaClockModule() {
    this(DateTimeZone.getDefault());
  }

  public JodaClockModule(DateTimeZone timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  protected void configure() {
    bind(Clock.class)
      .toInstance(
        timeZone.equals(DateTimeZone.getDefault()) ?
          JodaClock.withDefaultTimeZone() :
          JodaClock.with(timeZone)
      );
  }
}
