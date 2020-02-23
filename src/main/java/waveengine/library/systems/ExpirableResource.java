package waveengine.library.systems;

import waveengine.exception.ResourceExpiredException;

public class ExpirableResource<T> {
    private T resource;
    private long lastUsedTime;
    private boolean expired = false;

    public ExpirableResource(T resource) {
        this.resource = resource;
    }

    public void refresh() {
        lastUsedTime = System.currentTimeMillis();
    }

    public T getResource() {
        if (expired) {
            throw new ResourceExpiredException();
        }
        refresh();
        return resource;
    }

    public boolean isExpired() {
        return expired;
    }

    //default is 0.5s
    public boolean expireIfPossible() {
        long now = System.currentTimeMillis();
        if (lastUsedTime - now > 500) {
            expired = true;
        }
        return expired;
    }
}
