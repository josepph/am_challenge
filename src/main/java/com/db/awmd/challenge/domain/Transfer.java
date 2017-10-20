package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class Transfer {

  @NotNull
  private UUID transferId;

  @NotNull
  @NotEmpty
  private String accountFromId;

  @NotNull
  @NotEmpty
  private String accountToId;

  @NotNull
  @Min(value = 0, message = "Amount to be transferred.")
  private BigDecimal amount;

  @JsonCreator
  public Transfer(  @JsonProperty("accountFromId") String accountFromId,
                    @JsonProperty("accountToId") String accountToId,
                    @JsonProperty("amount") BigDecimal amount) {
    this.transferId = UUID.randomUUID();
    this.accountFromId = accountFromId;
    this.accountToId = accountToId;
    this.amount = amount;
  }

}
