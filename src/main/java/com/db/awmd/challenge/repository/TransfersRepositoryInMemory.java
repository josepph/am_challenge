package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateTransferIdException;
import com.db.awmd.challenge.exception.NegativeAmountException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
public class TransfersRepositoryInMemory implements TransfersRepository {

  private final Map<String, Transfer> transfers = new ConcurrentHashMap<>();

  private final String MSG_POSITIVE_AMOUNT = "Cannot create transfer because amount has to be a positive value.";

  @Override
  public Transfer getTransfer(String transferId) {
        return transfers.get(transferId);
    }

  @Override
  public void createTransfer(Transfer transfer) throws DuplicateTransferIdException, NegativeAmountException {
    if ((transfer.getAmount()).compareTo(BigDecimal.ZERO) <= 0) {
      log.info(MSG_POSITIVE_AMOUNT);
      throw new NegativeAmountException(MSG_POSITIVE_AMOUNT);
    } else {
      transfers.putIfAbsent(transfer.getTransferId(), transfer);
    }
  }

  @Override
  public void clearTransfers() {
        transfers.clear();
    }
}
