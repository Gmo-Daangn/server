package com.ktcloud.daangn.payment.repository;

import com.ktcloud.daangn.payment.entity.PaymentHistory;

import java.util.UUID;

public interface PaymentRepository {

    void save(PaymentHistory paymentHistory);

    Boolean existsByTranSeqNo(UUID tranSeqNo);
}
