package it.gov.pagopa.nodoverifykoaux.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface OpenAPITableMetadata {

    /**
     * The field that define if the API is used for internal communications.
     */
    boolean internal() default true;

    /**
     * The field that define if the API is exposed to public uses.
     */
    boolean external() default true;

    /**
     * The field that define if the communication with the API can be executed in synchronous mode.
     */
    APISynchronism synchronism() default APISynchronism.SYNC;

    /**
     * The field that define if the API is secured with an authorization strategy.
     */
    APISecurityMode authorization() default APISecurityMode.NONE;

    /**
     * The field that define if the API is secured with an authentication strategy.
     */
    APISecurityMode authentication() default APISecurityMode.NONE;

    /**
     * The field that define the estimated number of invocation per second for this API.
     */
    float tps() default 1;

    /**
     * The field that define if the API is idempotent.
     */
    boolean idempotency() default true;

    /**
     * The field that define if the API operates without volatile state.
     */
    boolean stateless() default true;

    /**
     * The field that define what kind of operation the API executes.
     */
    ReadWrite readWriteIntense() default ReadWrite.READ;

    /**
     * The field that define if the API is cacheable.
     */
    boolean cacheable() default false;

    @Getter
    @AllArgsConstructor
    enum ReadWrite {
        NONE(""),
        READ("Read"),
        WRITE("Write"),
        BOTH("Read and Write");

        public final String value;
    }

    @Getter
    @AllArgsConstructor
    enum APISecurityMode {
        NONE("N"),
        APIKEY("Y (Subscription Key)"),
        JWT("Y (JWT Token)");

        public final String value;
    }

    @Getter
    @AllArgsConstructor
    enum APISynchronism {
        SYNC("Synchronous"),
        ASYNC("Asynchronous");

        public final String value;
    }
}