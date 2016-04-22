package com.trunk.java.utils.health;

import org.testng.annotations.Test;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import com.netflix.governator.InjectorBuilder;

import static org.testng.Assert.assertEquals;

public class HealthIndicatorTest {
  @Test
  public void failureOnNoDefinedHealthCheck() {
    Injector injector = InjectorBuilder.fromModules(
      new GuiceApplicationLifecycleModule(),
      new TestApplicationLifecycleModule()
    ).createInjector();

    HealthCheck hc = injector.getInstance(HealthCheck.class);
    HealthCheckStatus status = hc.check().join();
    assertEquals(hc.check().join().getState(), HealthState.Healthy);
    assertEquals(status.getIndicators().size(), 1);
  }

  @Test
  public void successWithSingleHealthCheck() {
    Injector injector = InjectorBuilder.fromModules(
      new TestApplicationLifecycleModule(),
      new GuiceApplicationLifecycleModule(),
      new AbstractModule() {
        @Override
        protected void configure() {
          bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
        }
      }).createInjector();

    HealthCheck hc = injector.getInstance(HealthCheck.class);
    HealthCheckStatus status = hc.check().join();
    assertEquals(hc.check().join().getState(), HealthState.Healthy);
    assertEquals(status.getIndicators().size(), 2);
  }

  @Test
  public void successWithSingleAndNamedHealthCheck() {
    Injector injector = InjectorBuilder.fromModules(
      new TestApplicationLifecycleModule(),
      new GuiceApplicationLifecycleModule(),
      new AbstractModule() {
        @Override
        protected void configure() {
          bind(HealthIndicator.class).toInstance(HealthIndicators.alwaysHealthy("foo"));
          bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
        }
      }).createInjector();

    HealthCheck hc = injector.getInstance(HealthCheck.class);
    HealthCheckStatus status = hc.check().join();
    assertEquals(hc.check().join().getState(), HealthState.Unhealthy);
    assertEquals(status.getIndicators().size(), 3);

    System.out.println(status.getIndicators());
  }

  @Test
  public void successDefaultCompositeWithSingleNamedHealthCheck() {
    Injector injector = Guice.createInjector(
      new TestApplicationLifecycleModule(),
      new AbstractModule() {
        @Override
        protected void configure() {
          bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
        }
      });
    HealthCheck hc = injector.getInstance(HealthCheck.class);
    HealthCheckStatus status = hc.check().join();
    assertEquals(status.getState(), HealthState.Unhealthy);
    assertEquals(status.getIndicators().size(), 1);
  }

  @Test
  public void successDefaultCompositeWithMultipleNamedHealthCheck() {
    Injector injector = Guice.createInjector(
      new TestApplicationLifecycleModule(),
      new AbstractModule() {
        @Override
        protected void configure() {
          bind(HealthIndicator.class).annotatedWith(Names.named("foo")).toInstance(HealthIndicators.alwaysUnhealthy("foo"));
          bind(HealthIndicator.class).annotatedWith(Names.named("bar")).toInstance(HealthIndicators.alwaysUnhealthy("bar"));
        }
      });

    HealthCheck hc = injector.getInstance(HealthCheck.class);
    HealthCheckStatus status = hc.check().join();
    assertEquals(hc.check().join().getState(), HealthState.Unhealthy);
    assertEquals(status.getIndicators().size(), 2);
  }

  private class TestApplicationLifecycleModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(ApplicationLifecycle.class).to(TestApplicationLifecycle.class);
    }
  }

  private static class TestApplicationLifecycle implements ApplicationLifecycle {
    @Override
    public LifecycleState getState() {
      return LifecycleState.Running;
    }
  }
}
