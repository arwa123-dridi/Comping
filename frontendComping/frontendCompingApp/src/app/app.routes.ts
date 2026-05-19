import { Routes } from '@angular/router';
import { authGuard } from './guards/auth.guard';
import { roleGuard } from './guards/role.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'Campino', loadComponent: () => import('./Home/home/home.component').then(m => m.HomeComponent) },
  { path: 'login', loadComponent: () => import('./signin/signin.component').then(m => m.SigninComponent) },
  { path: 'signup', loadComponent: () => import('./signup/signup.component').then(m => m.SignupComponent) },
  
  { 
    path: 'marketplace', 
    loadComponent: () => import('./product-front/product-front.component').then(m => m.ProductFrontComponent) 
  },
  { 
    path: 'panier', 
    loadComponent: () => import('./product-panier/product-panier.component').then(m => m.ProductPanierComponent),
    canActivate: [authGuard]
  },
  
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./dashboard/dashboard-user/dashboard-user.component').then(m => m.DashboardUserComponent),
    children: [
      { path: 'messages', loadComponent: () => import('./chat/chat-conversation.component').then(m => m.ChatConversationComponent) },
      { path: 'profile', loadComponent: () => import('./profile/profile.component').then(m => m.ProfileComponent) }
    ]
  },
  
  {
    path: 'admin',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./layouts/admin-layout/admin-layout.component').then(m => m.AdminLayoutComponent),
    children: [
      { path: 'dashboard', loadComponent: () => import('./admin/dashboard/dashboard.component').then(m => m.DashboardComponent) },
      { path: 'users', loadComponent: () => import('./admin/users/users.component').then(m => m.UsersComponent) }
    ]
  },

  { path: 'checklist-ia', loadComponent: () => import('./checklist-ia/checklist-ia.component').then(m => m.ChecklistIaComponent) },
  { path: '**', redirectTo: 'Campino' }
];
