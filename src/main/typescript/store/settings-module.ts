import { Actions, Getters, Module, Mutations } from "vuex-smart-module";

export type Theme = "Dark" | "Light";
export type Locale = "en" | "ja" | "ru";

class SettingsState {
  theme: Theme = "Light";
  locale: Locale = "en";
}

class SettingsGetters extends Getters<SettingsState> {
  get isDarkTheme(): boolean {
    return this.state.theme === "Dark";
  }

  get theme(): Theme {
    return this.state.theme;
  }

  get locale(): Locale {
    return this.state.locale;
  }
}

class SettingsMutations extends Mutations<SettingsState> {
  setTheme(theme: Theme): void {
    this.state.theme = theme;
  }

  setLocale(locale: Locale): void {
    this.state.locale = locale;
  }
}

class SettingsActions extends Actions<SettingsState, SettingsGetters, SettingsMutations, SettingsActions> {
  setTheme(theme: Theme) {
    this.mutations.setTheme(theme);
  }

  setLocale(locale: Locale): void {
    this.mutations.setLocale(locale);
  }
}

export const settingsModule = new Module({
  state: SettingsState,
  getters: SettingsGetters,
  mutations: SettingsMutations,
  actions: SettingsActions
});
