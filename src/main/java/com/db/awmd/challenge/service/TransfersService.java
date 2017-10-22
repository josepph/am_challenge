package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.TransferNotAllowedException;
import com.db.awmd.challenge.repository.TransfersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransfersService {

  private final TransfersRepository transfersRepository;
  private final AccountsService accountsService;
  private final EmailNotificationService emailNotificationService;

  private final String MSG_POSITIVE_AMOUNT = "Cannot create transfer because amount has to be a positive value.";
  private final String MSG_NEGATIVE_BALANCE = "Cannot create transfer because the source account has not enough funds.";
  private final String MSG_TRANSFER_SUCCESS = "Transfer with id {} is already completed.";


  public Optional<Transfer> createTransfer(Transfer transfer){
    log.debug("createTransfer thread id: {}", Thread.currentThread().getId());
    try {
      makeTransfer(transfer);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return Optional.empty();
  }

  public Optional<Transfer> getTransfer(UUID transferId) {
    return Optional.of(transfersRepository.getTransfer(transferId));
  }

  private boolean validateTransfer(Transfer transfer) {

    // verify a transfer with same transferId does not exist
    Transfer previousTransfer = transfersRepository.getTransfer(transfer.getTransferId());
    if (previousTransfer != null) {
      log.info(MSG_TRANSFER_SUCCESS, transfer.getTransferId());
      return false;
    }

    // verify the transfer has a positive amount
    if (transfer.getAmount().compareTo(BigDecimal.ZERO)<=0) {
      log.info(MSG_POSITIVE_AMOUNT);
      return false;
    }

    // verify from account will not have a negative amount once transfer completed
    Account accountFrom = this.accountsService.getAccount(transfer.getAccountFromId());
    if (accountFrom.getBalance().compareTo(transfer.getAmount())<0) {
      log.info(MSG_NEGATIVE_BALANCE);
      return false;
    }
    return true;
  }

  @Async
  public Transfer makeTransfer(Transfer transfer) throws Exception {
    log.debug("makeTransfer thread id: {}", Thread.currentThread().getId());
    Account accountFrom = this.accountsService.getAccount(transfer.getAccountFromId());
    Account accountTo = this.accountsService.getAccount(transfer.getAccountToId());

    if (validateTransfer(transfer)) {

      try {

        accountFrom.setBalance(accountFrom.getBalance().subtract(transfer.getAmount()));
        accountTo.setBalance(accountTo.getBalance().add(transfer.getAmount()));

        transfersRepository.createTransfer(transfer);

        emailNotificationService.notifyAboutTransfer(accountTo, "Transfer identified as " + transfer.getTransferId() + " is completed.");
        emailNotificationService.notifyAboutTransfer(accountFrom, "Transfer identified as " + transfer.getTransferId() + " is completed.");

        return transfer;

      } catch (Exception e) {
        log.error("An error has occurred when creating transfer " + transfer.getTransferId() + " and has not been completed.");
        throw e;
      }
    } else {
      throw new TransferNotAllowedException("Transfer not processed. Please check logs to find reason.");
    }

  }


}
