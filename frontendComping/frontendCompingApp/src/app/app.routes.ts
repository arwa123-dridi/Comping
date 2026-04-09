
import { Routes } from '@angular/router';
import { SignupComponent } from './signup/signup.component';
import { HomeComponent } from './Home/home/home.component';
import { ProfileComponent } from './profile/profile.component'; 
import { SigninComponent } from './signin/signin.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { UsersComponent } from './admin/users/users.component';
import { EventComponent } from './admin/event/event.component';
import { AddEventComponent } from './client/add-event/add-event.component';

export const routes: Routes = [
  { path: 'Campino', component: HomeComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent }, 
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'login', component: SigninComponent },
   { path: 'events/add', component: AddEventComponent },
   {
    path: 'admin',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent },
      { path: 'users', component: UsersComponent },
      { path: 'events', component: EventComponent }
    ]
  },
];