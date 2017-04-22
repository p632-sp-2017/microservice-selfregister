package edu.iu.uits.selfregistry;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Enumeration;

import javax.annotation.PreDestroy;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * Created by naveenjetty on 4/6/17.
 */

@Configuration
@ComponentScan
@EnableConfigurationProperties(SelfRegistryProperties.class)
public class SelfRegistryAutoConfiguration {
	private String location;
	private String curUrl;
	private static Logger logger = Logger.getLogger(SelfRegistryAutoConfiguration.class);
	@Autowired
	private SelfRegistryProperties properties;

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder builder) {
		return builder.build();
	}

	@Bean
	public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
		return args -> {
			// Check if the required fields are present in the configuration or
			// not
			String title = properties.getTitle();
			String description = properties.getDescription();
			String email = properties.getContactemail();
			if (title == null || title.length() == 0) {
				throw new IllegalArgumentException("Title Cannot be null or blank");
			}
			if (description == null || description.length() == 0) {
				throw new IllegalArgumentException("Description Cannot be null or blank");
			}
			if (email == null || email.length() == 0) {
				throw new IllegalArgumentException("Contact E-Mail Cannot be null or blank");
			}

			boolean validEmail = EmailValidator.getInstance(true).isValid(email);
			if (!validEmail) {
				throw new IllegalArgumentException("Invalid E-Mail format");
			}
			// Get the url where this service is present.
			InetAddress inetAddress = getLocalHostLANAddress();
			String url = "http://" + inetAddress.getHostAddress() + ":8080";
			logger.info("Service is running on " + url);
			curUrl = url;

			// Check if the service is already registered.
			try {
				MicroService service = restTemplate.getForObject(
						"http://ms-catalog.herokuapp.com/catalog/search/findByTitle?title=" + title,
						MicroService.class);
				logger.info(service.toString());
				ResponseEntity<String> serviceResponseEntity = restTemplate.getForEntity(
						"http://ms-catalog.herokuapp.com/catalog/search/findByTitle?title=" + title, String.class);
				logger.info("Response Entity Output");
				logger.info(serviceResponseEntity.getBody().toString());
				JSONObject obj = new JSONObject(serviceResponseEntity.getBody().toString());
				String link = obj.getJSONObject("_links").getJSONObject("microServiceEntity").getString("href");
				logger.info("Self Link: " + link);
				location = link;
				// Link: will be used to patch/update operation to update the
				// url
				// This code will be done after making the url an array instead
				// of a field in the backend.

				// It is already registered earlier, Check if every thing is
				// correct
				// throw error if any mismatch occurs as duplicate titles are
				// not allowed.
				// No need to check for title match as it this executes iff it
				// matches
				if (!obj.getString("description").equals(description) || !obj.getString("email").equals(email)) {
					throw new IllegalArgumentException(
							"Duplicate Title is not allowed, another service already exists");
				} else {
					// Check if the url is already present in the list of URL's
					// retrieved from the catalog.
					// If present, do nothing
					// Else add the current url to the list and update it.
					JSONArray urls = obj.getJSONArray("url");
					boolean isUrlPresent = urls.toString().contains(url);
					if (isUrlPresent)
						logger.info("URL is already found");
					else {
						logger.info("URL is not present in the list");
						urls.put(url);
						service.getUrl().add(url);
						logger.info(service.toString());
						logger.info(obj.getJSONArray("url").toString());
						try {
							// ResponseEntity<String> response =
							// restTemplate.postForEntity(link, obj.toString(),
							// String.class);
							// System.out.println(response.toString());
							restTemplate.put(link, service);
						} catch (HttpClientErrorException he) {
							logger.info(he.getResponseBodyAsString());
						}
					}

				}

			} catch (HttpClientErrorException ex) {
				logger.info(ex.getStatusCode());
				logger.info("Did not find the service by the title: " + title);
				logger.info("Attempting to post a new record for the service");
				// It is not registered with the title name, register now.
				MicroService data = MicroService.builder().title(title).description(description).email(email)
						.url(Arrays.asList(url)).build();
				logger.info(data.toString());
				try {
					ResponseEntity<String> response = restTemplate
							.postForEntity("http://ms-catalog.herokuapp.com/catalog", data, String.class);
					logger.info("Posted a new service with the following response");
					// System.out.println(response.toString());
					logger.info(response.getBody());
					JSONObject obj = new JSONObject(response.getBody().toString());
					String link = obj.getJSONObject("_links").getJSONObject("microServiceEntity").getString("href");
					logger.info("Self Link: " + link);
					location = link;
				} catch (HttpClientErrorException e) {
					logger.info("Failed with following erros");
					// e.printStackTrace();
					logger.info(e.getResponseBodyAsString());
				}
			}
		};
	}

	@PreDestroy
	public void destroy(RestTemplate restTemplate) {
		// Need to figure out why it is not running while the program is
		// exiting.
		logger.info("Destroy is executed");
		logger.info(location + " Will be updated to remove the url or entry itself");

	}

	private static InetAddress getLocalHostLANAddress() throws UnknownHostException {
		try {
			InetAddress candidateAddress = null;
			// Iterate all NICs (network interface cards)...
			for (Enumeration ifaces = NetworkInterface.getNetworkInterfaces(); ifaces.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// Iterate all IP addresses assigned to each card...
				for (Enumeration inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {

						if (inetAddr.isSiteLocalAddress()) {
							// Found non-loopback site-local address. Return it
							// immediately...
							return inetAddr;
						} else if (candidateAddress == null) {
							// Found non-loopback address, but not necessarily
							// site-local.
							// Store it as a candidate to be returned if
							// site-local address is not subsequently found...
							candidateAddress = inetAddr;
							// Note that we don't repeatedly assign non-loopback
							// non-site-local addresses as candidates,
							// only the first. For subsequent iterations,
							// candidate will be non-null.
						}
					}
				}
			}
			if (candidateAddress != null) {
				// We did not find a site-local address, but we found some other
				// non-loopback address.
				// Server might have a non-site-local address assigned to its
				// NIC (or it might be running
				// IPv6 which deprecates the "site-local" concept).
				// Return this non-loopback candidate address...
				return candidateAddress;
			}
			// At this point, we did not find a non-loopback address.
			// Fall back to returning whatever InetAddress.getLocalHost()
			// returns...
			InetAddress jdkSuppliedAddress = InetAddress.getLocalHost();
			if (jdkSuppliedAddress == null) {
				throw new UnknownHostException("The JDK InetAddress.getLocalHost() method unexpectedly returned null.");
			}
			return jdkSuppliedAddress;
		} catch (Exception e) {
			UnknownHostException unknownHostException = new UnknownHostException(
					"Failed to determine LAN address: " + e);
			unknownHostException.initCause(e);
			throw unknownHostException;
		}
	}

}
