package vroong.laas.order.infrastructure.storage.db;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.ToString;

@MappedSuperclass
@Getter
@ToString(callSuper = true)
public abstract class ConcurrentEntity extends BaseEntity {

  @Version
  @Column(name = "version", nullable = false)
  private Long version;
}