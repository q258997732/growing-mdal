package bob.growingmdal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:adapter.properties")
@Getter
@Setter
public class AdapterConfig
{
}
