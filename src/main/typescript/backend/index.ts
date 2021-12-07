import {
  AssignedOrSubscribedTasksResponse,
  CreateGroupPayload,
  CreateTaskListPayload,
  CreateTaskPayload,
  EditTaskListPayload,
  EditTaskPayload,
  GetAccountGroupsResponse,
  GetAccountResponse,
  GetGroupListsResponse,
  GetGroupMemberResponse,
  GetGroupMembersResponse,
  GetGroupMessagesResponse,
  GetGroupResponse
} from "@/backend/dto";
import { axios } from "@/backend/axios";
import { EventBus, Events } from "@/event-bus";
import {
  AccessData,
  GroupEditInfo,
  ConciseGroup,
  GroupInfo,
  GroupTree,
  List,
  ListEditInfo,
  ListInfo,
  MessageBulk,
  Status,
  Task,
  TaskEditInfo,
  TaskInfo
} from "@/models";

export const api = {
  async getGroupTree(groupId: number, elemId: number, keepTasks: boolean): Promise<GroupTree | null> {
    return axios
      .get(`/api/group/${groupId}/tree/${keepTasks ? "task" : "list"}/${elemId}`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getTaskEditInfo(id: string): Promise<TaskEditInfo | null> {
    return axios
      .get(`/api/task/${id}/info/edit`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getListCreateInfo(id: string): Promise<ListEditInfo | null> {
    return axios
      .get(`/api/list/${id}/info/create`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getListEditInfo(id: string): Promise<ListEditInfo | null> {
    return axios
      .get(`/api/list/${id}/info/edit`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getGroupEditInfoLists(id: string): Promise<ConciseGroup | null> {
    return axios
      .get(`/api/group/${id}/info/lists`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getGroupEditInfo(id: string): Promise<GroupEditInfo | null> {
    return axios
      .get(`/api/group/${id}/info/edit`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async clearMessages(id: string | number): Promise<void> {
    return axios
      .delete(`/api/group/${id}/messages`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async loadMessages(id: string | number, offset: number, count: number): Promise<MessageBulk | null> {
    return axios
      .post(`/api/group/${id}/messages`, { offset: offset, count: count })
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getGroupInfo(id: string | number, msgCount: number): Promise<GroupInfo | null> {
    return axios
      .post(`/api/group/${id}/info`, { offset: 0, count: msgCount })
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getListInfo(id: string | number): Promise<ListInfo | null> {
    return axios
      .get(`/api/list/${id}/info`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getTaskInfo(id: string | number): Promise<TaskInfo | null> {
    return axios
      .get(`/api/task/${id}/info`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async authenticateLogin(login: string, password: string): Promise<void | null> {
    return axios
      .post(`/api/login`, { login: login, password: password })
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async unsubscribe(taskId: string | number): Promise<void> {
    return axios
      .delete(`/api/task/${taskId}/subscriber`)
      .then(value => value.data.tasks)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async subscribe(taskId: string | number): Promise<void> {
    return axios
      .post(`/api/task/${taskId}/subscriber`)
      .then(value => value.data.tasks)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async retract(taskId: string | number): Promise<void> {
    return axios
      .delete(`/api/task/${taskId}/assignee`)
      .then(value => value.data.tasks)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async assign(taskId: string | number): Promise<void> {
    return axios
      .post(`/api/task/${taskId}/assignee`)
      .then(value => value.data.tasks)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async updateTaskStatus(payload: { id: string | number; status: Status }): Promise<void> {
    return axios
      .post(`/api/task/${payload.id}/status`, { status: payload.status })
      .then(value => value.data.tasks)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getAssignedTasks(): Promise<AssignedOrSubscribedTasksResponse[] | null> {
    return axios
      .get(`/api/account/tasks/assigned`)
      .then(value => value.data.tasks)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getSubscribedTasks(): Promise<AssignedOrSubscribedTasksResponse[] | null> {
    return axios
      .get(`/api/account/tasks/subscribed`)
      .then(value => value.data.tasks)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getTask(id: string | number): Promise<Task | null> {
    return axios
      .get(`/api/task/${id}`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async createTask(payload: CreateTaskPayload): Promise<number | null> {
    return axios
      .post("/api/task/create", payload)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getSpecialAccess(id: string | number): Promise<AccessData | null> {
    return axios
      .get(`/api/list/${id}/accounts`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getList(id: string): Promise<List | null> {
    return axios
      .get(`/api/list/${id}`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getAccountById(id: string): Promise<GetAccountResponse | null> {
    return axios
      .get(`/api/account/${id}`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async deleteTask(id: string | number): Promise<void> {
    return axios
      .delete(`/api/task/${id}`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async deleteList(id: string | number): Promise<void> {
    return axios
      .delete(`/api/list/${id}`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async editTask(payload: EditTaskPayload): Promise<void> {
    return axios
      .post(`/api/task/${payload.id}/edit`, {
        name: payload.name,
        description: payload.description,
        parentId: payload.parentId,
        listId: payload.listId
      })
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async editList(payload: EditTaskListPayload): Promise<void> {
    return axios
      .post(`/api/list/${payload.id}/edit`, {
        name: payload.name,
        parentId: payload.parentId,
        accessList: payload.accessList
      })
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getListAccessData(listId: number | string): Promise<AccessData | null> {
    return axios
      .get(`/api/list/${listId}/accounts`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async createList(payload: CreateTaskListPayload): Promise<number | null> {
    return axios
      .post("/api/list/create", payload)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getGroupMembers(groupId: number | string): Promise<GetGroupMembersResponse | null> {
    return axios
      .get(`/api/group/${groupId}/members`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async deleteGroup(groupId: number | string): Promise<void> {
    return axios
      .delete(`/api/group/${groupId}`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async editGroup(
    groupId: number | string,
    payload: { name: string; members: GetGroupMemberResponse[] }
  ): Promise<void> {
    return axios
      .post(`/api/group/${groupId}/edit`, { name: payload.name, members: payload.members })
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async editAccount(payload: { name: string }): Promise<void> {
    return axios
      .post("/api/account", { value: payload.name })
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getAccountGroups(): Promise<GetAccountGroupsResponse | null> {
    return axios
      .get("/api/account/groups")
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getAccount(): Promise<GetAccountResponse | null> {
    return axios
      .get("/api/account")
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async createGroup(payload: CreateGroupPayload): Promise<number | null> {
    return axios
      .post("/api/group/create", payload)
      .then(e => e.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getGroupMessages(groupId: number | string): Promise<GetGroupMessagesResponse | null> {
    return axios
      .get(`/api/group/${groupId}/messages`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getGroup(groupId: number | string): Promise<GetGroupResponse | null> {
    return axios
      .get(`/api/group/${groupId}`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async getGroupLists(groupId: number | string): Promise<GetGroupListsResponse | null> {
    return axios
      .get(`/api/group/${groupId}/lists`)
      .then(value => value.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async postGroupMessage(groupId: number | string, message: string): Promise<void> {
    return axios
      .post(`/api/group/${groupId}/message`, { value: message })
      .then(e => e.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  },

  async deleteGroupMessage(messageId: number | string): Promise<void> {
    return axios
      .delete(`/api/message/${messageId}`)
      .then(e => e.data)
      .catch(e => {
        EventBus.$emit(Events.NETWORK_ERROR, e);
        return null;
      });
  }
};
