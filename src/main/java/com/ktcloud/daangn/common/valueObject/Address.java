package com.ktcloud.daangn.common.valueObject;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;

@Embeddable
public record Address(
        @NotBlank
        String city,
        @NotBlank
        String district,
        @NotBlank
        String town
) {
}
