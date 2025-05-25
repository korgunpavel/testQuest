package com.korgun.bankservice.service;

import com.korgun.bankservice.entity.Card;
import com.korgun.bankservice.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.math.BigDecimal;
import java.util.Optional;

public interface CardService {
    Card createCard(Card card);
    Page<Card> getAllCards(Pageable pageable);
    Page<Card> getCardsForUser(User user, Pageable pageable);
    Optional<Card> getByIdForUser(Long id, User user);
    void transferBetweenCards(Long fromId, Long toId, BigDecimal amount, Long userId);
    void deleteCard(Long id);
    Card updateCardStatus(Long id, String status);
}
