package it.gov.pagopa.nodoverifykoaux.repository;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobStorageException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import it.gov.pagopa.nodoverifykoaux.entity.BlobBodyReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class BlobStorageRepository {

    private final String containerName;

    private final BlobContainerClient blobClient;

    private final Gson mapper;

    public BlobStorageRepository(@Value("${verifyko.cold-storage.connection-string}") String connectionString,
                                 @Value("${verifyko.cold-storage.container-name}") String containerName) {
        this.blobClient = new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient()
                .getBlobContainerClient(containerName);
        this.containerName = containerName;
        this.mapper = new Gson();
    }

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> findByID(String id) {
        Map<String, Object> event = new HashMap<>();
        try {
            byte[] byteArray = blobClient.getBlobClient(id).downloadContent().toBytes();
            String eventInStringForm = new String(byteArray, StandardCharsets.UTF_8);
            event = this.mapper.fromJson(eventInStringForm, HashMap.class);
        } catch (BlobStorageException e) {
            log.error(String.format("An error occurred while retrieving blob object with id [%s]", id), e);
        } catch (JsonSyntaxException e) {
            log.error(String.format("An error occurred while mapping blob object with id [%s] as map.", id), e);
        }
        return event;
    }

    public String save(String blobFromEvent, String id) {
        String blobBodyReference = null;
        try {
            BlobClient blobClientInstance = this.blobClient.getBlobClient(id);
            BinaryData body = BinaryData.fromStream(new ByteArrayInputStream(blobFromEvent.getBytes(StandardCharsets.UTF_8)));
            blobClientInstance.upload(body, true);
            blobBodyReference = mapper.toJson(BlobBodyReference.builder()
                    .storageAccount(blobClient.getAccountName())
                    .containerName(this.containerName)
                    .fileName(id)
                    .fileLength(body.toString().length())
                    .build());
        } catch (BlobStorageException e) {
            log.error(String.format("An error occurred while persisting blob object with id [%s]", id), e);
        }
        return blobBodyReference;
    }
}
