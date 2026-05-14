import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  AbonnementResponse,
  AvisResponse,
  CampeurInfo,
  CommentaireResponse,
  CommunityService,
  PostResponse,
  PostRequest,
  UserStatus
} from '../services/community.service';
import { CommunitySidebarComponent } from '../shared/community-sidebar/community-sidebar.component';

type FeedMode = 'recents' | 'amis';
type FlatComment = CommentaireResponse & { indent: number };

@Component({
  selector: 'app-posts-feed',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive, CommunitySidebarComponent],
  templateUrl: './posts-feed.component.html',
  styleUrls: ['./posts-feed.component.css']
})
export class PostsFeedComponent implements OnInit, OnDestroy {
  sidebarCollapsed = false;
  showOverview = false;
  posts: PostResponse[] = [];
  pendingAvis: AvisResponse[] = [];
  onlineUsers: UserStatus[] = [];
  trendingHashtags: { tag: string; count: number }[] = [];
  recommandations: PostResponse[] = [];

  search = '';
  newPost = '';
  selectedImages: File[] = [];
  selectedImagePreviews: string[] = [];
  visibilite: 'PUBLIC' | 'AMIS' | 'PRIVE' = 'PUBLIC';
  selectedHashtag = '';
  commentsOpenFor: string | null = null;
  commentsByPost: Record<string, CommentaireResponse[]> = {};
  commentDrafts: Record<string, string> = {};
  commenting: Record<string, boolean> = {};

  feedMode: FeedMode = 'recents';
  loading = false;
  saving = false;
  error = '';
  success = '';

  // Reactions disponibles
  availableReactions = ['👍', '❤️', '🔥', '😂', '😮', '😢'];
  reactionPickerOpenFor: string | null = null;

  // Commentaires — état réponse / édition
  replyingToCommentId: string | null = null;
  replyDraft = '';
  editingCommentId: string | null = null;
  editCommentDraft = '';

  // Commentaires — liste aplatie (replies inclus avec indentation)
  flatCommentsByPost: Record<string, FlatComment[]> = {};

  // @mention
  mentionActive = false;
  mentionQuery = '';
  mentionSuggestions: CampeurInfo[] = [];
  mentionAnchorId = '';
  mentionedByPost: Record<string, string[]> = {};  // emails mentionnés par post draft
  mentionedReply: string[] = [];                   // emails mentionnés dans la réponse en cours

  // Edition de post
  editingPost: PostResponse | null = null;
  editContent = '';
  editVisibilite: 'PUBLIC' | 'AMIS' | 'PRIVE' = 'PUBLIC';
  editImages = '';
  saving_edit = false;

  followingIds = new Set<string>();
  followingList: AbonnementResponse[] = [];
  campeurs: CampeurInfo[] = [];

  private subs: Subscription[] = [];

