package dev.syntax.domain.investment.service;

import dev.syntax.domain.investment.dto.res.DashboardPortfolioRes;
import dev.syntax.domain.investment.dto.res.InvestAccountPortfolioRes;
import dev.syntax.domain.investment.dto.res.PortfolioRes;

public interface InvestmentPortfolioService {
    /**
     * 1. 포트폴리오 상세 조회 API
     * 상위 3개 + 기타 포함
     */
    PortfolioRes getPortfolio(String cano, Long userId);

    /**
     * 2. 계좌 조회 API
     * 보유 종목 전체 + 평가 정보(상위3 불필요)
     */
    InvestAccountPortfolioRes getAccountPortfolio(String cano, Long userId);

    /**
     * 3. 대시보드 조회 API
     * 보유 종목 상위 3개만
     */
    DashboardPortfolioRes getDashboardPortfolio(String cano, Long userId);
}
