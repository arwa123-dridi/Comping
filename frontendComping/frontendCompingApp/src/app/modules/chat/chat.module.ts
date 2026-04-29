import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Routes } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { ChatListComponent } from './chat-list.component';
import { ChatConversationComponent } from './chat-conversation.component';

const routes: Routes = [
  { path: '', component: ChatListComponent },
  { path: ':id', component: ChatConversationComponent }
];

@NgModule({
  declarations: [
    ChatListComponent,
    ChatConversationComponent
  ],
  imports: [
    CommonModule,
    FormsModule,
    RouterModule.forChild(routes)
  ]
})
export class ChatModule { }

