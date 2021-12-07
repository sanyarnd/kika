import { GetGroupMemberResponse, GroupRole } from "@/backend/dto";

export interface NavbarItem {
  id: number;
  name: string;
  type: ParentInfoType;
}

export interface Account {
  id: number;
  name: string;
}

export interface UserSpecialAccess {
  id: number;
  name: string;
  hasAccess: boolean;
}

export interface AccessData {
  set: boolean;
  accounts: UserSpecialAccess[];
}

export type Status = "COMPLETED" | "NOT_COMPLETED" | "DELETED";

export interface GroupTree {
  id: number;
  name: string;
  lists: SubTaskListWithChildren[];
}

export interface SubTaskListWithChildren {
  id: number;
  name: string;
  children: SubTaskListWithChildren[];
  tasks: SubTaskWithChildren[];
}

export interface SubTaskWithChildren {
  id: number;
  name: string;
  children: SubTaskWithChildren[];
}

export interface GroupInfo {
  id: number;
  name: string;
  role: GroupRole;
  lists: SubTaskList[];
  messages: MessageBulk;
}

export interface GroupEditInfo {
  id: number;
  name: string;
  members: GetGroupMemberResponse[];
  messageCount: number;
}

export interface ConciseGroup {
  id: number;
  name: string;
  lists: ConciseList[];
  members: AccessData;
}

export interface ConciseList {
  id: number;
  name: string;
  children: ConciseList[];
  tasks: ConciseTask[];
}

export interface ConciseTask {
  id: number;
  name: string;
  children: ConciseTask[];
}

export interface MessageBulk {
  messages: SubMessage[];
  count: number;
  offset: number;
}

export interface SubMessage {
  id: number;
  createdDate: string;
  body: string;
  sender: string;
}

export interface ListEditInfo {
  id: number;
  name: string;
  group: SubGroup;
  parent: SubTaskList | null;
  accessData: AccessData;
}

export interface ListInfo {
  id: number;
  name: string;
  group: SubGroup;
  parent: SubTaskList | null;
  children: SubTaskList[];
  tasks: ChildTask[];
}

export interface TaskEditInfo {
  id: number;
  name: string;
  description: string;
  parent: ParentTask | null;
  list: SubTaskListWithGroup;
}

export interface TaskInfo {
  id: number;
  name: string;
  description: string;
  status: Status;
  parent: ParentTask | null;
  children: ChildTask[];
  list: SubTaskList;
  group: SubGroup;
  assigned: boolean;
  subscribed: boolean;
}

export interface ChildTask {
  id: number;
  name: string;
  status: Status;
}

export interface ParentTask {
  id: number;
  name: string;
}

export interface SubTaskList {
  id: number;
  name: string;
}

export interface SubTaskListWithGroup {
  id: number;
  name: string;
  groupId: number;
}

export interface SubGroup {
  id: number;
  name: string;
  role: GroupRole;
}

export interface Task {
  id: number;
  name: string;
  description: string;
  status: Status;
  children: Task[];
  listId: number;
  parentId: number | null;
}

export interface List {
  name: string;
  id: number;
  children: List[];
  tasks: Task[];
  groupId: number;
  parentId: number | null;
}

export interface Group {
  id: number;
  name: string;
  ownerId: number;
  role: GroupRole;
  lists: List[];
}

export type ParentInfoType = "TASK" | "LIST" | "GROUP";

export interface FrozenElem {
  id: number;
  type: ParentInfoType;
}

export interface ElemInfo {
  id: number | null;
  object: null | ConciseGroup | ConciseList | ConciseTask;
  type: ParentInfoType | null;
}

export interface MoveElemInfo {
  object: Group | List | Task | null | SubGroup | SubTaskList;
  type: ParentInfoType | null;
}
