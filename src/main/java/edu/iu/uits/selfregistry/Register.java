package edu.iu.uits.selfregistry;

import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by naveenjetty on 4/6/17.
 */

//@SpringBootApplication
public class Register {
    public static void main(String args[]) {
        SpringApplication.run(Register.class);
    }

    //@Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder.build();
//    }

   // @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {
            String title = "test 12345Z";
            String description = "description from self registry";
            String url = "url";
            String email = "contact@uits.com";
            try {
//                MicroService quote = restTemplate.getForObject(
//                        "http://ms-catalog.herokuapp.com/catalog/search/findByTitle?title=test 2", MicroService.class);
//                System.out.println(quote.toString());
                ResponseEntity<String> serviceResponseEntity = restTemplate.getForEntity(
                        "http://ms-catalog.herokuapp.com/catalog/search/findByTitle?title=test 2Z", String.class);
                System.out.println("Response Entity Output");
                System.out.println(serviceResponseEntity.getBody().toString());
                JSONObject obj = new JSONObject(serviceResponseEntity.getBody().toString());
//                System.out.println(obj.get("microServiceEntity"));
                String link = obj.getJSONObject("_links").getJSONObject("microServiceEntity").getString("href");
                System.out.println("Self Link: "+ link);

            } catch (HttpClientErrorException ex){
                System.out.println(ex.getStatusCode());
                MicroService data = MicroService.builder()
                        .title(title)
                        .description(description)
                        .email(email)
                        .url(url).build();
                try {
                    ResponseEntity<MicroService> response = restTemplate.postForEntity(
                            "http://ms-catalog.herokuapp.com/catalog", data, MicroService.class);
                    System.out.println("Posted a new service with the following response");
                    System.out.println(response.toString());
                } catch (HttpClientErrorException e){
                    System.out.println("Failed with following erros");
                    e.printStackTrace();

                }
            }

        };
    }
}
