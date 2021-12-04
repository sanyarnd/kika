import { List, Status } from "@/models";

export interface AssignedOrSubscribedTasksResponse {
  name: string;
  id: number;
  status: Status;
}

export interface CreateTaskPayload {
  name: string;
  description: string;
  listId: number | string;
  parentId: number | string | null;
}

export interface EditTaskPayload {
  id: number | string;
  name: string;
  description: string | null;
  parentId: number | string | null;
  listId: number | string | null;
}

export interface EditTaskListPayload {
  id: number | string;
  name: string;
  parentId: number | string | null;
  accessList: number[] | null;
}

export interface CreateTaskListPayload {
  name: string;
  groupId: number | string;
  parentId: number | string | null;
  accessList: number[] | null;
}

export interface GetGroupMemberResponse {
  id: number;
  name: string;
  role: GroupRole;
}

export interface GetGroupMembersResponse {
  members: GetGroupMemberResponse[];
  count: number;
}

export interface GetAccountResponse {
  id: number;
  name: string;
}

export interface GetAccountGroupResponse {
  id: number;
  name: string;
  role: GroupRole;
  ownerId: number;
}

export interface GetAccountGroupsResponse {
  groups: GetAccountGroupResponse[];
  count: number;
}

export type GroupRole = "OWNER" | "MEMBER" | "RESTRICTED";

export interface GetGroupListsResponse {
  lists: List[];
  count: number;
}

export interface GetGroupResponse {
  name: string;
  id: number;
  role: GroupRole;
  messageCount: number;
  ownerId: number;
}

export interface PostGroupMessagePayload {
  groupId: number;
  value: string;
}

export interface GetGroupMessagesPayload {
  groupId: number;
}

export interface GetGroupMessagesResponse {
  messages: GetMessage[];
  count: number;
}

export interface GetMessage {
  id: number;
  groupId: number;
  createdDate: string;
  body: string;
  sender: string;
}

export interface CreateGroupPayload {
  name: string;
  members: GetGroupMemberResponse[];
}

export interface ErrorResponse {
  title: string;
  status: number;
  detail: string;
}
