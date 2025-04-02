package com.example.demo.repo;

import com.example.demo.dto.StudentOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface StudentOrderRepo extends JpaRepository<StudentOrder, Long> {
    Optional<StudentOrder> findByRazorpayorderid(String razorpayorderid);
}
