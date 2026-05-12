import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { CommunityService, PostResponse } from '../services/community.service';

@Component({
  selector: 'app-user-posts',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './user-posts.component.html',
  styleUrls: ['./user-posts.component.css']
})
export class UserPostsComponent implements OnInit {
  posts: PostResponse[] = [];
  userId = '';
  authorName = '';
  loading = false;
  error = '';
  success = '';

  availableReactions = ['👍', '❤️', '🔥', '😂', '😮', '😢'];
  reactionPickerOpenFor: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    public community: CommunityService
  ) {}

  ngOnInit(): void {
    this.route.paramMap.subscribe(params => {
      this.userId = params.get('userId') ?? '';
      if (this.userId) this.loadPosts();
    });
  }

  loadPosts(): void {
    this.loading = true;
    this.error = '';
    this.community.getUserPosts(this.userId).subscribe({
      next: posts => {
        this.posts = posts;
        this.authorName = posts[0]?.auteurNom ?? 'Utilisateur';
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les publications.';
        this.loading = false;
      }
    });
  }

  toggleReaction(post: PostResponse, emoji: string, event: Event): void {
    event.stopPropagation();
    this.reactionPickerOpenFor = null;
    if (post.myReaction === emoji) {
      this.community.removeReaction(post.id).subscribe({ next: u => Object.assign(post, u) });
    } else {
      this.community.reactToPost(post.id, emoji).subscribe({ next: u => Object.assign(post, u) });
    }
  }

  toggleReactionPicker(post: PostResponse, event: Event): void {
    event.stopPropagation();
    this.reactionPickerOpenFor = this.reactionPickerOpenFor === post.id ? null : post.id;
  }

  startChat(post: PostResponse, event: Event): void {
    event.stopPropagation();
    this.community.getOrCreateConversation(post.auteurId).subscribe({
      next: conv => void this.router.navigate(['/messages', conv.id]),
      error: () => this.error = 'Conversation impossible.'
    });
  }

  reactionsList(post: PostResponse): { emoji: string; count: number }[] {
    if (!post.reactions) return [];
    return Object.entries(post.reactions)
      .filter(([, c]) => c > 0)
      .sort((a, b) => b[1] - a[1])
      .map(([emoji, count]) => ({ emoji, count }));
  }

  totalReactions(post: PostResponse): number {
    if (!post.reactions) return 0;
    return Object.values(post.reactions).reduce((s, c) => s + c, 0);
  }

  isMyPost(post: PostResponse): boolean {
    return post.auteurId === this.community.getCurrentEmail();
  }

  initials(name: string): string {
    return (name || 'US').split(' ').map(w => w[0]).join('').slice(0, 2).toUpperCase();
  }

  timeAgo(dateStr: string): string {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    const m = Math.floor((Date.now() - d.getTime()) / 60000);
    if (m < 1) return 'à l\'instant';
    if (m < 60) return `il y a ${m} min`;
    const h = Math.floor(m / 60);
    if (h < 24) return `il y a ${h}h`;
    const j = Math.floor(h / 24);
    if (j < 7) return `il y a ${j}j`;
    return d.toLocaleDateString('fr-FR');
  }

  goBack(): void {
    void this.router.navigate(['/community']);
  }
}
