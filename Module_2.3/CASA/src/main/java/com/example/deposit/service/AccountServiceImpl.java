package com.example.deposit.service;

import com.example.deposit.client.CustomerClient;
import com.example.deposit.client.CustomerDTO;
import com.example.deposit.client.ProductClient;
import com.example.deposit.client.ProductDTO;
import com.example.deposit.dto.AccountResponse;
import com.example.deposit.dto.CasaAccountRequest;
import com.example.deposit.dto.TermDepositRequest;
import com.example.deposit.entity.DepositAccount;
import com.example.deposit.entity.TermDeposit;
import com.example.deposit.exception.ResourceNotFoundException;
import com.example.deposit.repository.DepositAccountRepository;
import com.example.deposit.repository.TermDepositRepository;
import com.example.deposit.service.validation.AmountValidator;
import com.example.deposit.service.validation.ProductValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AccountServiceImpl implements AccountService {

    private final CustomerClient customerClient;
    private final ProductClient productClient;
    private final DepositAccountRepository depositAccountRepository;
    private final TermDepositRepository termDepositRepository;
    private final ProductValidator productValidator;
    private final AmountValidator amountValidator;

    public AccountServiceImpl(CustomerClient customerClient,
                              ProductClient productClient,
                              DepositAccountRepository depositAccountRepository,
                              TermDepositRepository termDepositRepository,
                              ProductValidator productValidator,
                              AmountValidator amountValidator) {
        this.customerClient = customerClient;
        this.productClient = productClient;
        this.depositAccountRepository = depositAccountRepository;
        this.termDepositRepository = termDepositRepository;
        this.productValidator = productValidator;
        this.amountValidator = amountValidator;
    }

    @Override
    @Transactional
    public AccountResponse createCasaAccount(CasaAccountRequest request) {
        CustomerDTO customer = customerClient.getCustomerByCif(request.getCifNumber());

        if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
            throw new IllegalStateException("Customer is not active: " + request.getCifNumber());
        }
        if (!"VERIFIED".equalsIgnoreCase(customer.getKycStatus())) {
            throw new IllegalStateException("Customer KYC is not verified: " + request.getCifNumber());
        }

        ProductDTO product = productClient.getProductById(request.getProductId());
        productValidator.validateForCasa(product);

        DepositAccount account = new DepositAccount();
        account.setCustomerId(customer.getCustomerID());
        account.setProductId(product.getProductID());
        account.setAccountNumber(generateAccountNumber());
        account.setCategory(request.getCategory());
        account.setCurrency(request.getCurrency());
        account.setOpenDate(LocalDate.now());
        account.setStatus("ACTIVE");
        account.setCurrentBalance(BigDecimal.ZERO);

        depositAccountRepository.save(account);

        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountNumber(account.getAccountNumber());
        response.setStatus(account.getStatus());
        response.setMessage("CASA account created successfully");
        return response;
    }

    @Override
    @Transactional
    public AccountResponse createTermDepositAccount(TermDepositRequest request) {
        CustomerDTO customer = customerClient.getCustomerByCif(request.getCifNumber());

        if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
            throw new IllegalStateException("Customer is not active: " + request.getCifNumber());
        }
        if (!"VERIFIED".equalsIgnoreCase(customer.getKycStatus())) {
            throw new IllegalStateException("Customer KYC is not verified: " + request.getCifNumber());
        }

        ProductDTO product = productClient.getProductById(request.getProductId());
        productValidator.validateForTermDeposit(product);
        amountValidator.validate(request.getPrincipalAmount(), product);

        DepositAccount account = new DepositAccount();
        account.setCustomerId(customer.getCustomerID());
        account.setProductId(product.getProductID());
        account.setAccountNumber(generateAccountNumber());
        account.setCategory(product.getCategory());
        account.setCurrency(request.getCurrency());
        account.setOpenDate(LocalDate.now());
        account.setStatus("ACTIVE");
        account.setCurrentBalance(BigDecimal.ZERO);

        depositAccountRepository.save(account);

        TermDeposit termDeposit = new TermDeposit();
        termDeposit.setAccountId(account.getAccountId());
        termDeposit.setPrincipalAmount(request.getPrincipalAmount());
        termDeposit.setTenureMonths(request.getTenureMonths());
        termDeposit.setRate(request.getRate() != null ? request.getRate() : BigDecimal.valueOf(5.0));
        termDeposit.setStartDate(LocalDate.now());
        termDeposit.setMaturityDate(LocalDate.now().plusMonths(request.getTenureMonths()));
        termDeposit.setPayoutMode(request.getPayoutMode());
        termDeposit.setStatus("ACTIVE");

        termDepositRepository.save(termDeposit);

        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountNumber(account.getAccountNumber());
        response.setStatus(account.getStatus());
        response.setMessage("Term Deposit account created successfully");
        return response;
    }

    @Override
    public DepositAccount getDepositAccountById(Long accountId) {
        return depositAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
    }

    @Override
    public TermDeposit getTermDepositByAccountId(Long accountId) {
        return termDepositRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Term Deposit not found for account: " + accountId));
    }

    @Override
    public TermDeposit getTermDepositByTdId(Long tdId) {
        return termDepositRepository.findById(tdId)
                .orElseThrow(() -> new ResourceNotFoundException("Term Deposit not found: " + tdId));
    }

    @Override
    public List<TermDeposit> getTdsMaturingToday() {
        return termDepositRepository.findByMaturityDateAndStatus(LocalDate.now(), "ACTIVE");
    }

    @Override
    public List<DepositAccount> getAllActiveCasaAccounts() {
        return depositAccountRepository.findByStatusAndCategoryIn(
                "ACTIVE", List.of("SAVINGS", "CURRENT"));
    }

    @Override
    public List<DepositAccount> getAccountsByCustomerId(Long customerId) {
        return depositAccountRepository.findByCustomerId(customerId);
    }

    @Override
    @Transactional
    public void updateBalance(Long accountId, BigDecimal balance) {
        DepositAccount account = depositAccountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        account.setCurrentBalance(balance);
        depositAccountRepository.save(account);
    }

    @Override
    @Transactional
    public void updateTermDepositStatus(Long tdId, String status) {
        TermDeposit td = termDepositRepository.findById(tdId)
                .orElseThrow(() -> new ResourceNotFoundException("Term Deposit not found: " + tdId));
        td.setStatus(status);
        termDepositRepository.save(td);
    }

    private String generateAccountNumber() {
        return "ACCT-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}
