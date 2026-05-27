package com.ktcloud.daangn.member.entity;

import com.ktcloud.daangn.common.exception.InvalidInputException;
import com.ktcloud.daangn.common.valueObject.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "members")
public class Member {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;
    private String password;
    private String nickName;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;
    private Double manner;
    private String profileImageUrl;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @Enumerated(EnumType.STRING)
    private ProviderToken providerToken;
    private Long balance;

    public void changeBalance(boolean add, Long cash){
        if (cash == null || cash < 0L) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "입력값이 잘못되었습니다.");
        else if (this.balance - cash < 0L) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "잔액이 부족합니다.");

        if (add) this.balance += cash;
        else this.balance -= cash;
    }
}
