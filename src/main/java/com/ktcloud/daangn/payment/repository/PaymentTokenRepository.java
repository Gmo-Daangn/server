package com.ktcloud.daangn.payment.repository;

import com.ktcloud.daangn.payment.entity.PaymentToken;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTokenRepository {

    void save(PaymentToken paymentToken);

    Optional<PaymentToken> getToken(UUID tranSeqNo);
}
