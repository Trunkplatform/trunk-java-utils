package trunk.java.utils.clock;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

public class JodaTimeHelper {
  static final public DateTime MIN = new DateTime(0000, 1, 1, 0, 0, 0, DateTimeZone.UTC );
  static final public DateTime MAX = new DateTime( 9999, 1, 1, 0, 0, 0, DateTimeZone.UTC );
}
