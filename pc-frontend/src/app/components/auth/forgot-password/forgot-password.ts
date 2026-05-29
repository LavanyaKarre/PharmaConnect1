import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-forgot-password',
  imports: [FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css'
})
export class ForgotPassword {
  email = '';
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit() {
    if (!this.email.trim()) {
      this.errorMessage = 'Please enter your email address.';
      return;
    }
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.forgotPassword(this.email.trim()).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Reset link sent! Check your email inbox and follow the instructions.';
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = typeof err.error === 'string' ? err.error : 'Could not send reset email. Please check the address and try again.';
      }
    });
  }
}
