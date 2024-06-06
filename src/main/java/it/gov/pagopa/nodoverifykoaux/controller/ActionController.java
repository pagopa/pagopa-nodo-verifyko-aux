package it.gov.pagopa.nodoverifykoaux.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.nodoverifykoaux.model.ProblemJson;
import it.gov.pagopa.nodoverifykoaux.model.action.ReconciliationStatus;
import it.gov.pagopa.nodoverifykoaux.model.action.check.ReconciliationEventStatus;
import it.gov.pagopa.nodoverifykoaux.service.ReconciliationService;
import it.gov.pagopa.nodoverifykoaux.util.OpenAPITableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@Tag(name = "Actions", description = "Everything about actions on Verify KO events")
public class ActionController {

    private final ReconciliationService reconciliationService;

    public ActionController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @PostMapping(value = "/reconciliation", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Reconcile VerifyKO events in hot-storage and cold-storage",
            description = "The API execute a reconciliation of Verify KO events for the passed date, aligning hot-storage with cold-storage for this day.",
            security = {@SecurityRequirement(name = "ApiKey")},
            tags = {"Actions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reconciliation executed with success.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReconciliationStatus.class))),
            @ApiResponse(responseCode = "400", description = "If passed date is invalid.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "500", description = "If an error occurred during execution.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @OpenAPITableMetadata(external = false, idempotency = false, readWriteIntense = OpenAPITableMetadata.ReadWrite.BOTH)
    public ResponseEntity<ReconciliationStatus> reconcileEventsByDate(
            @Parameter(description = "The date, in yyyy-MM-dd format, on which the reconciliation will be executed.", example = "2024-01-01", required = true)
            @RequestParam String date,
            @Parameter(description = "The time frame according to which the blocks of elements to be reconciled are generated for each step. This avoids the large queries to storages. Defined in minutes.", example = "30")
            @RequestParam(value = "time-frame-in-minutes", required = false, defaultValue = "1440") Long timeFrame,
            @Parameter(description = "The flag that activate the extraction of the detailed report about the migration, generating the list of migrated events. Set it to 'false' if you know that a whole day's data must be migrated.", example = "false", required = true)
            @RequestParam(value = "include-events-in-report", defaultValue = "false") Boolean includeEventsInReport) {
        return ResponseEntity.ok(reconciliationService.reconcileEventsByDate(date, timeFrame, includeEventsInReport));
    }

    @PostMapping(value = "/reconciliation/check-event", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Check if VerifyKO event is reconciled and present in hot-storage and cold-storage",
            description = "The API execute a check of Verify KO event regarding its presence in hot-storage and in cold-storage.\n" +
                    "All the parameters are mandatory because, in order to execute the search of the event either in hot storage and in cold storage, " +
                    "they are necessary for the keys generation to be used for both storages.",
            security = {@SecurityRequirement(name = "ApiKey")},
            tags = {"Actions"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check executed with success.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ReconciliationEventStatus.class))),
            @ApiResponse(responseCode = "500", description = "If an error occurred during execution.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @OpenAPITableMetadata(external = false, idempotency = false, readWriteIntense = OpenAPITableMetadata.ReadWrite.BOTH)
    public ResponseEntity<ReconciliationEventStatus> checkIfEventIsReconciled(
            @Parameter(description = "The value used as partition key for searching data in both storages.", required = true, example = "1704063600-XXXXXXXXXXX-YYYYYYYYYYY")
            @RequestParam(value = "partition-key") String partitionKey,
            @Parameter(description = "The value used as row key for searching data in both storages.", required = true, example = "da5f8886-0781-444f-84ae-d990b72be70e")
            @RequestParam(value = "row-key") String rowKey,
            @Parameter(description = "The value used as timestamp for searching data in both storages.", required = true, example = "1704063600")
            @RequestParam(value = "timestamp") String timestamp) {
        return ResponseEntity.ok(reconciliationService.checkIfEventIsReconciled(partitionKey, rowKey, timestamp));
    }
}
