package com.ktcloud.daangn.payment.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_links")
public class PaymentToken {

    @Id
    @Column(columnDefinition = "BINARY(16)")
    public UUID tranSeqNo;

    @Column(nullable = false)
    public Long postId;

    @Column(nullable = false)
    public Long sellerId;

    @Column(nullable = false)
    public Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public PaymentTokenStatus status;

    public void markAsCompleted() {
        this.status = PaymentTokenStatus.COMPLETED;
    }

    public boolean isCompleted() {
        return this.status == PaymentTokenStatus.COMPLETED;
    }
}

