import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { AdminService } from '../../../services/admin.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-admin-documents',
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-documents.html',
  styleUrl: './admin-documents.css'
})
export class AdminDocuments implements OnInit {
  user: any;
  documents: any[] = [];
  loading = true;
  sidebarOpen = false;
  error = '';
  actionId: string | null = null;

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
    this.load();
  }

  load() {
    this.loading = true;
    this.error = '';
    this.adminService.getPendingDocuments().subscribe({
      next: (data) => { this.loading = false; this.documents = data; },
      error: (err) => {
        this.loading = false;
        this.documents = [];
        if (err.status !== 204) this.error = 'Failed to load pending documents.';
      }
    });
  }

  approve(doc: any) {
    this.actionId = doc.id;
    this.adminService.updateDocumentStatus(doc.id, 'APPROVED').subscribe({
      next: () => {
        this.actionId = null;
        this.showToast(`Document approved for ${doc.pharmacyName}.`, 'success');
        this.documents = this.documents.filter(d => d.id !== doc.id);
      },
      error: (err) => {
        this.actionId = null;
        this.showToast(typeof err.error === 'string' ? err.error : 'Approval failed.', 'error');
      }
    });
  }

  reject(doc: any) {
    this.actionId = doc.id;
    this.adminService.updateDocumentStatus(doc.id, 'REJECTED').subscribe({
      next: () => {
        this.actionId = null;
        this.showToast(`Document rejected for ${doc.pharmacyName}.`, 'error');
        this.documents = this.documents.filter(d => d.id !== doc.id);
      },
      error: (err) => {
        this.actionId = null;
        this.showToast(typeof err.error === 'string' ? err.error : 'Rejection failed.', 'error');
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

  getDocUrl(path: string): string {
    if (!path) return '';
    return `${environment.apiBaseUrl}/${path.replace(/\\/g, '/')}`;
  }

  isPdf(path: string): boolean {
    return path?.toLowerCase().endsWith('.pdf') ?? false;
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 4000);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
