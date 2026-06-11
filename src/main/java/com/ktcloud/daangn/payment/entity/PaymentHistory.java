package com.ktcloud.daangn.payment.entity;

import com.ktcloud.daangn.member.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "payment_history",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_tran_type",
                columnNames = {"tran_seq_no","type"}
        )
)
public class PaymentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    private String tranSeqNo;

    @Enumerated(EnumType.STRING)
    private PaymentStatus type;

    private Long changedCash;

    private Long balance;

    private LocalDateTime localDateTime;
}
