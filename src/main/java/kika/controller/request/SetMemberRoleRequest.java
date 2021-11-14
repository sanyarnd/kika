package kika.controller.request;

import kika.domain.AccountRole;

public record SetMemberRoleRequest(
    AccountRole.Role role
) {
}
