package org.jboss.da.bc.ws;

import org.jboss.da.bc.facade.BuildConfigurationsProductFacade;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.FinishResponse;
import org.jboss.da.bc.model.rest.ProductInfoEntity;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import java.util.HashMap;

import lombok.Getter;

/**
 *
 * @author Honza Brázdil <jbrazdil@redhat.com>
 */
@ApplicationScoped
public class MethodsImpl implements Methods {

    private final MethodImpl<EntryEntity, ProductInfoEntity> START_PROCESS;

    private final MethodImpl<ProductInfoEntity, ProductInfoEntity> NEXT_LEVEL;

    private final MethodImpl<ProductInfoEntity, FinishResponse<ProductInfoEntity>> FINISH_PROCESS;

    private final HashMap<String, Method> methods = new HashMap<>();

    @Inject
    public MethodsImpl(BuildConfigurationsProductFacade productFacade){
        START_PROCESS = new MethodImpl<>("buildConfiguration.product.start",
                EntryEntity.class,
                params -> productFacade.startAnalyse(params));
        methods.put(START_PROCESS.getName(), START_PROCESS);

        NEXT_LEVEL = new MethodImpl<>("buildConfiguration.product.nextLevel",
            ProductInfoEntity.class,
            params -> productFacade.analyseNextLevel(params));
        methods.put(NEXT_LEVEL.getName(), NEXT_LEVEL);

        FINISH_PROCESS = new MethodImpl<>("buildConfiguration.product.finish",
            ProductInfoEntity.class,
            params -> productFacade.finishAnalyse(params));
        methods.put(FINISH_PROCESS.getName(), FINISH_PROCESS);
    }

    @Override
    public boolean contains(String method) {
        return methods.containsKey(method);
    }

    @Override
    public Method get(String method) {
        return methods.get(method);
    }

    private static class MethodImpl<T, S> implements Method<T, S> {

        @Getter
        private final String name;

        private final Class<T> parameterClass;

        private final FF<T, S> method;

        private MethodImpl(String name, Class<T> parameterClass, FF<T, S> method) {
            this.name = name;
            this.parameterClass = parameterClass;
            this.method = method;
        }

        @Override
        public Class<T> getParameterClass() {
            return parameterClass;
        }

        @Override
        public S execute(T params) throws Exception {
            return method.apply(params);
        }
    }

    @FunctionalInterface
    private interface FF<T, R> {

        R apply(T t) throws Exception;
    }
}
