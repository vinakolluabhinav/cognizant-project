package com.example.deposit.client;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long customerID;
    private String cifNumber;
    private String fullName;
    private String segment;
    private String kycStatus;
    private String status;
}
