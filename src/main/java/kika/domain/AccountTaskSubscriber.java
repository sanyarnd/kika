package kika.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "account_task_subscriber")
@NoArgsConstructor
public class AccountTaskSubscriber implements Serializable {
    @EmbeddedId
    private AccountTaskSubscriber.PK id;

    @ManyToOne
    @MapsId("accountId")
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "task_id")
    @MapsId("taskId")
    private Task task;

    public AccountTaskSubscriber(Task task, Account account) {
        this.id = new AccountTaskSubscriber.PK(task.getId(), account.getId());
        this.task = task;
        this.account = account;
    }

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @Getter
    @Setter
    public static class PK implements Serializable {
        @Column(name = "account_id")
        private Long accountId;

        @Column(name = "task_id")
        private Long taskId;

        public PK(long taskId, long accountId) {
            this.taskId = taskId;
            this.accountId = accountId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            AccountTaskSubscriber.PK that = (AccountTaskSubscriber.PK) o;
            return Objects.equals(accountId, that.accountId) && Objects.equals(taskId, that.taskId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, taskId);
        }
    }
}
