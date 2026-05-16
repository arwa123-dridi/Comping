
import { Routes } from '@angular/router';
import { SignupComponent } from './signup/signup.component';
import { HomeComponent } from './Home/home/home.component';
import { ProfileComponent } from './profile/profile.component'; 
import { SigninComponent } from './signin/signin.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { UrgenceComponent } from './urgence/urgence.component';
import { AlertesAdminComponent } from './admin/alertes-admin/alertes-admin.component';
import { UrgencesAdminComponent } from './admin/urgences-admin/urgences-admin.component';
import { IncidentsAdminComponent } from './admin/incidents-admin/incidents-admin.component';
import { SecuriteAdminComponent } from './admin/securite-admin/securite-admin.component';
import { EmergencyChatbotComponent } from './admin/emergency-chatbot/emergency-chatbot.component';
import { PlaceholderPageComponent } from './shared/placeholder-page/placeholder-page.component';

export const routes: Routes = [
  { path: 'Campino', component: HomeComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent }, 
  { path: 'urgence', component: UrgenceComponent },
  { path: 'map', component: PlaceholderPageComponent, data: { title: 'Carte Météo' } },
  { path: 'materiel', component: PlaceholderPageComponent, data: { title: 'Matériel' } },
  { path: 'experiences', component: PlaceholderPageComponent, data: { title: 'Expériences' } },
  { path: 'blog', component: PlaceholderPageComponent, data: { title: 'Blog' } },
  { path: 'about', component: PlaceholderPageComponent, data: { title: 'À propos' } },
  { path: 'emplacements', component: PlaceholderPageComponent, data: { title: 'Emplacements' } },
  { path: 'reservations', component: PlaceholderPageComponent, data: { title: 'Réservations' } },
  { path: 'become-host', component: PlaceholderPageComponent, data: { title: 'Devenir hôte' } },
  { path: 'faq', component: PlaceholderPageComponent, data: { title: 'FAQ' } },
  { path: 'cgu', component: PlaceholderPageComponent, data: { title: 'CGU' } },
  { path: 'privacy', component: PlaceholderPageComponent, data: { title: 'Confidentialité' } },
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'login', component: SigninComponent },
   {
    path: 'admin',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'alertes', component: AlertesAdminComponent },
      { path: 'urgences', component: UrgencesAdminComponent },
      { path: 'incidents', component: IncidentsAdminComponent },
      { path: 'securite', component: SecuriteAdminComponent },
      { path: 'ai-chatbot', component: EmergencyChatbotComponent }
    ]
  },
  { path: '**', redirectTo: 'Campino' }
];
