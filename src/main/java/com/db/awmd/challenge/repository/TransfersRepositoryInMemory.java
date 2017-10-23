package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DatabaseErrorException;
import com.db.awmd.challenge.service.AccountsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@RequiredArgsConstructor
public class TransfersRepositoryInMemory implements TransfersRepository {

  private final Map<String, Transfer> transfers = new ConcurrentHashMap<>();
  private final AccountsService accountsService;

  @Override
  public Transfer getTransfer(UUID transferId) {
        return transfers.get(transferId.toString());
    }

  @Override
  public Transfer createTransfer(Transfer transfer) throws DatabaseErrorException {
    try {
      transfers.putIfAbsent(transfer.getTransferId().toString(), transfer);
      return transfer;
    } catch (DatabaseErrorException dberr) {
      throw new DatabaseErrorException("Error writing to the database for transfer " + transfer.getTransferId().toString());
    }
  }

  @Override
  public void clearTransfers() {
        transfers.clear();
    }
}
