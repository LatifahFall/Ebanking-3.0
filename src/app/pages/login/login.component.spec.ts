import { ComponentFixture, TestBed } from '@angular/core/testing';
import { FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of } from 'rxjs';

import { LoginComponent } from './login.component';
import { AuthService } from '../../core/services/auth.service';

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [LoginComponent, FormsModule, RouterTestingModule, MatFormFieldModule, MatInputModule, MatIconModule, MatCheckboxModule],
      providers: [
        { provide: AuthService, useValue: { login: () => of({ success: true, requiresMFA: false }), loginWithDTO: () => of({ access_token: 'mock', refresh_token: 'r', expires_in: 3600 }) } }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('shows username validation message when username is empty', () => {
    component.username = '';
    component.password = 'password123';
    component.onSubmit();
    (expect(component.errorMessage) as any).toBe('Please enter both username and password');
  });
});
