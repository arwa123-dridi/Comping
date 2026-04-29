import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PostsFeedComponent } from './posts-feed.component';
import { PostDetailComponent } from './post-detail.component';

const routes: Routes = [
  { path: '', component: PostsFeedComponent },
  { path: ':id', component: PostDetailComponent }
];

@NgModule({
  declarations: [
    PostsFeedComponent,
    PostDetailComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class ReseauSocialModule { }

