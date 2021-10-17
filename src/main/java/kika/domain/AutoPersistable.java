package kika.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.util.ProxyUtils;

/**
 * Base class for entities with ID field, cloned [org.springframework.data.jpa.domain.AbstractPersistable].
 * <p>
 * Uses autoincrement database feature.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AutoPersistable implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Transient // DATAJPA-622
    public boolean isNew() {
        return id == null;
    }

    /**
     * Type-safe access to [id] field.
     *
     * @throws IllegalStateException if id is null
     */
    @Transient
    public long safeId() {
        if (id == null) {
            throw new IllegalStateException(String.format("Entity %s has no id value", this));
        }
        return id;
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d}", this.getClass().getName(), id);
    }

    @Override
    @SuppressFBWarnings("BC_EQUALS_METHOD_SHOULD_WORK_FOR_ALL_OBJECTS")
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (this.getClass() != ProxyUtils.getUserClass(o)) {
            return false;
        }
        return null != id && id.equals(((AutoPersistable) o).id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return 17;
        }
        return 17 + id.hashCode() * 31;
    }
}
