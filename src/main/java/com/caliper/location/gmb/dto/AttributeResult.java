package com.caliper.location.gmb.dto;

import java.util.List;

import com.google.api.services.mybusinessbusinessinformation.v1.model.Attribute;

public class AttributeResult {

	public List<Attribute> attributeList;
	public String attributeMask;

	    public AttributeResult(List<Attribute> attributeList2, String attributeMask) {
	        this.attributeList = attributeList2;
	        this.attributeMask = attributeMask;
	    }
}
