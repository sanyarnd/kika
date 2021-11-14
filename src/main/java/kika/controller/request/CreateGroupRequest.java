package kika.controller.request;

import org.hibernate.validator.constraints.Length;

public record CreateGroupRequest(
    @Length(min = 1, max = 128)
    String name,
    long ownerId
) {
}
