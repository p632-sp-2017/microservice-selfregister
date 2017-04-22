package edu.iu.uits.selfregistry;

import org.json.JSONObject;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.net.*;
import java.util.Enumeration;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by naveenjetty on 4/6/17.
 */

//@SpringBootApplication
public class Register {
    public static void main(String[] args) throws Exception{
//        SpringApplication.run(Register.class);
        System.setProperty("java.net.preferIPv4Stack" , "false");
        System.setProperty("java.net.preferIPv6Addresses" , "true");
        InetAddress ip = InetAddress.getLocalHost();
        String url=null;
        String port = "8080";
        if (ip instanceof Inet6Address)
            url = "http://["+ip.getHostAddress()+"]:"+port;
        else if (ip instanceof Inet4Address)
            url = "http://"+ip.getHostAddress()+":"+port;
        System.out.println(url);
    }

    //@Bean
//    public RestTemplate restTemplate(RestTemplateBuilder builder) {
//        return builder.build();
//    }

   // @Bean
    /*public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
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
    }*/
}