  constructor(
    public community: CommunityService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  get isAdmin(): boolean {
    return this.community.isAdmin();
  }

  ngOnInit(): void {
    this.community.connectNotificationsSocket();
    this.loadFeed();
    this.loadFollowing();
    this.loadCampeurs();

    this.subs.push(
      this.route.queryParamMap.subscribe(params => {
        if (params.get('view') === 'overview') {
          this.openOverview();
        }
      })
    );

    // Rafraîchir le feed en temps réel selon le type de notification
    this.subs.push(
      this.community.notifications$.subscribe(notif => {
        if (notif.type === 'NEW_POST') {
          // Un ami a publié → rafraîchir automatiquement si on est en mode "amis" ou "recents"
          if (this.feedMode === 'amis' || this.feedMode === 'recents') {
            this.loadFeed(false);
          }
        } else if (notif.type === 'REACTION' || notif.type === 'COMMENT' || notif.type === 'REPLY') {
          // Mise à jour d'un post visible → rafraîchir silencieusement
          this.loadFeed(false);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
    this.clearSelectedImages();
  }

  get filteredPosts(): PostResponse[] {
    const query = this.search.trim().toLowerCase();
    if (!query) return this.posts;
    return this.posts.filter(p =>
      p.contenu.toLowerCase().includes(query) ||
      p.auteurNom.toLowerCase().includes(query) ||
      (p.hashtags || []).some(h => h.toLowerCase().includes(query))
    );
  }

  get totalLikes(): number {
    return this.posts.reduce((s, p) => s + (p.likesCount || 0), 0);
  }

  get totalComments(): number {
    return this.posts.reduce((s, p) => s + (p.commentairesCount || 0), 0);
  }

  loadFeed(showLoader = true): void {
    this.loading = showLoader;
    this.error = '';

    const request = this.feedMode === 'amis'
      ? this.community.getFriendsPosts(0, 20)
      : this.community.getFeed(0, 20);

    request.subscribe({
      next: posts => {
        this.posts = posts;
        this.loading = false;
        this.computeTrendingHashtags();
        this.computeRecommandations();
      },
      error: () => {
        this.error = 'Impossible de charger le fil.';
        this.loading = false;
      }
    });
  }

  loadCampeurs(): void {
    this.community.getCampeurs().subscribe({
      next: list => this.campeurs = list,
      error: () => {}
    });
  }

  loadFollowing(): void {
    this.community.getMyFollowing().subscribe({
      next: list => {
        this.followingList = list;
        this.followingIds = new Set(list.map(a => a.suiviId));
      },
      error: () => {}
    });
  }

  follow(userId: string): void {
    if (!userId || userId === this.community.getCurrentEmail()) return;
    this.community.followUser(userId).subscribe({
      next: abonnement => {
        this.followingList.push(abonnement);
        this.followingIds.add(userId);
        this.success = 'Campeur suivi !';
        setTimeout(() => this.success = '', 3000);
      },
      error: () => { this.error = 'Impossible de suivre ce campeur.'; }
    });
  }

  unfollow(userId: string): void {
    this.community.unfollowUser(userId).subscribe({
      next: () => {
        this.followingList = this.followingList.filter(a => a.suiviId !== userId);
        this.followingIds.delete(userId);
        this.success = 'Campeur retiré.';
        setTimeout(() => this.success = '', 3000);
      },
      error: () => { this.error = 'Impossible de retirer ce campeur.'; }
    });
  }

  navigateToAvis(): void {
    void this.router.navigate(['/reviews']);
  }

  openOverview(): void {
    if (!this.isAdmin) {
      this.error = 'Acces reserve a l administrateur.';
      setTimeout(() => this.error = '', 3000);
      return;
    }

    this.showOverview = true;
    this.error = '';
    this.loadOverviewData();
  }

  loadOverviewData(): void {
    this.community.getAvisByStatus('EN_ATTENTE').subscribe({
      next: avis => this.pendingAvis = avis,
      error: () => this.pendingAvis = []
    });

    this.community.getOnlineUsers().subscribe({
      next: users => this.onlineUsers = users,
      error: () => this.onlineUsers = []
    });
  }

  get activeAuthorsCount(): number {
    return new Set(this.posts.map(post => post.auteurId).filter(Boolean)).size;
  }

  setFeedMode(mode: FeedMode): void {
    this.showOverview = false;
    this.feedMode = mode;
    this.selectedHashtag = '';
    this.loadFeed();
  }

  loadByHashtag(tag: string): void {
    this.showOverview = false;
    this.selectedHashtag = tag;
    this.loading = true;
    this.community.getByHashtag(tag, 0, 20).subscribe({
      next: posts => { this.posts = posts; this.loading = false; },
      error: () => { this.error = 'Impossible de charger.'; this.loading = false; }
    });
  }

  publish(): void {
    const contenu = this.newPost.trim();
    if (!contenu) {
      this.error = 'Le contenu du post est obligatoire.';
      return;
    }

    this.saving = true;
    this.error = '';
    this.community.createPostWithImages(
      { contenu, images: [], visibilite: this.visibilite },
      this.selectedImages
    ).subscribe({
      next: () => {
        this.newPost = '';
        this.clearSelectedImages();
        this.success = '✅ Post publié.';
        this.saving = false;
        this.loadFeed();
        setTimeout(() => this.success = '', 3000);
      },
      error: () => {
        this.error = 'Publication impossible.';
        this.saving = false;
      }
    });
  }

  onImagesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = Array.from(input.files || []);
    const imageFiles = files.filter(file => file.type.startsWith('image/'));

    if (imageFiles.length !== files.length) {
      this.error = 'Seules les images sont autorisÃ©es.';
    }

    this.clearSelectedImages();
    this.selectedImages = imageFiles;
    this.selectedImagePreviews = imageFiles.map(file => URL.createObjectURL(file));
    input.value = '';
  }

  removeSelectedImage(index: number): void {
    URL.revokeObjectURL(this.selectedImagePreviews[index]);
    this.selectedImages.splice(index, 1);
    this.selectedImagePreviews.splice(index, 1);
  }

  private clearSelectedImages(): void {
    this.selectedImagePreviews.forEach(preview => URL.revokeObjectURL(preview));
    this.selectedImages = [];
    this.selectedImagePreviews = [];
  }

  startEdit(post: PostResponse, event: Event): void {
    event.stopPropagation();
    this.editingPost = post;
    this.editContent = post.contenu;
    this.editVisibilite = (post.visibilite as 'PUBLIC' | 'AMIS' | 'PRIVE') || 'PUBLIC';
    this.editImages = (post.images || []).join(', ');
  }

  cancelEdit(event: Event): void {
    event.stopPropagation();
    this.editingPost = null;
  }

  saveEdit(event: Event): void {
    event.stopPropagation();
    if (!this.editingPost || !this.editContent.trim()) return;
    const payload: PostRequest = {
      contenu: this.editContent.trim(),
      images: this.editImages.split(',').map(i => i.trim()).filter(Boolean),
      visibilite: this.editVisibilite
    };
    this.saving_edit = true;
    this.community.updatePost(this.editingPost.id, payload).subscribe({
      next: updated => {
        const idx = this.posts.findIndex(p => p.id === updated.id);
        if (idx !== -1) this.posts[idx] = updated;
        this.editingPost = null;
        this.saving_edit = false;
        this.success = 'Post modifié.';
        setTimeout(() => this.success = '', 3000);
      },
      error: () => {
        this.error = 'Modification impossible.';
        this.saving_edit = false;
      }
    });
  }

  openUserPosts(post: PostResponse, event: Event): void {
    event.stopPropagation();
    void this.router.navigate(['/community/user', post.auteurId]);
  }

  deletePost(post: PostResponse, event: Event): void {
    event.stopPropagation();
    if (!confirm('Supprimer cette publication ?')) return;
    this.community.deletePost(post.id).subscribe({
      next: () => {
        this.posts = this.posts.filter(p => p.id !== post.id);
        this.success = 'Post supprimé.';
        setTimeout(() => this.success = '', 3000);
      },
      error: () => this.error = 'Suppression impossible.'
    });
  }

  toggleReaction(post: PostResponse, emoji: string, event: Event): void {
    event.stopPropagation();
    this.reactionPickerOpenFor = null;

    if (post.myReaction === emoji) {
      this.community.removeReaction(post.id).subscribe({
        next: updated => Object.assign(post, updated),
        error: () => this.error = 'Action impossible.'
      });
    } else {
      this.community.reactToPost(post.id, emoji).subscribe({
        next: updated => Object.assign(post, updated),
        error: () => this.error = 'Action impossible.'
      });
    }
  }

  toggleReactionPicker(post: PostResponse, event: Event): void {
    event.stopPropagation();
    this.reactionPickerOpenFor = this.reactionPickerOpenFor === post.id ? null : post.id;
  }

  isMyPost(post: PostResponse): boolean {
    return post.auteurId === this.community.getCurrentEmail();
  }

  reactionsList(post: PostResponse): { emoji: string; count: number }[] {
    if (!post.reactions) return [];
    return Object.entries(post.reactions)
      .filter(([, count]) => count > 0)
      .sort((a, b) => b[1] - a[1])
      .map(([emoji, count]) => ({ emoji, count }));
  }

  totalReactions(post: PostResponse): number {
    if (!post.reactions) return 0;
    return Object.values(post.reactions).reduce((s, c) => s + c, 0);
  }

  toggleComments(post: PostResponse, event?: Event): void {
    event?.stopPropagation();
    this.commentsOpenFor = this.commentsOpenFor === post.id ? null : post.id;
    if (this.commentsOpenFor === post.id && !this.commentsByPost[post.id]) {
      this.loadComments(post.id);
    }
  }

  loadComments(postId: string): void {
    this.community.getComments(postId).subscribe({
      next: comments => {
        this.commentsByPost[postId] = comments;
        this.flatCommentsByPost[postId] = this.flattenComments(comments);
      },
      error: () => this.error = 'Impossible de charger les commentaires.'
    });
  }

  submitComment(post: PostResponse, event?: Event): void {
    event?.stopPropagation();
    const contenu = (this.commentDrafts[post.id] || '').trim();
    if (!contenu) {
      this.error = 'Le commentaire est obligatoire.';
      return;
    }

    const mentionedIds = [...(this.mentionedByPost[post.id] || [])];
    this.commenting[post.id] = true;
    this.error = '';
    this.community.createComment(post.id, contenu, undefined, mentionedIds).subscribe({
      next: comment => {
        this.commentsByPost[post.id] = [...(this.commentsByPost[post.id] || []), comment];
        this.flatCommentsByPost[post.id] = this.flattenComments(this.commentsByPost[post.id]);
        this.commentDrafts[post.id] = '';
        this.mentionedByPost[post.id] = [];
        post.commentairesCount = (post.commentairesCount || 0) + 1;
        this.commenting[post.id] = false;
        this.mentionActive = false;
      },
      error: () => {
        this.error = 'Ajout du commentaire impossible.';
        this.commenting[post.id] = false;
      }
    });
  }

  // ─── HELPERS COMMENTAIRES ───
  isMyComment(comment: CommentaireResponse): boolean {
    return comment.auteurId === this.community.getCurrentEmail();
  }

  canDeleteComment(post: PostResponse, comment: CommentaireResponse): boolean {
    const me = this.community.getCurrentEmail();
    return comment.auteurId === me || post.auteurId === me || this.isAdmin;
  }

  setReplyTarget(commentId: string, event: Event): void {
    event.stopPropagation();
    this.replyingToCommentId = this.replyingToCommentId === commentId ? null : commentId;
    this.replyDraft = '';
  }

  cancelReply(event: Event): void {
    event.stopPropagation();
    this.replyingToCommentId = null;
    this.replyDraft = '';
  }

  submitReply(post: PostResponse, parentCommentId: string, event: Event): void {
    event.stopPropagation();
    const contenu = this.replyDraft.trim();
    if (!contenu) return;
    const mentionedIds = [...this.mentionedReply];
    this.community.createComment(post.id, contenu, parentCommentId, mentionedIds).subscribe({
      next: () => {
        this.replyingToCommentId = null;
        this.replyDraft = '';
        this.mentionedReply = [];
        this.mentionActive = false;
        this.loadComments(post.id);
        post.commentairesCount = (post.commentairesCount || 0) + 1;
      },
      error: () => { this.error = 'Réponse impossible.'; }
    });
  }

  deleteCommentItem(post: PostResponse, postId: string, commentId: string, event: Event): void {
    event.stopPropagation();
    if (!confirm('Supprimer ce commentaire et ses réponses ?')) return;
    this.community.deleteComment(postId, commentId).subscribe({
      next: () => {
        this.loadComments(postId);
        post.commentairesCount = Math.max(0, (post.commentairesCount || 0) - 1);
      },
      error: () => { this.error = 'Suppression impossible.'; }
    });
  }

  startEditComment(comment: CommentaireResponse, event: Event): void {
    event.stopPropagation();
    this.editingCommentId = comment.id;
    this.editCommentDraft = comment.contenu;
    this.replyingToCommentId = null;
  }

  cancelEditComment(event: Event): void {
    event.stopPropagation();
    this.editingCommentId = null;
    this.editCommentDraft = '';
  }

  saveEditComment(postId: string, comment: CommentaireResponse, event: Event): void {
    event.stopPropagation();
    const contenu = this.editCommentDraft.trim();
    if (!contenu) return;
    this.community.updateComment(postId, comment.id, contenu).subscribe({
      next: updated => {
        comment.contenu = updated.contenu;
        this.editingCommentId = null;
        this.editCommentDraft = '';
      },
      error: () => { this.error = 'Modification impossible.'; }
    });
  }

  startChat(post: PostResponse, event: Event): void {
    event.stopPropagation();
    if (!post.auteurId || post.auteurId === this.community.getCurrentEmail()) {
      this.error = 'Conversation indisponible.';
      return;
    }
    this.community.getOrCreateConversation(post.auteurId).subscribe({
      next: conv => void this.router.navigate(['/messages', conv.id]),
      error: () => this.error = 'Conversation impossible.'
    });
  }

  openPost(post: PostResponse): void {
    void this.router.navigate(['/community', post.id]);
  }

  initials(name: string): string {
    return (name || 'US').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
  }

  timeAgo(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const diffMs = Date.now() - d.getTime();
    const m = Math.floor(diffMs / 60000);
    if (m < 1) return 'à l\'instant';
    if (m < 60) return `il y a ${m} min`;
    const h = Math.floor(m / 60);
    if (h < 24) return `il y a ${h}h`;
    const j = Math.floor(h / 24);
    if (j < 7) return `il y a ${j}j`;
    return d.toLocaleDateString('fr-FR');
  }

  private computeTrendingHashtags(): void {
    const counts: Record<string, number> = {};
    this.posts.forEach(p => (p.hashtags || []).forEach(h => counts[h] = (counts[h] || 0) + 1));
    this.trendingHashtags = Object.entries(counts)
      .map(([tag, count]) => ({ tag, count }))
      .sort((a, b) => b.count - a.count)
      .slice(0, 8);
  }

  get stories(): {id: string; nom: string; photo?: string}[] {
    const me = this.community.getCurrentEmail();
    const seen = new Set<string>();
    const result: {id: string; nom: string; photo?: string}[] = [];
    for (const post of this.posts) {
      if (post.auteurId && post.auteurId !== me && !seen.has(post.auteurId)) {
        seen.add(post.auteurId);
        result.push({ id: post.auteurId, nom: post.auteurNom || 'Campeur', photo: post.auteurPhoto });
      }
    }
    return result.slice(0, 7);
  }

  get suggestions(): CampeurInfo[] {
    const me = this.community.getCurrentEmail();
    return this.campeurs
      .filter(c => c.id !== me && !this.followingIds.has(c.id))
      .slice(0, 6);
  }

  storyColor(nom: string): string {
    const colors = [
      'linear-gradient(135deg,#1a9e6a,#0f6e56)',
      'linear-gradient(135deg,#7c3aed,#5b21b6)',
      'linear-gradient(135deg,#ef9f27,#c57e12)',
      'linear-gradient(135deg,#3b82f6,#1d4ed8)',
      'linear-gradient(135deg,#ec4899,#be185d)',
      'linear-gradient(135deg,#14b8a6,#0f766e)',
    ];
    let h = 0;
    for (let i = 0; i < nom.length; i++) h = (h + nom.charCodeAt(i)) % colors.length;
    return colors[h];
  }

  startChatWithUser(userId: string): void {
    if (!userId || userId === this.community.getCurrentEmail()) return;
    this.community.getOrCreateConversation(userId).subscribe({
      next: conv => void this.router.navigate(['/messages', conv.id]),
      error: () => this.error = 'Conversation impossible.'
    });
  }

  openUserById(userId: string, event: Event): void {
    event.stopPropagation();
    void this.router.navigate(['/community/user', userId]);
  }

  private computeRecommandations(): void {
    this.recommandations = [...this.posts]
      .filter(p => (p.trendScore || 0) > 0)
      .sort((a, b) => (b.trendScore || 0) - (a.trendScore || 0))
      .slice(0, 4);
  }

  // ─── COMMENTAIRES : FLAT TREE ───────────────────────
  private flattenComments(comments: CommentaireResponse[], indent = 0): FlatComment[] {
    const result: FlatComment[] = [];
    for (const c of comments) {
      result.push({ ...c, indent });
      if (c.replies?.length) result.push(...this.flattenComments(c.replies, indent + 1));
    }
    return result;
  }

  // ─── LIKE COMMENTAIRE ───────────────────────────────
  toggleCommentLike(post: PostResponse, comment: FlatComment, event: Event): void {
    event.stopPropagation();
    const optimistic = !comment.likedByCurrentUser;
    comment.likedByCurrentUser = optimistic;
    comment.likesCount += optimistic ? 1 : -1;
    const req = optimistic
      ? this.community.likeComment(post.id, comment.id)
      : this.community.unlikeComment(post.id, comment.id);
    req.subscribe({
      error: () => {
        comment.likedByCurrentUser = !optimistic;
        comment.likesCount += optimistic ? -1 : 1;
      }
    });
  }

  // ─── @MENTION ───────────────────────────────────────
  onCommentInput(postId: string, event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.commentDrafts[postId] = value;
    const match = value.match(/@(\w*)$/);
    if (match) {
      this.mentionActive = true;
      this.mentionAnchorId = postId;
      this.mentionQuery = match[1].toLowerCase();
      this.mentionSuggestions = this.campeurs
        .filter(c => c.nom.toLowerCase().includes(this.mentionQuery) || c.id.toLowerCase().includes(this.mentionQuery))
        .slice(0, 5);
    } else {
      if (this.mentionAnchorId === postId) { this.mentionActive = false; this.mentionSuggestions = []; }
    }
  }

  onReplyInput(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    this.replyDraft = value;
    const match = value.match(/@(\w*)$/);
    if (match) {
      this.mentionActive = true;
      this.mentionAnchorId = '__reply__';
      this.mentionQuery = match[1].toLowerCase();
      this.mentionSuggestions = this.campeurs
        .filter(c => c.nom.toLowerCase().includes(this.mentionQuery) || c.id.toLowerCase().includes(this.mentionQuery))
        .slice(0, 5);
    } else {
      if (this.mentionAnchorId === '__reply__') { this.mentionActive = false; this.mentionSuggestions = []; }
    }
  }

  onEditCommentInput(event: Event): void {
    const value = (event.target as HTMLTextAreaElement).value;
    this.editCommentDraft = value;
    const match = value.match(/@(\w*)$/);
    if (match) {
      this.mentionActive = true;
      this.mentionAnchorId = '__edit__';
      this.mentionQuery = match[1].toLowerCase();
      this.mentionSuggestions = this.campeurs
        .filter(c => c.nom.toLowerCase().includes(this.mentionQuery) || c.id.toLowerCase().includes(this.mentionQuery))
        .slice(0, 5);
    } else {
      if (this.mentionAnchorId === '__edit__') { this.mentionActive = false; this.mentionSuggestions = []; }
    }
  }

  insertMention(campeur: CampeurInfo, postId: string): void {
    const firstName = campeur.nom.split(' ')[0];
    this.commentDrafts[postId] = (this.commentDrafts[postId] || '').replace(/@\w*$/, `@${firstName} `);
    if (!this.mentionedByPost[postId]) this.mentionedByPost[postId] = [];
    if (!this.mentionedByPost[postId].includes(campeur.id)) this.mentionedByPost[postId].push(campeur.id);
    this.mentionActive = false;
    this.mentionSuggestions = [];
  }

  insertMentionReply(campeur: CampeurInfo): void {
    const firstName = campeur.nom.split(' ')[0];
    this.replyDraft = this.replyDraft.replace(/@\w*$/, `@${firstName} `);
    if (!this.mentionedReply.includes(campeur.id)) this.mentionedReply.push(campeur.id);
    this.mentionActive = false;
    this.mentionSuggestions = [];
  }

  insertMentionEdit(campeur: CampeurInfo): void {
    const firstName = campeur.nom.split(' ')[0];
    this.editCommentDraft = this.editCommentDraft.replace(/@\w*$/, `@${firstName} `);
    this.mentionActive = false;
    this.mentionSuggestions = [];
  }

  renderMentions(text: string): string {
    if (!text) return '';
    return text.replace(/@(\w+)/g, '<span class="mention-tag">@$1</span>');
  }
}
