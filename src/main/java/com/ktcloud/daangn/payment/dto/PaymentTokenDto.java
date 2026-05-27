package com.ktcloud.daangn.payment.dto;

import com.ktcloud.daangn.common.exception.InvalidInputException;
import org.springframework.http.HttpStatus;

import static java.lang.Long.*;

public record PaymentTokenDto(
        String tranSeqNo,
        Long amount,
        Long postId
) {
    public static PaymentTokenDto parse(String tx) {
        String[] parts = tx.split("_");
        if (parts.length != 3) throw new InvalidInputException(HttpStatus.BAD_REQUEST.value(), "잘못된 uri입니다.");

        return new PaymentTokenDto(
                parts[0],
                parseLong(parts[1]),
                parseLong(parts[2])
        );
    }
}
