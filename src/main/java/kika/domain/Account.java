package kika.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.util.Set;

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
