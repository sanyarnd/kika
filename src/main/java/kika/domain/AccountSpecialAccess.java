package kika.domain;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
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
@Table(name = "task_list_account_special_access")
@NoArgsConstructor
@SuppressFBWarnings("FCCD_FIND_CLASS_CIRCULAR_DEPENDENCY")
public class AccountSpecialAccess implements Serializable {
    @EmbeddedId
    private PK id;

    @ManyToOne
    @MapsId("accountId")
    @JoinColumn(name = "account_id")
    private Account account;

    @ManyToOne
    @JoinColumn(name = "tasklist_id")
    @MapsId("tasklistId")
    private TaskList taskList;

    public AccountSpecialAccess(TaskList taskList, Account account) {
        this.id = new AccountSpecialAccess.PK(taskList.getId(), account.getId());
        this.taskList = taskList;
        this.account = account;
    }

    @Embeddable
    @NoArgsConstructor(access = AccessLevel.PUBLIC)
    @Getter
    @Setter
    public static class PK implements Serializable {
        @Column(name = "account_id")
        private Long accountId;

        @Column(name = "tasklist_id")
        private Long tasklistId;

        public PK(long tasklistId, long accountId) {
            this.tasklistId = tasklistId;
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
            AccountSpecialAccess.PK that = (AccountSpecialAccess.PK) o;
            return Objects.equals(accountId, that.accountId) && Objects.equals(tasklistId, that.tasklistId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(accountId, tasklistId);
        }
    }
}
