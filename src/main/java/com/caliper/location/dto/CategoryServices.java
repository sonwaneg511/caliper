package com.caliper.location.dto;

import java.util.ArrayList;

public class CategoryServices {
	public String categoryId;
    public ArrayList<String> freeFoundServiceItem;
    public ArrayList<String> structuredServiceItem;
    public String structuredServiceDescription;
    public String freeFoundServiceDescription;
	

	public CategoryServices(String categoryId, ArrayList<String> freeFoundServiceItem,
			ArrayList<String> structuredServiceItem, String structuredServiceDescription,
			String freeFoundServiceDescription) {
		super();
		this.categoryId = categoryId;
		this.freeFoundServiceItem = freeFoundServiceItem;
		this.structuredServiceItem = structuredServiceItem;
		this.structuredServiceDescription = structuredServiceDescription;
		this.freeFoundServiceDescription = freeFoundServiceDescription;
	}



	public CategoryServices() {
		// TODO Auto-generated constructor stub
	}
    
}
