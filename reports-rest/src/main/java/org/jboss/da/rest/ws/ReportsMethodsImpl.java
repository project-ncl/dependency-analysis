package org.jboss.da.rest.ws;

import org.jboss.da.common.websocket.DefaultMethod;
import org.jboss.da.common.websocket.Method;
import org.jboss.da.common.websocket.Methods;
import org.jboss.da.reports.model.rest.AdvancedReport;
import org.jboss.da.reports.model.rest.AlignReport;
import org.jboss.da.reports.model.rest.AlignReportRequest;
import org.jboss.da.reports.model.rest.BuiltReport;
import org.jboss.da.reports.model.rest.BuiltReportRequest;
import org.jboss.da.reports.model.rest.GAVRequest;
import org.jboss.da.reports.model.rest.LookupGAVsRequest;
import org.jboss.da.reports.model.rest.LookupReport;
import org.jboss.da.reports.model.rest.Report;
import org.jboss.da.reports.model.rest.SCMReportRequest;
import org.jboss.da.rest.facade.ReportsFacade;

import javax.faces.bean.ApplicationScoped;
import javax.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ApplicationScoped
@ReportsWebsocketMethods
public class ReportsMethodsImpl implements Methods {

    private final DefaultMethod<SCMReportRequest, Report, Map> SCM;

    private final DefaultMethod<SCMReportRequest, AdvancedReport, Map> SCM_ADVANCED;

    private final DefaultMethod<GAVRequest, Report, Map> GAV;

    private final DefaultMethod<BuiltReportRequest, Set<BuiltReport>, Set> BUILT;

    private final DefaultMethod<AlignReportRequest, AlignReport, Map> ALIGN;

    private final DefaultMethod<LookupGAVsRequest, List<LookupReport>, List> LOOKUP_GAV;

    private final HashMap<String, Method<?, ?, ?>> methods = new HashMap<>();

    @Inject
    public ReportsMethodsImpl(ReportsFacade facade){

        SCM = put(new DefaultMethod<>("reports.scm",
                SCMReportRequest.class,
                Map.class,
                params -> facade.scmReport(params)));

        SCM_ADVANCED = put(new DefaultMethod<>("reports.scmAdvanced",
                SCMReportRequest.class,
                Map.class,
                params -> facade.advancedScmReport(params)));

        GAV = put(new DefaultMethod<>("reports.gav",
                GAVRequest.class,
                Map.class,
                params -> facade.gavReport(params)));

        BUILT = put(new DefaultMethod<>("reports.built",
                BuiltReportRequest.class,
                Set.class,
                params -> facade.builtReport(params)));

        ALIGN = put(new DefaultMethod<>("reports.align",
                AlignReportRequest.class,
                Map.class,
                params -> facade.alignReport(params)));

        LOOKUP_GAV = put(new DefaultMethod<>("reports.lookup.gav",
                LookupGAVsRequest.class,
                List.class,
                params -> facade.gavsReport(params)));
    }

    private <T, S, R> DefaultMethod<T, S, R> put(DefaultMethod<T, S, R> method) {
        methods.put(method.getName(), method);
        return method;
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
