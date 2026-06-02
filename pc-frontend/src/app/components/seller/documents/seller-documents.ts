import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { SellerService } from '../../../services/seller.service';

@Component({
  selector: 'app-seller-documents',
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './seller-documents.html',
  styleUrl: './seller-documents.css'
})
export class SellerDocuments implements OnInit {
  user: any;
  pharmacyId = '';
  pharmacyName = '';
  sidebarOpen = false;

  documentType = 'PHARMACY_LICENSE';
  selectedFile: File | null = null;
  uploading = false;
  error = '';

  toast: { msg: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;

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
        this.pharmacyId = data.pharmacyId;
        this.pharmacyName = data.pharmacyName;
      },
      error: () => {
        this.error = 'Could not load pharmacy info. Please try again.';
      }
    });
  }

  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    this.selectedFile = input.files?.[0] ?? null;
  }

  upload() {
    if (!this.selectedFile) {
      this.error = 'Please select a file to upload.';
      return;
    }
    if (!this.pharmacyId) {
      this.error = 'Pharmacy ID not available. Please refresh.';
      return;
    }
    this.uploading = true;
    this.error = '';

    const formData = new FormData();
    formData.append('file', this.selectedFile);
    formData.append('docType', this.documentType);

    this.sellerService.uploadLicense(this.pharmacyId, formData).subscribe({
      next: () => {
        this.uploading = false;
        this.selectedFile = null;
        this.showToast('Document uploaded successfully! An admin will review it shortly.', 'success');
      },
      error: (err) => {
        this.uploading = false;
        this.error = typeof err.error === 'string' ? err.error : 'Upload failed. Please try again.';
      }
    });
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 5000);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
