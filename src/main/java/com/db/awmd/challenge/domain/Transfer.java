package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class Transfer {

  @NotNull
  @NotEmpty
  private String transferId;

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
  public Transfer(@JsonProperty("transferId") String transferId,
                    @JsonProperty("accountFromId") String accountFromId,
                    @JsonProperty("accountToId") String accountToId,
                    @JsonProperty("amount") BigDecimal amount) {
    this.transferId = transferId;
    this.accountFromId = accountFromId;
    this.accountToId = accountToId;
    this.amount = amount;
  }

}
