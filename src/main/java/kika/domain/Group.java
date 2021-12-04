package kika.domain;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "group")
public class Group extends AutoPersistableAuditable {
    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Account owner;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountRole> members;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GroupMessage> messages;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskList> taskLists;

    public Group(String name, Account owner) {
        this.name = name;
        this.owner = owner;
        this.members = new HashSet<>();
    }

    public void addMember(AccountRole member) {
        members.add(member);
    }

    public void removeMember(AccountRole member) {
        members.remove(member);
    }

    public AccountRole.Role getRole(long id) {
        return this.getMembers().stream()
            .filter(accountRole -> accountRole.getAccount().safeId() == id)
            .findFirst()
            .map(AccountRole::getRole)
            .orElse(null);
    }

    public AccountRole getMember(long id) {
        return this.getMembers().stream()
            .filter(accountRole -> accountRole.getAccount().safeId() == id)
            .findFirst()
            .orElse(null);
    }

    public Set<TaskList> rootLists() {
        return getTaskLists().stream().filter(taskList -> taskList.getParent() == null).collect(Collectors.toSet());
    }
}
