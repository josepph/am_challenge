package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.TransferNotFoundException;
import com.db.awmd.challenge.service.TransfersService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/v1/transfers")
@Slf4j
@RequiredArgsConstructor
public class TransfersController {

  private final TransfersService transfersService;

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createTransfer(@RequestBody @Valid Transfer transfer) {
    log.info("Started process to create transfer {}", transfer);

    try {
      this.transfersService.createTransfer(transfer);
    } catch (Exception dtie) {
      return new ResponseEntity<>(dtie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(transfer, HttpStatus.CREATED);
  }

  @GetMapping(path = "/{transferId}")
  public ResponseEntity<Object> getTransfer(@PathVariable String transferId) {
    log.info("Retrieving transfer for id {}", transferId);

    Optional<Transfer> transfer = this.transfersService.getTransfer(UUID.fromString(transferId));
    if (!transfer.isPresent()) {
      throw new TransferNotFoundException("Transfer not found for transferId :" + transferId);
    }
    return new ResponseEntity<>(transfer, HttpStatus.OK);

  }

}
