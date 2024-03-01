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
            @Parameter(description = "The size of the batch on which the reconciliation will be executed for each steps. This avoids the large queries to storages. Defined in minutes.", example = "30")
            @RequestParam(value = "batch-size-in-minutes", required = false, defaultValue = "1440") Long batchSizeInMinutes) {
        return ResponseEntity.ok(reconciliationService.reconcileEventsByDate(date, batchSizeInMinutes));
    }
}
