package org.talend.dataprep.dataset;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
@Profile("standalone")
@ComponentScan(basePackages = "org.talend.dataprep")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
