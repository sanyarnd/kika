package kika.domain;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "account")
public class Account extends AutoPersistableAuditable {
    @Column(name = "name")
    private String name;

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

    public Account(String name) {
        this.name = name;
    }
}
