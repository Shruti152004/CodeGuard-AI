import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AdminService } from '../services/admin.service';

@Component({
  selector: 'app-login',
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  private adminService = inject(AdminService);
  private router = inject(Router);

  public username = '';
  public password = '';
  public errorMessage = '';
  public loading = false;

  onSubmit() {
    if (!this.username || !this.password) {
      this.errorMessage = 'Please enter all fields.';
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.adminService.login({ usernameOrEmail: this.username, password: this.password }).subscribe({
      next: (res) => {
        localStorage.setItem('accessToken', res.accessToken);
        localStorage.setItem('username', res.username);
        this.router.navigate(['/']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Invalid username or password.';
      }
    });
  }
}
