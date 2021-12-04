import { Actions, Getters, Module, Mutations } from "vuex-smart-module";
import { api } from "@/backend";
import {Account, Group, List, Task, TaskInfo} from "@/models";
import group from "@/pages/app/views/_Group.vue";

class AppState {
  account: Account = { id: 0, name: "Unknown" };
  groups: Group[] = [];
  version: string = process.env.VUE_APP_VERSION;

  msgPerPage: number = 5;
}

class AppGetters extends Getters<AppState> {
  public get version(): string {
    return this.state.version;
  }

  public get msgPerPage(): number {
    return this.state.msgPerPage;
  }

  private traverseChildrenForTasks(taskId: number | string, list: List): Task | undefined {
    for (const child of list.children) {
      for (const task of child.tasks) {
        const res = this.getters.traverseTasks(taskId, task);
        if (res != undefined) {
          return res;
        }
      }
    }
  }

  private traverseTasks(taskId: number | string, task: Task): Task | undefined {
    if (task.id == taskId) {
      return task;
    }
    for (const child of task.children) {
      const res = this.getters.traverseTasks(taskId, child);
      if (res != undefined) {
        return res;
      }
    }
    return undefined;
  }

  private traverseLists(listId: number | string, list: List): List | undefined {
    if (list.id == listId) {
      return list;
    }
    for (const child of list.children) {
      const res = this.getters.traverseLists(listId, child);
      if (res != undefined) {
        return res;
      }
    }
    return undefined;
  }

  public task(taskId: number | string): Task | undefined {
    for (const group of this.state.groups) {
      for (const list of group.lists) {
        for (const task of list.tasks) {
          const res = this.getters.traverseTasks(taskId, task);
          console.log("root tasks");
          console.log(res);
          if (res != undefined) {
            return res;
          }
        }
        const res = this.getters.traverseChildrenForTasks(taskId, list);
        console.log("child tasks");
        console.log(res);
        if (res != undefined) {
          return res;
        }
      }
    }
  }

  public list(listId: number): List | undefined {
    for (const group of this.state.groups) {
      for (const list of group.lists) {
        const res = this.getters.traverseLists(listId, list);
        if (res != undefined) {
          return res;
        }
      }
    }
  }

  public get account(): Account {
    return this.state.account;
  }

  public get groups(): Group[] {
    return this.state.groups;
  }

  public group(groupId: number | string): Group | undefined {
    const idx = this.state.groups.findIndex(value => value.id === +groupId);
    return this.state.groups[idx];
  }
}

class AppMutations extends Mutations<AppState> {
  public msgPerPage(count: number): void {
    this.state.msgPerPage = count;
  }

  public listTasks(payload: { list: List; tasks: Task[] }): void {
    const data = payload.list.tasks;
    data.splice(0, data.length);
    payload.tasks.forEach(task => data.push(task));
  }

  public lists(payload: { groupId: number | string; list: List[] }): void {
    const data = this.state.groups.find(value => value.id === +payload.groupId)!.lists;
    data.splice(0, data.length);
    for (const e of payload.list) {
      data.push(e);
    }
  }

  public groups(newGroups: Group[]): void {
    const data = this.state.groups;
    data.splice(0, data.length);
    for (const d of newGroups) {
      data.push(d);
    }
  }

  public account(payload: Account): void {
    this.state.account.id = payload.id;
    this.state.account.name = payload.name;
  }
}

class AppActions extends Actions<AppState, AppGetters, AppMutations, AppActions> {
  public setMsgPerPage(count: number): void {
    this.mutations.msgPerPage(count);
  }

  private traverseTasks(taskId: number | string, task: Task): Task | undefined {
    if (task.id == taskId) {
      return task;
    }
    for (const child of task.children) {
      const res = this.traverseTasks(taskId, child);
      if (res != undefined) {
        return res;
      }
    }
    return undefined;
  }

  private traverseLists(listId: number, list: List): List | undefined {
    console.log("LISTID: " + listId);
    console.log(list.id);
    console.log(list.id == listId);
    if (list.id == listId) {
      console.log(list);
      return list;
    }
    for (const child of list.children) {
      const res = this.traverseLists(listId, child);
      if (res != undefined) {
        return res;
      }
    }
    return undefined;
  }

  public task(taskId: number | string): Task | undefined {
    for (const group of this.state.groups) {
      for (const list of group.lists) {
        for (const task of list.tasks) {
          const res = this.traverseTasks(taskId, task);
          if (res != undefined) {
            return res;
          }
        }
      }
    }
  }

  public list(listId: number): List | undefined {
    for (const group of this.state.groups) {
      for (const list of group.lists) {
        const res = this.traverseLists(listId, list);
        if (res != undefined) {
          console.log("111111111111111111111111111111111");
          console.log(res);
          return res;
        }
      }
    }
  }

  public async fetchAll() {
    await this.dispatch("fetchAccount");
    await this.dispatch("fetchGroups");
    for (const group of this.getters.groups) {
      await this.dispatch("fetchGroupLists", group.id);
    }
  }

  public async fetchGroups() {
    const resp = await api.getAccountGroups();

    if (resp == null) {
      console.log("failed to fetch account groups");
      return;
    }

    this.mutations.groups(
      resp.groups.map(g => {
        return { id: g.id, role: g.role, name: g.name, lists: [], messages: [], members: [], ownerId: g.ownerId };
      })
    );
  }

  // public async fetchTask(taskId: number | string) {
  //   const resp = await api.getTask(taskId);
  //
  //   if (resp == null) {
  //     console.log(`failed to fetch task (id=${taskId}`);
  //     return;
  //   }
  //   this.mutations.listTasks({ groupId: resp.groupId, listId: resp.id, tasks: resp.tasks });
  // }

  public async fetchGroupLists(groupId: number | string) {
    const resp = await api.getGroupLists(groupId);

    if (resp == null) {
      console.log(`failed to fetch group lists (group id=${groupId}`);
      return;
    }
    this.mutations.lists({ groupId: groupId, list: resp.lists });
  }

  public async fetchListTasks(listId: number | string) {
    const resp = await api.getList(`${listId}`);

    if (resp == null) {
      console.log(`failed to fetch tasks (list id=${listId}`);
      return;
    }

    const list = this.getters.list(resp.id);
    if (list == undefined) {
      console.log(`no such list in local storage (id=${listId}`);
      return;
    }

    this.mutations.listTasks({ list: list, tasks: resp.tasks });
  }

  public async fetchAccount() {
    const resp = await api.getAccount();

    if (resp == null) {
      console.log("failed to fetch account info");
      return;
    }
    this.mutations.account({ id: resp.id, name: resp.name });
  }
}

export const appModule = new Module({
  state: AppState,
  getters: AppGetters,
  mutations: AppMutations,
  actions: AppActions
});
