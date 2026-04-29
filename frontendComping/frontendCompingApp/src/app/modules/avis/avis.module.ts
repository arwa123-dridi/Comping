import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AvisComponent } from './avis.component';
import { AvisDetailComponent } from './avis-detail.component';

const routes: Routes = [
  { path: '', component: AvisComponent },
  { path: ':id', component: AvisDetailComponent }
];

@NgModule({
  declarations: [
    AvisComponent,
    AvisDetailComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class AvisModule { }

