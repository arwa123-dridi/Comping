
import { Routes } from '@angular/router';
import { SignupComponent } from './signup/signup.component';
import { HomeComponent } from './Home/home/home.component';
import { ProfileComponent } from './profile/profile.component'; 
import { SigninComponent } from './signin/signin.component';
import { AdminLayoutComponent } from './layouts/admin-layout/admin-layout.component';
import { DashboardComponent } from './admin/dashboard/dashboard.component';
import { ProductListComponent } from './product-list/product-list.component';
import { ProductCardComponent } from './product-card/product-card.component';
import { ProductFrontComponent } from './product-front/product-front.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';

export const routes: Routes = [
  { path: 'Campino', component: HomeComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent }, 
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'login', component: SigninComponent },
   {
    path: 'admin',
    component: AdminLayoutComponent,
    children: [
      { path: 'dashboard', component: DashboardComponent }
    ]
  },
  { path: 'productTable', component: ProductListComponent},
    { path: 'marketplace', component: ProductFrontComponent},
{
    path: 'products/:id',
    component: ProductDetailComponent
  }
];