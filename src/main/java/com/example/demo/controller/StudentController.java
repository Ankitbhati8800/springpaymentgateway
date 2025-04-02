package com.example.demo.controller;

import com.example.demo.dto.StudentOrder;
import com.example.demo.service.PaymentService;
import com.razorpay.RazorpayException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class StudentController {

    @Autowired
    private PaymentService paymentService;

    // Create Order
    @PostMapping("/create-order")
    public StudentOrder createOrder(@RequestBody StudentOrder order) throws RazorpayException {
        return paymentService.createOrder(order);
    }

    // Handle Payment Callback
    @PostMapping("/payment-callback") 
    public String handlePaymentCallback(@RequestBody Map<String, String> paymentDetails) {
        paymentService.updatePaymentStatus(paymentDetails);  
        return "Payment callback received!";
    }
}
