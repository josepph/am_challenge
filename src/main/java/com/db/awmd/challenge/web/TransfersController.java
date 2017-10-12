package com.db.awmd.challenge.web;

import com.db.awmd.challenge.domain.Transfer;
import com.db.awmd.challenge.exception.DuplicateTransferIdException;
import com.db.awmd.challenge.service.TransfersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/v1/transfers")
@Slf4j
public class TransfersController {

  private final TransfersService transfersService;

  @Autowired
  public TransfersController(TransfersService transfersService) {
        this.transfersService = transfersService;
    }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Object> createTransfer(@RequestBody @Valid Transfer transfer) {
    log.info("Creating transfer {}", transfer);

    try {
      this.transfersService.createTransfer(transfer);
    } catch (DuplicateTransferIdException dtie) {
      return new ResponseEntity<>(dtie.getMessage(), HttpStatus.BAD_REQUEST);
    }

    return new ResponseEntity<>(HttpStatus.CREATED);
  }

  @GetMapping(path = "/{transferId}")
  public Transfer getTransfer(@PathVariable String transferId) {
    log.info("Retrieving transfer for id {}", transferId);
    return this.transfersService.getTransfer(transferId);
  }

}
