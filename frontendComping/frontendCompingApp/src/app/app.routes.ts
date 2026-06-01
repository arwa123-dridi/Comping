// src/app/app.routes.ts — VERSION FUSIONNÉE (tout garder)
import { Routes } from '@angular/router';

/* ===================== IMPORTS HEAD (tes composants) ===================== */
import { SignupComponent } from './signup/signup.component';
import { HomeComponent } from './Home/home/home.component';
import { ProfileComponent } from './profile/profile.component';
import { SigninComponent } from './signin/signin.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';

import { SortieListComponent } from './sortie/sortie-list/sortie-list.component';
import { SortieFormComponent } from './sortie/sortie-form/sortie-form.component';
import { SortieDetailComponent } from './sortie/sortie-detail/sortie-detail.component';
import { EquipeFormComponent } from './equipe/equipe-form/equipe-form.component';
import { EquipeDetailComponent } from './equipe/equipe-detail/equipe-detail.component';
import { DashboardUserComponent } from './dashboard/dashboard-user/dashboard-user.component';
import { DashboardOrganizerComponent } from './dashboard/dashboard-organizer/dashboard-organizer.component';
import { DashboardAdminComponent } from './dashboard/dashboard-admin/dashboard-admin.component';
import { ChecklistIaComponent } from './checklist-ia/checklist-ia.component';
import { PlanningSrComponent } from './planning-sr/planning-sr.component';
import { SortieRecommandationsComponent } from './sortie/sortie-recommandations/sortie-recommandations-module';

// Admin lists (HEAD)
import { AdminSortiesListComponent } from './admin/admin-sorties-list.component';
import { AdminEquipesListComponent } from './admin/admin-equipes-list.component';
import { AdminOrganisateursListComponent } from './admin/admin-organisateurs-list.component';
import { AdminParticipantsListComponent } from './admin/admin-participants-list.component';

// Guards (HEAD)
import { AuthGuard } from './guards/auth.guard';
import { AdminGuard } from './guards/admin.guard';
import { AdminOnlyGuard } from './guards/admin-only.guard';
import { OrganizerGuard } from './guards/organizer.guard';
import { OrganizerOnlyGuard } from './guards/organizer-only.guard';

// Autres modules collègues (HEAD)
import { UsersComponent } from './admin/users/users.component';
import { EventComponent } from './admin/event/event.component';
import { AddEventComponent } from './client/add-event/add-event.component';
import { ListEventComponent } from './client/list-event/list-event.component';
import { EditeEventComponent } from './client/edite-event/edite-event.component';
import { ActivityCreateComponent } from './client/activity-create/activity-create.component';
import { ListActivityComponent } from './client/list-activity/list-activity.component';
import { EditActivityComponent } from './client/edit-activity/edit-activity.component';
import { ProductListComponent } from './product-list/product-list.component';
import { ProductFrontComponent } from './product-front/product-front.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';
import { SuccessEvent } from './pages/success-event/success-event';
import { RecommendationEvent } from './client/recommendation-event/recommendation-event';
import { ActivityComponent } from './admin/activity/activity';
import { EquipeListComponent } from './equipe/equipe-list/equipe-list.component';
import { AdminStatisticsComponent } from './admin/statistics/admin-statistics.component';

/* ===================== IMPORTS MARIEM (marketplace, chat, social, reset) ===================== */
import { EditProductComponent } from './edit-product/edit-product.component';
import { ProductPanierComponent } from './product-panier/product-panier.component';
import { ProductCheckoutComponent } from './product-checkout/product-checkout.component';
import { ProductDetailPageComponent } from './product-detail-page/product-detail-page.component';
import { ProductConfirmationCommandeComponent } from './product-confirmation-commande/product-confirmation-commande.component';
import { ProductCommandeListComponent } from './product-commande-list/product-commande-list.component';
import { ProductCommandHistoryComponent } from './product-command-history/product-command-history.component';
import { ProductLivreur } from './product-livreur/product-livreur';

import { ChatConversationComponent } from './chat/chat-conversation.component';
import { ChatListComponent } from './chat/chat-list.component';
import { PostsFeedComponent } from './reseau-social/posts-feed.component';
import { UserPostsComponent } from './reseau-social/user-posts.component';
import { AvisComponent } from './avis/avis.component';
import { HomeComponent as CampinoSocialHomeComponent } from './campinosocialhome/campinosocialhome.component';
import { ResetPasswordComponent } from './reset-password/reset-password.component';
import { ForgotPasswordComponent } from './forgot-password/forgot-password.component';

