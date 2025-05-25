package com.korgun.bankservice.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CardRequest {
    private String cardNumber;
    private LocalDate expiryDate;
    private BigDecimal balance;
}
