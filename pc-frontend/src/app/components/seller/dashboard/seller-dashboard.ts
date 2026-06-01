import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { SellerService } from '../../../services/seller.service';

@Component({
  selector: 'app-seller-dashboard',
  imports: [CommonModule, RouterLink],
  templateUrl: './seller-dashboard.html',
  styleUrl: './seller-dashboard.css'
})
export class SellerDashboard implements OnInit {
  user: any;
  dashboard: any = null;
  analytics: any[] = [];
  loading = true;
  error = '';
  sidebarOpen = false;

  constructor(
    private authService: AuthService,
    private sellerService: SellerService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (!this.user?.id) { this.router.navigate(['/login']); return; }
    this.sellerService.getDashboard(this.user.email).subscribe({
      next: (data) => {
        this.loading = false;
        this.dashboard = data;
        if (data.pharmacyId) {
          this.sellerService.getAnalytics(data.pharmacyId).subscribe({
            next: (a) => this.analytics = a,
            error: () => {}
          });
        }
      },
      error: (err) => {
        this.loading = false;
        this.error = typeof err.error === 'string' ? err.error : 'Failed to load dashboard.';
      }
    });
  }

  formatDate(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      return new Date(dt[0], dt[1] - 1, dt[2]).toLocaleDateString([], { dateStyle: 'medium' });
    }
    return new Date(dt).toLocaleDateString([], { dateStyle: 'medium' });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
