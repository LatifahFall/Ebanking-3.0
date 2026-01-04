/**
 * User Preferences Model
 * Matches backend UserPreferences entity
 */
export interface UserPreferences {
  id?: number;
  language: string; // "en", "fr", etc.
  theme: string; // "light", "dark"
  notificationEmail: boolean;
  notificationSms: boolean;
  notificationPush: boolean;
  notificationInApp: boolean;
}

export interface UserPreferencesRequest {
  language?: string;
  theme?: string;
  notificationEmail?: boolean;
  notificationSms?: boolean;
  notificationPush?: boolean;
  notificationInApp?: boolean;
}

export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

