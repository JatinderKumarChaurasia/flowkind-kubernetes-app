package app.flowkind.microservices.utils.http;

import org.springframework.http.HttpStatus;

import java.time.ZonedDateTime;

public record HttpErrorInfo(ZonedDateTime zonedDateTimeStamp, String path, HttpStatus httpStatus, String message) {
    public HttpErrorInfo() {
        this(null,null,null,null);
    }
    public HttpErrorInfo(HttpStatus httpStatus, String path, String message) {
       this(ZonedDateTime.now(),path,httpStatus,message);
    }
}
