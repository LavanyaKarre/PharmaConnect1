import { Routes } from '@angular/router';
import { LandingPage } from './components/landing-page/landing-page';
import { Login } from './components/auth/login/login';
import { Register } from './components/auth/register/register';
import { RegisterPharmacy } from './components/auth/register-pharmacy/register-pharmacy';
import { ForgotPassword } from './components/auth/forgot-password/forgot-password';
import { ResetPassword } from './components/auth/reset-password/reset-password';
import { SearchPage } from './components/patient/search/search';
import { MyReservations } from './components/patient/my-reservations/my-reservations';
import { SellerDashboard } from './components/seller/dashboard/seller-dashboard';
import { SellerInventory } from './components/seller/inventory/seller-inventory';
import { SellerReservations } from './components/seller/reservations/seller-reservations';
import { SellerDocuments } from './components/seller/documents/seller-documents';
import { AdminSellers } from './components/admin/sellers/admin-sellers';
import { AdminMedicines } from './components/admin/medicines/admin-medicines';
import { AdminAnalytics } from './components/admin/analytics/admin-analytics';
import { AdminDocuments } from './components/admin/documents/admin-documents';
import { AdminProfile } from './components/admin/profile/admin-profile';
import { SellerProfile } from './components/seller/profile/seller-profile';
import { Profile } from './components/patient/profile/profile';
import { authGuard } from './guards/auth.guard';
import { sellerGuard } from './guards/seller.guard';
import { adminGuard } from './guards/admin.guard';
import { guestGuard } from './guards/guest.guard';
import { NotFound } from './components/not-found/not-found';

export const routes: Routes = [
  { path: '', component: LandingPage },
  { path: 'login', component: Login, canActivate: [guestGuard] },
  { path: 'register', component: Register, canActivate: [guestGuard] },
  { path: 'register-pharmacy', component: RegisterPharmacy, canActivate: [guestGuard] },
  { path: 'forgot-password', component: ForgotPassword, canActivate: [guestGuard] },
  { path: 'reset-password', component: ResetPassword },
  { path: 'search', component: SearchPage },
  { path: 'my-reservations', component: MyReservations, canActivate: [authGuard] },
  { path: 'seller/dashboard', component: SellerDashboard, canActivate: [sellerGuard] },
  { path: 'seller/inventory', component: SellerInventory, canActivate: [sellerGuard] },
  { path: 'seller/reservations', component: SellerReservations, canActivate: [sellerGuard] },
  { path: 'seller/documents', component: SellerDocuments, canActivate: [sellerGuard] },
  { path: 'admin/sellers', component: AdminSellers, canActivate: [adminGuard] },
  { path: 'admin/medicines', component: AdminMedicines, canActivate: [adminGuard] },
  { path: 'admin/analytics', component: AdminAnalytics, canActivate: [adminGuard] },
  { path: 'admin/documents', component: AdminDocuments, canActivate: [adminGuard] },
  { path: 'admin/profile', component: AdminProfile, canActivate: [adminGuard] },
  { path: 'seller/profile', component: SellerProfile, canActivate: [sellerGuard] },
  { path: 'profile', component: Profile, canActivate: [authGuard] },
  { path: '**', component: NotFound }
];
