import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { InventoryService } from '../../../services/inventory.service';
import { AdminService } from '../../../services/admin.service';
import { SellerService } from '../../../services/seller.service';

@Component({
  selector: 'app-seller-inventory',
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './seller-inventory.html',
  styleUrl: './seller-inventory.css'
})
export class SellerInventory implements OnInit {
  user: any;
  pharmacyId = '';
  isVerified = false;
  sidebarOpen = false;

  stock: any[] = [];
  allMedicines: any[] = [];
  loading = true;
  error = '';

  // Inline editing
  editingId: string | null = null;
  editQty = 0;
  editPrice = 0;
  savingId: string | null = null;

  // Add form
  showAddForm = false;
  addMedicineId = '';
  addQty = 0;
  addPrice = 0;
  addMfgDate = '';
  addExpDate = '';
  addLoading = false;
  addError = '';

  // Bulk upload
  bulkFile: File | null = null;
  bulkLoading = false;
  bulkMsg = '';
  showBulkHelp = false;

  filterQuery = '';
  sortBy: 'name' | 'recent' = 'name';

  get sortedStock(): any[] {
    const q = (this.filterQuery || '').trim().toLowerCase();
    let arr = [...this.stock];
    if (q) {
      arr = arr.filter(s =>
        (s.medicineName || '').toLowerCase().includes(q) ||
        (s.genericName || '').toLowerCase().includes(q) ||
        (s.manufacturer || '').toLowerCase().includes(q)
      );
    }
    if (this.sortBy === 'recent') {
      return arr.sort((a, b) => this.toMillis(b.lastUpdated) - this.toMillis(a.lastUpdated));
    }
    return arr.sort((a, b) => (a.medicineName || '').localeCompare(b.medicineName || ''));
  }

  private toMillis(d: any): number {
    if (!d) return 0;
    if (Array.isArray(d)) {
      return new Date(d[0], d[1] - 1, d[2] ?? 1, d[3] ?? 0, d[4] ?? 0, d[5] ?? 0).getTime();
    }
    const t = new Date(d).getTime();
    return isNaN(t) ? 0 : t;
  }

  toast: { msg: string; type: 'success' | 'error' } | null = null;
  private toastTimer: any;

  constructor(
    private authService: AuthService,
    private inventoryService: InventoryService,
    private adminService: AdminService,
    private sellerService: SellerService,
    private router: Router
  ) {
    this.user = this.authService.getCurrentUser();
  }

  ngOnInit() {
    if (!this.user?.id) { this.router.navigate(['/login']); return; }
    this.sellerService.getDashboard(this.user.email).subscribe({
      next: (d) => {
        this.pharmacyId = d.pharmacyId;
        this.isVerified = d.isPharmacyVerified;
        this.loadStock();
        this.adminService.getAllMedicines().subscribe({
          next: (m) => this.allMedicines = m,
          error: () => {}
        });
      },
      error: () => {
        this.loading = false;
        this.error = 'Could not load pharmacy info.';
      }
    });
  }

  loadStock() {
    this.loading = true;
    this.inventoryService.getByPharmacy(this.pharmacyId).subscribe({
      next: (data) => { this.loading = false; this.stock = data; },
      error: () => { this.loading = false; this.error = 'Failed to load inventory.'; }
    });
  }

  startEdit(item: any) {
    this.editingId = item.stockId;
    this.editQty = item.quantity;
    this.editPrice = item.price;
  }

  cancelEdit() { this.editingId = null; }

