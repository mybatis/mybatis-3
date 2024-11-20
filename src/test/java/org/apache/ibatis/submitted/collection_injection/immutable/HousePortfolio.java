package org.apache.ibatis.submitted.collection_injection.immutable;

import java.util.List;

public class HousePortfolio {

  private int portfolioId;
  private List<ImmutableHouse> houses;

  public int getPortfolioId() {
    return portfolioId;
  }

  public void setPortfolioId(int portfolioId) {
    this.portfolioId = portfolioId;
  }

  public List<ImmutableHouse> getHouses() {
    return houses;
  }

  public void setHouses(List<ImmutableHouse> houses) {
    this.houses = houses;
  }
}
