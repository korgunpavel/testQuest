package com.korgun.bankservice.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class TransferRequest {
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
}
