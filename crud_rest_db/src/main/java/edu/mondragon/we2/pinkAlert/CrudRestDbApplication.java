package edu.mondragon.we2.pinkAlert;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class CrudRestDbApplication extends SpringBootServletInitializer {

  @Override
  protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
    return application.sources(CrudRestDbApplication.class);
  }

  public static void main(String[] args) {
    SpringApplication.run(CrudRestDbApplication.class, args);
  }
}
