// src/app/app.routes.ts — VERSION FINALE CORRIGÉE
import { Routes } from '@angular/router';

import { SignupComponent }        from './signup/signup.component';
import { HomeComponent }          from './Home/home/home.component';
import { ProfileComponent }       from './profile/profile.component';
import { SigninComponent }        from './signin/signin.component';
import { AdminLayoutComponent }   from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent }     from './admin/dashboard/dashboard.component';

import { SortieListComponent }            from './sortie/sortie-list/sortie-list.component';
import { SortieFormComponent }            from './sortie/sortie-form/sortie-form.component';
import { SortieDetailComponent }          from './sortie/sortie-detail/sortie-detail.component';
import { EquipeFormComponent }            from './equipe/equipe-form/equipe-form.component';
import { EquipeDetailComponent }          from './equipe/equipe-detail/equipe-detail.component';
import { DashboardUserComponent }         from './dashboard/dashboard-user/dashboard-user.component';
import { DashboardOrganizerComponent }    from './dashboard/dashboard-organizer/dashboard-organizer.component';
import { DashboardAdminComponent }        from './dashboard/dashboard-admin/dashboard-admin.component';
import { ChecklistIaComponent }           from './checklist-ia/checklist-ia.component';
import { PlanningSrComponent }            from './planning-sr/planning-sr.component';
import { SortieRecommandationsComponent } from './sortie/sortie-recommandations/sortie-recommandations-module';

// Admin lists
import { AdminSortiesListComponent }       from './admin/admin-sorties-list.component';
import { AdminEquipesListComponent }       from './admin/admin-equipes-list.component';
import { AdminOrganisateursListComponent } from './admin/admin-organisateurs-list.component';
import { AdminParticipantsListComponent }  from './admin/admin-participants-list.component';

// Guards
import { AuthGuard }          from './guards/auth.guard';
import { AdminGuard }         from './guards/admin.guard';
import { AdminOnlyGuard }     from './guards/admin-only.guard';
import { OrganizerGuard }     from './guards/organizer.guard';
import { OrganizerOnlyGuard } from './guards/organizer-only.guard';

// Autres modules collègues
import { UsersComponent }          from './admin/users/users.component';
import { EventComponent }          from './admin/event/event.component';
import { AddEventComponent }       from './client/add-event/add-event.component';
import { ListEventComponent }      from './client/list-event/list-event.component';
import { EditeEventComponent }     from './client/edite-event/edite-event.component';
import { ActivityCreateComponent } from './client/activity-create/activity-create.component';
import { ListActivityComponent }   from './client/list-activity/list-activity.component';
import { EditActivityComponent }   from './client/edit-activity/edit-activity.component';
import { ProductListComponent }    from './product-list/product-list.component';
import { ProductFrontComponent }   from './product-front/product-front.component';
import { ProductDetailComponent }  from './product-detail/product-detail.component';
import { SuccessEvent }            from './pages/success-event/success-event';
import { RecommendationEvent }     from './client/recommendation-event/recommendation-event';
import { ActivityComponent }       from './admin/activity/activity';
import { EquipeListComponent } from './equipe/equipe-list/equipe-list.component';
import { AdminStatisticsComponent } from './admin/statistics/admin-statistics.component';

export const routes: Routes = [

  // ─── PUBLIQUES ─────────────────────────────────────────────
  { path: '',        redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'Campino', component: HomeComponent },
  { path: 'signup',  component: SignupComponent },
  { path: 'login',   component: SigninComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },

  // Événements / Activités (autres modules)
  { path: 'events/add',          component: AddEventComponent },
  { path: 'events/list',         component: ListEventComponent },
  { path: 'events/edit/:id',     component: EditeEventComponent },
  { path: 'activities/add',      component: ActivityCreateComponent },
  { path: 'activities/list',     component: ListActivityComponent },
  { path: 'activities/edit/:id', component: EditActivityComponent },
  { path: 'success',             component: SuccessEvent },
  { path: 'recommendation',      component: RecommendationEvent },
  { path: 'productTable',        component: ProductListComponent },
  { path: 'marketplace',         component: ProductFrontComponent },
  { path: 'products/:id',        component: ProductDetailComponent },

  // Sorties & Équipes publiques
  { path: 'sorties',     component: SortieListComponent   },
  { path: 'sorties/:id', component: SortieDetailComponent },
  { path: 'equipes',     component: EquipeListComponent   },
  { path: 'equipes/:id', component: EquipeDetailComponent },

  // ─── ESPACE USER CONNECTÉ ──────────────────────────────────
  {
    path: 'dashboard',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '',                component: DashboardUserComponent },
      { path: 'recommandations', component: SortieRecommandationsComponent },
      { path: 'planning',        component: PlanningSrComponent },
      { path: 'checklist-ia',    component: ChecklistIaComponent },
    ]
  },

  // ─── BACK OFFICE (ADMIN + ORGANISATEUR) ────────────────────
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [OrganizerGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    
 
      // Dashboard principal (admin/dashboard)
      { path: 'dashboard', component: DashboardComponent },

      // Dashboard organizer
      { path: 'organizer', component: DashboardOrganizerComponent, canActivate: [OrganizerOnlyGuard] },

      // Dashboard admin board (admin only)
      { path: 'admin-board', component: DashboardAdminComponent, canActivate: [AdminOnlyGuard] },
// Dans le tableau children du path 'admin', ajoute cette ligne
      { path: 'statistics', component: AdminStatisticsComponent, canActivate: [AdminOnlyGuard] },
      // ── Sorties ──
      { path: 'sorties/create',   component: SortieFormComponent,   canActivate: [OrganizerOnlyGuard] },
      { path: 'sorties/edit/:id', component: SortieFormComponent },
      { path: 'sorties/:id',      component: SortieDetailComponent },
      { path: 'sorties',          component: SortieListComponent   },

      // ── Équipes ──
      { path: 'equipes/create',   component: EquipeFormComponent,   canActivate: [OrganizerOnlyGuard] },
      { path: 'equipes/edit/:id', component: EquipeFormComponent },
      { path: 'equipes/:id',      component: EquipeDetailComponent },
      { path: 'equipes',          component: EquipeListComponent },

      // ── Outils IA (organisateur) ──
      { path: 'checklist-ia', component: ChecklistIaComponent },
      { path: 'planning',     component: PlanningSrComponent  },

      // ── Pages admin uniquement ──
      { path: 'admin-sorties',       component: AdminSortiesListComponent,       canActivate: [AdminOnlyGuard] },
      { path: 'admin-equipes',       component: AdminEquipesListComponent,       canActivate: [AdminOnlyGuard] },
      { path: 'admin-organisateurs', component: AdminOrganisateursListComponent, canActivate: [AdminOnlyGuard] },
      { path: 'admin-participants',  component: AdminParticipantsListComponent,  canActivate: [AdminOnlyGuard] },

      // ── Modules collègues (ne pas toucher) ──
      { path: 'users',      component: UsersComponent    },
      { path: 'events',     component: EventComponent    },
      { path: 'activities', component: ActivityComponent },
    ]
  },

  { path: '**', redirectTo: 'Campino' }
];
