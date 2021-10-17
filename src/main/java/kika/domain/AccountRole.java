package kika.domain;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "account_group_role")
@NoArgsConstructor
public class AccountRole implements Serializable {
    @EmbeddedId
    private PK id;

    @ManyToOne
    @MapsId("accountId")
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @MapsId("groupId")
    @JoinColumn(name = "group_id")
    private Group group;

    @Enumerated(EnumType.STRING)
    private Role role;

    public AccountRole(Group group, Account owner) {
        this.id = new PK(group.getId(), owner.getId());
        this.group = group;
        this.account = owner;
        this.role = Role.OWNER;
    }

    public AccountRole(Group group, Account member, AccountRole.Role role) {
        this.id = new PK(group.getId(), member.getId());
        this.group = group;
        this.account = member;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AccountRole that = (AccountRole) o;
        return Objects.equals(account, that.account) && Objects.equals(group, that.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, group);
    }

    public enum Role {
        OWNER,
        MEMBER,
        RESTRICTED
    }

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @Getter
    @Setter
    public static class PK implements Serializable {
        @Column(name = "account_id")
        private Long accountId;

        @Column(name = "group_id")
        private Long groupId;

        public PK(long groupId, long accountId) {
            this.groupId = groupId;
            this.accountId = accountId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            PK that = (PK) o;
            return Objects.equals(accountId, that.accountId) && Objects.equals(groupId, that.groupId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, groupId);
        }
    }
}
