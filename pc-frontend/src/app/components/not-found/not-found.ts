import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-not-found',
  imports: [RouterLink],
  templateUrl: './not-found.html',
  styleUrl: './not-found.css'
})
export class NotFound {
  constructor(private authService: AuthService) {}

  get homeLink(): string {
    const role = this.authService.getRole();
    if (role === 'ADMIN') return '/admin/sellers';
    if (role === 'SELLER') return '/seller/dashboard';
    if (this.authService.isLoggedIn()) return '/search';
    return '/';
  }
}
