package com.loopmarket.common.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends BaseController {

    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception e, Model model) {
        model.addAttribute("message", e.getMessage());
        return renderError("error/error", model); // layout + viewName 방식
    }

    @ExceptionHandler(ResponseStatusException.class)
    public String handleResponseStatusException(ResponseStatusException e, Model model) {
        model.addAttribute("message", e.getReason());
        return renderError("error/error", model);
    }
}