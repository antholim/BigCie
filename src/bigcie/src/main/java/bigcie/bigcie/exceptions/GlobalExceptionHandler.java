// java
package bigcie.bigcie.exceptions;

import bigcie.bigcie.exceptions.responses.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Hidden
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("IllegalArgumentException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.error("MethodArgumentTypeMismatchException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse("Invalid request parameter: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(SourceAndTargetStationAreEqualsException.class)
    public ResponseEntity<ErrorResponse> handleSourceAndTargetStationAreEquals(SourceAndTargetStationAreEqualsException ex) {
        log.error("SourceAndTargetStationAreEqualsException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(StationIsEmptyException.class)
    public ResponseEntity<ErrorResponse> handleStationIsEmpty(StationIsEmptyException ex) {
        log.error("StationIsEmptyException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(StationIsFullException.class)
    public ResponseEntity<ErrorResponse> handleStationIsFull(StationIsFullException ex) {
        log.error("StationIsFullException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(UserAlreadyHasReservationException.class)
    public ResponseEntity<ErrorResponse> handleUserAlreadyHasReservation(UserAlreadyHasReservationException ex) {
        log.error("UserAlreadyHasReservationException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(RiderAlreadyHasBikeException.class)
    public ResponseEntity<ErrorResponse> handleRiderAlreadyHasBikeException(RiderAlreadyHasBikeException ex) {
        log.error("RiderAlreadyHasBikeException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.error("RuntimeException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        log.error("Generic Exception: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    @ExceptionHandler(AllBikesReservedException.class)
    public ResponseEntity<ErrorResponse> handleAllBikesReservedException(AllBikesReservedException ex) {
        log.error("AllBikesReservedException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(StationOutOfServiceException.class)
    public ResponseEntity<ErrorResponse> handleStationOutOfServiceException(StationOutOfServiceException ex) {
        log.error("StationOutOfServiceException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(UserIsNotRiderException.class)
    public ResponseEntity<ErrorResponse> handleUserIsNotRiderException(UserIsNotRiderException ex) {
        log.error("UserIsNotRiderException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(NoDefaultPaymentMethodFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoDefaultPaymentMethodFoundException(NoDefaultPaymentMethodFoundException ex) {
        log.error("NoDefaultPaymentMethodFoundException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(NoAvailableBikesOfRequestedTypeException.class)
    public ResponseEntity<ErrorResponse> handleNoAvailableBikesOfRequestedTypeException(NoAvailableBikesOfRequestedTypeException ex) {
        log.error("NoAvailableBikesOfRequestedTypeException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(StationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStationNotFoundException(StationNotFoundException ex) {
        log.error("StationNotFoundException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }


}
