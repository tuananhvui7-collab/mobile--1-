package com.ecommerce.mobile.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecommerce.mobile.entity.Feedback;
import com.ecommerce.mobile.service.FeedbackService;

@Controller
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @GetMapping("/profile/feedbacks")
    public String customerList(@AuthenticationPrincipal UserDetails principal, Model model) {
        List<Feedback> feedbacks = feedbackService.getFeedbacksForCustomer(principal.getUsername());
        model.addAttribute("feedbacks", feedbacks);
        return "profile/feedbacks";
    }

    @GetMapping("/profile/feedbacks/new")
    public String customerCreateForm() {
        return "profile/feedback-form";
    }

    @PostMapping("/profile/feedbacks")
    public String customerCreate(@AuthenticationPrincipal UserDetails principal,
                                 @RequestParam String content,
                                 RedirectAttributes redirectAttributes) {
        try {
            feedbackService.createFeedback(principal.getUsername(), content);
            redirectAttributes.addFlashAttribute("success", "Đã gửi phản hồi");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/profile/feedbacks";
    }

    @GetMapping("/employee/feedbacks")
    public String employeeList(@AuthenticationPrincipal UserDetails principal,
                               Model model) {
        model.addAttribute("feedbacks", feedbackService.getPendingFeedbacks());
        model.addAttribute("myFeedbacks", feedbackService.getFeedbacksForEmployee(principal.getUsername()));
        return "employee/feedback/list";
    }

    @PostMapping("/employee/feedbacks/{feedbackId}/assign")
    public String assignToMe(@AuthenticationPrincipal UserDetails principal,
                             @PathVariable Long feedbackId,
                             RedirectAttributes redirectAttributes) {
        try {
            feedbackService.assignToMe(principal.getUsername(), feedbackId);
            redirectAttributes.addFlashAttribute("success", "Đã nhận xử lý phản hồi");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/employee/feedbacks";
    }

    @GetMapping("/employee/feedbacks/{feedbackId}")
    public String employeeDetail(@AuthenticationPrincipal UserDetails principal,
                                 @PathVariable Long feedbackId,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            Feedback feedback = feedbackService.viewFeedbackForEmployee(principal.getUsername(), feedbackId);
            model.addAttribute("feedback", feedback);
            return "employee/feedback/detail";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/employee/feedbacks";
        }
    }

    @PostMapping("/employee/feedbacks/{feedbackId}/resolve")
    public String resolve(@AuthenticationPrincipal UserDetails principal,
                          @PathVariable Long feedbackId,
                          @RequestParam String resolution,
                          RedirectAttributes redirectAttributes) {
        try {
            feedbackService.resolveFeedback(principal.getUsername(), feedbackId, resolution);
            redirectAttributes.addFlashAttribute("success", "Đã xử lý phản hồi");
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/employee/feedbacks/" + feedbackId;
    }
}
