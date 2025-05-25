package com.korgun.bankservice.service.impl;

import com.korgun.bankservice.entity.Card;
import com.korgun.bankservice.entity.CardStatus;
import com.korgun.bankservice.entity.User;
import com.korgun.bankservice.repository.CardRepository;
import com.korgun.bankservice.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    @Transactional
    @Override
    public Card createCard(Card card) {
        return cardRepository.save(card);
    }

    @Override
    public Page<Card> getAllCards(Pageable pageable) {
        return cardRepository.findAll(pageable);
    }

    @Override
    public Page<Card> getCardsForUser(User user, Pageable pageable) {
        return cardRepository.findByOwner(user, pageable);
    }

    @Override
    public Optional<Card> getByIdForUser(Long id, User user) {
        return cardRepository.findById(id)
                .filter(card -> card.getOwner().getId().equals(user.getId()));
    }

    @Transactional
    @Override
    public void transferBetweenCards(Long fromId, Long toId, BigDecimal amount, Long userId) {
        if (fromId.equals(toId)) {
            throw new IllegalArgumentException("Cannot transfer to the same card");
        }

        Card from = cardRepository.findByIdAndOwnerWithLock(fromId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Source card not found or not owned by user"));

        Card to = cardRepository.findByIdWithLock(toId)
                .orElseThrow(() -> new IllegalArgumentException("Destination card not found"));

        if (from.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance");
        }

        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        cardRepository.save(from);
        cardRepository.save(to);
        cardRepository.flush();

        log.info("Transfer completed: {} from card {} to card {}", amount, fromId, toId);
    }

    @Transactional
    @Override
    public void deleteCard(Long id) {
        try {
            if (!cardRepository.existsById(id)) {
                throw new EntityNotFoundException("Card not found with id: " + id);
            }
            cardRepository.deleteById(id);
            log.info("Card with id {} successfully deleted", id);
        } catch (Exception e) {
            log.error("Failed to delete card with id {}: {}", id, e.getMessage());
            throw e; // Пробрасываем исключение дальше
        }
    }

    @Transactional
    @Override
    public Card updateCardStatus(Long id, String status) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));
        card.setStatus(CardStatus.valueOf(status.toUpperCase()));
        return cardRepository.save(card);
    }
}
