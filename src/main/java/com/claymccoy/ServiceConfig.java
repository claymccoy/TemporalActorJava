package com.claymccoy;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import jakarta.validation.constraints.NotBlank;

@Component
@ConfigurationProperties(prefix = "hello")
public class ServiceConfig
{
    @NotBlank
    private String message = "Hello Spring Boot!";

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}
