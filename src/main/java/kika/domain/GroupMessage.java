package kika.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Table(name = "group_message")
public class GroupMessage extends AutoPersistableAuditable {
    @Column(name = "body")
    private String body;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.NO_ACTION)
    private Group group;
}
