package edu.iu.uits.selfregistry;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Created by naveenjetty on 4/6/17.
 */

@ConfigurationProperties(prefix = "spring.application")
@Data
public class SelfRegistryProperties {

    private String title;

    private String description;

    private String contactemail;

    private String port;

}
