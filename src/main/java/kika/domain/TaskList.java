package kika.domain;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
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
    private Group group;

    @JoinColumn(name = "parent_id", referencedColumnName = "id")
    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<TaskList> children;

    @OneToMany(mappedBy = "list",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private Set<Task> tasks;

    @OneToMany(orphanRemoval = true,
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            mappedBy = "taskList")
//    @OnDelete(action = OnDeleteAction.CASCADE)
    private Set<AccountSpecialAccess> specialAccess;

    public Long getParentId() {
        return parent == null ? null : parent.safeId();
    }

    public void revokeSpecialAccessRecursively(long id) {
        if (specialAccess.removeIf(accSpAcc -> accSpAcc.getAccount().safeId() == id)) {
            children.forEach(child -> child.revokeSpecialAccessRecursively(id));
        }
    }

    public TaskList(String name, TaskList parent, Group group) {
        this.name = name;
        this.parent = parent;
        this.group = group;
    }
}
