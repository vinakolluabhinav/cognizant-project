package com.example.deposit.service;

import com.example.deposit.dto.AccountResponse;
import com.example.deposit.dto.CasaAccountRequest;
import com.example.deposit.dto.TermDepositRequest;
import com.example.deposit.entity.DepositAccount;
import com.example.deposit.entity.TermDeposit;

import java.math.BigDecimal;
import java.util.List;

public interface AccountService {

    AccountResponse createCasaAccount(CasaAccountRequest request);

    AccountResponse createTermDepositAccount(TermDepositRequest request);

    DepositAccount getDepositAccountById(Long accountId);

    TermDeposit getTermDepositByAccountId(Long accountId);

    TermDeposit getTermDepositByTdId(Long tdId);
    List<TermDeposit> getTdsMaturingToday();

    List<DepositAccount> getAccountsByCustomerId(Long customerId);
    List<DepositAccount> getAllActiveCasaAccounts();

    void updateBalance(Long accountId, BigDecimal balance);

    void updateTermDepositStatus(Long tdId, String status);
}
