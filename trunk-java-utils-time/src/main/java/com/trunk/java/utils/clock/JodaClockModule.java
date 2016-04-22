package com.trunk.java.utils.clock;

import org.joda.time.DateTimeZone;

import com.google.inject.AbstractModule;

public class JodaClockModule extends AbstractModule {

  private DateTimeZone timeZone;

  public JodaClockModule(DateTimeZone timeZone) {
    this.timeZone = timeZone;
  }

  @Override
  protected void configure() {
    bind(Clock.class).toInstance(new JodaClock(timeZone));
  }
}
