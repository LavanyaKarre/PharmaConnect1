import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { ReservationService } from '../../../services/reservation.service';
import { SellerService } from '../../../services/seller.service';

@Component({
  selector: 'app-seller-reservations',
  imports: [CommonModule, RouterLink],
  templateUrl: './seller-reservations.html',
  styleUrl: './seller-reservations.css'
})
export class SellerReservations implements OnInit, OnDestroy {
  user: any;
  pharmacyId = '';
  reservations: any[] = [];
  filtered: any[] = [];
  loading = true;
  error = '';
  filterStatus: 'ALL' | 'PENDING' | 'CLAIMED' | 'EXPIRED' | 'CANCELLED' = 'ALL';
  sidebarOpen = false;

  toast: { msg: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;
  private expiryTimers: any[] = [];

  constructor(
    private authService: AuthService,
    private reservationService: ReservationService,
    private sellerService: SellerService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (!this.user?.id) { this.router.navigate(['/login']); return; }
    this.sellerService.getDashboard(this.user.email).subscribe({
      next: (d) => { this.pharmacyId = d.pharmacyId; this.loadReservations(); },
      error: () => { this.loading = false; this.error = 'Could not load pharmacy info.'; }
    });
  }

  ngOnDestroy() {
    this.expiryTimers.forEach(t => clearTimeout(t));
  }

  loadReservations() {
    this.loading = true;
    this.reservationService.getPharmacyReservations(this.pharmacyId).subscribe({
      next: (data) => {
        this.loading = false;
        this.reservations = data;
        this.applyFilter();
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
        const delay = this.toDate(r.expiresAt).getTime() - now;
        if (delay > 0) {
          this.expiryTimers.push(setTimeout(() => this.loadReservations(), delay + 1000));
        }
      });
  }

  private toDate(dt: any): Date {
    if (Array.isArray(dt)) return new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
    return new Date(dt);
  }

  isExpiredLocally(r: any): boolean {
    if (r.status !== 'PENDING' || !r.expiresAt) return false;
    return this.toDate(r.expiresAt).getTime() < new Date().getTime();
  }

  setFilter(status: typeof this.filterStatus) {
    this.filterStatus = status;
    this.applyFilter();
  }

  private applyFilter() {
    this.filtered = this.filterStatus === 'ALL'
      ? [...this.reservations]
      : this.reservations.filter(r => r.status === this.filterStatus);
  }

  claim(id: string) {
    this.reservationService.claim(id).subscribe({
      next: () => { this.showToast('Reservation claimed successfully.', 'success'); this.loadReservations(); },
      error: (err) => this.showToast(typeof err.error === 'string' ? err.error : 'Claim failed.', 'error')
    });
  }

  parseDate(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      const d = new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
      return d.toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
    }
    return new Date(dt).toLocaleString([], { dateStyle: 'medium', timeStyle: 'short' });
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 4000);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
