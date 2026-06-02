import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { ReservationService } from '../../../services/reservation.service';
import { AuthService } from '../../../services/auth.service';

@Component({
  selector: 'app-my-reservations',
  imports: [CommonModule, RouterLink],
  templateUrl: './my-reservations.html',
  styleUrl: './my-reservations.css'
})
export class MyReservations implements OnInit, OnDestroy {
  reservations: any[] = [];
  loading = false;
  error = '';
  message = '';
  sidebarOpen = false;

  user: any;
  private expiryTimers: any[] = [];

  constructor(
    private reservationService: ReservationService,
    private authService: AuthService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (!this.user.id) {
      this.router.navigate(['/login']);
      return;
    }
    this.load();
  }

  ngOnDestroy() {
    this.expiryTimers.forEach(t => clearTimeout(t));
  }

  load() {
    this.loading = true;
    this.reservationService.getUserReservations(this.user.id!).subscribe({
      next: (data) => {
        this.loading = false;
        this.reservations = data;
        this.scheduleExpiryRefresh(data);
      },
      error: () => { this.loading = false; this.error = 'Failed to load reservations.'; }
    });
  }

  private scheduleExpiryRefresh(reservations: any[]) {
    this.expiryTimers.forEach(t => clearTimeout(t));
    this.expiryTimers = [];

    const now = new Date().getTime();
    reservations
      .filter(r => r.status === 'PENDING' && r.expiresAt)
      .forEach(r => {
        const expiresAt = this.toDate(r.expiresAt).getTime();
        const delay = expiresAt - now;
        if (delay > 0) {
          const timer = setTimeout(() => this.load(), delay + 1000);
          this.expiryTimers.push(timer);
        }
      });
  }

  private toDate(dt: any): Date {
    if (Array.isArray(dt)) {
      return new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
    }
    return new Date(dt);
  }

  isExpiredLocally(r: any): boolean {
    if (r.status !== 'PENDING' || !r.expiresAt) return false;
    return this.toDate(r.expiresAt).getTime() < new Date().getTime();
  }

  cancel(id: string) {
    this.error = '';
    this.reservationService.cancel(id).subscribe({
      next: () => { this.message = 'Reservation cancelled.'; this.load(); },
      error: (err) => { this.error = err.error || 'Cancellation failed.'; }
    });
  }

  formatTime(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      const d = new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
      return d.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
    }
    return new Date(dt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
  }

  logout() {
    this.authService.logout();
    this.router.navigate(['/']);
  }
}
