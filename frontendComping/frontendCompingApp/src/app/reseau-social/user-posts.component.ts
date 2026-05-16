import { Component, OnInit, HostListener } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CommunityService, PostResponse } from '../services/community.service';
import { forkJoin, of } from 'rxjs';
import { catchError } from 'rxjs/operators';

type SortMode = 'recent' | 'popular';

@Component({
  selector: 'app-user-posts',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './user-posts.component.html',
  styleUrls: ['./user-posts.component.css']
})
export class UserPostsComponent implements OnInit {

  posts: PostResponse[] = [];
  userId         = '';
  authorName     = '';
  authorPhoto    = '';
  followersCount = 0;
  followingCount = 0;

  loading       = true;
  followLoading = false;
  isFollowing   = false;
  isOwnProfile  = false;
  error         = '';
  success       = '';
  sortMode: SortMode = 'recent';
  headerSticky  = false;

  readonly skeletons = [1, 2, 3];
  readonly availableReactions = ['👍', '❤️', '🔥', '😂', '😮', '😢'];
  reactionPickerFor: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    public community: CommunityService
  ) {}

  @HostListener('window:scroll')
  onScroll(): void {
    this.headerSticky = window.scrollY > 220;
  }

  @HostListener('document:click')
  onDocClick(): void {
    this.reactionPickerFor = null;
  }

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.userId = params.get('userId') ?? '';
      this.isOwnProfile = this.userId === this.community.getCurrentEmail();
      if (this.userId) this.loadAll();
    });
  }

  loadAll(): void {
    this.loading = true;
    this.error   = '';

    const posts$ = this.community.getUserPosts(this.userId).pipe(
      catchError(() => { this.error = 'Impossible de charger les publications.'; return of([]); })
    );
    const stats$ = this.community.getFollowStats(this.userId).pipe(
      catchError(() => of({ followers: 0, following: 0 }))
    );
    const follow$ = this.isOwnProfile
      ? of(false)
      : this.community.checkIsFollowing(this.userId).pipe(catchError(() => of(false)));

    forkJoin([posts$, stats$, follow$]).subscribe(([posts, stats, following]) => {
      this.posts          = posts as PostResponse[];
      this.authorName     = (posts as PostResponse[])[0]?.auteurNom   ?? 'Campeur';
      this.authorPhoto    = (posts as PostResponse[])[0]?.auteurPhoto  ?? '';
      this.followersCount = (stats as { followers: number; following: number }).followers;
      this.followingCount = (stats as { followers: number; following: number }).following;
      this.isFollowing    = following as boolean;
      this.loading        = false;
    });
  }

  get sortedPosts(): PostResponse[] {
    return [...this.posts].sort((a, b) =>
      this.sortMode === 'popular'
        ? (b.trendScore || 0) - (a.trendScore || 0)
        : new Date(b.datePublication).getTime() - new Date(a.datePublication).getTime()
    );
  }

  get totalLikes(): number {
    return this.posts.reduce((s, p) => s + (p.likesCount || 0), 0);
  }

  toggleFollow(): void {
    if (this.followLoading) return;
    this.followLoading = true;

    const wasFollowing = this.isFollowing;
    const onSuccess = () => {
      this.isFollowing    = !wasFollowing;
      this.followersCount += this.isFollowing ? 1 : -1;
      this.followLoading  = false;
      this.success = this.isFollowing ? '✅ Vous suivez ce campeur.' : 'Abonnement retiré.';
      setTimeout(() => this.success = '', 3000);
    };
    const onError = () => {
      this.error = 'Action impossible. Réessayez.';
      this.followLoading = false;
    };

    if (wasFollowing) {
      this.community.unfollowUser(this.userId).subscribe({ next: onSuccess, error: onError });
    } else {
      this.community.followUser(this.userId).subscribe({ next: onSuccess, error: onError });
    }
  }

  startChat(): void {
    this.community.getOrCreateConversation(this.userId).subscribe({
      next: conv => void this.router.navigate(['/messages', conv.id]),
      error: () => { this.error = 'Conversation impossible.'; }
    });
  }

  goToEditProfile(): void { void this.router.navigate(['/profile']); }

  toggleReactionPicker(post: PostResponse, e: Event): void {
    e.stopPropagation();
    this.reactionPickerFor = this.reactionPickerFor === post.id ? null : post.id;
  }

  toggleReaction(post: PostResponse, emoji: string, e: Event): void {
    e.stopPropagation();
    this.reactionPickerFor = null;
    const req = post.myReaction === emoji
      ? this.community.removeReaction(post.id)
      : this.community.reactToPost(post.id, emoji);
    req.subscribe({ next: u => Object.assign(post, u) });
  }

  reactionsList(post: PostResponse): { emoji: string; count: number }[] {
    if (!post.reactions) return [];
    return Object.entries(post.reactions)
      .filter(([, c]) => c > 0)
      .sort((a, b) => b[1] - a[1])
      .map(([emoji, count]) => ({ emoji, count }));
  }

  postTotalReactions(post: PostResponse): number {
    if (!post.reactions) return 0;
    return Object.values(post.reactions).reduce((s, c) => s + c, 0);
  }

  initials(name: string): string {
    return (name || 'US').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
  }

  timeAgo(dateStr: string): string {
    if (!dateStr) return '';
    const diff = Date.now() - new Date(dateStr).getTime();
    const m = Math.floor(diff / 60000);
    if (m < 1)   return 'à l\'instant';
    if (m < 60)  return `il y a ${m} min`;
    const h = Math.floor(m / 60);
    if (h < 24)  return `il y a ${h}h`;
    const j = Math.floor(h / 24);
    if (j < 7)   return `il y a ${j}j`;
    if (j < 30)  return `il y a ${j} jours`;
    return new Date(dateStr).toLocaleDateString('fr-FR', { day: 'numeric', month: 'short' });
  }

  goBack(): void { void this.router.navigate(['/community']); }
}
