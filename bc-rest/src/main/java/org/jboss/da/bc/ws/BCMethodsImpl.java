package org.jboss.da.bc.ws;

import org.jboss.da.bc.facade.BuildConfigurationsProductFacade;
import org.jboss.da.bc.model.rest.EntryEntity;
import org.jboss.da.bc.model.rest.FinishResponse;
import org.jboss.da.bc.model.rest.ProductInfoEntity;
import org.jboss.da.common.websocket.DefaultMethod;
import org.jboss.da.common.websocket.Method;
import org.jboss.da.common.websocket.Methods;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
@BuildConfigurationWebsocketMethods
public class BCMethodsImpl implements Methods {

    private final DefaultMethod<EntryEntity, ProductInfoEntity, Map> START_PROCESS;

    private final DefaultMethod<ProductInfoEntity, ProductInfoEntity, Map> NEXT_LEVEL;

    private final DefaultMethod<ProductInfoEntity, FinishResponse<ProductInfoEntity>, Map> FINISH_PROCESS;

    private final HashMap<String, Method<?, ?, ?>> methods = new HashMap<>();

    @Inject
    public BCMethodsImpl(BuildConfigurationsProductFacade productFacade) {
        START_PROCESS = new DefaultMethod<>("buildConfiguration.product.start",
                EntryEntity.class,
                Map.class,
                params -> productFacade.startAnalyse(params));
        methods.put(START_PROCESS.getName(), START_PROCESS);

        NEXT_LEVEL = new DefaultMethod<>("buildConfiguration.product.nextLevel",
            ProductInfoEntity.class,
            Map.class,
            params -> productFacade.analyseNextLevel(params));
        methods.put(NEXT_LEVEL.getName(), NEXT_LEVEL);

        FINISH_PROCESS = new DefaultMethod<>("buildConfiguration.product.finish",
            ProductInfoEntity.class,
            Map.class,
            params -> productFacade.finishAnalyse(params));
        methods.put(FINISH_PROCESS.getName(), FINISH_PROCESS);
    }

    @Override
    public boolean contains(String method) {
        return methods.containsKey(method);
    }

    @Override
    public Method<?, ?, ?> get(String method) {
        return methods.get(method);
    }
}
