import { Component, OnInit } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import {
  CommunityService, AvisRequest, AvisResponse,
  AvisStats, AvisStatus, TargetType
} from '../services/community.service';

@Component({
  selector: 'app-avis',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, RouterLinkActive],
  templateUrl: './avis.component.html',
  styleUrls: ['./avis.component.css']
})
export class AvisComponent implements OnInit {
  // Liste des avis
  avis: AvisResponse[] = [];
  filteredAvis: AvisResponse[] = [];
  myAvis: AvisResponse[] = [];

  // Stats
  stats: AvisStats | null = null;

  // Filtres
  selectedTab: 'tous' | 'mes-avis' | 'admin' = 'tous';
  selectedStatus: AvisStatus | 'TOUS' = 'TOUS';
  selectedTargetType: TargetType = 'EVENEMENT';
  cibleId = '';
  search = '';

  // Formulaire création
  showForm = false;
  newAvis: AvisRequest = {
    note: 5,
    commentaire: '',
    cibleId: '',
    typeCible: 'EVENEMENT'
  };

  loading = false;
  saving = false;
  error = '';
  success = '';

  // Rejet
  rejectingId: string | null = null;
  rejectMotif = '';

  constructor(public community: CommunityService, private router: Router) {}

  ngOnInit(): void {
    this.community.connectNotificationsSocket();
    this.loadAvis();
  }

  get isAdmin(): boolean { return this.community.isAdmin(); }

  get statusLabel(): Record<AvisStatus, string> {
    return {
      EN_ATTENTE: '⏳ En attente',
      VALIDE: '✅ Validé',
      REJETE: '❌ Rejeté'
    };
  }

  setTab(tab: 'tous' | 'mes-avis' | 'admin'): void {
    this.selectedTab = tab;
    this.loadAvis();
  }

  loadAvis(): void {
    this.loading = true;
    this.error = '';

    if (this.selectedTab === 'mes-avis') {
      this.community.getMyAvis().subscribe({
        next: avis => { this.avis = avis; this.applyFilters(); this.loading = false; },
        error: () => { this.error = 'Impossible de charger vos avis.'; this.loading = false; }
      });
    } else if (this.selectedTab === 'admin' && this.isAdmin) {
      this.community.getAvisByStatus('EN_ATTENTE').subscribe({
        next: avis => { this.avis = avis; this.applyFilters(); this.loading = false; },
        error: () => { this.error = 'Impossible de charger les avis à modérer.'; this.loading = false; }
      });
    } else {
      if (!this.cibleId.trim()) {
        this.avis = [];
        this.filteredAvis = [];
        this.loading = false;
        return;
      }
      this.community.getAvisByTarget(this.cibleId.trim(), this.selectedTargetType).subscribe({
        next: avis => {
          this.avis = avis;
          this.applyFilters();
          this.loading = false;
          this.loadStats();
        },
        error: () => { this.error = 'Impossible de charger les avis.'; this.loading = false; }
      });
    }
  }

  loadStats(): void {
    if (!this.cibleId.trim()) return;
    this.community.getAvisStats(this.cibleId.trim(), this.selectedTargetType).subscribe({
      next: stats => this.stats = stats,
      error: () => this.stats = null
    });
  }

  applyFilters(): void {
    let filtered = [...this.avis];

    if (this.selectedStatus !== 'TOUS') {
      filtered = filtered.filter(a => a.statut === this.selectedStatus);
    }

    const query = this.search.trim().toLowerCase();
    if (query) {
      filtered = filtered.filter(a =>
        a.commentaire.toLowerCase().includes(query) ||
        a.utilisateurNom.toLowerCase().includes(query)
      );
    }

    this.filteredAvis = filtered;
  }

  setStatusFilter(status: AvisStatus | 'TOUS'): void {
    this.selectedStatus = status;
    this.applyFilters();
  }

