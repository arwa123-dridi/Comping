import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { CommunityService, PostResponse } from '../../services/community.service';

@Component({
  selector: 'app-posts-feed',
  standalone: false,
  templateUrl: './posts-feed.component.html',
  styleUrls: ['./posts-feed.component.css']
})
export class PostsFeedComponent implements OnInit {
  posts: PostResponse[] = [];
  search = '';
  newPost = '';
  imageDraft = '';
  loading = false;
  saving = false;
  error = '';
  success = '';
  page = 0;
  pageSize = 20;

  constructor(
    private community: CommunityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadFeed();
  }

  get filteredPosts(): PostResponse[] {
    const query = this.search.trim().toLowerCase();
    if (!query) {
      return this.posts;
    }
    return this.posts.filter(post =>
      post.contenu.toLowerCase().includes(query) ||
      post.auteurNom.toLowerCase().includes(query)
    );
  }

  get totalLikes(): number {
    return this.posts.reduce((sum, post) => sum + post.likesCount, 0);
  }

  get totalComments(): number {
    return this.posts.reduce((sum, post) => sum + post.commentairesCount, 0);
  }

  loadFeed(): void {
    this.loading = true;
    this.error = '';
    this.community.getFeed(this.page, this.pageSize).subscribe({
      next: posts => {
        this.posts = posts;
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger le feed.';
        this.loading = false;
      }
    });
  }

  publish(): void {
    const contenu = this.newPost.trim();
    if (!contenu) {
      this.error = 'Le contenu du post est obligatoire.';
      return;
    }

    const images = this.imageDraft
      .split(',')
      .map(image => image.trim())
      .filter(Boolean);

    this.saving = true;
    this.error = '';
    this.community.createPost({ contenu, images }).subscribe({
      next: () => {
        this.newPost = '';
        this.imageDraft = '';
        this.success = 'Post publie.';
        this.saving = false;
        this.loadFeed();
      },
      error: () => {
        this.error = 'Publication impossible.';
        this.saving = false;
      }
    });
  }

  like(post: PostResponse, event: Event): void {
    event.stopPropagation();
    this.community.likePost(post.id).subscribe({
      next: updated => {
        post.likesCount = updated.likesCount;
      },
      error: () => {
        this.error = 'Action impossible.';
      }
    });
  }

  openPost(post: PostResponse): void {
    void this.router.navigate(['/modules/reseau-social', post.id]);
  }
}
