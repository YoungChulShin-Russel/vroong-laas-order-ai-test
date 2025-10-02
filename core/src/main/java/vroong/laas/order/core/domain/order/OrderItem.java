package vroong.laas.order.core.domain.order;

import vroong.laas.order.core.domain.shared.Money;
import vroong.laas.order.core.domain.shared.Volume;
import vroong.laas.order.core.domain.shared.Weight;

public record OrderItem(
    String itemName, int quantity, Money price, String category, Weight weight, Volume volume) {

  public OrderItem {
    if (itemName == null || itemName.isBlank()) {
      throw new IllegalArgumentException("상품명은 필수입니다");
    }
    if (quantity <= 0) {
      throw new IllegalArgumentException("수량은 1개 이상이어야 합니다");
    }
    if (price == null) {
      throw new IllegalArgumentException("가격은 필수입니다");
    }
  }

  public Money getTotalPrice() {
    return price.multiply(quantity);
  }
}

