import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../core/services/user.service';
import { User } from '../../models';

@Component({
  selector: 'app-admin-users',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="page">
      <h2>Admin — Users</h2>
      <button (click)="createMock()">Create Mock User</button>
      <ul>
        <li *ngFor="let u of users">
          {{ u.fullName }} — {{ u.email }} — {{ u.role }} — {{ u.status }}
          <button (click)="toggleActive(u)">{{ u.status === 'ACTIVE' ? 'Deactivate' : 'Activate' }}</button>
        </li>
      </ul>
    </div>
  `
})
export class AdminUsersComponent implements OnInit {
  users: User[] = [];

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.userService.searchUsers('', undefined, 0, 50).subscribe(r => (this.users = r.users));
  }

  createMock(): void {
    this.userService.createUser({ email: `test${Date.now()}@example.com`, firstName: 'Test', lastName: 'User' }).subscribe(() => this.loadUsers());
  }

  toggleActive(u: User): void {
    if (u.status === 'ACTIVE') {
      this.userService.deactivateUser(u.id).subscribe(() => this.loadUsers());
    } else {
      this.userService.activateUser(u.id).subscribe(() => this.loadUsers());
    }
  }
}