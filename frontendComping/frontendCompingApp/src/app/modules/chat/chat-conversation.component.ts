import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommunityService, MessageResponse } from '../../services/community.service';

@Component({
  selector: 'app-chat-conversation',
  standalone: false,
  templateUrl: './chat-conversation.component.html',
  styleUrls: ['./chat-conversation.component.css']
})
export class ChatConversationComponent implements OnInit {
  conversationId = '';
  messages: MessageResponse[] = [];
  newMessage = '';
  loading = false;
  sending = false;
  error = '';
  currentName = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private community: CommunityService
  ) {}

  ngOnInit(): void {
    this.conversationId = this.route.snapshot.paramMap.get('id') ?? '';
    this.currentName = this.community.getCurrentEmail();
    this.loadMessages();
  }

  loadMessages(): void {
    if (!this.conversationId) {
      this.error = 'Conversation introuvable.';
      return;
    }

    this.loading = true;
    this.error = '';
    this.community.getMessages(this.conversationId).subscribe({
      next: messages => {
        this.messages = messages;
        this.loading = false;
        this.community.markAsRead(this.conversationId).subscribe({ next: () => undefined, error: () => undefined });
      },
      error: () => {
        this.error = 'Impossible de charger les messages.';
        this.loading = false;
      }
    });
  }

  sendMessage(): void {
    const contenu = this.newMessage.trim();
    if (!contenu || !this.conversationId) {
      return;
    }

    this.sending = true;
    this.community.sendMessage(this.conversationId, contenu).subscribe({
      next: message => {
        this.messages = [...this.messages, message];
        this.newMessage = '';
        this.sending = false;
      },
      error: () => {
        this.error = 'Message non envoye.';
        this.sending = false;
      }
    });
  }

  isMine(message: MessageResponse): boolean {
    return message.expediteurNom === this.currentName || message.expediteurNom === 'Vous';
  }

  back(): void {
    void this.router.navigate(['/modules/chat']);
  }
}
