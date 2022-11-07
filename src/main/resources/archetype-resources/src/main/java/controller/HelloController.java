package ${package}.controller;

import ${package}.service.HelloService;
import ${package}.aspect.annotation.ControllerLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
public class HelloController {

    @Autowired
    private HelloService helloService;

    @ControllerLog
    @GetMapping
    public String hello() {
        return helloService.hello();
    }
}
