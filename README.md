SpringBoot学习项目
---
> 根据Spring.io官网教程学习。[官网教程](https://spring.io/guides)

简介
---
SpringBoot为微服务架构技术提供支持。
使用注解Spring4中的新注解`@RestController`来注解一个控制器，在被注解的类中所有的方法均返回一个`Domain`
对象。是一个将`@Controller`和`@ResponseBody`合并到一起的注解。在序列化时默认使用`Jackson`进行序列化。

控制器示例：
```java
package hello;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.atomic.AtomicLong;

@RestController
public class GreetingController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value = "name", defaultValue = "world") String name) {
        return new Greeting(counter.incrementAndGet(), String.format(template, name));
    }
}
```

SpringBoot启动方式特殊，使用`SpringApplication`的静态`run()`方法进行启动。此外，启动类的注解`@SpringBootApplication`是一系列注解的集合。
包括但不仅限于`@Configuration、@EnableAutoConfiguration、@ComponentScan`。

启动器示例：
```java
package hello;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```