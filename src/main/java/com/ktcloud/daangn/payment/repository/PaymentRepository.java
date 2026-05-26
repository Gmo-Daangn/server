package com.ktcloud.daangn.payment.repository;

import com.ktcloud.daangn.payment.entity.PaymentHistory;

public interface PaymentRepository {

    void save(PaymentHistory paymentHistory);

    Boolean existsByTranSeqNo(String tranSeqNo);
}
