package com.obel.miniurl.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.obel.miniurl.controller.dto.UrlDTO;
import com.obel.miniurl.controller.dto.UrlFullDTO;
import com.obel.miniurl.dao.MiniUrlAutoGenerator;
import com.obel.miniurl.dao.UrlRepository;
import com.obel.miniurl.dao.UserRepository;
import com.obel.miniurl.model.Url;

@RestController
public class UrlController {

	@Value("${base_url}")
	private String baseUrl;

	@Autowired
	private UrlRepository urlRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private MiniUrlAutoGenerator miniUrlAutoGenerator;

	@GetMapping("/")
	public List<UrlFullDTO> allUrls(Principal principal) {
		System.out.println(principal.getName());
		return urlRepository.findByCreatedByUsernameOrderByCreated(principal.getName()).stream()
				.map(url -> urlToFullDTO(url)).collect(Collectors.toList());
	}

	@PostMapping("/")
	public UrlDTO minifyUrl(@Valid @RequestBody UrlDTO urlDto, Errors errors, Principal principal) {
		if (errors.hasErrors()) {
			throw new UrlRequestInvalidException();
		} else if (StringUtils.isEmpty(urlDto.miniurl)) {
			urlDto.miniurl = miniUrlAutoGenerator.getNextMiniUrl();
		} else {
			if (urlRepository.findByMiniUrl(urlDto.miniurl) != null) {
				throw new UrlAlreadyExistsException();
			}
		}

		Url url = new Url();
		url.setUrl(urlDto.url);
		url.setMiniUrl(urlDto.miniurl);
		url.setCreatedBy(userRepository.findByUsername(principal.getName()));
		url.setCreated(new Date());

		urlRepository.save(url);

		return urlToDTO(url);
	}

	@GetMapping("/{miniUrl}")
	public UrlFullDTO status(@PathVariable String miniUrl, Principal principal) throws IOException {
		Url url = urlRepository.findByMiniUrl(miniUrl);
		if (url == null) {
			throw new UrlNotFoundException();
		}

		return urlToFullDTO(url);
	}

	@GetMapping("/{miniUrl}/redirect")
	public void redirect(@PathVariable String miniUrl, HttpServletResponse response) throws IOException {
		Url url = urlRepository.findByMiniUrl(miniUrl);
		if (url == null) {
			throw new UrlNotFoundException();
		}

		incrementAccessed(url);

		response.sendRedirect(url.getUrl());
	}

	private void incrementAccessed(Url url) {
		url.setAccessed(url.getAccessed() + 1);
		urlRepository.save(url);
	}

	private String getAbsoluteUrl(String miniUrl) {
		return baseUrl + "/" + miniUrl;
	}

	private UrlDTO urlToDTO(Url url) {
		UrlDTO urlDTO = new UrlDTO();

		urlDTO.miniurl = getAbsoluteUrl(url.getMiniUrl());
		urlDTO.url = url.getUrl();

		return urlDTO;
	}

	private UrlFullDTO urlToFullDTO(Url url) {
		UrlFullDTO urlFullDTO = new UrlFullDTO();

		urlFullDTO.accessed = url.getAccessed();
		urlFullDTO.created = url.getCreated();
		urlFullDTO.miniurl = getAbsoluteUrl(url.getMiniUrl());
		urlFullDTO.url = url.getUrl();

		return urlFullDTO;
	}

}

class UrlNotFoundException extends ResponseStatusException {
	private static final long serialVersionUID = 4435179056868697418L;

	UrlNotFoundException() {
		super(HttpStatus.NOT_FOUND, "URL not found");
	}
}

class UrlAlreadyExistsException extends ResponseStatusException {
	private static final long serialVersionUID = 8551159894381334861L;

	UrlAlreadyExistsException() {
		super(HttpStatus.BAD_REQUEST, "Could not create new link. One with the given `miniurl` already exists");
	}
}

class UrlRequestInvalidException extends ResponseStatusException {
	private static final long serialVersionUID = 8551159894381334861L;

	UrlRequestInvalidException() {
		super(HttpStatus.BAD_REQUEST, "Make sure the `miniurl` field is no more than 23 alphanumeric chars.");
	}
}
