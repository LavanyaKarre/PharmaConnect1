import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-medicines',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './admin-medicines.html',
  styleUrl: './admin-medicines.css'
})
export class AdminMedicines implements OnInit {
  user: any;
  medicines: any[] = [];
  filtered: any[] = [];
  sidebarOpen = false;
  loading = true;
  error = '';

  showAddForm = false;
  newMed = { name: '', genericName: '', category: '', manufacturer: '', dosageForm: '', strength: '' };
  addLoading = false;
  addError = '';

  showLinkForm = false;
  linkMedicineId = '';
  linkAlternativeId = '';
  linkNote = '';
  linkLoading = false;
  linkError = '';
  linkSuccess = '';

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
    this.loadMedicines();
  }

  loadMedicines() {
    this.loading = true;
    this.adminService.getAllMedicines().subscribe({
      next: (data) => { this.loading = false; this.medicines = data; this.applySearch(); },
      error: () => { this.loading = false; this.error = 'Failed to load medicines.'; }
    });
  }

  onSearchChange() {
    this.applySearch();
  }

  private applySearch() {
    const t = this.searchTerm.toLowerCase();
    this.filtered = t
      ? this.medicines.filter(m =>
          m.name?.toLowerCase().includes(t) || m.genericName?.toLowerCase().includes(t) || m.category?.toLowerCase().includes(t)
        )
      : [...this.medicines];
  }

  submitAdd() {
    if (!this.newMed.name.trim()) { this.addError = 'Medicine name is required.'; return; }
    this.addLoading = true;
    this.addError = '';
    this.adminService.addMedicine(this.newMed).subscribe({
      next: (med) => {
        this.addLoading = false;
        this.medicines.unshift(med);
        this.applySearch();
        this.showAddForm = false;
        this.newMed = { name: '', genericName: '', category: '', manufacturer: '', dosageForm: '', strength: '' };
        this.showToast('Medicine added successfully.', 'success');
      },
      error: (err) => {
        this.addLoading = false;
        this.addError = typeof err.error === 'string' ? err.error : 'Failed to add medicine.';
      }
    });
  }

  submitLink() {
    if (!this.linkMedicineId || !this.linkAlternativeId) {
      this.linkError = 'Please select both medicines.';
      return;
    }
    if (this.linkMedicineId === this.linkAlternativeId) {
      this.linkError = 'Cannot link a medicine to itself.';
      return;
    }
    this.linkLoading = true;
    this.linkError = '';
    this.linkSuccess = '';
    this.adminService.linkAlternatives({ medicineId: this.linkMedicineId, alternativeId: this.linkAlternativeId, equivalenceNote: this.linkNote }).subscribe({
      next: (res) => {
        this.linkLoading = false;
        this.linkSuccess = res.message || 'Medicines linked as alternatives.';
        this.linkMedicineId = '';
        this.linkAlternativeId = '';
        this.linkNote = '';
        this.showToast('Alternatives linked.', 'success');
      },
      error: (err) => {
        this.linkLoading = false;
        this.linkError = typeof err.error === 'string' ? err.error : 'Failed to link alternatives.';
      }
    });
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 4000);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
