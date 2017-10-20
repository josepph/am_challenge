package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Transfer;

import java.util.UUID;

public interface TransfersRepository {

  Transfer getTransfer(UUID transferId);

  Transfer createTransfer(Transfer transfer);

  void clearTransfers();

}
