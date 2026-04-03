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
// import { AuthGuard } from './guards/auth.guard';  // ❌ COMMENTÉ TEMPORAIREMENT
// import { AdminGuard } from './guards/admin.guard';  // ❌ COMMENTÉ TEMPORAIREMENT

export const routes: Routes = [
    // Pages publiques
    { path: 'home', component: HomeComponent },
    { path: 'signup', component: SignupComponent },
    { path: 'login', component: SignupComponent },
    { path: 'profile', component: ProfileComponent },  
    
    // Routes pour les randonnées
    { path: 'sorties', component: SortieListComponent },
    { path: 'sorties/create', component: SortieFormComponent },  
    { path: 'sorties/edit/:id', component: SortieFormComponent }, 
    { path: 'sorties/:id', component: SortieDetailComponent },
    
    // Routes pour les équipes
    { path: 'equipes', component: EquipeListComponent },
    { path: 'equipes/create', component: EquipeFormComponent },  
    { path: 'equipes/edit/:id', component: EquipeFormComponent },  
    { path: 'equipes/:id', component: EquipeDetailComponent },
    
    // Dashboards
    { path: 'dashboard', component: DashboardUserComponent },
    { path: 'admin', component: DashboardAdminComponent },
    
    // Redirection
    { path: '', redirectTo: 'home', pathMatch: 'full' },
    { path: '**', redirectTo: 'home' }
];