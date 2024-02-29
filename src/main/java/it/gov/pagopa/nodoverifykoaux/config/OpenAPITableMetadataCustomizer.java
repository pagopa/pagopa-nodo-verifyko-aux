package it.gov.pagopa.nodoverifykoaux.config;

import io.swagger.v3.oas.models.Operation;
import it.gov.pagopa.nodoverifykoaux.util.OpenAPITableMetadata;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;

import static it.gov.pagopa.nodoverifykoaux.util.CommonUtility.deNull;

@Component
public class OpenAPITableMetadataCustomizer implements OperationCustomizer {

    private static final String SEPARATOR = " | ";

    private static String buildData(OpenAPITableMetadata annotation) {
        return "**API properties:**\n" +
                "Property" + SEPARATOR + "Value\n" +
                "-" + SEPARATOR + "-\n" +
                "Internal" + SEPARATOR + parseBoolToYN(annotation.internal()) + "\n" +
                "External" + SEPARATOR + parseBoolToYN(annotation.external()) + "\n" +
                "Synchronous" + SEPARATOR + annotation.synchronism() + "\n" +
                "Authorization" + SEPARATOR + annotation.authorization() + "\n" +
                "Authentication" + SEPARATOR + annotation.authentication() + "\n" +
                "TPS" + SEPARATOR + annotation.tps() + "/sec" + "\n" +
                "Idempotency" + SEPARATOR + parseBoolToYN(annotation.idempotency()) + "\n" +
                "Stateless" + SEPARATOR + parseBoolToYN(annotation.stateless()) + "\n" +
                "Read/Write Intense" + SEPARATOR + parseReadWrite(annotation.readWriteIntense()) + "\n" +
                "Cacheable" + SEPARATOR + parseBoolToYN(annotation.cacheable()) + "\n";
    }

    private static String parseReadWrite(OpenAPITableMetadata.ReadWrite readWrite) {
        return readWrite.getValue();
    }

    private static String parseBoolToYN(boolean value) {
        return value ? "Y" : "N";
    }

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        OpenAPITableMetadata annotation = handlerMethod.getMethodAnnotation(OpenAPITableMetadata.class);
        if (annotation != null) {
            operation.description("**Description:**  \n" + deNull(operation.getDescription()) + "  \n\n" + buildData(annotation));
        }
        return operation;
    }
}