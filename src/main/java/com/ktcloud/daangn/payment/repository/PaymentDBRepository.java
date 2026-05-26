package com.ktcloud.daangn.payment.repository;

import com.ktcloud.daangn.payment.entity.PaymentHistory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentDBRepository implements PaymentRepository{

    private final EntityManager em;

    @Override
    public void save(PaymentHistory paymentHistory) {
        em.persist(paymentHistory);
    }

    @Override
    public Boolean existsByTranSeqNo(String tranSeqNo) {
        return !em.createQuery("select p from PaymentHistory p where p.tranSeqNo = :tranSeqNo", PaymentHistory.class)
                .setParameter("tranSeqNo", tranSeqNo)
                .getResultList()
                .isEmpty();
    }
}