  saveEdit(item: any) {
    this.savingId = item.stockId;
    const qtyChanged = this.editQty !== item.quantity;
    const priceChanged = this.editPrice !== item.price;

    if (!qtyChanged && !priceChanged) {
      this.editingId = null;
      this.savingId = null;
      return;
    }

    const done = () => {
      this.savingId = null;
      this.editingId = null;
      this.loadStock();
    };

    if (qtyChanged) {
      this.inventoryService.updateQuantity(item.stockId, this.editQty).subscribe({
        next: () => {
          if (priceChanged) {
            this.inventoryService.updatePrice(item.stockId, this.editPrice).subscribe({
              next: () => { this.showToast('Item updated.', 'success'); done(); },
              error: () => { this.savingId = null; this.showToast('Price update failed.', 'error'); }
            });
          } else {
            this.showToast('Quantity updated.', 'success');
            done();
          }
        },
        error: () => { this.savingId = null; this.showToast('Quantity update failed.', 'error'); }
      });
    } else if (priceChanged) {
      this.inventoryService.updatePrice(item.stockId, this.editPrice).subscribe({
        next: () => { this.showToast('Price updated.', 'success'); done(); },
        error: () => { this.savingId = null; this.showToast('Price update failed.', 'error'); }
      });
    }
  }

  adjust(item: any, delta: number) {
    this.inventoryService.adjustQuantity(item.stockId, delta).subscribe({
      next: () => { this.showToast(`Stock ${delta > 0 ? '+' + delta : delta}.`, 'success'); this.loadStock(); },
      error: (err) => this.showToast(typeof err.error === 'string' ? err.error : 'Adjust failed.', 'error')
    });
  }

  deleteItem(item: any) {
    if (!confirm(`Remove ${item.medicineName} from inventory?`)) return;
    this.inventoryService.deleteItem(item.stockId).subscribe({
      next: () => { this.showToast('Item removed.', 'success'); this.loadStock(); },
      error: () => this.showToast('Delete failed.', 'error')
    });
  }

  submitAdd() {
    if (!this.addMedicineId || this.addQty < 1 || this.addPrice <= 0) {
      this.addError = 'Select a medicine and enter valid quantity and price.';
      return;
    }
    if (!this.addMfgDate) {
      this.addError = 'Manufacturing date is mandatory.';
      return;
    }
    if (!this.addExpDate) {
      this.addError = 'Expiry date is mandatory.';
      return;
    }
    const today = new Date().toISOString().slice(0, 10);
    if (this.addMfgDate > today) {
      this.addError = 'Invalid manufacturing date — cannot be a future date.';
      return;
    }
    if (this.addExpDate < today) {
      this.addError = 'Invalid expiry date — date is already in the past.';
      return;
    }
    if (this.addExpDate < this.addMfgDate) {
      this.addError = 'Expiry date must be on or after the manufacturing date.';
      return;
    }

    const newMfg = this.addMfgDate || null;
    const newExp = this.addExpDate || null;
    const exactMatch = this.stock.find(s =>
      s.medicineId === this.addMedicineId &&
      this.toIsoDateStr(s.manufacturingDate) === newMfg &&
      this.toIsoDateStr(s.expiryDate) === newExp &&
      Number(s.price) === Number(this.addPrice)
    );

    if (exactMatch) {
      const ok = confirm(
        `Item "${exactMatch.medicineName}" already exists with the same manufacturing date, expiry date and price (current quantity: ${exactMatch.quantity}).\n\nWould you like to modify it by adding ${this.addQty} to the existing quantity?`
      );
      if (!ok) { this.addError = ''; return; }
      this.addLoading = true;
      this.addError = '';
      this.inventoryService.adjustQuantity(exactMatch.stockId, this.addQty).subscribe({
        next: () => {
          this.addLoading = false;
          this.resetAddForm();
          this.showToast('Existing inventory entry updated.', 'success');
          this.loadStock();
        },
        error: (err) => {
          this.addLoading = false;
          this.addError = this.extractError(err, 'Failed to update existing item.');
        }
      });
      return;
    }

    this.addLoading = true;
    this.addError = '';
    this.inventoryService.addItem({
      pharmacy: { id: this.pharmacyId },
      medicine: { id: this.addMedicineId },
      quantity: this.addQty,
      price: this.addPrice,
      manufacturingDate: newMfg,
      expiryDate: newExp
    }).subscribe({
      next: () => {
        this.addLoading = false;
        this.resetAddForm();
        this.showToast('Item added to inventory.', 'success');
        this.loadStock();
      },
      error: (err) => {
        this.addLoading = false;
        this.addError = this.extractError(err, 'Failed to add item.');
      }
    });
  }

