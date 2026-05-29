import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink, CommonModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {
  email = '';
  password = '';
  rememberMe = false;
  errorMessage = '';
  loading = false;
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  onSubmit() {
    if (!this.email || !this.password) {
      this.errorMessage = 'Please fill in all fields.';
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.authService.login(this.email, this.password).subscribe({
      next: (user) => this.redirectByRole(user.role),
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err.error === 'string' ? err.error : 'Invalid email or password.';
      }
    });
  }
  private redirectByRole(role: string) {
    if (role === 'ADMIN') this.router.navigate(['/admin/sellers']);
    else if (role === 'SELLER') this.router.navigate(['/seller/dashboard']);
    else this.router.navigate(['/search']);
  }
}
