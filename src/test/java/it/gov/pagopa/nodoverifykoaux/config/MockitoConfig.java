package it.gov.pagopa.nodoverifykoaux.config;

import com.azure.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MockitoConfig {

    @Bean
    @Primary
    public CosmosClientBuilder getCosmosClientBuilder() {
        return Mockito.mock(CosmosClientBuilder.class);
    }

    @Bean
    @Primary
    public CosmosTemplate cosmosTemplate() {
        return Mockito.mock(CosmosTemplate.class);
    }
}
