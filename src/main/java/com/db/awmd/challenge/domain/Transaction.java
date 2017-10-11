package com.db.awmd.challenge.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class Transaction {

    @NotNull
    private String transactionId;

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
    public Transaction( @JsonProperty("transactionId") String transactionId,
                        @JsonProperty("accountFromId") String accountFromId,
                        @JsonProperty("accountToId") String accountToId,
                        @JsonProperty("amount") BigDecimal amount) {
        this.transactionId = transactionId;
        this.accountFromId = accountFromId;
        this.accountToId = accountToId;
        this.amount = amount;
    }

}