  submitAvis(): void {
    if (!this.newAvis.commentaire.trim()) {
      this.error = 'Le commentaire est obligatoire.';
      return;
    }
    if (!this.newAvis.cibleId.trim()) {
      this.error = 'L\'ID de la cible est obligatoire.';
      return;
    }
    if (this.newAvis.note < 1 || this.newAvis.note > 5) {
      this.error = 'La note doit être entre 1 et 5.';
      return;
    }

    this.saving = true;
    this.error = '';

    this.community.createAvis(this.newAvis).subscribe({
      next: () => {
        this.success = '✅ Avis envoyé. En attente de validation par un administrateur.';
        this.saving = false;
        this.showForm = false;
        this.newAvis = { note: 5, commentaire: '', cibleId: '', typeCible: 'EVENEMENT' };
        setTimeout(() => this.success = '', 5000);
        this.loadAvis();
      },
      error: (e) => {
        this.error = e.error?.message || 'Erreur lors de l\'envoi.';
        this.saving = false;
      }
    });
  }

  validate(a: AvisResponse): void {
    if (!confirm('Valider cet avis ?')) return;
    this.community.validateAvis(a.id).subscribe({
      next: () => {
        this.success = '✅ Avis validé et rendu visible.';
        setTimeout(() => this.success = '', 3000);
        this.loadAvis();
      },
      error: () => this.error = 'Validation impossible.'
    });
  }

  openReject(a: AvisResponse): void {
    this.rejectingId = a.id;
    this.rejectMotif = '';
  }

  cancelReject(): void {
    this.rejectingId = null;
    this.rejectMotif = '';
  }

  confirmReject(): void {
    if (!this.rejectingId) return;
    const motif = this.rejectMotif.trim() || 'Non conforme aux règles de la communauté';

    this.community.rejectAvis(this.rejectingId, motif).subscribe({
      next: () => {
        this.success = '❌ Avis rejeté.';
        this.rejectingId = null;
        this.rejectMotif = '';
        setTimeout(() => this.success = '', 3000);
        this.loadAvis();
      },
      error: () => this.error = 'Rejet impossible.'
    });
  }

  stars(note: number): string[] {
    return Array(5).fill(0).map((_, i) => i < note ? '★' : '☆');
  }

  setNote(n: number): void {
    this.newAvis.note = n;
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
    if (m < 60) return `il y a ${m}min`;
    const h = Math.floor(m / 60);
    if (h < 24) return `il y a ${h}h`;
    const j = Math.floor(h / 24);
    if (j < 7) return `il y a ${j}j`;
    return d.toLocaleDateString('fr-FR');
  }

  detail(a: AvisResponse): void {
    void this.router.navigate(['/reviews', a.id]);
  }

  contactAuthor(a: AvisResponse): void {
    if (!a.utilisateurId || a.utilisateurId === this.community.getCurrentEmail()) return;
    this.community.getOrCreateConversation(a.utilisateurId, a.id).subscribe({
      next: conv => void this.router.navigate(['/messages', conv.id]),
      error: () => this.error = 'Conversation impossible.'
    });
  }

  // Distribution des notes pour le graphique
  notesDistribution(): { note: number; count: number; pct: number }[] {
    if (!this.stats) return [];
    const total = this.stats.nombreTotal || 1;
    return [
      { note: 5, count: this.stats.nombre5Etoiles, pct: (this.stats.nombre5Etoiles / total) * 100 },
      { note: 4, count: this.stats.nombre4Etoiles, pct: (this.stats.nombre4Etoiles / total) * 100 },
      { note: 3, count: this.stats.nombre3Etoiles, pct: (this.stats.nombre3Etoiles / total) * 100 },
      { note: 2, count: this.stats.nombre2Etoiles, pct: (this.stats.nombre2Etoiles / total) * 100 },
      { note: 1, count: this.stats.nombre1Etoile, pct: (this.stats.nombre1Etoile / total) * 100 }
    ];
  }
}
