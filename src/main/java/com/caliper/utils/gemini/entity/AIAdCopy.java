package com.caliper.utils.gemini.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "ai_ad_copy")
public class AIAdCopy {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private long id;
	
	@Column(name="id_of_client")
	private long idOfClient;
	
	@Column(name="client_id")
	private String clientId;
	
	private String type;
	
	@Column(name="head_desc_value")
	private String headDescValue;
	
	public static final String HEADLINE = "HEADLINE";
	public static final String LONG_HEADLINE = "LONG_HEADLINE";
	public static final String DESCRIPTION = "DESCRIPTION";
}
