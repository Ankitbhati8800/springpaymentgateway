package com.example.demo.service;

import com.example.demo.dto.StudentOrder;
import com.example.demo.repo.StudentOrderRepo;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Autowired
    private StudentOrderRepo studentRepo;

    @Value("${razorpay.key.id}")
    private String razorpayKey;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    private RazorpayClient client;

    @PostConstruct
    public void initRazorpayClient() {
        try {
            this.client = new RazorpayClient(razorpayKey, razorpaySecret);
            logger.info("✅ Razorpay client initialized successfully.");
        } catch (RazorpayException e) {
            logger.error("❌ Failed to initialize Razorpay client: {}", e.getMessage());
        }
    }

    public StudentOrder createOrder(StudentOrder order) throws RazorpayException {
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", order.getAmount() * 100); // Convert to paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", order.getEmail());

        try {
            Order razorpayOrder = client.orders.create(orderRequest);
            logger.info("✅ Razorpay Order Created: {}", razorpayOrder);

            order.setRazorpayorderid(razorpayOrder.get("id"));
            order.setOrderstatus("CREATED");

            return studentRepo.save(order); // Save order in DB

        } catch (RazorpayException e) {
            logger.error("❌ Error creating Razorpay order: {}", e.getMessage());
            throw e;
        }
    }

    public void updatePaymentStatus(Map<String, String> responsePayload) {
        String razorpayOrderId = responsePayload.get("razorpay_order_id");
        String paymentId = responsePayload.get("razorpay_payment_id");

        logger.info("✅ Payment received: Order ID = {}, Payment ID = {}", razorpayOrderId, paymentId);

        StudentOrder order = studentRepo.findByRazorpayorderid(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + razorpayOrderId));

        order.setOrderstatus("PAYMENT_COMPLETED");
        studentRepo.save(order);  // Save updated status to DB
    }
}
