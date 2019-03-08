package io.craft.proxy.discovery;

import io.craft.core.lbschedule.LBSchedule;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class ServiceHolder {
    private String applicationName;
    private int pos = 0;
    private List<String> serviceList = new ArrayList<>();
    private LBSchedule lbSchedule;

    public ServiceHolder(String applicationName){
        this.applicationName = applicationName;
    }

    public synchronized void addService(String service,String old){
        if(StringUtils.isBlank(service)) {
            if(StringUtils.isNotBlank(old)) {
                serviceList.remove(old);
                //ChannelPoolManager.removeChannel(applicationName,old);
            }
            return ;
        }
        if(!serviceList.contains(service))
            serviceList.add(service);
        if(lbSchedule!=null)
            lbSchedule.set(serviceList.toArray());
    }

    public synchronized String next(){
        if(pos>=serviceList.size())
            pos = 0;
        return serviceList.get(pos++);
    }

    public void setLbSchedule(LBSchedule lbSchedule) {
        lbSchedule.set(serviceList.toArray());
        this.lbSchedule = lbSchedule;
    }
}
