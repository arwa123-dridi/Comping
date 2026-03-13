import { Routes } from '@angular/router';
import { SignupComponent } from './signup/signup.component';
import {HomeComponent} from './Home/home/home.component';

export const routes: Routes = [
  { path: 'Campino', component: HomeComponent},
  { path: 'signup', component: SignupComponent },
  { path: '', redirectTo: 'Campino', pathMatch: 'full' }
];
