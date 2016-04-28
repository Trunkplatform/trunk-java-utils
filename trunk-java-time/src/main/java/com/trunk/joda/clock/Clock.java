package com.trunk.joda.clock;

import org.joda.time.DateTime;

/**
 * A shim to allow the injection of time into objects that depend on
 * {@link DateTime#now()}.
 */
public interface Clock {
  DateTime now();
}
