SpringBoot学习项目
---
> 根据Spring.io官网教程学习。[官网教程](https://spring.io/guides)

创建一个RESTful Web Service
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

关于使用`Spring Boot Maven plugin`的方便之处：
> 1. 将会收集所有依赖的类加入到classpath中，然后建立一个可以独立运行的jar文件，这将会很方便的执行和分发创建的服务。
> 2. 将会扫描`public static void main()`方法并标记该类为执行类入口。
> 3. 它提供了一个内置的依赖关系解析器，用于设置版本号以匹配Spring Boot依赖关系。你可以覆盖你想要的任何版本，但是它会默认使用Boot的选择版本。

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

使用RESTful Web Service
---
创建符合RESTful接口数据结构的对象。

例如：
```java
package hello;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {
    private String type;
    private Value value;

    public Quote() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
```
获取到数据后使用`RestTemplate`结合`Jackson`进行反序列化。该处使用`CommandLineRunner`执行操作（`CommandLineRunner`具备SpringBoot启动
器的回掉接口，当所有初始化工作完成后默认执行），并使用日志工具打印内容。

例如：
```java
package hello;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(RestTemplate restTemplate) throws Exception {
        return args -> {
            Quote quote = restTemplate.getForObject(
                    "http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
            log.info(quote.toString());
        };
    }
}
```