  private resetAddForm() {
    this.showAddForm = false;
    this.addMedicineId = '';
    this.addQty = 0;
    this.addPrice = 0;
    this.addMfgDate = '';
    this.addExpDate = '';
  }

  private extractError(err: any, fallback: string): string {
    if (!err) return fallback;
    const e = err.error;
    if (typeof e === 'string' && e.trim()) return e;
    if (e && typeof e === 'object') {
      if (typeof e.message === 'string' && e.message.trim()) return e.message;
      if (typeof e.error === 'string' && e.error.trim()) return e.error;
    }
    if (typeof err.message === 'string' && err.message.trim()) return err.message;
    return fallback;
  }

  private toIsoDateStr(d: any): string | null {
    if (!d) return null;
    if (Array.isArray(d)) {
      const y = d[0];
      const m = String(d[1]).padStart(2, '0');
      const day = String(d[2] ?? 1).padStart(2, '0');
      return `${y}-${m}-${day}`;
    }
    if (typeof d === 'string') return d.slice(0, 10);
    const dt = new Date(d);
    return isNaN(dt.getTime()) ? null : dt.toISOString().slice(0, 10);
  }

  isExpired(item: any): boolean {
    if (!item.expiryDate) return false;
    const exp = this.toDate(item.expiryDate);
    if (!exp) return false;
    return exp.getTime() < Date.now();
  }

  isExpiringSoon(item: any): boolean {
    if (!item.expiryDate) return false;
    const exp = this.toDate(item.expiryDate);
    if (!exp) return false;
    const days = (exp.getTime() - Date.now()) / 86400000;
    return days >= 0 && days <= 30;
  }

  get expiredCount(): number {
    return this.stock.filter(s => this.isExpired(s)).length;
  }

  formatDateOnly(d: any): string {
    const dt = this.toDate(d);
    if (!dt) return '—';
    return dt.toLocaleDateString([], { year: 'numeric', month: 'short', day: 'numeric' });
  }

  private toDate(d: any): Date | null {
    if (!d) return null;
    if (Array.isArray(d)) return new Date(d[0], d[1] - 1, d[2] ?? 1);
    return new Date(d);
  }

  downloadSampleCsv() {
    const csv = 'medicineName,quantity,price,manufacturingDate,expiryDate\nParacetamol,100,25.00,2025-01-15,2027-01-15\nIbuprofen,50,42.00,2025-03-01,2027-03-01\n';
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'pharmaconnect-inventory-sample.csv';
    a.click();
    URL.revokeObjectURL(url);
  }

  onFileChange(event: any) {
    this.bulkFile = event.target.files[0] ?? null;
  }

  uploadBulk() {
    if (!this.bulkFile) return;
    this.bulkLoading = true;
    this.bulkMsg = '';
    this.inventoryService.bulkUpload(this.pharmacyId, this.bulkFile).subscribe({
      next: (msg) => {
        this.bulkLoading = false;
        this.bulkMsg = msg;
        this.bulkFile = null;
        this.showToast('Bulk upload successful.', 'success');
        this.loadStock();
      },
      error: (err) => {
        this.bulkLoading = false;
        this.bulkMsg = typeof err.error === 'string' ? err.error : 'Bulk upload failed.';
        this.showToast(this.bulkMsg, 'error');
      }
    });
  }

  parseDate(dt: any): string {
    if (!dt) return '—';
    if (Array.isArray(dt)) {
      const d = new Date(dt[0], dt[1] - 1, dt[2], dt[3] ?? 0, dt[4] ?? 0);
      return d.toLocaleString([], { dateStyle: 'short', timeStyle: 'short' });
    }
    return new Date(dt).toLocaleString([], { dateStyle: 'short', timeStyle: 'short' });
  }

  private showToast(msg: string, type: 'success' | 'error') {
    clearTimeout(this.toastTimer);
    this.toast = { msg, type };
    this.toastTimer = setTimeout(() => (this.toast = null), 4000);
  }

  logout() { this.authService.logout(); this.router.navigate(['/']); }
}
