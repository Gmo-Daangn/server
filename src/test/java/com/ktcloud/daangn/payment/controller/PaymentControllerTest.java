package com.ktcloud.daangn.payment.controller;


import com.github.f4b6a3.uuid.UuidCreator;
import com.ktcloud.daangn.auth.authority.SecurityConfig;
import com.ktcloud.daangn.auth.dto.CustomUser;
import com.ktcloud.daangn.auth.jwt.JwtAccessDeniedHandler;
import com.ktcloud.daangn.auth.jwt.JwtAuthenticationEntryPoint;
import com.ktcloud.daangn.auth.jwt.JwtTokenProvider;
import com.ktcloud.daangn.payment.dto.PaymentInitRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentRequestDto;
import com.ktcloud.daangn.payment.dto.PaymentResponseDto;
import com.ktcloud.daangn.payment.dto.PaymentTokenDto;
import com.ktcloud.daangn.payment.service.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(PaymentController.class)
@Import({
        SecurityConfig.class,
        JwtAuthenticationEntryPoint.class,
        JwtAccessDeniedHandler.class
})
@ExtendWith(RestDocumentationExtension.class)
public class PaymentControllerTest {

    private MockMvc mockMvc;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JsonMapper jsonMapper;

    @MockitoBean
    private PaymentService paymentService;

    private static final Long TO_MEMBER_ID = 1L;
    private static final Long FROM_MEMBER_ID = 2L;
    private static final Long TRAN_AMT = 5000L;
    private static final Long POST_ID = 1L;
    private static final Long ROOM_ID = 1L;

