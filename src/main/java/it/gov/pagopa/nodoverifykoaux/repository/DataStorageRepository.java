package it.gov.pagopa.nodoverifykoaux.repository;

import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import it.gov.pagopa.nodoverifykoaux.entity.HotStorageVerifyKO;
import org.springframework.stereotype.Repository;

import java.util.Set;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
public class DataStorageRepository {

    CosmosTemplate cosmosTemplate;

    public DataStorageRepository(CosmosTemplate cosmosTemplate) {
        this.cosmosTemplate = cosmosTemplate;
    }

    public Set<String> getIDsByDate(String date, Long lowerBoundTimestamp, Long upperBoundTimestamp) {
        SqlQuerySpec query = new SqlQuerySpec("SELECT VALUE e.id FROM e" +
                " WHERE e.PartitionKey LIKE '" + date + "%'" +
                " AND e.faultBean.timestamp >= " + lowerBoundTimestamp +
                " AND e.faultBean.timestamp <= " + upperBoundTimestamp);
        Spliterator<String> iterator = cosmosTemplate.runQuery(query, HotStorageVerifyKO.class, String.class).spliterator();
        return StreamSupport.stream(iterator, false).collect(Collectors.toSet());
    }

    public HotStorageVerifyKO findById(String id) {
        return cosmosTemplate.findById("events", id, HotStorageVerifyKO.class);
    }

    public void save(HotStorageVerifyKO entity) {
        cosmosTemplate.insert("events", entity);
    }

}
