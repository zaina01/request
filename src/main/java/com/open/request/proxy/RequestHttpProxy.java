package com.open.request.proxy;

import com.open.request.Requests;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

public class RequestHttpProxy<T> implements InvocationHandler {
    private final Class<T> httpInterface;
    private final Map<Method, HttpMethodInvoker> methodCache;
    private final Requests requests;
    public  RequestHttpProxy(Class<T> httpInterface,Map<Method, HttpMethodInvoker> methodCache,Requests requests) {
        this.httpInterface = httpInterface;
        this.methodCache = methodCache;
        this.requests=requests;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        try {
            if (Object.class.equals(method.getDeclaringClass())) {
                return method.invoke(this, args);
            }
            return cachedInvoker(method).invoke(proxy, method, args,requests);
        } catch (Throwable t) {
            throw new RuntimeException(t.getMessage(), t);
        }
    }

    private HttpMethodInvoker cachedInvoker(Method method){

        return methodCache.computeIfAbsent(method, m -> {
            if (!m.isDefault()) {
                return new PlainMethodInvoker(new RequestHttpMethod(httpInterface,method,requests.getConfiguration()));
            }
            try {
                return new DefaultMethodInvoker(getMethodHandle(method));
            } catch (IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

    }
    private MethodHandle getMethodHandle(Method method) throws IllegalAccessException, NoSuchMethodException {
        Class<?> declaringClass = method.getDeclaringClass();
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(declaringClass, MethodHandles.lookup());
        return lookup.findSpecial(declaringClass, method.getName(), MethodType.methodType(method.getReturnType(), method.getParameterTypes()), declaringClass);
    }

    interface HttpMethodInvoker {
        Object invoke(Object proxy, Method method, Object[] args, Requests requests) throws Throwable;
    }

    private record PlainMethodInvoker(RequestHttpMethod httpMethod) implements HttpMethodInvoker {
        @Override
            public Object invoke(Object proxy, Method method, Object[] args, Requests requests) throws Throwable {
                return httpMethod.execute(requests,args);
            }
        }

    private record DefaultMethodInvoker(MethodHandle methodHandle) implements HttpMethodInvoker {

        @Override
            public Object invoke(Object proxy, Method method, Object[] args, Requests requests) throws Throwable {
                return methodHandle.bindTo(proxy).invokeWithArguments(args);
            }
        }
}
