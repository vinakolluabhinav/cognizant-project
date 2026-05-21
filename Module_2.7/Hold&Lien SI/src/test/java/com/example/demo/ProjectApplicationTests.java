package com.example.demo;

import com.depositcorex.Main.Client.AccountDTO;
import com.depositcorex.Main.Client.CasaAccountClient;
import com.depositcorex.Main.Entities.HoldOrLien;
import com.depositcorex.Main.Entities.StandingInstruction;
import com.depositcorex.Main.Exception.InvalidTransactionException;
import com.depositcorex.Main.Exception.ResourceNotFoundException;
import com.depositcorex.Main.Repository.HoldRepository;
import com.depositcorex.Main.Repository.StandingInstructionRepository;
import com.depositcorex.Main.Service.ServicingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectApplicationTests{

    @Mock
    private HoldRepository holdRepo;

    @Mock
    private StandingInstructionRepository siRepo;

    @Mock
    private CasaAccountClient casaAccountClient;

    @InjectMocks
    private ServicingService servicingService;

    // --- 1. PLACE HOLD TESTS ---

    @Test
    void placeHold_Success() {
        // Arrange
        Long accountId = 1L;
        AccountDTO mockAccount = new AccountDTO(); // Assume standard fields exist
        HoldOrLien savedHold = new HoldOrLien();
        savedHold.setStatus("ACTIVE");

        when(casaAccountClient.getAccountById(accountId)).thenReturn(mockAccount);
        when(holdRepo.save(any(HoldOrLien.class))).thenReturn(savedHold);

        // Act
        HoldOrLien result = servicingService.placeHold(accountId, BigDecimal.TEN, "Court Order", "LIEN");

        // Assert
        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        verify(holdRepo, times(1)).save(any(HoldOrLien.class));
    }

    @Test
    void placeHold_AccountNotFound_ThrowsException() {
        // Arrange
        Long accountId = 99L;
        when(casaAccountClient.getAccountById(accountId)).thenReturn(null);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> 
            servicingService.placeHold(accountId, BigDecimal.TEN, "Reason", "HOLD")
        );
        verify(holdRepo, never()).save(any(HoldOrLien.class));
    }

    // --- 2. RELEASE HOLD TESTS ---

    @Test
    void releaseHold_Success() {
        // Arrange
        Long holdId = 123L;
        HoldOrLien existingHold = new HoldOrLien();
        existingHold.setStatus("ACTIVE");

        when(holdRepo.findById(holdId)).thenReturn(Optional.of(existingHold));
        when(holdRepo.save(any(HoldOrLien.class))).thenReturn(existingHold);

        // Act
        servicingService.releaseHold(holdId);

        // Assert
        assertEquals("RELEASED", existingHold.getStatus());
        verify(holdRepo, times(1)).save(existingHold);
    }

    @Test
    void releaseHold_HoldNotFound_ThrowsException() {
        // Arrange
        Long holdId = 999L;
        when(holdRepo.findById(holdId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> servicingService.releaseHold(holdId));
        verify(holdRepo, never()).save(any(HoldOrLien.class));
    }

    // --- 3. GET AVAILABLE BALANCE TESTS ---

    @Test
    void getAvailableBalance_Success() {
        // Arrange
        Long accountId = 1L;
        AccountDTO mockAccount = new AccountDTO();
        mockAccount.setCurrentBalance(new BigDecimal("1000.00"));

        when(casaAccountClient.getAccountById(accountId)).thenReturn(mockAccount);
        when(holdRepo.sumActiveHolds(accountId)).thenReturn(new BigDecimal("200.00"));

        // Act
        BigDecimal availableBalance = servicingService.getAvailableBalance(accountId);

        // Assert
        assertEquals(new BigDecimal("800.00"), availableBalance);
    }

    @Test
    void getAvailableBalance_NoActiveHolds_ReturnsCurrentBalance() {
        // Arrange
        Long accountId = 1L;
        AccountDTO mockAccount = new AccountDTO();
        mockAccount.setCurrentBalance(new BigDecimal("500.00"));

        when(casaAccountClient.getAccountById(accountId)).thenReturn(mockAccount);
        when(holdRepo.sumActiveHolds(accountId)).thenReturn(null); // Database SUM returns null if no rows match

        // Act
        BigDecimal availableBalance = servicingService.getAvailableBalance(accountId);

        // Assert
        assertEquals(new BigDecimal("500.00"), availableBalance);
    }

    // --- 4. STANDING INSTRUCTION TESTS ---

    @Test
    void createStandingInstruction_Success() {
        // Arrange
        Long fromAcc = 1L;
        Long toAcc = 2L;
        StandingInstruction savedSi = new StandingInstruction();
        savedSi.setStatus("ACTIVE");

        when(casaAccountClient.getAccountById(fromAcc)).thenReturn(new AccountDTO());
        when(casaAccountClient.getAccountById(toAcc)).thenReturn(new AccountDTO());
        when(siRepo.save(any(StandingInstruction.class))).thenReturn(savedSi);

        // Act
        StandingInstruction result = servicingService.createStandingInstruction(fromAcc, toAcc, new BigDecimal("50.00"), "MONTHLY");

        // Assert
        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        verify(siRepo, times(1)).save(any(StandingInstruction.class));
    }

    // --- 5. VALIDATION LAYER TESTS ---

    @Test
    void validateFundAvailability_SufficientFunds_NoException() {
        // Arrange
        Long accountId = 1L;
        AccountDTO mockAccount = new AccountDTO();
        mockAccount.setCurrentBalance(new BigDecimal("100.00"));

        when(casaAccountClient.getAccountById(accountId)).thenReturn(mockAccount);
        when(holdRepo.sumActiveHolds(accountId)).thenReturn(BigDecimal.ZERO);

        // Act & Assert (Should pass smoothly without raising exceptions)
        assertDoesNotThrow(() -> servicingService.validateFundAvailability(accountId, new BigDecimal("50.00")));
    }

    @Test
    void validateFundAvailability_InsufficientFunds_ThrowsException() {
        // Arrange
        Long accountId = 1L;
        AccountDTO mockAccount = new AccountDTO();
        mockAccount.setCurrentBalance(new BigDecimal("100.00"));

        when(casaAccountClient.getAccountById(accountId)).thenReturn(mockAccount);
        when(holdRepo.sumActiveHolds(accountId)).thenReturn(BigDecimal.ZERO);

        // Act & Assert
        assertThrows(InvalidTransactionException.class, () -> 
            servicingService.validateFundAvailability(accountId, new BigDecimal("150.00"))
        );
    }

    @Test
    void validateAccountStatusForSI_ActiveStatus_NoException() {
        // Arrange
        Long accountId = 1L;
        AccountDTO mockAccount = new AccountDTO();
        mockAccount.setStatus("ACTIVE");

        when(casaAccountClient.getAccountById(accountId)).thenReturn(mockAccount);

        // Act & Assert
        assertDoesNotThrow(() -> servicingService.validateAccountStatusForSI(accountId));
    }

    @Test
    void validateAccountStatusForSI_DormantOrClosedStatus_ThrowsException() {
        // Arrange
        Long accountId = 1L;
        AccountDTO mockAccount = new AccountDTO();
        mockAccount.setStatus("DORMANT");
        mockAccount.setAccountNumber("123456789");

        when(casaAccountClient.getAccountById(accountId)).thenReturn(mockAccount);

        // Act & Assert
        assertThrows(InvalidTransactionException.class, () -> servicingService.validateAccountStatusForSI(accountId));
    }
}
