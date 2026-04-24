package com.ktcloud.daangn.member.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String Location; // ERD를 통해서 테이블 분리 혹은 외부 api를 통한 값 적용

    @Enumerated(EnumType.STRING)
    private MemberRole memberRole;
    private Double manner;
    private String profileImageUrl;
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @Enumerated(EnumType.STRING)
    private ProviderToken providerToken;
    private Long balance;
}
