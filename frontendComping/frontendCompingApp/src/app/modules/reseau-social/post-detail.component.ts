import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommentaireResponse, CommunityService, PostResponse } from '../../services/community.service';

@Component({
  selector: 'app-post-detail',
  standalone: false,
  templateUrl: './post-detail.component.html',
  styleUrls: ['./post-detail.component.css']
})
export class PostDetailComponent implements OnInit {
  post: PostResponse | null = null;
  comments: CommentaireResponse[] = [];
  commentDraft = '';
  replyDrafts: Record<string, string> = {};
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private community: CommunityService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Post introuvable.';
      return;
    }

    this.loading = true;
    this.community.getPost(id).subscribe({
      next: post => {
        this.post = post;
        this.loadComments(post.id);
      },
      error: () => {
        this.error = 'Impossible de charger le post.';
        this.loading = false;
      }
    });
  }

  loadComments(postId: string): void {
    this.community.getComments(postId).subscribe({
      next: comments => {
        this.comments = comments;
        this.loading = false;
      },
      error: () => {
        this.error = 'Commentaires indisponibles.';
        this.loading = false;
      }
    });
  }

  addComment(): void {
    if (!this.post || !this.commentDraft.trim()) {
      return;
    }
    this.community.createComment(this.post.id, this.commentDraft.trim()).subscribe({
      next: () => {
        this.commentDraft = '';
        this.loadComments(this.post?.id ?? '');
      },
      error: () => {
        this.error = 'Commentaire non envoye.';
      }
    });
  }

  addReply(comment: CommentaireResponse): void {
    if (!this.post) {
      return;
    }
    const contenu = this.replyDrafts[comment.id]?.trim();
    if (!contenu) {
      return;
    }
    this.community.createComment(this.post.id, contenu, comment.id).subscribe({
      next: () => {
        this.replyDrafts[comment.id] = '';
        this.loadComments(this.post?.id ?? '');
      },
      error: () => {
        this.error = 'Reponse non envoyee.';
      }
    });
  }

  back(): void {
    void this.router.navigate(['/modules/reseau-social']);
  }
}
