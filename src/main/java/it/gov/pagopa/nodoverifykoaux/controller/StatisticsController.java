package it.gov.pagopa.nodoverifykoaux.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.nodoverifykoaux.model.ProblemJson;
import it.gov.pagopa.nodoverifykoaux.model.statistics.DataReport;
import it.gov.pagopa.nodoverifykoaux.service.StatisticsService;
import it.gov.pagopa.nodoverifykoaux.util.OpenAPITableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Slf4j
@RestController
@Tag(name = "Statistics", description = "Everything about statistics on Verify KO events")
public class StatisticsController {

    private final StatisticsService statisticsService;

    private final Gson gsonMapper;

    public StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
        this.gsonMapper = new GsonBuilder().setPrettyPrinting().create();
    }

    @GetMapping(value = "/reports", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Export Verify KO report from Hot Storage",
            description = "The API execute the export of a report about Verify KO events from hot-storage stored for the passed date or month.",
            security = {@SecurityRequirement(name = "ApiKey")},
            tags = {"Statistics"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Export extracted with success.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "400", description = "If passed date is invalid.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "500", description = "If an error occurred during execution.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @OpenAPITableMetadata(external = false, cacheable = true)
    public ResponseEntity<Resource> extractReportFromHotStorageByMonth(
            @Parameter(description = "The year on which the report extraction will be executed.", example = "2020", required = true)
            @RequestParam Integer year,
            @Parameter(description = "The month on which the report extraction will be executed, within four months from today.", example = "1", required = true)
            @RequestParam @Min(1) @Max(12) Integer month,
            @Parameter(description = "The day on which the report extraction will be executed, from yesterday.")
            @RequestParam(required = false) @Min(1) @Max(31) Integer day) {

        DataReport dataReport = statisticsService.extractReportFromHotStorage(year, month, day);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=VerifyKO_HotStorage_Report_" + year + "-" + month + "-" + (day == null ? "X" : day) + ".json");

        String content = gsonMapper.toJson(dataReport);
        ByteArrayResource resource = new ByteArrayResource(content.getBytes());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(content.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }

    @GetMapping(value = "/reports/cold", produces = {MediaType.APPLICATION_JSON_VALUE})
    @Operation(summary = "Export Verify KO report from Cold Storage",
            description = "The API execute the export of a report about Verify KO events from cold-storage stored for the passed date or month.",
            security = {@SecurityRequirement(name = "ApiKey")},
            tags = {"Statistics"})
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Export extracted with success.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Void.class))),
            @ApiResponse(responseCode = "400", description = "If passed date is invalid.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "500", description = "If an error occurred during execution.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @OpenAPITableMetadata(external = false, cacheable = true)
    public ResponseEntity<Resource> extractReportFromColdStorageByMonth(
            @Parameter(description = "The year on which the report extraction will be executed.", example = "2020", required = true)
            @RequestParam Integer year,
            @Parameter(description = "The month on which the report extraction will be executed.", example = "1", required = true)
            @RequestParam @Min(1) @Max(12) Integer month,
            @Parameter(description = "The day on which the report extraction will be executed, from yesterday.")
            @RequestParam(required = false) @Min(1) @Max(31) Integer day) {

        DataReport dataReport = statisticsService.extractReportFromColdStorage(year, month, day);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=VerifyKO_ColdStorage_Report_" + year + "-" + month + "-" + (day == null ? "X" : day) + ".json");

        String content = gsonMapper.toJson(dataReport);
        ByteArrayResource resource = new ByteArrayResource(content.getBytes());
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(content.length())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(resource);
    }
}
