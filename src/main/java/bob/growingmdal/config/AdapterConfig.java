package bob.growingmdal.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = {
        "classpath:adapter.properties",              // 兜底，保证本地能启动
        "file:./adapter.properties"      // 外部目录优先
}, ignoreResourceNotFound = true)   // 外部文件找不到也不报错
@Getter
@Setter
public class AdapterConfig {
    /*
      把adapter的参数拉进来
      */
}
