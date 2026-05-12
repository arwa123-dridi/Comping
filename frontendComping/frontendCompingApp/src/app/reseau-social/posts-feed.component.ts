import { Component, OnInit, OnDestroy } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { Subscription } from 'rxjs';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { CommunityService, PostResponse, PostRequest } from '../services/community.service';

type FeedMode = 'tendances' | 'recents' | 'amis';

@Component({
  selector: 'app-posts-feed',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './posts-feed.component.html',
  styleUrls: ['./posts-feed.component.css']
})
export class PostsFeedComponent implements OnInit, OnDestroy {
  posts: PostResponse[] = [];
  trendingHashtags: { tag: string; count: number }[] = [];
  recommandations: PostResponse[] = [];

  search = '';
  newPost = '';
  imageDraft = '';
  visibilite: 'PUBLIC' | 'AMIS' | 'PRIVE' = 'PUBLIC';
  selectedHashtag = '';

  feedMode: FeedMode = 'tendances';
  loading = false;
  saving = false;
  error = '';
  success = '';

  // Reactions disponibles
  availableReactions = ['👍', '❤️', '🔥', '😂', '😮', '😢'];
  reactionPickerOpenFor: string | null = null;

  // Edition de post
  editingPost: PostResponse | null = null;
  editContent = '';
  editVisibilite: 'PUBLIC' | 'AMIS' | 'PRIVE' = 'PUBLIC';
  editImages = '';
  saving_edit = false;

  private subs: Subscription[] = [];

  constructor(public community: CommunityService, private router: Router) {}

  ngOnInit(): void {
    this.community.connectNotificationsSocket();
    this.loadFeed();

    // Écouter les notifications temps réel pour rafraîchir
    this.subs.push(
      this.community.notifications$.subscribe(notif => {
        if (notif.type === 'REACTION' || notif.type === 'COMMENT') {
          this.loadFeed(false);
        }
      })
    );
  }

  ngOnDestroy(): void {
    this.subs.forEach(s => s.unsubscribe());
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

    const request = this.feedMode === 'tendances'
      ? this.community.getTrending(0, 20)
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

  setFeedMode(mode: FeedMode): void {
    this.feedMode = mode;
    this.selectedHashtag = '';
    this.loadFeed();
  }

  loadByHashtag(tag: string): void {
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

    const images = this.imageDraft.split(',').map(i => i.trim()).filter(Boolean);
    this.saving = true;
    this.error = '';
    this.community.createPost({ contenu, images, visibilite: this.visibilite }).subscribe({
      next: () => {
        this.newPost = '';
        this.imageDraft = '';
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

  private computeRecommandations(): void {
    // Posts les mieux scorés par l'IA (trendScore)
    this.recommandations = [...this.posts]
      .filter(p => (p.trendScore || 0) > 0)
      .sort((a, b) => (b.trendScore || 0) - (a.trendScore || 0))
      .slice(0, 4);
  }
}
