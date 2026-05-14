package com.ktcloud.daangn.chat.dto;

import jakarta.validation.constraints.NotNull;

/*TODO: 클라이언트 제공 memberId를 인가에 사용하면 Broken Access Control 취약점이 발생합니다.
        요청 본문에서 받은 memberId는 공격자가 임의로 다른 사용자의 ID를 입력해 타인의 메시지를 삭제할 수 있게 합니다. 서버 측에서는 인증된 사용자 컨텍스트(Spring Security의 Authentication 또는 JWT 클레임)에서 현재 회원 ID를 직접 추출해야 하며, 클라이언트 입력에 의존해서는 안 됩니다.
        이 패턴은 이번 PR 내 다른 요청 DTO에도 동일하게 적용됩니다(ChatMessageWriteRequestDto 등의 memberId 필드).
*/
public record ChatMessageDeleteRequestDto(
        @NotNull(message = "회원 ID는 비어 있을 수 없습니다.")
        Long memberId
) {
}
