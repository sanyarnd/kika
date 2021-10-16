package kika.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.Where;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "task")
public class Task extends AutoPersistableAuditable {
    @Column(name = "name")
    @Setter
    private String name;

    @Column(name = "description")
    @Setter
    private String description;

    @Column(name = "status")
    @Setter
    @Enumerated(EnumType.STRING)
    private Status status;

    @Setter
    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Task parent;

    @JoinColumn(name = "parent_id", referencedColumnName = "id")
//    @Where(clause = "parent_id is not null")
    @OneToMany(orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<Task> children;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id")
    private TaskList list;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountTaskAssignee> assignees;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AccountTaskSubscriber> subscribers;

    public Task(String name, String description, Task parent, TaskList list) {
        this.name = name;
        this.description = description;
        this.parent = parent;
        this.list = list;
        this.status = Status.NOT_COMPLETED;
    }

    /**
     * @return parent id or null if parent is null
     */
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
