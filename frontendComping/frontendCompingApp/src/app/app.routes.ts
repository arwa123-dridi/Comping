import { Routes } from '@angular/router';
import { SignupComponent } from './signup/signup.component';
import { ProfileComponent } from './profile/profile.component'; 

export const routes: Routes = [
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent }, 
  { path: '', redirectTo: 'signup', pathMatch: 'full' } 
];