package org.jboss.da.rest.ws;

import org.jboss.da.common.websocket.DefaultMethod;
import org.jboss.da.common.websocket.Method;
import org.jboss.da.common.websocket.Methods;
import org.jboss.da.reports.model.rest.AlignReport;
import org.jboss.da.reports.model.rest.AlignReportRequest;
import org.jboss.da.reports.model.rest.BuiltReport;
import org.jboss.da.reports.model.rest.BuiltReportRequest;
import org.jboss.da.rest.facade.ReportsFacade;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@ReportsWebsocketMethods
public class ReportsMethodsImpl implements Methods {

    private final DefaultMethod<BuiltReportRequest, Set<BuiltReport>, Set> BUILT;

    private final DefaultMethod<AlignReportRequest, AlignReport, Map> ALIGN;

    private final HashMap<String, Method<?, ?, ?>> methods = new HashMap<>();

    @Inject
    public ReportsMethodsImpl(ReportsFacade facade){
        BUILT = new DefaultMethod<>("reports.built",
                BuiltReportRequest.class,
                Set.class,
                params -> facade.builtReport(params));
        methods.put(BUILT.getName(), BUILT);
        
        ALIGN = new DefaultMethod<>("reports.align",
                AlignReportRequest.class,
                Map.class,
                params -> facade.alignReport(params));
        methods.put(ALIGN.getName(), ALIGN);
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
