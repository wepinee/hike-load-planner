package ru.hikeload.web.advice;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.hikeload.service.BusinessException;
import ru.hikeload.service.NotFoundException;

@ControllerAdvice(assignableTypes = ru.hikeload.web.controller.HikePageController.class)
public class MvcExceptionHandler {

    @ExceptionHandler({BusinessException.class, NotFoundException.class})
    public String handleKnown(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/hikes";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage",
                "Ошибка: " + (ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName()));
        return "redirect:/hikes";
    }
}
