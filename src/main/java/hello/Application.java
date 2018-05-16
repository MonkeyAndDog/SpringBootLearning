package hello;

import hello.storage.StorageProperties;
import hello.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

    private static final Logger log = LoggerFactory.getLogger(Application.class);
    private JdbcTemplate jdbcTemplate;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner runn(RestTemplate restTemplate) throws Exception {
        return args -> {
            Quote quote = restTemplate.getForObject(
                    "http://gturnquist-quoters.cfapps.io/api/random", Quote.class);
            log.info(quote.toString());
        };
    }

    @Bean
    public CommandLineRunner run(JdbcTemplate jdbcTemplate) throws Exception {
        return args -> {
            jdbcTemplate.execute("DROP TABLE IF EXISTS customer");
            jdbcTemplate.execute("CREATE TABLE customer(" +
                    "id int(32)," +
                    "name char(255)," +
                    "email char(255)" +
                    ")");
            jdbcTemplate.batchUpdate("INSERT INTO customer values(2, \"张\", \"小明\")");
            jdbcTemplate.batchUpdate("INSERT INTO customer values(1, \"王\", \"小明\")");
            RowMapper<Customer> rowMapper;

            List<Customer> list = new ArrayList<>();
            jdbcTemplate.query(
                    "SELECT id, name, email FROM customer WHERE id = ?", new Object[]{"2"},
                    rowMapper = new RowMapper<Customer>() {
                        @Override
                        public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
                            list.add(new Customer(rs.getInt("id"), rs.getString("name"), rs.getString("email")));
                            return null;
                        }
                    }
            );
            for (Customer customer : list) {
                log.info(customer.toString());
            }
        };
    }


    public JdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Bean
    public CommandLineRunner initFileSystem(StorageService storageService) {
        CommandLineRunner commandLineRunner = new CommandLineRunner() {
            @Override
            public void run(String... args) throws Exception {
                storageService.deleteAll();
                storageService.init();
            }
        };
        return commandLineRunner;
    }
}
