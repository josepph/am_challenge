package com.db.awmd.challenge.service;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.repository.TransfersRepository;
import com.google.common.base.Throwables;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransfersService {

  @Getter
  private final TransfersRepository transfersRepository;
  private final AccountsService accountsService;
  private final EmailNotificationService emailNotificationService;

  public Optional<Transfer> createTransfer(Transfer transfer) {

    Observable<Transfer> concurrentData = (
            putTransfer (
                    () -> transfersRepository.createTransfer(transfer), transfer)
    );

    if (concurrentData.isEmpty().blockingGet()) {
      return Optional.empty();
    }
    return Optional.of(concurrentData.blockingFirst());
  }


  public Optional<Transfer> getTransfer(UUID transferId) {

     Observable<Transfer> concurrentData = (
            retrieveTransfer (
                    () -> transfersRepository.getTransfer(transferId), transferId)
     );

    if (concurrentData.isEmpty().blockingGet()) {
      return Optional.empty();
    }
    return Optional.of(concurrentData.blockingFirst());
  }

  private <T> Observable<T> retrieveTransfer(Callable<T> callable, UUID transferId) {
    return Observable.fromCallable(callable)
            .doOnError(error -> log.error("Error retrieving transfer " + transferId + " info: " + error,
                                          Throwables.getRootCause(error)))
            .subscribeOn(Schedulers.newThread());
  }

  private <T> Observable<T> putTransfer(Callable<T> callable, Transfer transfer) {
    return Observable.fromCallable(callable)
            .doOnError(error -> log.error("Error creating transfer " + transfer.getTransferId().toString() + " info: " + error,
                                          Throwables.getRootCause(error)))
            .subscribeOn(Schedulers.single());
  }

}
