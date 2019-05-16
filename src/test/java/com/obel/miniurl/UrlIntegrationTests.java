package com.obel.miniurl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.util.Base64Utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.obel.miniurl.controller.dto.UrlDTO;
import com.obel.miniurl.dao.MiniUrlAutoGenerator;
import com.obel.miniurl.dao.UrlRepository;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application.properties")
public class UrlIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UrlRepository repository;

	@Autowired
	private MiniUrlAutoGenerator miniUrlAutoGenerator;
	
	private JacksonTester<UrlDTO> urlJsonBuilder;

	private static final String TEST_URL = "http://example.com";

	@Value("${base_url}")
	private String baseUrl;

	@Before
	public void setup() {
		JacksonTester.initFields(this, new ObjectMapper());
		repository.deleteAll();
		miniUrlAutoGenerator.reset();
	}

	@Test
	public void test_urlPost_unauthorised() throws Exception {
		// WHEN
		ResultActions result = mockMvc.perform(post("/").contentType(MediaType.APPLICATION_JSON));
		// THEN
		result.andExpect(status().isUnauthorized());
	}

	@Test
	public void test_urlPost_withoutPayload() throws Exception {
		// WHEN
		ResultActions result = mockMvc.perform(authorize(post("/")).contentType(MediaType.APPLICATION_JSON));

		result.andExpect(status().isBadRequest());
	}

	@Test
	public void test_urlPost_withoutMiniUrlSpecified() throws Exception {
		// GIVEN
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.url = TEST_URL;

		// WHEN
		ResultActions result = authPost("/", urlDTO);
		
		// THEN
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value(TEST_URL))
				.andExpect(jsonPath("$.miniurl").value(getAbsoluteUrl("1")));
	}

	@Test
	public void test_urlPost_withCustomMiniUrlSpecified() throws Exception {
		// GIVEN
		String myMiniUrl = "myminiurl";
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.miniurl = myMiniUrl;
		urlDTO.url = TEST_URL;

		// WHEN
		ResultActions result = authPost("/", urlDTO);

		// THEN
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value(TEST_URL))
				.andExpect(jsonPath("$.miniurl").value(getAbsoluteUrl(myMiniUrl)));
	}
	
	@Test
	public void test_urlPost_withCustomMiniUrlAlreadyTaken() throws Exception {
		// GIVEN
		String myMiniUrl = "myminiurl";
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.miniurl = myMiniUrl;
		urlDTO.url = TEST_URL;
		// given a miniurl was already used
		authPost("/", urlDTO);

		// WHEN
		ResultActions result = authPost("/", urlDTO);

		// THEN
		result.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.error").value("Could not create new link. One with the given `miniurl` already exists"));
	}

	@Test
	public void test_urlPost_customUrlTooLong() throws Exception {
		// GIVEN
		String myMiniUrl = "123456789012345678901234";
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.miniurl = myMiniUrl;
		urlDTO.url = TEST_URL;

		// WHEN
		ResultActions result = authPost("/", urlDTO);

		// THEN
		result.andExpect(status().isBadRequest()).andExpect(
				jsonPath("$.error").value("Make sure the `miniurl` field is no more than 23 alphanumeric chars."));
	}

	@Test
