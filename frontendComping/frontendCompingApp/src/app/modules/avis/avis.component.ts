import { Component, OnInit } from '@angular/core';
import { forkJoin } from 'rxjs';
import { AvisResponse, AvisStats, CommunityService, TargetType } from '../../services/community.service';

type AvisView = 'cible' | 'mes-avis' | 'moderation';

@Component({
  selector: 'app-avis',
  standalone: false,
  templateUrl: './avis.component.html',
  styleUrls: ['./avis.component.css']
})
export class AvisComponent implements OnInit {
  targetTypes: TargetType[] = ['SITE_CAMPING', 'PRODUIT', 'EVENEMENT', 'ACTIVITE'];
  selectedTargetType: TargetType = 'SITE_CAMPING';
  selectedTargetId = 'demo-cible';
  activeView: AvisView = 'cible';
  search = '';
  loading = false;
  saving = false;
  error = '';
  success = '';
  isAdmin = false;

  targetAvis: AvisResponse[] = [];
  myAvis: AvisResponse[] = [];
  moderationAvis: AvisResponse[] = [];
  stats: AvisStats = this.emptyStats();

  form = {
    note: 5,
    commentaire: ''
  };

  rejectMotifs: Record<string, string> = {};

  constructor(private community: CommunityService) {}

  ngOnInit(): void {
    this.isAdmin = this.community.isAdmin();
    this.loadAll();
  }

  get visibleAvis(): AvisResponse[] {
    const source = this.activeView === 'mes-avis'
      ? this.myAvis
      : this.activeView === 'moderation'
        ? this.moderationAvis
        : this.targetAvis;
    const query = this.search.trim().toLowerCase();
    if (!query) {
      return source;
    }
    return source.filter(item =>
      item.commentaire.toLowerCase().includes(query) ||
      item.utilisateurNom.toLowerCase().includes(query) ||
      item.cibleId.toLowerCase().includes(query)
    );
  }

  get pendingCount(): number {
    return this.moderationAvis.length;
  }

  loadAll(): void {
    this.loading = true;
    this.error = '';

    const requests = {
      targetAvis: this.community.getAvisByTarget(this.selectedTargetId, this.selectedTargetType),
      myAvis: this.community.getMyAvis(),
      stats: this.community.getAvisStats(this.selectedTargetId, this.selectedTargetType),
      moderationAvis: this.isAdmin ? this.community.getAvisByStatus('EN_ATTENTE') : this.community.getMyAvis()
    };

    forkJoin(requests).subscribe({
      next: result => {
        this.targetAvis = result.targetAvis;
        this.myAvis = result.myAvis;
        this.stats = result.stats;
        this.moderationAvis = this.isAdmin ? result.moderationAvis : [];
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger les avis. Verifiez le backend et votre session.';
        this.loading = false;
      }
    });
  }

  submitAvis(): void {
    if (!this.form.commentaire.trim() || this.form.note < 1 || this.form.note > 5) {
      this.error = 'La note et le commentaire sont obligatoires.';
      return;
    }

    this.saving = true;
    this.error = '';
    this.community.createAvis({
      note: this.form.note,
      commentaire: this.form.commentaire.trim(),
      cibleId: this.selectedTargetId.trim(),
      typeCible: this.selectedTargetType
    }).subscribe({
      next: () => {
        this.form.commentaire = '';
        this.form.note = 5;
        this.success = 'Avis envoye en moderation.';
        this.saving = false;
        this.loadAll();
      },
      error: () => {
        this.error = 'Avis non envoye. Verifiez les champs et votre authentification.';
        this.saving = false;
      }
    });
  }

  approve(item: AvisResponse): void {
    this.community.validateAvis(item.id).subscribe({
      next: () => {
        this.success = 'Avis valide.';
        this.loadAll();
      },
      error: () => {
        this.error = 'Validation impossible.';
      }
    });
  }

  reject(item: AvisResponse): void {
    const motif = this.rejectMotifs[item.id]?.trim() || 'Non conforme';
    this.community.rejectAvis(item.id, motif).subscribe({
      next: () => {
        this.success = 'Avis rejete.';
        this.rejectMotifs[item.id] = '';
        this.loadAll();
      },
      error: () => {
        this.error = 'Rejet impossible.';
      }
    });
  }

  stars(note: number): number[] {
    return Array.from({ length: Math.max(0, Math.min(note, 5)) }, (_, index) => index);
  }

  statusLabel(statut: string): string {
    const labels: Record<string, string> = {
      EN_ATTENTE: 'En attente',
      VALIDE: 'Valide',
      REJETE: 'Rejete'
    };
    return labels[statut] ?? statut;
  }

  private emptyStats(): AvisStats {
    return {
      nombreTotal: 0,
      noteMoyenne: 0,
      nombre5Etoiles: 0,
      nombre4Etoiles: 0,
      nombre3Etoiles: 0,
      nombre2Etoiles: 0,
      nombre1Etoile: 0
    };
  }
}
