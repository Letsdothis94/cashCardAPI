package com.example.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
public class CashCardController {
    private final CashCardRepository cashCardRepository;

    private CashCardController(CashCardRepository cashCardRepository) {
        this.cashCardRepository = cashCardRepository;
    }

    // Method to reduce duplicating code
    private CashCard findCashCard(Long requestedId, Principal principal) {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }

    // AUTHENTICATION BY USING PRINCIPAL(current user logged in)
    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);
//        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
        if (cashCard != null) {
            return ResponseEntity.ok(cashCard);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
//    @GetMapping("/{requestedId}")
//    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
//        Optional<CashCard> cashCardOptional = Optional.ofNullable(cashCardRepository.findByIdAndOwner(requestedId, principal.getName()));
//        if (cashCardOptional.isPresent()) {
//            System.out.println("Current user: " + principal.getName());
//            return ResponseEntity.ok(cashCardOptional.get());
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

    @GetMapping
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwner(
                principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashWithOwner = new CashCard(null, newCashCardRequest.amount(), principal.getName());
        CashCard savedCashCard = cashCardRepository.save(cashWithOwner);
        URI locationOfNewCashCard = ucb.path("/cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal) {
        CashCard cashCard = findCashCard(requestedId, principal);
//        CashCard cashCard = cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
        if (cashCard != null) {
            CashCard updatedCashCard = new CashCard(cashCard.id(), cashCardUpdate.amount(), principal.getName());
            cashCardRepository.save(updatedCashCard);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        if (cashCardRepository.existsByIdAndOwner(id, principal.getName())) {
            cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Methods without authorization required

//    @GetMapping("/{requestedId}")
//    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
//        Optional<CashCard> cashCardOptional = cashCardRepository.findById(requestedId);
//        if (cashCardOptional.isPresent()) {
//            return ResponseEntity.ok(cashCardOptional.get());
//        } else {
//            return ResponseEntity.notFound().build();
//        }
//    }

//    @GetMapping
//    private ResponseEntity<List<CashCard>> findAll(Pageable pageable) {
//        Page<CashCard> page = (Page<CashCard>) cashCardRepository.findAll(
//                PageRequest.of(
//                        pageable.getPageNumber(),
//                        pageable.getPageSize(),
//                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
//                ));
//        return ResponseEntity.ok(page.getContent());
//    }

//    @PostMapping
//    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb) {
//        CashCard savedCashCard = cashCardRepository.save(newCashCardRequest);
//        URI locationOfNewCashCard = ucb.path("/cashcards/{id}").buildAndExpand(savedCashCard.id()).toUri();
//        return ResponseEntity.created(locationOfNewCashCard).build();
//    }
}
