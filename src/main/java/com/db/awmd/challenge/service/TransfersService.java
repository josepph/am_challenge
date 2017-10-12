package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateTransferIdException;
import com.db.awmd.challenge.repository.TransfersRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransfersService {

  @Getter
  private final TransfersRepository transfersRepository;
  private final AccountsService accountsService;
  private final EmailNotificationService emailNotificationService;

  @Autowired
  public TransfersService(TransfersRepository transfersRepository, AccountsService accountsService, EmailNotificationService emailNotificationService) {
    this.transfersRepository = transfersRepository;
    this.accountsService = accountsService;
    this.emailNotificationService = emailNotificationService;
  }

  public synchronized void createTransfer(Transfer transfer) {

    Transfer previousTransfer = transfersRepository.getTransfer(transfer.getTransferId());
    if (previousTransfer != null) {
      throw new DuplicateTransferIdException(
        "Transfer with id " + transfer.getTransferId() + " is already completed.");
      }

    Account accountFrom = this.accountsService.getAccount(transfer.getAccountFromId());
    Account accountTo = this.accountsService.getAccount(transfer.getAccountToId());

    if (accountFrom.getBalance().compareTo(transfer.getAmount()) >= 0) {
      accountFrom.setBalance(accountFrom.getBalance().subtract(transfer.getAmount()));
      accountTo.setBalance(accountTo.getBalance().add(transfer.getAmount()));

      try {
        this.transfersRepository.createTransfer(transfer);
      } catch (Exception ex) {
        log.error("Exception: " + ex.getMessage());
        throw (ex);
      }
      this.emailNotificationService.notifyAboutTransfer(accountTo, "Transfer identified as " + transfer.getTransferId() + " is completed.");
    } else {
      log.info("Transfer " + transfer.getTransferId() + " could not be created because source account had not enough funds.");
    }

  }

  public Transfer getTransfer(String transferId) {
        return this.transfersRepository.getTransfer(transferId);
    }

}
