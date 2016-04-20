package trunk.java.utils.health;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

public class GuiceApplicationLifecycleModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(HealthIndicator.class).annotatedWith(Names.named("guice")).to(InjectorHealthIndicator.class).asEagerSingleton();
  }
}
