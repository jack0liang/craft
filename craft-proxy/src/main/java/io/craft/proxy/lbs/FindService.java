package io.craft.proxy.lbs;

import io.craft.core.lbschedule.HashSchedule;
import io.craft.core.lbschedule.LBSchedule;
import io.craft.core.lbschedule.RoundRobinSchedule;
import io.craft.core.lbschedule.WeightedRoundRobinSchedule;
import io.craft.proxy.discovery.EtcdServiceDiscovery;
import io.craft.proxy.discovery.ServiceHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class FindService {
    private EtcdServiceDiscovery discovery;
    private Class<? extends LBSchedule> lbsClz;
    private Map<String, LBSchedule> app2LBSchedule = new HashMap<>();

    public FindService(EtcdServiceDiscovery discovery,Class<? extends LBSchedule> lbsClz){
        this.discovery = discovery;
        this.lbsClz = lbsClz;
    }

    public String find(String applicationName) throws ExecutionException, InterruptedException {
        LBSchedule lbSchedule = app2LBSchedule.get(applicationName);
        if(lbSchedule==null){
            synchronized (app2LBSchedule) {
                lbSchedule = app2LBSchedule.get(applicationName);
                if(lbSchedule==null) {
                    lbSchedule = getLbSchedule();
                    ServiceHolder serviceHolder = discovery.getServiceHolder(applicationName);
                    serviceHolder.setLbSchedule(lbSchedule);
                    app2LBSchedule.put(applicationName, lbSchedule);
                }
            }
        }
        return (String)lbSchedule.get();
    }

    private LBSchedule getLbSchedule() {
        LBSchedule lbSchedule;
        if(WeightedRoundRobinSchedule.class.equals(lbsClz))
            lbSchedule = new WeightedRoundRobinSchedule(null);
        else if(HashSchedule.class.equals(lbsClz))
            lbSchedule = new HashSchedule(null);
        else
            lbSchedule = new RoundRobinSchedule(null);
        return lbSchedule;
    }

    public static void main(String[] args) {
        FindService service = new FindService(null,RoundRobinSchedule.class);
        System.out.println(service.lbsClz.equals(RoundRobinSchedule.class));
    }
}
