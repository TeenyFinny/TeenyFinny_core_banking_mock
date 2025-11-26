package dev.syntax.domain.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 한국투자증권(KIS) 시세 API에서 조회한 단일 종목의 가격 정보를 담는 DTO.
 * <p>
 * 본 객체는 아래 서비스 계층에서 사용된다:
 * <ul>
 *     <li>{@link dev.syntax.domain.investment.service.StockMarketService} : KIS 응답을 DTO로 변환</li>
 *     <li>{@link dev.syntax.domain.investment.service.PortfolioService} : 실시간 평가금액 및 수익률 계산</li>
 * </ul>
 *
 * <h2> 사용 목적</h2>
 * <p>
 * StockMarketService는 PriceItem(KIS API 응답)을 기반으로 본 객체를 생성하며,
 * PortfolioService에서는 각 보유 종목의 다음 정보를 계산할 때 활용한다:
 * </p>
 *
 * <ul>
 *     <li>현재 평가금액(evaluationAmount)</li>
 *     <li>평가손익(profitAmount)</li>
 *     <li>수익률(profitRate)</li>
 *     <li>포트폴리오 내 비중(weight)</li>
 * </ul>
 *
 * <h2> KIS API 필드 매핑</h2>
 * <table border="1">
 *     <tr>
 *         <th>StockPrice 필드</th>
 *         <th>설명</th>
 *         <th>KIS API 필드명</th>
 *     </tr>
 *     <tr>
 *         <td>productCode</td>
 *         <td>종목 코드</td>
 *         <td>inter_shrn_iscd</td>
 *     </tr>
 *     <tr>
 *         <td>productName</td>
 *         <td>종목명</td>
 *         <td>inter_kor_isnm</td>
 *     </tr>
 *     <tr>
 *         <td>currentPrice</td>
 *         <td>현재가</td>
 *         <td>inter2_prpr</td>
 *     </tr>
 *     <tr>
 *         <td>diffAmount</td>
 *         <td>전일 대비 금액</td>
 *         <td>inter2_prdy_vrss</td>
 *     </tr>
 *     <tr>
 *         <td>diffRate</td>
 *         <td>전일 대비율 (%)</td>
 *         <td>prdy_ctrt</td>
 *     </tr>
 *     <tr>
 *         <td>volume</td>
 *         <td>누적 거래량</td>
 *         <td>acml_vol</td>
 *     </tr>
 * </table>
 *
 *
 * 본 DTO는 오직 시세정보 전달 및 포트폴리오 계산용 객체이며,
 * DB에는 저장하지 않는다.
 *
 *
 * @author hyunjung_yang
 * @see dev.syntax.external.kis.dto.PriceItem
 * @see dev.syntax.domain.investment.dto.HoldingItem
 */
@Data
@AllArgsConstructor
public class StockPrice {

    private String productCode;   // 종목코드 (e.g., 005930)
    private String productName;   // 종목명 (e.g., 삼성전자)
    private long currentPrice;    // 현재가 (KIS inter2_prpr)
    private long diffAmount;      // 전일 대비 금액 (KIS inter2_prdy_vrss)
    private double diffRate;      // 전일 대비율(%) (KIS prdy_ctrt)
    private long volume;          // 누적 거래량 (KIS acml_vol)
}
