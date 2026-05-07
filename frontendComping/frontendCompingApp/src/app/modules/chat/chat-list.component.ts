import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommunityService, ConversationResponse } from '../../services/community.service';

@Component({
  selector: 'app-chat-list',
  standalone: false,
  templateUrl: './chat-list.component.html',
  styleUrls: ['./chat-list.component.css']
})
export class ChatListComponent implements OnInit {
  conversations: ConversationResponse[] = [];
  participant2Id = '';
  avisId = '';
  search = '';
  loading = false;
  creating = false;
  error = '';

  constructor(
    private community: CommunityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadConversations();
  }

  get filteredConversations(): ConversationResponse[] {
    const query = this.search.trim().toLowerCase();
    if (!query) {
      return this.conversations;
    }
    return this.conversations.filter(conv =>
      conv.participant1Nom.toLowerCase().includes(query) ||
      conv.participant2Nom.toLowerCase().includes(query) ||
      (conv.avisId ?? '').toLowerCase().includes(query)
    );
  }

  get unreadTotal(): number {
    return this.conversations.reduce((sum, conv) => sum + conv.messagesNonLus, 0);
  }

  loadConversations(): void {
    this.loading = true;
    this.error = '';
    this.community.getConversations().subscribe({
      next: conversations => {
        this.conversations = conversations;
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les conversations.';
        this.loading = false;
      }
    });
  }

  createConversation(): void {
    const participant = this.participant2Id.trim();
    if (!participant) {
      this.error = 'Le destinataire est obligatoire.';
      return;
    }

    this.creating = true;
    this.error = '';
    this.community.getOrCreateConversation(participant, this.avisId.trim() || undefined).subscribe({
      next: conversation => {
        this.creating = false;
        this.participant2Id = '';
        this.avisId = '';
        void this.router.navigate(['/modules/chat', conversation.id]);
      },
      error: () => {
        this.error = 'Creation de conversation impossible.';
        this.creating = false;
      }
    });
  }

  openConversation(conversation: ConversationResponse): void {
    void this.router.navigate(['/modules/chat', conversation.id]);
  }

  initials(conversation: ConversationResponse): string {
    const current = this.community.getCurrentEmail();
    const name = conversation.participant1Id === current ? conversation.participant2Nom : conversation.participant1Nom;
    return (name || 'US').slice(0, 2).toUpperCase();
  }

  title(conversation: ConversationResponse): string {
    const current = this.community.getCurrentEmail();
    return conversation.participant1Id === current ? conversation.participant2Nom : conversation.participant1Nom;
  }
}
