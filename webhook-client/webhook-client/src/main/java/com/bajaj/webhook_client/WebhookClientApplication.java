package com.bajaj.webhook_client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebhookClientApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(WebhookClientApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		RestTemplate restTemplate = new RestTemplate();

		// STEP 1: Registration
		String registrationUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("name", "Tanmay Indoriya");
		requestBody.put("regNo", "REG12347");
		requestBody.put("email", "tanmayindoriya@gmail.com");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

		// GET RAW JSON response
		ResponseEntity<String> rawResponse = restTemplate.postForEntity(registrationUrl, entity, String.class);
		System.out.println("Raw Response: " + rawResponse.getBody());

		// PARSE JSON
		ObjectMapper mapper = new ObjectMapper();
		JsonNode json = mapper.readTree(rawResponse.getBody());

		String webhookUrl = json.get("webhook").asText();
		String accessToken = json.get("accessToken").asText();

		if (accessToken == null || accessToken.isBlank()) {
			System.err.println("accessToken is missing — check response format");
			return;
		}

		System.out.println("Webhook: " + webhookUrl);
		System.out.println("AccessToken: " + accessToken);

		// STEP 2: Send answer
		String finalQuery = "SELECT MAX(salary) AS SecondHighestSalary FROM Employee WHERE salary < (SELECT MAX(salary) FROM Employee);";
 
		Map<String, String> answerBody = new HashMap<>();
		answerBody.put("finalQuery", finalQuery);

		HttpHeaders answerHeaders = new HttpHeaders();
		answerHeaders.setContentType(MediaType.APPLICATION_JSON);
		answerHeaders.setBearerAuth(accessToken); // adds "Bearer " prefix

		HttpEntity<Map<String, String>> answerEntity = new HttpEntity<>(answerBody, answerHeaders);

		ResponseEntity<String> answerResponse = restTemplate.postForEntity("https://bfhldevapigw.healthrx.co.in/hiring/testWebhook/JAVA", answerEntity, String.class);
		System.out.println("Response from webhook submission: " + answerResponse.getBody());
	}

}