    @BeforeEach
    void setUp(WebApplicationContext context, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation))
                .apply(springSecurity())
                .build();
        this.customUser = new CustomUser(
                TO_MEMBER_ID,
                "test@test.com",
                "password",
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }

    CustomUser customUser;

    @Nested
    @DisplayName("입금")
    class deposit{

        @Test
        @DisplayName("[HAPPY] 은행으로부터 입급 요청이 올 경우 정상적으로 반환된다.")
        public void deposit_ValidMember_Success() throws Exception {
            //given
            PaymentRequestDto requestDto = new PaymentRequestDto(
                    UuidCreator.getTimeOrderedEpoch(),
                    TRAN_AMT,
                    TO_MEMBER_ID
            );
            PaymentResponseDto responseDto = new PaymentResponseDto("name", TRAN_AMT);
            given(paymentService.deposit(any(PaymentRequestDto.class))).willReturn(responseDto);

            //when & then
            mockMvc.perform(post("/api/v1/payments/deposit")                  // 1) POST, 엔드포인트
                                    .accept(MediaType.APPLICATION_JSON_VALUE)
                                    .contentType(APPLICATION_JSON_VALUE)           // 2) JSON 바디
                                    .content(jsonMapper.writeValueAsString(requestDto))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("name"))
                    .andExpect(jsonPath("$.data.balance").value(TRAN_AMT))
                    .andDo(document("deposit-callback-success",
                            requestFields(
                                    fieldWithPath("tranSeqNo").description("거래 ID (UUID)"),
                                    fieldWithPath("tranAmt").description("입금 금액"),
                                    fieldWithPath("memberId").description("입금 받을 회원 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시각"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data.name").description("입금된 회원 닉네임"),
                                    fieldWithPath("data.balance").description("입금된 후 잔액")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("출금")
    class withdraw{

        @Test
        @DisplayName("[HAPPY] 은행으로부터 출금 요청이 올 경우 정상적으로 반환된다.")
        public void withdraw_ValidMember_Success() throws Exception {
            //given
            PaymentRequestDto requestDto = new PaymentRequestDto(
                    UuidCreator.getTimeOrderedEpoch(),
                    TRAN_AMT,
                    TO_MEMBER_ID
            );
            PaymentResponseDto responseDto = new PaymentResponseDto("name", 0L);
            given(paymentService.withdraw(any(PaymentRequestDto.class))).willReturn(responseDto);

            //when & then
            mockMvc.perform(post("/api/v1/payments/withdraw")                  // 1) POST, 엔드포인트
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .contentType(APPLICATION_JSON_VALUE)           // 2) JSON 바디
                            .content(jsonMapper.writeValueAsString(requestDto))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("name"))
                    .andExpect(jsonPath("$.data.balance").value(0L))
                    .andDo(document("withdraw-callback-success",
                            requestFields(
                                    fieldWithPath("tranSeqNo").description("거래 ID (UUID)"),
                                    fieldWithPath("tranAmt").description("출금 금액"),
                                    fieldWithPath("memberId").description("출금된 회원 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시각"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data.name").description("출금된 회원 닉네임"),
                                    fieldWithPath("data.balance").description("출금된 후 잔액")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("거래 링크 생성")
    class createPaymentLink {

        @Test
        @DisplayName("[HAPPY] 인증된 사용자가 거래 링크 생성 요청 시 성공한다.")
        public void createPaymentLink_ValidMember_Success() throws Exception {
            //given
            PaymentInitRequestDto requestDto = new PaymentInitRequestDto(
                    ROOM_ID,
                    POST_ID,
                    TRAN_AMT
            );
            doNothing().when(paymentService).requestPayment(anyLong(), any(PaymentInitRequestDto.class));

            //when & then
            mockMvc.perform(post("/api/v1/payments/links")
                            .with(SecurityMockMvcRequestPostProcessors.user(customUser))
                            .header("Authorization", "Bearer {accessToken}")
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .contentType(APPLICATION_JSON_VALUE)
                            .content(jsonMapper.writeValueAsString(requestDto))
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").value("거래 생성 성공"))
                    .andDo(document("payment-link-create-success",
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 액세스 토큰")
                            ),
                            requestFields(
                                    fieldWithPath("roomId").description("거래중인 채팅방 ID"),
                                    fieldWithPath("amount").description("거래 금액"),
                                    fieldWithPath("postId").description("물품 게시물 ID")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시각"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data").description("결과 메시지")
                            )
                    ));

            verify(paymentService).requestPayment(eq(TO_MEMBER_ID), any(PaymentInitRequestDto.class));
        }
    }

    @Nested
    @DisplayName("거래 토큰 조회")
    class getTokenInfo {

        @Test
        @DisplayName("[HAPPY] 거래 ID로 토큰 정보를 정상 조회한다.")
        public void getTokenInfo_ValidTx_Success() throws Exception {
            //given
            UUID tx = UuidCreator.getTimeOrderedEpoch();
            PaymentTokenDto tokenDto = new PaymentTokenDto(tx, TRAN_AMT, POST_ID);
            given(paymentService.getTokenInfo(any(UUID.class))).willReturn(tokenDto);

            //when & then
            mockMvc.perform(get("/api/v1/payments/links/{tx}", tx)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andDo(document("payment-token-detail-success",
                            pathParameters(
                                    parameterWithName("tx").description("거래 ID (UUID)")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시각"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data.tranSeqNo").description("거래 코드"),
                                    fieldWithPath("data.amount").description("거래 금액"),
                                    fieldWithPath("data.postId").description("물품 게시물 ID")
                            )
                    ));
        }
    }

    @Nested
    @DisplayName("거래 확정")
    class confirmPayment {

        @Test
        @DisplayName("[HAPPY] 인증된 사용자가 거래 확정 요청 시 정상 처리된다.")
        public void confirmPayment_ValidMember_Success() throws Exception {
            //given
            UUID tx = UuidCreator.getTimeOrderedEpoch();
            PaymentResponseDto responseDto = new PaymentResponseDto("name", TRAN_AMT);
            given(paymentService.confirmPayment(anyLong(), any(UUID.class))).willReturn(responseDto);

            //when & then
            mockMvc.perform(post("/api/v1/payments/links/{tx}", tx)
                            .with(SecurityMockMvcRequestPostProcessors.user(customUser))
                            .header("Authorization", "Bearer {accessToken}")
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                    )
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.name").value("name"))
                    .andExpect(jsonPath("$.data.balance").value(TRAN_AMT))
                    .andDo(document("payment-confirm-success",
                            requestHeaders(
                                    headerWithName("Authorization").description("Bearer 액세스 토큰")
                            ),
                            pathParameters(
                                    parameterWithName("tx").description("거래 ID (UUID)")
                            ),
                            responseFields(
                                    fieldWithPath("code").description("HTTP 상태 코드"),
                                    fieldWithPath("localDateTime").description("응답 시각"),
                                    fieldWithPath("message").description("응답 메시지"),
                                    fieldWithPath("data.name").description("회원 닉네임"),
                                    fieldWithPath("data.balance").description("거래 후 잔액")
                            )
                    ));

            verify(paymentService).confirmPayment(eq(TO_MEMBER_ID), eq(tx));
        }
    }
}
