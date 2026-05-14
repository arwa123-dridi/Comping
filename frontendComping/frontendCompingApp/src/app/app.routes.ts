// src/app/app.routes.ts — VERSION SIMPLIFIÉE (un seul layout)
import { Routes } from '@angular/router';

import { SignupComponent }               from './signup/signup.component';
import { HomeComponent }                 from './Home/home/home.component';
import { ProfileComponent }              from './profile/profile.component';
import { SigninComponent }               from './signin/signin.component';
import { AdminLayoutComponent }          from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent }            from './admin/dashboard/dashboard.component';

import { SortieListComponent }           from './sortie/sortie-list/sortie-list.component';
import { SortieFormComponent }           from './sortie/sortie-form/sortie-form.component';
import { SortieDetailComponent }         from './sortie/sortie-detail/sortie-detail.component';
import { EquipeListComponent }           from './equipe/equipe-list/equipe-list.component';
import { EquipeFormComponent }           from './equipe/equipe-form/equipe-form.component';
import { EquipeDetailComponent }         from './equipe/equipe-detail/equipe-detail.component';

import { DashboardUserComponent }        from './dashboard/dashboard-user/dashboard-user.component';
import { DashboardOrganizerComponent }   from './dashboard/dashboard-organizer/dashboard-organizer.component';
import { DashboardAdminComponent }       from './dashboard/dashboard-admin/dashboard-admin.component';

import { ChecklistIaComponent }          from './checklist-ia/checklist-ia.component';
import { PlanningSrComponent }           from './planning-sr/planning-sr.component';
import { AuthGuard }                     from './guards/auth.guard';
import { AdminGuard }                    from './guards/admin.guard';
import { OrganizerGuard }               from './guards/organizer.guard';
import { SortieRecommandationsComponent } from './sortie/sortie-recommandations/sortie-recommandations-module';

export const routes: Routes = [

  // ── PUBLIQUES ───────────────────────────────────────────────
  { path: '',        redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'Campino', component: HomeComponent },
  { path: 'signup',  component: SignupComponent },
  { path: 'login',   component: SigninComponent },

  // Profil standalone (accessible depuis partout via /profile)
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },

  // Pages publiques (sans sidebar)
  { path: 'sorties',     component: SortieListComponent   },
  { path: 'sorties/:id', component: SortieDetailComponent },
  { path: 'equipes',     component: EquipeListComponent   },
  { path: 'equipes/:id', component: EquipeDetailComponent },

  // ── ESPACE CONNECTÉ (AdminLayout réutilisé pour TOUS les rôles) ─
  {
    path: 'dashboard',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard],          // ← AuthGuard suffit (user + orga + admin)
    children: [
      { path: '',              component: DashboardUserComponent },
      { path: 'recommandations', component: SortieRecommandationsComponent },
      { path: 'planning',        component: PlanningSrComponent },
      { path: 'checklist-ia',    component: ChecklistIaComponent },
    ]
  },

  // ── BACK OFFICE ORGANISATEUR / ADMIN ────────────────────────
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [OrganizerGuard],
    children: [
      { path: '',          redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },

      // Sorties — ordre OBLIGATOIRE : create/edit AVANT :id
      { path: 'sorties',          component: SortieListComponent   },
      { path: 'sorties/create',   component: SortieFormComponent   },
      { path: 'sorties/edit/:id', component: SortieFormComponent   },
      { path: 'sorties/:id',      component: SortieDetailComponent },

      // Équipes — ordre OBLIGATOIRE : create/edit AVANT :id
      { path: 'equipes',          component: EquipeListComponent   },
      { path: 'equipes/create',   component: EquipeFormComponent   },
      { path: 'equipes/edit/:id', component: EquipeFormComponent   },
      { path: 'equipes/:id',      component: EquipeDetailComponent },

      { path: 'organizer',   component: DashboardOrganizerComponent },
      { path: 'admin-board', component: DashboardAdminComponent, canActivate: [AdminGuard] },
    ]
  },

  { path: '**', redirectTo: 'Campino' }
];
