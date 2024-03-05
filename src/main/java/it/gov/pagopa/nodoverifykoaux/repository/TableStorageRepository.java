package it.gov.pagopa.nodoverifykoaux.repository;

import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.StorageException;
import com.microsoft.azure.storage.table.CloudTable;
import com.microsoft.azure.storage.table.TableOperation;
import com.microsoft.azure.storage.table.TableQuery;
import it.gov.pagopa.nodoverifykoaux.entity.ColdStorageVerifyKO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.HashSet;
import java.util.Set;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

@Service
public class TableStorageRepository {

    private final CloudTable table;

    public TableStorageRepository(@Value("${verifyko.cold-storage.connection-string}") String connectionString,
                                  @Value("${verifyko.cold-storage.table-name}") String tableName) throws URISyntaxException, InvalidKeyException, StorageException {
        this.table = CloudStorageAccount.parse(connectionString)
                .createCloudTableClient()
                .getTableReference(tableName);
    }

    public Set<String> getIDsByDate(String partitionKey, Long dateLowerBound, Long dateUpperBound) {
        Set<String> ids = new HashSet<>();
        String queryWhereClausePartitionKey = TableQuery.generateFilterCondition("PartitionKey", TableQuery.QueryComparisons.EQUAL, partitionKey);
        String queryWhereClauseDateTimeLowerBound = TableQuery.generateFilterCondition("timestamp", TableQuery.QueryComparisons.GREATER_THAN_OR_EQUAL, dateLowerBound);
        String queryWhereClauseDateTimeUpperBound = TableQuery.generateFilterCondition("timestamp", TableQuery.QueryComparisons.LESS_THAN_OR_EQUAL, dateUpperBound);
        String queryWhereClauseDateTime = TableQuery.combineFilters(queryWhereClauseDateTimeLowerBound, TableQuery.Operators.AND, queryWhereClauseDateTimeUpperBound);
        String queryWhereClause = TableQuery.combineFilters(queryWhereClausePartitionKey, TableQuery.Operators.AND, queryWhereClauseDateTime);
        TableQuery<ColdStorageVerifyKO> query = TableQuery.from(ColdStorageVerifyKO.class).where(queryWhereClause).select(new String[]{"RowKey"});
        Iterable<ColdStorageVerifyKO> result = table.execute(query);
        result.forEach(entity -> ids.add(entity.getRowKey()));
        return ids;
    }

    public ColdStorageVerifyKO findById(String rowKey, String partitionKey) {
        String rowKeyWhereClause = TableQuery.generateFilterCondition("RowKey", TableQuery.QueryComparisons.EQUAL, rowKey);
        String partitionKeyWhereClause = TableQuery.generateFilterCondition("PartitionKey", TableQuery.QueryComparisons.EQUAL, partitionKey);
        String queryWhereClause = TableQuery.combineFilters(partitionKeyWhereClause, TableQuery.Operators.AND, rowKeyWhereClause);
        TableQuery<ColdStorageVerifyKO> query = TableQuery.from(ColdStorageVerifyKO.class).where(queryWhereClause);
        Spliterator<ColdStorageVerifyKO> iterator = table.execute(query).spliterator();
        return StreamSupport.stream(iterator, false).findFirst().orElse(null);
    }

    public ColdStorageVerifyKO findByRowKey(String rowKey) {
        String queryWhereClause = TableQuery.generateFilterCondition("RowKey", TableQuery.QueryComparisons.EQUAL, rowKey);
        TableQuery<ColdStorageVerifyKO> query = TableQuery.from(ColdStorageVerifyKO.class).where(queryWhereClause);
        Spliterator<ColdStorageVerifyKO> iterator = table.execute(query).spliterator();
        return StreamSupport.stream(iterator, false).findFirst().orElse(null);
    }

    public void save(ColdStorageVerifyKO eventToBeSavedInColdStorage) throws StorageException {
        table.execute(TableOperation.insertOrReplace(eventToBeSavedInColdStorage));
    }
}
