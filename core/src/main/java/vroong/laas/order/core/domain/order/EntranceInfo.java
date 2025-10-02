package vroong.laas.order.core.domain.order;

public record EntranceInfo(String password, String guide, String requestMessage) {

  public static EntranceInfo empty() {
    return new EntranceInfo(null, null, null);
  }
}

