// src/app/app.routes.ts — VERSION FINALE COMPLÈTE
import { Routes } from '@angular/router';

// ── Auth / Layout ─────────────────────────────────────────────────────────────
import { SignupComponent }               from './signup/signup.component';
import { SigninComponent }               from './signin/signin.component';
import { HomeComponent }                 from './Home/home/home.component';
import { ProfileComponent }              from './profile/profile.component';
import { AdminLayoutComponent }          from './layouts/admin-layout/admin-layout.component';

// ── Dashboards ────────────────────────────────────────────────────────────────
import { DashboardComponent }            from './admin/dashboard/dashboard.component';
import { DashboardUserComponent }        from './dashboard/dashboard-user/dashboard-user.component';
import { DashboardOrganizerComponent }   from './dashboard/dashboard-organizer/dashboard-organizer.component';
import { DashboardAdminComponent }       from './dashboard/dashboard-admin/dashboard-admin.component';

// ── Sorties ───────────────────────────────────────────────────────────────────
import { SortieListComponent }           from './sortie/sortie-list/sortie-list.component';
import { SortieFormComponent }           from './sortie/sortie-form/sortie-form.component';
import { SortieDetailComponent }         from './sortie/sortie-detail/sortie-detail.component';

// ── Équipes ───────────────────────────────────────────────────────────────────
import { EquipeListComponent }           from './equipe/equipe-list/equipe-list.component';
import { EquipeFormComponent }           from './equipe/equipe-form/equipe-form.component';
import { EquipeDetailComponent }         from './equipe/equipe-detail/equipe-detail.component';

// ── Module Amal — IA Checklist + Planning SR + Recommandations ────────────────
import { ChecklistIaComponent }          from './checklist-ia/checklist-ia.component';

// ── Guards ────────────────────────────────────────────────────────────────────
import { AuthGuard }                     from './guards/auth.guard';
import { AdminGuard }                    from './guards/admin.guard';
import { OrganizerGuard }               from './guards/organizer.guard';  //  fichier créé
import { SortieRecommandationsComponent } from './sortie/sortie-recommandations/sortie-recommandations-module';
import { PlanningSrComponent } from './planning-sr/planning-sr.component';
// ═════════════════════════════════════════════════════════════════════════════
export const routes: Routes = [

  // ── Page d'accueil ──────────────────────────────────────────────────────────
  { path: '',        redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'Campino', component: HomeComponent },

  // ── Auth ────────────────────────────────────────────────────────────────────
  { path: 'signup', component: SignupComponent },
  { path: 'login',  component: SigninComponent },

  // ── Profil (accessible à tout utilisateur connecté) ─────────────────────────
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },

  // ═══════════════════════════════════════════════════════════════════════════
  // SORTIES (randonnées)
  // USER        : consulter liste, voir détail, s'inscrire, se désinscrire
  // ORGANISATEUR: créer, modifier, supprimer (+ actions user)
  // ═══════════════════════════════════════════════════════════════════════════
  { path: 'sorties',            component: SortieListComponent   },                               // PUBLIC — tout le monde peut voir
  { path: 'sorties/create',     component: SortieFormComponent,   canActivate: [OrganizerGuard] }, // ORGANISATEUR seulement
  { path: 'sorties/edit/:id',   component: SortieFormComponent,   canActivate: [OrganizerGuard] }, // ORGANISATEUR seulement
  { path: 'sorties/:id',        component: SortieDetailComponent  },                               // PUBLIC — inscription gérée dans le composant

  // ═══════════════════════════════════════════════════════════════════════════
  // ÉQUIPES
  // USER        : voir liste, voir détail, rejoindre, quitter
  // ORGANISATEUR: créer, modifier, supprimer équipe (+ ses membres)
  // ═══════════════════════════════════════════════════════════════════════════
  { path: 'equipes',            component: EquipeListComponent   },                               // PUBLIC
  { path: 'equipes/create',     component: EquipeFormComponent,   canActivate: [OrganizerGuard] }, // ORGANISATEUR
  { path: 'equipes/edit/:id',   component: EquipeFormComponent,   canActivate: [OrganizerGuard] }, // ORGANISATEUR
  { path: 'equipes/:id',        component: EquipeDetailComponent  },                               // PUBLIC — rejoindre/quitter dans le composant

  // ═══════════════════════════════════════════════════════════════════════════
  // DASHBOARDS
  // ═══════════════════════════════════════════════════════════════════════════
  { path: 'dashboard',           component: DashboardUserComponent,      canActivate: [AuthGuard] },       // USER connecté
  { path: 'dashboard/organizer', component: DashboardOrganizerComponent, canActivate: [OrganizerGuard] },  // ORGANISATEUR
  { path: 'admin/dashboard',     component: DashboardAdminComponent,     canActivate: [AdminGuard] },      // ADMIN

  // ═══════════════════════════════════════════════════════════════════════════
  // ADMIN (layout enfant)
  // ═══════════════════════════════════════════════════════════════════════════
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [AdminGuard],
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'profil',    component: ProfileComponent   },
      { path: '',          redirectTo: 'dashboard', pathMatch: 'full' }
    ]
  },

  // ═══════════════════════════════════════════════════════════════════════════
  // MODULE AMAL — IA Checklist + Planning SR + Recommandations
  // ═══════════════════════════════════════════════════════════════════════════

  // Checklist IA météo (accessible à tout connecté + visiteur pour démo)
  { path: 'checklist-ia',    component: ChecklistIaComponent },

  // SR1 — Recommandations de sorties selon profil (USER connecté)
  { path: 'recommandations', component: SortieRecommandationsComponent, canActivate: [AuthGuard] },

  // SR2 — Planning intelligent calendrier selon historique (USER connecté)
  { path: 'planning',        component: PlanningSrComponent,            canActivate: [AuthGuard] },

  // ── Wildcard ────────────────────────────────────────────────────────────────
  { path: '**', redirectTo: 'Campino' }
];