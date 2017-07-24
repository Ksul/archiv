package de.ksul.archiv.controller;

import de.ksul.archiv.response.RestResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

/**
 * Created with IntelliJ IDEA.
 * User: Klaus Schulte (m500288)
 * Date: 7/19/17
 * Time: 4:00 PM
 */
@RestControllerAdvice
public class ExceptionHandlingController {

    //@ResponseStatus(value= HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public RestResponse handleError(Exception ex) {

        RestResponse response = new RestResponse();
        response.setSuccess(false);
        response.setError(ex);
        return response;
    }
}
