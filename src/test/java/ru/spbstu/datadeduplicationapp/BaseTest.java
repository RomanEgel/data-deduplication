package ru.spbstu.datadeduplicationapp;


import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import ru.spbstu.datadeduplicationapp.repository.HashRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ContextConfiguration(initializers = {BaseTest.Initializer.class})
public abstract class BaseTest {
  @Autowired
  protected TestRestTemplate testRestTemplate;
  @Autowired
  protected HashRepository hashRepository;
  @Autowired
  protected RedisConnectionFactory redisConnectionFactory;
  @Value("${file.persistence.path}")
  protected String persistenceDir;


  @Container
  public static GenericContainer redis = new GenericContainer(DockerImageName.parse("redis:5.0.3-alpine"))
      .withExposedPorts(6379);

  static class Initializer
      implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
      TestPropertyValues.of(
          "spring.redis.port=" + redis.getMappedPort(6379)
      ).applyTo(configurableApplicationContext.getEnvironment());
    }
  }

  @BeforeEach
  public void reset() {
    redisConnectionFactory.getConnection().flushAll();
  }
}