//	@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
	public void test_urlPost_customUrlWithDissallowedCharacters() throws Exception {
		// GIVEN
		String myMiniUrl = "żółtą";
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.miniurl = myMiniUrl;
		urlDTO.url = TEST_URL;

		// WHEN
		ResultActions result = authPost("/", urlDTO);

		// THEN
		result.andExpect(status().isBadRequest()).andExpect(
				jsonPath("$.error").value("Make sure the `miniurl` field is no more than 23 alphanumeric chars."));
	}

	@Test
	public void test_getNewUrlStatusUnauthorised() throws Exception {
		// WHEN
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.url = TEST_URL;
		authPost("/", urlDTO);
		String defaultMiniUrl = "1";
		
		// WHEN
		ResultActions result = mockMvc.perform(get("/" + defaultMiniUrl));
		
		// THEN
		result.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void test_getNewUrlStatus() throws Exception {
		// WHEN
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.url = TEST_URL;
		authPost("/", urlDTO);
		String defaultMiniUrl = "1";
		
		// WHEN
		ResultActions result = mockMvc.perform(authorize(get("/" + defaultMiniUrl)));
		
		// THEN
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.url").value(TEST_URL))
				.andExpect(jsonPath("$.miniurl").value(getAbsoluteUrl(defaultMiniUrl)))
				.andExpect(jsonPath("$.accessed").value(0))
				.andExpect(jsonPath("$.created").exists());
	}
	
	@Test
	public void test_getNewUrlsStatusesUnauthorised() throws Exception {
		// WHEN
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.url = TEST_URL;
		authPost("/", urlDTO);
		
		// WHEN
		ResultActions result = mockMvc.perform(get("/"));
		
		// THEN
		result.andExpect(status().isUnauthorized());
	}
	
	@Test
	public void test_getAllNewUrlsStatuses() throws Exception {
		// WHEN
		String[] miniUrls = { "aaa", "bbb", "ccc"};
		for(String miniUrl : miniUrls) {
			UrlDTO urlDTO = new UrlDTO();
			urlDTO.url = TEST_URL;
			urlDTO.miniurl = miniUrl;
			authPost("/", urlDTO);
		}
		
		// WHEN
		ResultActions result = mockMvc.perform(authorize(get("/")));
		
		// THEN
		result.andExpect(status().isOk());
		for(int i = 0; i < miniUrls.length; i++) {
			String element = "$[" + i + "]";
			result.andExpect(jsonPath(element + ".url").value(TEST_URL))
				.andExpect(jsonPath(element + ".miniurl").value(getAbsoluteUrl(miniUrls[i])));
		}
	}
	
	@Test
	public void test_redirect() throws Exception {
		// WHEN
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.url = TEST_URL;
		authPost("/", urlDTO);
		String defaultMiniUrl = "/1";
		
		// WHEN
		ResultActions result = mockMvc.perform(get(defaultMiniUrl + "/redirect"));
		
		// THEN
		result.andExpect(status().is3xxRedirection())
				.andExpect(header().string("location", TEST_URL));
	}
	
	@Test
	public void test_getRedirectedUrlStatus() throws Exception {
		// WHEN
		UrlDTO urlDTO = new UrlDTO();
		urlDTO.url = TEST_URL;
		authPost("/", urlDTO);
		String defaultMiniUrl = "1";
		mockMvc.perform(get("/" + defaultMiniUrl + "/redirect"));
		mockMvc.perform(get("/" + defaultMiniUrl + "/redirect"));
		
		// WHEN
		ResultActions result = mockMvc.perform(authorize(get("/" + defaultMiniUrl)));
		
		// THEN
		result.andExpect(status().isOk())
				.andExpect(jsonPath("$.accessed").value(2));
	}
	
	/*
	 * Send an authorised POST message with the DTO as JSON payload
	 */
	private ResultActions authPost(String path, UrlDTO urlDTO) throws Exception {
		String requestJson = urlJsonBuilder.write(urlDTO).getJson();
		return authPost(path, requestJson);
	}

	/*
	 * Send an authorised POST message
	 */
	private ResultActions authPost(String path, String requestJson) throws Exception {
		return mockMvc.perform(authorize(post("/")).contentType(MediaType.APPLICATION_JSON).content(requestJson))
				.andDo(print());
	}

	private MockHttpServletRequestBuilder authorize(MockHttpServletRequestBuilder builder) {
		return builder.header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString("jimmy:secret".getBytes())); // TODO: externalize
	}

	private String getAbsoluteUrl(String miniUrl) {
		return baseUrl + "/" + miniUrl;
	}
}