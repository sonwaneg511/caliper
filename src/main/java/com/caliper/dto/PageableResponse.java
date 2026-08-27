package com.caliper.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PageableResponse<T> {

	private List<T> content;
	private Integer pageNo;
	private Integer pageSize;
	private Long totalElements;
	private Integer totalPages;
	private Boolean isLast;

	public PageableResponse(Page<T> pageData) {
		this.content = pageData.getContent();
		this.pageNo = pageData.getNumber();
		this.pageSize = pageData.getSize();
		this.totalElements = pageData.getTotalElements();
		this.totalPages = pageData.getTotalPages();
		this.isLast = pageData.isLast();
	}
}