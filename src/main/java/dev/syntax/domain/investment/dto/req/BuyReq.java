package dev.syntax.domain.investment.dto.req;

import lombok.Data;

@Data
public class BuyReq {
    private String cano;          // 계좌번호
    private Long userId;          // 사용자 ID
    private String productCode;   // 종목코드
    private String productName;   // 종목명
    private int quantity;         // 매수수량
    private long price;           // 매수가격(단가)
}
