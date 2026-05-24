package com.xingqiu.server.repair.dto;

import java.time.LocalDateTime;

public class CreateDeviceRequest {

    private String deviceNo;
    private String name;
    private String location;
    private String area;
    private String type;
    private String model;
    private String manufacturer;
    private LocalDateTime installDate;

    public String getDeviceNo() { return deviceNo; }
    public void setDeviceNo(String deviceNo) { this.deviceNo = deviceNo; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public LocalDateTime getInstallDate() { return installDate; }
    public void setInstallDate(LocalDateTime installDate) { this.installDate = installDate; }
}
