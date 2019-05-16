package com.obel.miniurl.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.obel.miniurl.model.Url;

public interface UrlRepository extends CrudRepository<Url, Long> {

	Url findByMiniUrl(String miniUrl);

	List<Url> findByCreatedByUsernameOrderByCreated(String name);
}
