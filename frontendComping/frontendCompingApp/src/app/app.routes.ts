
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
import { ListEventComponent } from './client/list-event/list-event.component';
import { EditeEventComponent } from './client/edite-event/edite-event.component';
import { ActivityCreateComponent } from './client/activity-create/activity-create.component';
import { ListActivityComponent } from './client/list-activity/list-activity.component';
import { EditActivityComponent } from './client/edit-activity/edit-activity.component';

import { ProductListComponent } from './product-list/product-list.component';
import { ProductCardComponent } from './product-card/product-card.component';
import { ProductFrontComponent } from './product-front/product-front.component';
import { ProductDetailComponent } from './product-detail/product-detail.component';
import { EditProductComponent } from './edit-product/edit-product.component';
import { ProductPanierComponent } from './product-panier/product-panier.component';
import { ProductCheckoutComponent } from './product-checkout/product-checkout.component';
import { ProductDetailPageComponent } from './product-detail-page/product-detail-page.component';
import { ProductConfirmationCommandeComponent } from './product-confirmation-commande/product-confirmation-commande.component';
import { ProductCommandeListComponent } from './product-commande-list/product-commande-list.component';
import { ProductCommandHistoryComponent } from './product-command-history/product-command-history.component';


export const routes: Routes = [
  { path: 'Campino', component: HomeComponent },
  { path: 'signup', component: SignupComponent },
  { path: 'profile', component: ProfileComponent }, 
  { path: '', redirectTo: 'Campino', pathMatch: 'full' },
  { path: 'login', component: SigninComponent },
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
      { path: 'events', component: EventComponent }
    ]
  },
  { path: 'productTable', component: ProductListComponent},
    { path: 'marketplace', component: ProductFrontComponent},
{
    path: 'products/:id',
    component: ProductDetailComponent
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
];