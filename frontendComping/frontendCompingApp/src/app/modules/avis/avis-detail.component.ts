import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { AvisResponse, CommunityService } from '../../services/community.service';

@Component({
  selector: 'app-avis-detail',
  standalone: false,
  templateUrl: './avis-detail.component.html',
  styleUrls: ['./avis-detail.component.css']
})
export class AvisDetailComponent implements OnInit {
  avis: AvisResponse | null = null;
  isAdmin = false;
  motif = '';
  error = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private community: CommunityService
  ) {}

  ngOnInit(): void {
    this.isAdmin = this.community.isAdmin();
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.error = 'Avis introuvable.';
      return;
    }
    this.community.getAvisById(id).subscribe({
      next: avis => {
        this.avis = avis;
      },
      error: () => {
        this.error = 'Impossible de charger cet avis.';
      }
    });
  }

  approve(): void {
    if (!this.avis) {
      return;
    }
    this.community.validateAvis(this.avis.id).subscribe({
      next: avis => {
        this.avis = avis;
      },
      error: () => {
        this.error = 'Validation impossible.';
      }
    });
  }

  reject(): void {
    if (!this.avis) {
      return;
    }
    this.community.rejectAvis(this.avis.id, this.motif.trim() || 'Non conforme').subscribe({
      next: avis => {
        this.avis = avis;
      },
      error: () => {
        this.error = 'Rejet impossible.';
      }
    });
  }

  back(): void {
    void this.router.navigate(['/modules/avis']);
  }
}
