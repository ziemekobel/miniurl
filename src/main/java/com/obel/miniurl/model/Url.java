package com.obel.miniurl.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class Url {

	@Id
	@GeneratedValue
	private Long id;
	
	@Column(nullable = false)
	private String url;
	
	@Column(nullable = false, unique = true)
	private String miniUrl;
	
	@ManyToOne
	private User createdBy;
	
	private Date created;
	
	private Integer accessed = 0;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMiniUrl() {
		return miniUrl;
	}

	public void setMiniUrl(String miniUrl) {
		this.miniUrl = miniUrl;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getAccessed() {
		return accessed;
	}

	public void setAccessed(Integer accessed) {
		this.accessed = accessed;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}
}
