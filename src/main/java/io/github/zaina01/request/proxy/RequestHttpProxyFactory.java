package io.github.zaina01.request.proxy;

import io.github.zaina01.request.Requests;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RequestHttpProxyFactory<T> {
    private final Class<T> httpInterface;
    private final Map<Method, RequestHttpProxy.HttpMethodInvoker> methodCache = new ConcurrentHashMap<>();

    public RequestHttpProxyFactory(Class<T> httpInterface){
        this.httpInterface = httpInterface;
    }
    @SuppressWarnings("unchecked")
    protected T newInstance(RequestHttpProxy<T> requestHttpProxy){
        return (T) Proxy.newProxyInstance(httpInterface.getClassLoader(),new Class[]{httpInterface},requestHttpProxy);
    }

    public T newInstance(Requests requests){
        final RequestHttpProxy<T> httpProxy =new RequestHttpProxy<>(httpInterface,methodCache,requests);
        return newInstance(httpProxy);
    }
}
