package com.bridge4biz.laundry.io.model.map;

import java.util.ArrayList;
import java.util.Collection;

public class AddressComponent {
    private String long_name;
    private String short_name;
    private Collection<String> types = new ArrayList<String>();

    public String getLongName() {
        return long_name;
    }

    public void setLongName(String longName) {
        this.long_name = longName;
    }

    public String getShortName() {
        return short_name;
    }

    public void setShortName(String shortName) {
        this.short_name = shortName;
    }

    public Collection<String> getTypes() {
        return types;
    }

    public void setTypes(Collection<String> types) {
        this.types = types;
    }
}
