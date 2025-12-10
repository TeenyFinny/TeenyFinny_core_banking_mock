package dev.syntax.global.response.error;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum ErrorInvestmentCode implements ErrorBaseCodeForErrorCode{
    /**
     * 401 UNAUTHORIZED - 인증 관련
     */
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", "AUTH01"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 토큰입니다.", "AUTH02"),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "인증 토큰이 만료되었습니다.", "AUTH03"),

    /**
     * 403 FORBIDDEN - 권한 관련
     */
    FORBIDDEN(HttpStatus.FORBIDDEN, "해당 계좌에 대한 권한이 없습니다.", "AUTH04"),

    /**
     * 404 NOT FOUND - 계좌/종목 관련
     */
    ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "계좌를 찾을 수 없습니다.", "ACC01"),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 종목을 찾을 수 없습니다.", "STK01"),
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "포트폴리오 정보를 찾을 수 없습니다.", "PFT01"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "거래 내역을 찾을 수 없습니다.", "ORD01"),

    /**
     * 400 BAD REQUEST - 입력 오류 및 도메인 규칙 위반
     */
    INVALID_ORDER_QUANTITY(HttpStatus.BAD_REQUEST, "주문 수량이 올바르지 않습니다.", "ORD02"),
    INVALID_ORDER_PRICE(HttpStatus.BAD_REQUEST, "주문 가격이 올바르지 않습니다.", "ORD03"),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "예수금이 부족합니다.", "ACC02"),
    INSUFFICIENT_HOLDING(HttpStatus.BAD_REQUEST, "보유 수량이 부족합니다.", "PFT02"),
    INVALID_TRADE_TYPE(HttpStatus.BAD_REQUEST, "올바르지 않은 거래 유형입니다.", "ORD04"),

    /**
     * 409 CONFLICT - 거래 불가 상태
     */
    ORDER_ALREADY_EXECUTED(HttpStatus.CONFLICT, "이미 체결된 주문입니다.", "ORD05"),
    ORDER_ALREADY_CANCELLED(HttpStatus.CONFLICT, "이미 취소된 주문입니다.", "ORD06"),
    INVALID_ORDER(HttpStatus.CONFLICT, "잘못된 주문입니다.", "ORD07"),

    /**
     * 429 TOO MANY REQUESTS - 외부 API 제한
     */
    API_RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "외부 API 호출 제한을 초과했습니다.", "API01"),

    /**
     * 502 BAD GATEWAY - 외부 시스템 오류(KIS)
     */
    KIS_SERVER_ERROR(HttpStatus.BAD_GATEWAY, "증권사 서버 오류가 발생했습니다.", "KIS01"),
    KIS_COMMUNICATION_FAILED(HttpStatus.BAD_GATEWAY, "증권사 API 통신에 실패했습니다.", "KIS02"),

    /**
     * 500 INTERNAL SERVER ERROR - 시스템 내부 오류
     */
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "투자 서비스 내부 오류가 발생했습니다.", "SYS01"),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.", "SYS02");



    private final HttpStatus httpStatus;
    private final String message;
    private final String errorCode;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }
}
