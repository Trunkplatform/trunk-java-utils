package com.trunk.java.utils.health;

import java.util.concurrent.CompletableFuture;

import javax.inject.Singleton;

import com.netflix.governator.spi.LifecycleListener;

@Singleton
public class InjectorHealthIndicator implements HealthIndicator, LifecycleListener {

    private volatile Throwable error;
    
    @Override
    public CompletableFuture<HealthIndicatorStatus> check() {
        return (error != null) 
            ? CompletableFuture.completedFuture(HealthIndicatorStatuses.unhealthy(getName(), error))
            : CompletableFuture.completedFuture(HealthIndicatorStatuses.healthy(getName()));
    }

    @Override
    public String getName() {
        return "guice";
    }

    @Override
    public void onStarted() {

    }

    @Override
    public void onStopped(Throwable error) {
        this.error = error;
    }
}
