package com.nature;

import com.nature.base.util.LoggerUtil;
import com.nature.base.util.SpringContextUtil;
import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@PropertySources({
        @PropertySource(value = "classpath:apiConfig.properties", encoding = "utf-8"),
        @PropertySource(value = "classpath:baseConfig.properties", encoding = "utf-8")
})
@MapperScan(basePackages = "com.nature.mapper.*.*")
@EnableTransactionManagement
@SpringBootApplication
public class Application {

    static Logger logger = LoggerUtil.getLogger();

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(Application.class, args);
        SpringContextUtil.setApplicationContext(context);
        logger.warn("***************************************************************");
        logger.warn("***************** Spring Boot Startup Success *****************");
        logger.warn("***************************************************************");
    }
}
