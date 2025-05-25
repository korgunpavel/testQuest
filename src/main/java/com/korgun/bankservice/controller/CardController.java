package com.korgun.bankservice.controller;

import com.korgun.bankservice.dto.request.CardRequest;
import com.korgun.bankservice.dto.request.TransferRequest;
import com.korgun.bankservice.dto.response.CardResponse;
import com.korgun.bankservice.entity.Card;
import com.korgun.bankservice.entity.CardStatus;
import com.korgun.bankservice.entity.User;
import com.korgun.bankservice.repository.UserRepository;
import com.korgun.bankservice.security.CardCryptoUtils;
import com.korgun.bankservice.security.UserPrincipal;
import com.korgun.bankservice.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final UserRepository userRepository;

    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CardResponse>> getAllCards(Pageable pageable) {
        return ResponseEntity.ok(
                cardService.getAllCards(pageable)
                        .map(card -> toResponse(card, card.getEncryptedCardNumber()))
        );
    }

    @GetMapping
    public ResponseEntity<Page<CardResponse>> getMyCards(@AuthenticationPrincipal UserPrincipal principal,
                                                         Pageable pageable) {

        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Page<CardResponse> cards = cardService.getCardsForUser(user, pageable)
                .map(card -> {
                    try {
                        return toResponse(card, CardCryptoUtils.decrypt(card.getEncryptedCardNumber()));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        return ResponseEntity.ok(cards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardResponse> getCardById(@PathVariable("id") Long id,
                                                    @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Optional<Card> cardOpt = cardService.getByIdForUser(id, user);

        if (cardOpt.isPresent()) {
            Card card = cardOpt.get();
            try {
                String decrypted = CardCryptoUtils.decrypt(card.getEncryptedCardNumber());
                CardResponse response = toResponse(card, decrypted);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                return ResponseEntity.status(500).body(null);
            }
        } else {
            return ResponseEntity.status(403).body(null);
        }
    }

    @PostMapping
    public ResponseEntity<CardResponse> createCard(@RequestBody CardRequest request,
                                                   @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String encrypted = CardCryptoUtils.encrypt(request.getCardNumber());

        Card card = Card.builder()
                .encryptedCardNumber(encrypted)
                .expiryDate(request.getExpiryDate())
                .status(CardStatus.ACTIVE)
                .balance(request.getBalance())
                .owner(user)
                .build();

        Card saved = cardService.createCard(card);

        return ResponseEntity.ok(toResponse(saved, request.getCardNumber()));
    }

    private CardResponse toResponse(Card card, String originalCardNumber) {
        return CardResponse.builder()
                .id(card.getId())
                .expiryDate(card.getExpiryDate())
                .status(card.getStatus())
                .balance(card.getBalance())
                .maskedCardNumber(CardCryptoUtils.mask(originalCardNumber))
                .build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request,
                                      @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            cardService.transferBetweenCards(
                    request.getFromCardId(),
                    request.getToCardId(),
                    request.getAmount(),
                    user.getId());
            return ResponseEntity.ok("Transfer completed");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/block")
    public ResponseEntity<?> blockCard(@PathVariable("id") Long id,
                                       @AuthenticationPrincipal UserPrincipal principal) {
        User user = userRepository.findById(principal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cardService.getByIdForUser(id, user)
                .map(card -> {
                    card.setStatus(CardStatus.BLOCKED);
                    cardService.createCard(card); // save
                    return ResponseEntity.ok("Card blocked successfully");
                })
                .orElse(ResponseEntity.status(403).body("Card not found or not owned by user"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCard(@PathVariable("id") Long id) {
        try {
            cardService.deleteCard(id);
            return ResponseEntity.ok("Card deleted successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting card: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body("Failed to delete card: " + e.getMessage());
        }
    }

}
