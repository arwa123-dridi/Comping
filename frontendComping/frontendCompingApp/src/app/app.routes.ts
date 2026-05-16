// src/app/app.routes.ts — FUSION COMPLÈTE (aucune perte)
import { Routes } from '@angular/router';
<<<<<<< HEAD



import { EditProductComponent } from './edit-product/edit-product.component';
import { ProductPanierComponent } from './product-panier/product-panier.component';
import { ProductCheckoutComponent } from './product-checkout/product-checkout.component';
import { ProductDetailPageComponent } from './product-detail-page/product-detail-page.component';
import { ProductConfirmationCommandeComponent } from './product-confirmation-commande/product-confirmation-commande.component';
import { ProductCommandeListComponent } from './product-commande-list/product-commande-list.component';
import { ProductCommandHistoryComponent } from './product-command-history/product-command-history.component';
import { ProductLivreur } from './product-livreur/product-livreur';

// Composants communs (présents dans les deux versions)
import { SignupComponent }        from './signup/signup.component';
import { HomeComponent }          from './Home/home/home.component';
import { ProfileComponent }       from './profile/profile.component';
import { SigninComponent }        from './signin/signin.component';
import { AdminLayoutComponent }   from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent }     from './admin/dashboard/dashboard.component';

// Composants de la branche USER (version principale)
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
import { SortieRecommandationsComponent } from './sortie/sortie-recommandations/sortie-recommandations-module';

// Guards (version USER)
import { AuthGuard }      from './guards/auth.guard';
import { AdminGuard }     from './guards/admin.guard';
import { OrganizerGuard } from './guards/organizer.guard';

// Composants de la branche COLLÈGUE (admin/users, events, activities, marketplace...)
import { UsersComponent }           from './admin/users/users.component';
import { EventComponent }           from './admin/event/event.component';
import { AddEventComponent }        from './client/add-event/add-event.component';
import { ListEventComponent }       from './client/list-event/list-event.component';
import { EditeEventComponent }      from './client/edite-event/edite-event.component';
import { ActivityCreateComponent }  from './client/activity-create/activity-create.component';
import { ListActivityComponent }    from './client/list-activity/list-activity.component';
import { EditActivityComponent }    from './client/edit-activity/edit-activity.component';
import { ProductListComponent }     from './product-list/product-list.component';
import { ProductCardComponent }     from './product-card/product-card.component';
import { ProductFrontComponent }    from './product-front/product-front.component';
import { ProductDetailComponent }   from './product-detail/product-detail.component';
import { SuccessEvent }             from './pages/success-event/success-event';
import { RecommendationEvent }      from './client/recommendation-event/recommendation-event';
import { ActivityComponent }        from './admin/activity/activity';

export const routes: Routes = [
=======
import { SignupComponent } from './signup/signup.component';
import { HomeComponent } from './Home/home/home.component';
import { ProfileComponent } from './profile/profile.component'; 
import { SigninComponent } from './signin/signin.component';
import { ReservationsComponent } from './reservations/reservations.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { UsersComponent } from './admin/users/users.component';
import { EventComponent } from './admin/event/event.component';
import { AddEventComponent } from './client/add-event/add-event.component';
import { ListEventComponent } from './client/list-event/list-event.component';
import { EditeEventComponent } from './client/edite-event/edite-event.component';
import { ActivityCreateComponent } from './client/activity-create/activity-create.component';
import { ListActivityComponent } from './client/list-activity/list-activity.component';
import { EditActivityComponent } from './client/edit-activity/edit-activity.component';

import { ProductListComponent } from './product-list/product-list.component';
import { ProductCardComponent } from './product-card/product-card.component';
import { ProductFrontComponent } from './product-front/product-front.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';
import { CampingSiteComponent } from './admin/camping-site/camping-site';
import { PaiementComponent } from './paiement/paiement';
import { ReservationsCamping } from './admin/reservations-camping/reservations-camping';

>>>>>>> origin/ahmed

  // ──────────────────────────────────────────────────────────────
  // ROUTES PUBLIQUES (fusion des deux versions)
  // ──────────────────────────────────────────────────────────────
  { path: '',        redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'Campino', component: HomeComponent },
<<<<<<< HEAD
  { path: 'signup',  component: SignupComponent },
  { path: 'login',   component: SigninComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] }, // gardé de USER

  // Routes "client" de la collègue (events, activities) – publiques (sans guard)
  { path: 'events/add',      component: AddEventComponent },
  { path: 'events/list',     component: ListEventComponent },
  { path: 'events/edit/:id', component: EditeEventComponent },
  { path: 'activities/add',  component: ActivityCreateComponent },
  { path: 'activities/list', component: ListActivityComponent },
  { path: 'activities/edit/:id', component: EditActivityComponent },
  { path: 'success',         component: SuccessEvent },
{ path: 'recommendation', component: RecommendationEvent },

  // Routes marketplace (collègue)
  { path: 'productTable',  component: ProductListComponent },
  { path: 'marketplace',   component: ProductFrontComponent },
  { path: 'products/:id',  component: ProductDetailComponent },

  // Routes publiques "sorties" et "equipes" (USER) – sans layout
  { path: 'sorties',          component: SortieListComponent   },
  { path: 'sorties/:id',      component: SortieDetailComponent },
  { path: 'equipes',          component: EquipeListComponent   },
  { path: 'equipes/:id',      component: EquipeDetailComponent },

  // ──────────────────────────────────────────────────────────────
  // ESPACE CONNECTÉ (dashboard utilisateur standard) – layout AdminLayout
  // ──────────────────────────────────────────────────────────────
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

