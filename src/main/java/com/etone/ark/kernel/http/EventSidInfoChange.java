package com.etone.ark.kernel.http;

import java.util.ArrayList;
import java.util.Collection;

import com.etone.ark.kernel.service.model.SidDevice;

public class EventSidInfoChange {

    private final Collection<SidDevice> liSid;
    
    public EventSidInfoChange(){
    	liSid = new ArrayList<SidDevice>();
    }
    
    public EventSidInfoChange(SidDevice sid){
        liSid = new ArrayList<SidDevice>();
        this.liSid.add(sid);
    }
    
    public EventSidInfoChange(Collection<SidDevice> liSid){
        this.liSid = liSid;
    }

    public Collection<SidDevice> getLiSid() {
        return liSid;
    }
}
