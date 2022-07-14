package com.library.library;

import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LibraryApplicationTests {

//	@LocalServerPort
//	private int port;
//
//	@Autowired
//	private TestRestTemplate restTemplate;
//
//	@Test
//	void contextLoads() {
//		HttpStatus statusCode = restTemplate.getForEntity("http://localhost:" + port + "/actuator/health", String.class).getStatusCode();
//		assertEquals(statusCode, HttpStatus.OK);
//	}

}
