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

使用JDBC访问数据库
---
使用JDBCTemplate接口访问数据库，并执行SQL语句。

JDBCTemplate封装了对数据库的连接和处理SQL的接口，更易使用。

使用CrudRepository接口对数据库进行操作
---
创建model类的相关服务接口并实现`CrudRepository`接口(同时Spring会默认注入一些方法的实现)
然后直接在控制器中使用该model类的服务接口。

服务接口示例：
```java
package hello;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserRepository extends CrudRepository<User, Long> {

}
```
服务接口调用示例（使用了自动装配）:
```java
package hello;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class MainController {

    private UserRepository userRepository;

    @RequestMapping("/add")
    public @ResponseBody String addNewUser(@RequestParam String name, @RequestParam String email) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        userRepository.save(user);
        return "储存成功";
    }

    @RequestMapping(path="/all")
    public @ResponseBody Iterable<User> getAllUsers() {
        return userRepository.findAll();
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

文件上传
---
首先配置服务器信息，然后创建处理储存逻辑的服务，然后创建控制器。

1. 启动类注解`@EnableConfigurationProperties(StorageProperties.class)`，并且相关的配置类注解`@ConfigurationProperties("storage")`;
2. 表单使用`enctype="multipart/form-data"`不变，后台使用控制器接收相关请求，控制器中使用相关的`Service`对象处理储存逻辑。

控制器示例：
```java
package hello;

import hello.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class FileUploadController {

    private StorageService storageService;

    @Autowired
    public FileUploadController(StorageService storageService) {
        this.storageService = storageService;
    }

    @RequestMapping("/upload")
    @ResponseBody
    public String handleFileUpload(@RequestParam("file") MultipartFile file) {
        storageService.store(file);
        System.out.println("666");
        return "上传文件成功";
    }

    @RequestMapping("/")
    public String home() {
        return "uploadForm";
    }
}
```

文件储存`Service`示例
```java
package hello.storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemStorageService implements StorageService {

    private Path rootLocation;

    @Autowired
    public FileSystemStorageService(StorageProperties properties) {
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void store(MultipartFile file) {
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            InputStream inputStream = file.getInputStream();
            Files.copy(inputStream, this.rootLocation.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }
}
```

参数验证
---
使用以下依赖进行处理（Spring使用hibernate的验证器，该项目用的是javax的验证器接口）
```xml
<dependency>
    <groupId>org.hibernate.validator</groupId>
    <artifactId>hibernate-validator</artifactId>
</dependency>
```
在domain类中需要验证的字段上加注解。

例如：
```java
package hello;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PersonForm {

    @Min(18)
    private int age;

    @NotNull
    @Size(max = 20, min = 6)
    private String name;

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PersonForm{" +
                "age=" + age +
                ", name='" + name + '\'' +
                '}';
    }
}
```
然后在`BindingResult`接口中获取相应的错误内容。