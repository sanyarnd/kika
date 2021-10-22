package kika.domain;

import java.time.Instant;
import java.util.Set;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "account")
public class Account extends AutoPersistableAuditable {
    @Column(name = "name")
    private String name;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_exp")
    private Instant refreshTokenExpireAt;

    @OneToMany(mappedBy = "owner", orphanRemoval = true)
    private Set<Group> ownedGroups;

    @OneToMany(mappedBy = "account", orphanRemoval = true)
    private Set<AccountRole> accountRoles;

    @OneToMany(mappedBy = "account", orphanRemoval = true)
    private Set<AccountSpecialAccess> specialAccess;

    @OneToMany(mappedBy = "account", orphanRemoval = true)
    private Set<AccountTaskAssignee> assignedTasks;

    @OneToMany(mappedBy = "account", orphanRemoval = true)
    private Set<AccountTaskSubscriber> subscribedTasks;

    public Account(String name, Provider provider, String providerId) {
        this.name = name;
        this.provider = provider;
        this.providerId = providerId;
        this.refreshToken = "empty"; // doesn't matter, because expireAt must be invalidated anyway
        this.refreshTokenExpireAt = Instant.EPOCH;
    }

    public enum Provider {
        GOOGLE, GITHUB
    }
}
