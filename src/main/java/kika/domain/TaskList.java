package kika.domain;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Entity
@Table(name = "task_list")
public class TaskList extends AutoPersistableAuditable {
    @Column(name = "name")
    private String name;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private TaskList parent;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Group group;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TaskList> children = new HashSet<>();

    @OneToMany(mappedBy = "list", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "taskList", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountSpecialAccess> specialAccess = new HashSet<>();

    public TaskList(String name, TaskList parent, Group group) {
        this.name = name;
        this.parent = parent;
        this.group = group;
    }

    public Long getParentId() {
        return parent == null ? null : parent.safeId();
    }

    public boolean accountHasAccess(long accountId) {
        if (this.getSpecialAccess().isEmpty()) {
            if (this.getParent() != null) {
                return this.getParent().accountHasAccess(accountId);
            } else {
                return this.getGroup().getMembers().stream()
                    .anyMatch(accountRole -> accountRole.getAccount().safeId() == accountId);
            }
        } else {
            return this.getSpecialAccess().stream()
                .anyMatch(accountSpecialAccess -> accountSpecialAccess.getAccount().safeId() == accountId);
        }
    }

    public boolean hasChild(long listId) {
        return this.getChildren().stream().anyMatch(child -> child.safeId() == listId || child.hasChild(listId));
    }
}
