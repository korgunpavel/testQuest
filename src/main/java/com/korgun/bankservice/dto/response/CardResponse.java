package com.korgun.bankservice.dto.response;

import com.korgun.bankservice.entity.CardStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Builder
public class CardResponse {
    private Long id;
    private String maskedCardNumber;
    private LocalDate expiryDate;
    private CardStatus status;
    private BigDecimal balance;
}
