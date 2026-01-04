import { Injectable, signal } from '@angular/core';

/**
 * Theme Service
 * Manages dark/light theme switching and persistence
 */
@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private readonly THEME_KEY = 'ebanking-theme';
  
  // Signal for reactive theme state
  public isDarkMode = signal<boolean>(this.getStoredTheme() === 'dark');

  constructor() {
    this.applyTheme(this.isDarkMode());
  }

  /**
   * Toggle between light and dark themes
   */
  toggleTheme(): void {
    const newTheme = !this.isDarkMode();
    this.isDarkMode.set(newTheme);
    this.applyTheme(newTheme);
    this.storeTheme(newTheme ? 'dark' : 'light');
  }

  /**
   * Set specific theme
   */
  setTheme(theme: 'light' | 'dark'): void {
    const isDark = theme === 'dark';
    this.isDarkMode.set(isDark);
    this.applyTheme(isDark);
    this.storeTheme(theme);
  }

  /**
   * Apply theme to document
   */
  private applyTheme(isDark: boolean): void {
    const htmlElement = document.documentElement;
    
    if (isDark) {
      htmlElement.classList.add('dark-theme');
      htmlElement.classList.remove('light-theme');
    } else {
      htmlElement.classList.add('light-theme');
      htmlElement.classList.remove('dark-theme');
    }
  }

  /**
   * Get stored theme preference
   */
  private getStoredTheme(): 'light' | 'dark' {
    const stored = localStorage.getItem(this.THEME_KEY);
    if (stored === 'dark' || stored === 'light') {
      return stored;
    }
    
    // Default to system preference
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }

  /**
   * Store theme preference
   */
  private storeTheme(theme: 'light' | 'dark'): void {
    localStorage.setItem(this.THEME_KEY, theme);
  }
}