=======
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent }, 
  {path: 'reservations', component: ReservationsComponent},  
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'login', component: SigninComponent },
  { path: 'paiement/:id', component:   PaiementComponent },
   { path: 'events/add', component: AddEventComponent },
     { path: 'events/list', component: ListEventComponent },
       { path: 'events/edit/:id', component: EditeEventComponent },
         { path: 'activities/add', component: ActivityCreateComponent },
            { path: 'activities/list', component: ListActivityComponent },
             { path: 'activities/edit/:id', component: EditActivityComponent },
   {
    path: 'admin',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'users', component: UsersComponent },
      { path: 'events', component: EventComponent },
      { path: 'camping-sites', component: CampingSiteComponent },
      { path: 'reservations-camping', component: ReservationsCamping },
    ]
  },
>>>>>>> origin/ahmed
  { path: 'productTable', component: ProductListComponent},
    { path: 'marketplace', component: ProductFrontComponent},
{
    path: 'products/:id',
    component: ProductDetailComponent
<<<<<<< HEAD
  },
  { path: 'edit-product/:id', component: EditProductComponent },
  
  { path: 'panier', component: ProductPanierComponent },
   { path: 'command', component: ProductCheckoutComponent },
     { path: 'product/:id', component: ProductDetailPageComponent },
 {
    path: 'confirm-order',
    component: ProductConfirmationCommandeComponent
  },

   {
    path: 'commandList',
    component: ProductCommandeListComponent
  },

    {
    path: 'commandHistory',
    component: ProductCommandHistoryComponent
  },

    {
    path: 'livraison',
    component: ProductLivreur
  },


  // ──────────────────────────────────────────────────────────────
  // BACK OFFICE ADMIN / ORGANISATEUR (fusion des deux)
  //   - Garde les enfants de la version USER (sorties, equipes, organizer, admin-board)
  //   - Ajoute les enfants de la collègue (users, events, activities)
  // ──────────────────────────────────────────────────────────────
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [OrganizerGuard],      // garde de USER (orga ou admin)
    children: [
      // Dashboard principal
      { path: '',          redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },

      // --- Enfants de la branche USER ---
      { path: 'sorties',          component: SortieListComponent   },
      { path: 'sorties/create',   component: SortieFormComponent   },
      { path: 'sorties/edit/:id', component: SortieFormComponent   },
      { path: 'sorties/:id',      component: SortieDetailComponent },

      { path: 'equipes',          component: EquipeListComponent   },
      { path: 'equipes/create',   component: EquipeFormComponent   },
      { path: 'equipes/edit/:id', component: EquipeFormComponent   },
      { path: 'equipes/:id',      component: EquipeDetailComponent },

      { path: 'organizer',   component: DashboardOrganizerComponent },
      { path: 'admin-board', component: DashboardAdminComponent, canActivate: [AdminGuard] },

      // --- Enfants de la branche COLLÈGUE ---
      { path: 'users',     component: UsersComponent     },
      { path: 'events',    component: EventComponent     },
      { path: 'activities', component: ActivityComponent },
    ]
  },

  // Route wildcard (redirection)
  { path: '**', redirectTo: 'Campino' }

=======
  }
>>>>>>> origin/ahmed
];