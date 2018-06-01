package org.jboss.da.rest.websocket;

import lombok.Getter;

public class DefaultMethod<T, S, R> implements Method<T, S, R> {

    @Getter
    private final String name;

    private final Class<T> parameterClass;

    private final Class<R> jsonClass;

    private final FF<T, S> method;

    public DefaultMethod(String name, Class<T> parameterClass, Class<R> jsonClass, FF<T, S> method) {
        this.name = name;
        this.parameterClass = parameterClass;
        this.method = method;
        this.jsonClass = jsonClass;
    }

    @Override
    public Class<T> getParameterClass() {
        return parameterClass;
    }

    @Override
    public S execute(T params) throws Exception {
        return method.apply(params);
    }

    @FunctionalInterface
    public interface FF<T, R> {

        R apply(T t) throws Exception;
    }

    @Override
    public Class<R> getJsonOutputClass() {
        return jsonClass;
    }

}