export const routes: Routes = [

  // ─── PUBLIQUES (HEAD + MARIEM) ─────────────────────────────────────
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'Campino', component: HomeComponent },

  { path: 'signup', component: SignupComponent },
  { path: 'login', component: SigninComponent },
  { path: 'profile', component: ProfileComponent, canActivate: [AuthGuard] },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },

  // SOCIAL (Mariem)
  { path: 'social-home', component: CampinoSocialHomeComponent },
  { path: 'reviews', component: AvisComponent },
  { path: 'community', component: PostsFeedComponent },
  { path: 'community/user/:userId', component: UserPostsComponent },

  // Événements / Activités (HEAD)
  { path: 'events/add', component: AddEventComponent },
  { path: 'events/list', component: ListEventComponent },
  { path: 'events/edit/:id', component: EditeEventComponent },
  { path: 'activities/add', component: ActivityCreateComponent },
  { path: 'activities/list', component: ListActivityComponent },
  { path: 'activities/edit/:id', component: EditActivityComponent },
  { path: 'success', component: SuccessEvent },
  { path: 'recommendation', component: RecommendationEvent },

  // MARKETPLACE (Mariem)
  { path: 'productTable', component: ProductListComponent },
  { path: 'marketplace', component: ProductFrontComponent },
  { path: 'products/:id', component: ProductDetailComponent },
  { path: 'edit-product/:id', component: EditProductComponent },
  { path: 'panier', component: ProductPanierComponent },
  { path: 'command', component: ProductCheckoutComponent },
  { path: 'product/:id', component: ProductDetailPageComponent },
  { path: 'confirm-order', component: ProductConfirmationCommandeComponent },
  { path: 'commandList', component: ProductCommandeListComponent },
  { path: 'commandHistory', component: ProductCommandHistoryComponent },
  { path: 'livraison', component: ProductLivreur },

  // Sorties & Équipes publiques (HEAD)
  { path: 'sorties', component: SortieListComponent },
  { path: 'sorties/:id', component: SortieDetailComponent },
  { path: 'equipes', component: EquipeListComponent },
  { path: 'equipes/:id', component: EquipeDetailComponent },

  // CHAT (Mariem)
  {
    path: 'messages',
    component: ChatListComponent,
    children: [
      { path: ':id', component: ChatConversationComponent }
    ]
  },

  // ─── ESPACE USER CONNECTÉ (HEAD) ──────────────────────────────────
  {
    path: 'dashboard',
    component: AdminLayoutComponent,
    canActivate: [AuthGuard],
    children: [
      { path: '', component: DashboardUserComponent },
      { path: 'recommandations', component: SortieRecommandationsComponent },
      { path: 'planning', component: PlanningSrComponent },
      { path: 'checklist-ia', component: ChecklistIaComponent },
      // Sorties et équipes dans le layout pour les connectés
      { path: 'sorties', component: SortieListComponent },
      { path: 'sorties/:id', component: SortieDetailComponent },
      { path: 'equipes', component: EquipeListComponent },
      { path: 'equipes/:id', component: EquipeDetailComponent },
    ]
  },

  // ─── BACK OFFICE (ADMIN + ORGANISATEUR) – VERSION HEAD COMPLÈTE ────
  {
    path: 'admin',
    component: AdminLayoutComponent,
    canActivate: [OrganizerGuard],
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { path: 'dashboard', component: DashboardComponent },
      { path: 'organizer', component: DashboardOrganizerComponent, canActivate: [OrganizerOnlyGuard] },
      { path: 'admin-board', component: DashboardAdminComponent, canActivate: [AdminOnlyGuard] },
      { path: 'statistics', component: AdminStatisticsComponent, canActivate: [AdminOnlyGuard] },

      // Sorties
      { path: 'sorties/create', component: SortieFormComponent, canActivate: [OrganizerOnlyGuard] },
      { path: 'sorties/edit/:id', component: SortieFormComponent },
      { path: 'sorties/:id', component: SortieDetailComponent },
      { path: 'sorties', component: SortieListComponent },

      // Équipes
      { path: 'equipes/create', component: EquipeFormComponent, canActivate: [OrganizerOnlyGuard] },
      { path: 'equipes/edit/:id', component: EquipeFormComponent },
      { path: 'equipes/:id', component: EquipeDetailComponent },
      { path: 'equipes', component: EquipeListComponent },

      // Outils IA
      { path: 'checklist-ia', component: ChecklistIaComponent },
      { path: 'planning', component: PlanningSrComponent },

      // Pages admin uniquement
      { path: 'admin-sorties', component: AdminSortiesListComponent, canActivate: [AdminOnlyGuard] },
      { path: 'admin-equipes', component: AdminEquipesListComponent, canActivate: [AdminOnlyGuard] },
      { path: 'admin-organisateurs', component: AdminOrganisateursListComponent, canActivate: [AdminOnlyGuard] },
      { path: 'admin-participants', component: AdminParticipantsListComponent, canActivate: [AdminOnlyGuard] },

      // Modules collègues (HEAD)
      { path: 'users', component: UsersComponent },
      { path: 'events', component: EventComponent },
      { path: 'activities', component: ActivityComponent },
    ]
  },

  // FALLBACK
  { path: '**', redirectTo: 'Campino' }
];
