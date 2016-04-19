package trunk.java.utils.clock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.TimeZone;

public class JodaClock implements Clock {

  public static final DateTimeZone SYDNEY_TZ = DateTimeZone.forTimeZone(TimeZone.getTimeZone("Australia/Sydney"));

  private final DateTimeZone dateTimeZone;

  public JodaClock() {
    this(DateTimeZone.getDefault());
  }

  public JodaClock(DateTimeZone dateTimeZone) {
    this.dateTimeZone = dateTimeZone;
  }

  @Override
  public DateTime now() {
    return DateTime.now().withZone(dateTimeZone);
  }

}
