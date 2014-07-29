package com.etone.ark.kernel.http;

import java.util.ArrayList;
import java.util.List;

import com.etone.ark.kernel.service.model.RtdDevice;

public class EventRtdInfoChange {

    private List<RtdDevice> liRtd;
    
    public EventRtdInfoChange(){
    	this.liRtd = new ArrayList<RtdDevice>(); 
    }
     
    public EventRtdInfoChange(RtdDevice rtd){
        this.liRtd = new ArrayList<RtdDevice>();
        this.liRtd.add(rtd);
    }
    
    public EventRtdInfoChange(List<RtdDevice> liRtd){
        this.liRtd = liRtd;
    }

    public List<RtdDevice> getLiRtd() {
        return liRtd;
    }
}
