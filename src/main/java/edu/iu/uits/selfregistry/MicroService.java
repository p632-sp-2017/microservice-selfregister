package edu.iu.uits.selfregistry;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

/**
 * Created by naveenjetty on 4/6/17.
 */

@Data
@Builder
@ToString(includeFieldNames=true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MicroService {
    private String title;
    private String description;
    private String url;
    private String email;
}
