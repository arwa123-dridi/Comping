// src/app/app.routes.ts
import { Routes } from '@angular/router';
import { SignupComponent } from './signup/signup.component';
import { HomeComponent } from './Home/home/home.component';
import { ProfileComponent } from './profile/profile.component';
import { SortieListComponent } from './sortie/sortie-list/sortie-list.component';
import { SortieFormComponent } from './sortie/sortie-form/sortie-form.component';
import { SortieDetailComponent } from './sortie/sortie-detail/sortie-detail.component';
import { EquipeListComponent } from './equipe/equipe-list/equipe-list.component';
import { EquipeFormComponent } from './equipe/equipe-form/equipe-form.component';
import { EquipeDetailComponent } from './equipe/equipe-detail/equipe-detail.component';
import { DashboardUserComponent } from './dashboard/dashboard-user/dashboard-user.component';
import { DashboardAdminComponent } from './dashboard/dashboard-admin/dashboard-admin.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { SigninComponent } from './signin/signin.component';

import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';

export const routes: Routes = [
  { path: 'Campino', component: HomeComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent }, 
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'login', component: SigninComponent },
   {
    path: 'admin',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent }
    ]
  },
    // Routes pour les randonnées (dynamic CRUD)
    { path: 'sorties', component: SortieListComponent },
    { path: 'sorties/create', component: SortieFormComponent },  
    { path: 'sorties/edit/:id', component: SortieFormComponent }, 
    { path: 'sorties/:id', component: SortieDetailComponent },
    
    // Routes pour les équipes (dynamic CRUD)
    { path: 'equipes', component: EquipeListComponent },
    { path: 'equipes/create', component: EquipeFormComponent },  
    { path: 'equipes/edit/:id', component: EquipeFormComponent },  
    { path: 'equipes/:id', component: EquipeDetailComponent },
    
    // Dashboards randonner/equipe (protected)
    { path: 'dashboard', component: DashboardUserComponent, canActivate: [AuthGuard] },
    { path: 'dashboard/admin', component: DashboardAdminComponent, canActivate: [AdminGuard] },
    
    // Redirection
    { path: '**', redirectTo: 'Campino' }
];

