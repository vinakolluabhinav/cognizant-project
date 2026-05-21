package com.depositcore.entity;

public enum PostingType {
    CASAInterest,        // Daily/periodic interest credited to Savings/Current account
    FDMaturityInterest,  // Interest paid at FD maturity
    RDMaturityInterest   // Interest paid at RD maturity
}
