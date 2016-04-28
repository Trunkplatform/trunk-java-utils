# trunk-java-utils

A collection of utility libraries for Java

## trunk-java-test

A [Netty](http://netty.io/) based HTTP test harness.

### withServer(...)

`TestServer#withServer(...)` provides utilities to set up a light-weight test server for testing.
It manages the server lifecycle by breaking down the the lifecycle so that (a)
test resources are properly closed, (b) test server address is generated and passed
to the test, and (c) that there is a channel between the test server handlers and the
test assertion code so that we can verify what the server receives.


### withOauthServer(...)

`TestServer#withOuathServer(...)` provides a simple OAuth2 proxy server based on the output of
[Doorkeeper](https://github.com/doorkeeper-gem/doorkeeper). It will validate Oauth2 bearer requests
and return responses as defined by the tester.

The OAuth server responds to the `/oauth/token` and `/oauth/token/info` paths.

See [TestServerTest](https://github.com/Trunkplatform/trunk-java-utils/blob/master/trunk-java-utils-test/src/test/java/com/trunk/test/http/TestServerTest.java)
for examples.

## trunk-java-time

`Clock` is a simple shim to allow dependency injection of `now()`.

`JodaClock` is the default [Joda-Time](http://www.joda.org/joda-time/) implementation.
It features timezone manipulation to set the active timezone of `#now()` to be different from
the system default timezone.

`JodaClockModule` is a [Guice](https://github.com/google/guice) module to allow injection of
`JodaClock` for all `Clock` injections.

`JodaTimeHelper` and `DateTimeComparison` supply utility objects and functions.

## License

Copyright 2016 Trunk Platform.

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

> http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
