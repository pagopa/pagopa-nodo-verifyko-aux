package it.gov.pagopa.nodoverifykoaux.entity;

import lombok.*;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BlobBodyReference {
    private String storageAccount;
    private String containerName;
    private String fileName;
    private Integer fileLength;
}
