import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { AdminService } from '../../../services/admin.service';

type Tab = 'pending' | 'all';
type StatusFilter = 'ALL' | 'VERIFIED' | 'UNVERIFIED' | 'INACTIVE';

@Component({
  selector: 'app-admin-sellers',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-sellers.html',
  styleUrl: './admin-sellers.css'
})
export class AdminSellers implements OnInit {
  user: any;
  pending: any[] = [];
  all: any[] = [];
  filteredAll: any[] = [];
  loading = true;
  sidebarOpen = false;
  error = '';
  actionId: string | null = null;
  tab: Tab = 'pending';
  statusFilter: StatusFilter = 'ALL';
  searchTerm = '';

  toast: { msg: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;

  constructor(
    private authService: AuthService,
    private adminService: AdminService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (this.authService.getRole() !== 'ADMIN') { this.router.navigate(['/login']); return; }
    this.loadPending();
  }

  setTab(t: Tab) {
    this.tab = t;
    if (t === 'all' && this.all.length === 0) this.loadAll();
  }

  loadPending() {
    this.loading = true;
    this.adminService.getPendingSellers().subscribe({
      next: (data) => { this.loading = false; this.pending = data; },
      error: (err) => {
        this.loading = false;
        this.pending = [];
        if (err.status !== 204) this.error = 'Failed to load pending sellers.';
      }
    });
  }

  loadAll() {
    this.loading = true;
    this.adminService.getAllSellers().subscribe({
      next: (data) => { this.loading = false; this.all = data; this.applyAllFilter(); },
      error: (err) => {
        this.loading = false;
        this.all = [];
        if (err.status !== 204) this.error = 'Failed to load sellers.';
      }
    });
  }

  setStatusFilter(s: StatusFilter) {
    this.statusFilter = s;
    this.applyAllFilter();
  }

  onSearchChange() {
    this.applyAllFilter();
  }

  private applyAllFilter() {
    const term = this.searchTerm.trim().toLowerCase();
    this.filteredAll = this.all.filter(p => {
      // Mutually exclusive buckets: a deactivated pharmacy belongs to INACTIVE only,
      // never to VERIFIED or UNVERIFIED.
      if (this.statusFilter === 'VERIFIED'   && !(p.isActive && p.isVerified)) return false;
      if (this.statusFilter === 'UNVERIFIED' && !(p.isActive && !p.isVerified)) return false;
      if (this.statusFilter === 'INACTIVE'   && p.isActive) return false;
      if (term) {
        const hay = `${p.pharmacyName} ${p.ownerName} ${p.ownerEmail} ${p.pharmacyAddress}`.toLowerCase();
        if (!hay.includes(term)) return false;
      }
      return true;
    });
  }

  verify(pharmacyId: string, pharmacyName: string) {
    this.actionId = pharmacyId;
    this.adminService.verifySeller(pharmacyId).subscribe({
      next: () => {
        this.actionId = null;
        this.showToast(`${pharmacyName} verified and approved.`, 'success');
        this.pending = this.pending.filter(p => p.pharmacyId !== pharmacyId);
        if (this.tab === 'all') this.loadAll();
      },
      error: (err) => {
        this.actionId = null;
        this.showToast(typeof err.error === 'string' ? err.error : 'Verification failed.', 'error');
      }
    });
  }

  reject(pharmacyId: string, pharmacyName: string) {
    this.actionId = pharmacyId;
    this.adminService.rejectSeller(pharmacyId).subscribe({
      next: () => {
        this.actionId = null;
        this.showToast(`${pharmacyName} rejected.`, 'error');
        this.pending = this.pending.filter(p => p.pharmacyId !== pharmacyId);
        if (this.tab === 'all') this.loadAll();
      },
      error: (err) => {
        this.actionId = null;
        this.showToast(typeof err.error === 'string' ? err.error : 'Rejection failed.', 'error');
      }
    });
  }

  toggleActive(p: any) {
    const action = p.isActive ? 'deactivate' : 'activate';
    if (!confirm(`Are you sure you want to ${action} ${p.pharmacyName}?`)) return;
    this.actionId = p.pharmacyId;
    const obs = p.isActive
      ? this.adminService.deactivatePharmacy(p.pharmacyId)
      : this.adminService.activatePharmacy(p.pharmacyId);
    obs.subscribe({
      next: () => {
        this.actionId = null;
        p.isActive = !p.isActive;
        this.applyAllFilter();
        this.showToast(`${p.pharmacyName} ${p.isActive ? 'reactivated' : 'deactivated'}.`, p.isActive ? 'success' : 'error');
      },
      error: (err) => {
        this.actionId = null;
        this.showToast(typeof err.error === 'string' ? err.error : `${action} failed.`, 'error');
      }
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

  countByStatus(s: StatusFilter): number {
    if (s === 'ALL') return this.all.length;
    return this.all.filter(p =>
      s === 'VERIFIED'   ? p.isActive && p.isVerified :
      s === 'UNVERIFIED' ? p.isActive && !p.isVerified :
      /* INACTIVE */       !p.isActive
    ).length;
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 4000);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
