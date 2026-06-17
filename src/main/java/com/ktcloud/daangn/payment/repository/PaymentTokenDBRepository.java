package com.ktcloud.daangn.payment.repository;

import com.ktcloud.daangn.payment.entity.PaymentToken;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class PaymentTokenDBRepository implements PaymentTokenRepository {

    private final EntityManager em;

    @Override
    public void save(PaymentToken paymentToken) {
        em.persist(paymentToken);
    }

    @Override
    public Optional<PaymentToken> getToken(UUID tranSeqNo) {
        return Optional.ofNullable(em.find(PaymentToken.class, tranSeqNo));
    }
}
