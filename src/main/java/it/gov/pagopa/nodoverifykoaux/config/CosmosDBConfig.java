package it.gov.pagopa.nodoverifykoaux.config;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.DirectConnectionConfig;
import com.azure.cosmos.GatewayConnectionConfig;
import com.azure.spring.data.cosmos.config.AbstractCosmosConfiguration;
import com.azure.spring.data.cosmos.config.CosmosConfig;
import com.azure.spring.data.cosmos.core.ResponseDiagnostics;
import com.azure.spring.data.cosmos.core.ResponseDiagnosticsProcessor;
import com.azure.spring.data.cosmos.repository.config.EnableCosmosRepositories;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

@Configuration
@EnableCosmosRepositories("it.gov.pagopa.nodoverifykoaux.repository")
@EnableConfigurationProperties
@ConditionalOnExpression("'${info.properties.environment}'!='test'")
@Slf4j
public class CosmosDBConfig extends AbstractCosmosConfiguration {

    @Value("${verifyko.hot-storage.uri}")
    private String cosmosUri;

    @Value("${verifyko.hot-storage.key}")
    private String cosmosKey;

    @Value("${verifyko.hot-storage.database}")
    private String cosmosDatabase;

    @Value("${verifyko.hot-storage.populate-query-metrics}")
    private Boolean cosmosQueryMetrics;

    @Bean
    public CosmosClientBuilder getCosmosClientBuilder() {
        var azureKeyCredential = new AzureKeyCredential(cosmosKey);
        var directConnectionConfig = new DirectConnectionConfig();
        var gatewayConnectionConfig = new GatewayConnectionConfig();
        return new CosmosClientBuilder()
                .endpoint(cosmosUri)
                .credential(azureKeyCredential)
                .directMode(directConnectionConfig, gatewayConnectionConfig);
    }

    @Override
    public CosmosConfig cosmosConfig() {
        return CosmosConfig.builder()
                .enableQueryMetrics(cosmosQueryMetrics)
                .responseDiagnosticsProcessor(new ResponseDiagnosticsProcessorImplementation())
                .build();
    }

    @Override
    protected String getDatabaseName() {
        return cosmosDatabase;
    }

    private static class ResponseDiagnosticsProcessorImplementation implements ResponseDiagnosticsProcessor {

        @Override
        public void processResponseDiagnostics(@Nullable ResponseDiagnostics responseDiagnostics) {
            log.debug("Response Diagnostics {}", responseDiagnostics);
        }
    }
}