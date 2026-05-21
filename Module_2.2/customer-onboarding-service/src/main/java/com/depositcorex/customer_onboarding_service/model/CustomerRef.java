package com.depositcorex.customer_onboarding_service.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_ref", indexes = {
        @Index(name = "idx_cif_number", columnList = "cifNumber")
})

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class CustomerRef {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long customerID;

    @Column(name = "user_id")
    private Long userId;

    // Ensures CIF is not null, not empty, and has a specific length
    @NotBlank(message = "CIF Number is mandatory")
    @Size(min = 5, max = 10, message = "CIF Number must be between 5 and 20 characters")
    @Column(nullable = false, unique = true, length = 10)
    private String cifNumber;

    // Ensures Full Name is not null or blank
    @NotBlank(message = "Full Name is mandatory")
    @Column(nullable = false)
    private String fullName;

    // Ensures the Enum is not null
    @NotNull(message = "Customer Segment (RETAIL/CORPORATE) is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerSegment segment;

    // Ensures KYC status is provided (e.g., PENDING, VERIFIED)
    @NotBlank(message = "KYC Status is mandatory")
    @Column(nullable = false)
    private String kycStatus;

    // Ensures Account status is provided (e.g., ACTIVE, INACTIVE)
    @NotBlank(message = "Account Status is mandatory")
    @Column(nullable = false)
    private String status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}


