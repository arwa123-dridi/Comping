import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommunityService, ConversationResponse, UserStatus } from '../services/community.service';

@Component({
  selector: 'app-chat-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './chat-list.component.html',
  styleUrls: ['./chat-list.component.css']
})
export class ChatListComponent implements OnInit, OnDestroy {
  conversations: ConversationResponse[] = [];
  onlineUsers: UserStatus[] = [];

  // Direct conversation form
  participant2Id = '';
  avisId = '';

  // Group creation form
  groupName = '';
  groupParticipants = ''; // emails séparés par virgule
  showGroupForm = false;

  search = '';
  loading = false;
  creating = false;
  error = '';
  success = '';

  private subs: Subscription[] = [];

  constructor(public community: CommunityService, private router: Router) {}

  ngOnInit(): void {
    this.community.connectNotificationsSocket();
    this.loadConversations();
    this.loadOnlineUsers();

    // Refresh on new messages
    this.subs.push(
      this.community.notifications$.subscribe(notif => {
        if (notif.type === 'NEW_MESSAGE' || notif.type === 'NEW_GROUP') {
          this.loadConversations();
        }
      })
    );

    // Real-time online status update
    this.subs.push(
      this.community.userStatusChanges$.subscribe(({ userId, online }) => {
        this.conversations.forEach(c => {
          if (!c.groupe && this.otherParticipantId(c) === userId) {
            c.autreParticipantEnLigne = online;
          }
        });
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
  }

  get filteredConversations(): ConversationResponse[] {
    const query = this.search.trim().toLowerCase();
    if (!query) return this.conversations;
    return this.conversations.filter(c =>
      this.title(c).toLowerCase().includes(query) ||
      (c.dernierMessageContenu || '').toLowerCase().includes(query)
    );
  }

  get groupConversations(): ConversationResponse[] {
    return this.filteredConversations.filter(c => c.groupe);
  }

  get directConversations(): ConversationResponse[] {
    return this.filteredConversations.filter(c => !c.groupe);
  }

  get unreadTotal(): number {
    return this.conversations.reduce((sum, c) => sum + (c.messagesNonLus || 0), 0);
  }

  loadConversations(): void {
    this.loading = true;
    this.error = '';
    this.community.getConversations().subscribe({
      next: convs => {
        this.conversations = convs;
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les conversations.';
        this.loading = false;
      }
    });
  }

  loadOnlineUsers(): void {
    this.community.getOnlineUsers().subscribe({
      next: users => this.onlineUsers = users,
      error: () => {}
    });
  }

  createConversation(): void {
    const participant = this.participant2Id.trim();
    if (!participant) {
      this.error = 'Email du destinataire requis.';
      return;
    }

    this.creating = true;
    this.error = '';
    this.community.getOrCreateConversation(participant, this.avisId.trim() || undefined).subscribe({
      next: conv => {
        this.creating = false;
        this.participant2Id = '';
        this.avisId = '';
        void this.router.navigate(['/messages', conv.id]);
      },
      error: () => {
        this.error = 'Impossible de créer la conversation.';
        this.creating = false;
      }
    });
  }

  createGroup(): void {
    const name = this.groupName.trim();
    const participants = this.groupParticipants.split(',').map(p => p.trim()).filter(Boolean);

    if (!name) {
      this.error = 'Nom du groupe requis.';
      return;
    }
    if (participants.length < 2) {
      this.error = 'Au moins 2 participants requis pour un groupe.';
      return;
    }

    this.creating = true;
    this.community.createGroup(name, participants).subscribe({
      next: conv => {
        this.creating = false;
        this.groupName = '';
        this.groupParticipants = '';
        this.showGroupForm = false;
        this.success = '✅ Groupe créé !';
        setTimeout(() => this.success = '', 3000);
        void this.router.navigate(['/messages', conv.id]);
      },
      error: (e) => {
        this.error = e.error?.message || 'Création du groupe impossible.';
        this.creating = false;
      }
    });
  }

  openConversation(conv: ConversationResponse): void {
    void this.router.navigate(['/messages', conv.id]);
  }

  title(conv: ConversationResponse): string {
    if (conv.groupe) return conv.nomGroupe || 'Groupe';
    const current = this.community.getCurrentEmail();
    return conv.participant1Id === current
      ? (conv.participant2Nom || '')
      : (conv.participant1Nom || '');
  }

  initials(conv: ConversationResponse): string {
    return (this.title(conv) || 'GR').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
  }

  isOnline(conv: ConversationResponse): boolean {
    return !conv.groupe && !!conv.autreParticipantEnLigne;
  }

  timeAgo(dateStr?: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const diffMs = Date.now() - d.getTime();
    const m = Math.floor(diffMs / 60000);
    if (m < 1) return 'maintenant';
    if (m < 60) return `${m}min`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h}h`;
    const j = Math.floor(h / 24);
    if (j < 7) return `${j}j`;
    return d.toLocaleDateString('fr-FR', { day: '2-digit', month: '2-digit' });
  }

  participantPreview(conv: ConversationResponse): string {
    if (!conv.groupe || !conv.participantNoms) return '';
    if (conv.participantNoms.length <= 3) return conv.participantNoms.join(', ');
    return `${conv.participantNoms.slice(0, 3).join(', ')} +${conv.participantNoms.length - 3}`;
  }

  private otherParticipantId(conv: ConversationResponse): string {
    const current = this.community.getCurrentEmail();
    return conv.participant1Id === current ? (conv.participant2Id || '') : (conv.participant1Id || '');
  }
}
