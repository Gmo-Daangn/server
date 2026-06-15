package com.ktcloud.daangn.payment.entity;

import com.ktcloud.daangn.member.entity.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    @NotNull
    private String tranSeqNo;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PaymentStatus type;

    @NotNull
    private Long changedCash;

    @NotNull
    private Long balance;

    @NotNull
    private LocalDateTime localDateTime;
}
