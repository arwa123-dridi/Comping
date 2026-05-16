import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommunityService, ConversationResponse, UserStatus, AbonnementResponse } from '../services/community.service';
import { CommunitySidebarComponent } from '../shared/community-sidebar/community-sidebar.component';

@Component({
  selector: 'app-chat-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, RouterOutlet, CommunitySidebarComponent],
  templateUrl: './chat-list.component.html',
  styleUrls: ['./chat-list.component.css']
})
export class ChatListComponent implements OnInit, OnDestroy {
  conversations: ConversationResponse[] = [];
  onlineUsers: UserStatus[] = [];

  followingList: AbonnementResponse[] = [];

  // Direct conversation form
  selectedFriendId = '';

  // Group creation form
  groupName = '';
  selectedGroupMemberIds: string[] = [];
  showGroupForm = false;
  showNewConvForm = false;

  search = '';
  loading = false;
  creating = false;
  error = '';
  success = '';
  convOpen = false;
  sidebarCollapsed = false;

  private subs: Subscription[] = [];

  constructor(public community: CommunityService, private router: Router) {}

  ngOnInit(): void {
    this.community.connectNotificationsSocket();
    this.loadConversations();
    this.loadOnlineUsers();
    this.loadFollowing();

    // Refresh on new messages
    this.subs.push(
      this.community.notifications$.subscribe(notif => {
        if (notif.type === 'NEW_MESSAGE' || notif.type === 'NEW_GROUP') {
          this.loadConversations();
        }
      })
    );

    // Mise à jour optimiste du statut en ligne sans rechargement API — poussé par WebSocket depuis le backend
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

  loadFollowing(): void {
    this.community.getMyFollowing().subscribe({
      next: list => this.followingList = list,
      error: () => {}
    });
  }

  toggleGroupForm(): void {
    this.showGroupForm = !this.showGroupForm;
    this.showNewConvForm = false;
    if (!this.showGroupForm) this.selectedGroupMemberIds = [];
    this.error = '';
  }

  toggleNewConvForm(): void {
    this.showNewConvForm = !this.showNewConvForm;
    this.showGroupForm = false;
    if (!this.showNewConvForm) this.selectedFriendId = '';
    if (this.showGroupForm === false) this.selectedGroupMemberIds = [];
    this.error = '';
  }

  toggleGroupMember(email: string): void {
    const idx = this.selectedGroupMemberIds.indexOf(email);
    if (idx === -1) {
      this.selectedGroupMemberIds = [...this.selectedGroupMemberIds, email];
    } else {
      this.selectedGroupMemberIds = this.selectedGroupMemberIds.filter(e => e !== email);
    }
  }

  isGroupMemberSelected(email: string): boolean {
    return this.selectedGroupMemberIds.includes(email);
  }

  createConversation(): void {
    if (!this.selectedFriendId) {
      this.error = 'Sélectionnez un ami.';
      return;
    }
    this.creating = true;
    this.error = '';
    this.community.getOrCreateConversation(this.selectedFriendId).subscribe({
      next: conv => {
        this.creating = false;
        this.selectedFriendId = '';
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
    if (!name) {
      this.error = 'Nom du groupe requis.';
      return;
    }
    if (this.selectedGroupMemberIds.length < 2) {
      this.error = 'Sélectionnez au moins 2 participants.';
      return;
    }
    this.creating = true;
    this.community.createGroup(name, [...this.selectedGroupMemberIds]).subscribe({
      next: conv => {
        this.creating = false;
        this.groupName = '';
        this.selectedGroupMemberIds = [];
        this.showGroupForm = false;
        this.success = 'Groupe créé !';
        setTimeout(() => this.success = '', 3000);
        void this.router.navigate(['/messages', conv.id]);
      },
      error: (e: { error?: { message?: string } }) => {
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

  // Aperçu tronqué des membres d'un groupe pour éviter l'overflow dans la liste
  participantPreview(conv: ConversationResponse): string {
    if (!conv.groupe || !conv.participantNoms) return '';
    if (conv.participantNoms.length <= 3) return conv.participantNoms.join(', ');
    return `${conv.participantNoms.slice(0, 3).join(', ')} +${conv.participantNoms.length - 3}`;
  }

  userInitials(userId: string): string {
    if (!userId) return '?';
    const parts = userId.split(/[@.\s]/);
    return parts.filter(Boolean).slice(0, 2).map(p => p[0]).join('').toUpperCase() || userId[0].toUpperCase();
  }

  // En conversation directe, l'autre participant n'est pas toujours participant2 (dépend de qui a initié)
  private otherParticipantId(conv: ConversationResponse): string {
    const current = this.community.getCurrentEmail();
    return conv.participant1Id === current ? (conv.participant2Id || '') : (conv.participant1Id || '');
  }
}
