package kika.domain;

import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.jetbrains.annotations.Nullable;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "task")
public class Task extends AutoPersistableAuditable {
    @Setter
    @Column(name = "name")
    private String name;

    @Setter
    @Column(name = "description")
    private String description;

    @Setter
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private Status status;

    @Setter
    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Task parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> children;

    @Setter
    @ManyToOne
    @JoinColumn(name = "list_id")
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private TaskList list;

    @OneToMany(mappedBy = "task", orphanRemoval = true)
    private Set<AccountTaskAssignee> assignees;

    @OneToMany(mappedBy = "task", orphanRemoval = true)
    private Set<AccountTaskSubscriber> subscribers;

    public Task(String name, String description, Task parent, TaskList list) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.list = list;
        this.status = Status.NOT_COMPLETED;
    }

    @Nullable
    public Long getParentId() {
        return parent == null ? null : parent.safeId();
    }

    public void moveChildrenIntoList(TaskList list) {
        children.forEach(child -> {
            child.setList(list);
            child.moveChildrenIntoList(list);
        });
    }

    public enum Status {
        NOT_COMPLETED,
        COMPLETED,
        DELETED
    }
}
