package it.gov.pagopa.nodoverifykoaux.config;


import it.gov.pagopa.nodoverifykoaux.entity.ColdStorageVerifyKO;
import it.gov.pagopa.nodoverifykoaux.entity.HotStorageVerifyKO;
import it.gov.pagopa.nodoverifykoaux.mapper.ConvertHotStorageVerifyKOToColdStorageVerifyKO;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingsConfiguration {

    @Bean
    ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        mapper.getConfiguration().setAmbiguityIgnored(true);

        mapper.createTypeMap(HotStorageVerifyKO.class, ColdStorageVerifyKO.class).setConverter(new ConvertHotStorageVerifyKOToColdStorageVerifyKO());

        return mapper;
    }

}
