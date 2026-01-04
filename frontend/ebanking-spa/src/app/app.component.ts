import { Component, OnInit } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/services/theme.service';

/**
 * Root App Component
 * Initializes theme and provides router outlet
 */
@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  title = 'E-Banking 3.0';

  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    // Theme service is initialized, applying stored theme preference
  }
}

