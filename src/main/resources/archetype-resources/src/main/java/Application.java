package ${package};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
//        new SpringApplicationBuilder().main(Application.class)
//                .sources(Application.class)
//                .web(WebApplicationType.NONE)
//                .build(args)
//                .run();
        SpringApplication.run(Application.class, args);
    }

}
