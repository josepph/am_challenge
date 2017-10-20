package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateTransferIdException;
import com.db.awmd.challenge.exception.NegativeAmountException;
import com.db.awmd.challenge.exception.NotEnoughFundsException;
import com.db.awmd.challenge.service.AccountsService;
import com.db.awmd.challenge.service.EmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TransfersRepositoryInMemory implements TransfersRepository {

  private final Map<String, Transfer> transfers = new ConcurrentHashMap<>();
  private final AccountsService accountsService;
  private final EmailNotificationService emailNotificationService;
  private final String MSG_POSITIVE_AMOUNT = "Cannot create transfer because amount has to be a positive value.";

  @Override
  public Transfer getTransfer(UUID transferId) {
        return transfers.get(transferId.toString());
    }

  @Override
  public Transfer createTransfer(Transfer transfer) throws DuplicateTransferIdException, NegativeAmountException, NotEnoughFundsException {

    Transfer previousTransfer = getTransfer(transfer.getTransferId());
    if (previousTransfer != null) {
      //throw new DuplicateTransferIdException(
      //        "Transfer with id " + transfer.getTransferId() + " is already completed.");
      log.info("Transfer with id " + transfer.getTransferId() + " is already completed.");
      return previousTransfer;
    }

    Account accountFrom = this.accountsService.getAccount(transfer.getAccountFromId());
    Account accountTo = this.accountsService.getAccount(transfer.getAccountToId());

    if (accountFrom.getBalance().compareTo(transfer.getAmount()) >= 0) {
      accountFrom.setBalance(accountFrom.getBalance().subtract(transfer.getAmount()));
      accountTo.setBalance(accountTo.getBalance().add(transfer.getAmount()));

      if ((transfer.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
        log.info(MSG_POSITIVE_AMOUNT);
        throw new NegativeAmountException(MSG_POSITIVE_AMOUNT);
      } else {
        transfers.putIfAbsent(transfer.getTransferId().toString(), transfer);
      }

      emailNotificationService.notifyAboutTransfer(accountTo, "Transfer identified as " + transfer.getTransferId() + " is completed.");
      emailNotificationService.notifyAboutTransfer(accountFrom, "Transfer identified as " + transfer.getTransferId() + " is completed.");
    } else {
      log.info("Transfer {} could not be created because source account had not enough funds.", transfer.getTransferId());
      throw new NotEnoughFundsException("Transfer " + transfer.getTransferId() +  " could not be created because source account had not enough funds.");
    }
    return transfer;
  }

  @Override
  public void clearTransfers() {
        transfers.clear();
    }
}
