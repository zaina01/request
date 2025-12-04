package io.github.zaina01.request;

import io.github.zaina01.request.proxy.RequestHttpProxyFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestHttpRegistry {
    private final Configuration config;
    private final Map<Class<?>, RequestHttpProxyFactory<?>> knownHttps = new ConcurrentHashMap<>();

    RequestHttpRegistry(Configuration config) {
        this.config = config;
    }

    public <T> T getRequest(Class<T> type, Requests requests) {
        RequestHttpProxyFactory<T> requestHttpProxyFactory = (RequestHttpProxyFactory<T>) knownHttps.get(type);
        return requestHttpProxyFactory.newInstance(requests);
    }

    public <T> boolean hasRequest(Class<T> type) {
        return knownHttps.containsKey(type);
    }

    public <T> void addRequest(Class<T> type) {
        if (type.isInterface()) {
            if (hasRequest(type)) {
                return;
            }
            boolean loadCompleted = false;
            try {
                knownHttps.put(type, new RequestHttpProxyFactory<>(type));
                RequestHttpAnnotationBuilder parser = new RequestHttpAnnotationBuilder(config, type);
                parser.parse();
                loadCompleted = true;
            }finally {
                if (!loadCompleted) {
                    knownHttps.remove(type);
                }
            }

        }
    }

}
