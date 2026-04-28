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
import { DashboardOrganizerComponent } from './dashboard/dashboard-organizer/dashboard-organizer.component';
import { DashboardAdminComponent } from './dashboard/dashboard-admin/dashboard-admin.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { SigninComponent } from './signin/signin.component';

import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { OrganizerGuard } from './guards/organizer.guard';
import { ChecklistIaComponent } from './checklist-ia/checklist-ia.component';


export const routes: Routes = [
  { path: 'Campino', component: HomeComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent }, 
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'login', component: SigninComponent },
   { path: 'checklist-ia', component: ChecklistIaComponent },
   {
path: 'admin',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'profil', component: ProfileComponent, canActivate: [AdminGuard] }
    ]
  },
    // Routes pour les randonnées
    { path: 'sorties', component: SortieListComponent },
    { path: 'sorties/create', component: SortieFormComponent, canActivate: [OrganizerGuard] },  
    { path: 'sorties/edit/:id', component: SortieFormComponent, canActivate: [OrganizerGuard] }, 
    { path: 'sorties/:id', component: SortieDetailComponent },
    
    // Routes pour les équipes 
    { path: 'equipes', component: EquipeListComponent },
    { path: 'equipes/create', component: EquipeFormComponent, canActivate: [OrganizerGuard] },  
    { path: 'equipes/edit/:id', component: EquipeFormComponent, canActivate: [OrganizerGuard] },  
    { path: 'equipes/:id', component: EquipeDetailComponent },
    
    // Dashboards randonner/equipe (protected)
    { path: 'dashboard/organizer', component: DashboardOrganizerComponent, canActivate: [OrganizerGuard] },
    { path: 'dashboard', component: DashboardUserComponent, canActivate: [AuthGuard] },
    { path: 'admin/dashboard', component: DashboardAdminComponent, canActivate: [AdminGuard] },
    
    // Redirection
    { path: '**', redirectTo: 'Campino' }
];

