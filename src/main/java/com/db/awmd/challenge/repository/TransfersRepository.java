package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Transfer;

public interface TransfersRepository {

  Transfer getTransfer(String transferId);

  void createTransfer(Transfer transfer);

  void clearTransfers();

}
