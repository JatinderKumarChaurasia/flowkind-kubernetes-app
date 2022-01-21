package app.flowkind.microservices.utils.http;

import app.flowkind.microservices.utils.exceptions.BadRequestException;
import app.flowkind.microservices.utils.exceptions.InvalidInputException;
import app.flowkind.microservices.utils.exceptions.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
class GlobalControllerExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = NotFoundException.class)
    public @ResponseBody HttpErrorInfo handleNotFoundExceptions(ServerHttpRequest serverHttpRequest, NotFoundException notFoundException) {
        return createHttpErrorInfo(HttpStatus.NOT_FOUND,serverHttpRequest,notFoundException);
    }

    @ResponseStatus(value = HttpStatus.UNPROCESSABLE_ENTITY)
    @ExceptionHandler(value = InvalidInputException.class)
    public @ResponseBody HttpErrorInfo handleInvalidInputExceptions(ServerHttpRequest serverHttpRequest, InvalidInputException invalidInputException) {
        return createHttpErrorInfo(HttpStatus.UNPROCESSABLE_ENTITY,serverHttpRequest,invalidInputException);
    }

    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = BadRequestException.class)
    public @ResponseBody HttpErrorInfo handleBadRequestExceptions(ServerHttpRequest serverHttpRequest, BadRequestException badRequestException) {
        return createHttpErrorInfo(HttpStatus.BAD_REQUEST, serverHttpRequest, badRequestException);
    }

    private HttpErrorInfo createHttpErrorInfo(HttpStatus httpStatus,ServerHttpRequest serverHttpRequest, Exception exception) {
        final String path = serverHttpRequest.getPath().pathWithinApplication().value();
        LOGGER.info("Path within Application: {}",path);
        final String message = exception.getMessage();
        LOGGER.debug("Returning HTTP status: {} for path: {}, message: {}", httpStatus, path, message);
        return new HttpErrorInfo(httpStatus,path,message);
    }
}
