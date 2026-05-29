// src/app/sortie/sortie-detail/sortie-detail.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { ParticipationDTO } from '../../models/participation.model';

@Component({
  selector: 'app-sortie-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './sortie-detail.component.html',
  styleUrls: ['./sortie-detail.component.css']
})
export class SortieDetailComponent implements OnInit {

  sortie:            SortieResponse | null = null;
  loading            = true;
  error:             string | null = null;
  inscriptionEnCours = false;
  showModal          = false;          // ✅ Modal désinscription

  toastMessage: string | null = null;
  toastType:    'success' | 'error' | 'info' = 'info';
  private toastTimer: any;

  equipeNom = '';
  userRole: string | null = null;

  // Images hero par difficulté
  private readonly heroImages: Record<string, string> = {
    FACILE:    'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?w=1400&q=70',
    MOYEN:     'https://images.unsplash.com/photo-1551632811-561732d1e306?w=1400&q=70',
    DIFFICILE: 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1400&q=70',
    DEFAULT:   'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1400&q=70',
  };

  // Couleurs avatars participants
  private readonly avatarColors = [
    '#16a34a','#1d4ed8','#d97706','#dc2626',
    '#7c3aed','#0891b2','#be185d','#15803d',
  ];

  constructor(
    private route:        ActivatedRoute,
    private router:       Router,
    private sortieService:SortieService,
    private equipeService:EquipeService
  ) {}

  ngOnInit(): void {
    this.userRole = localStorage.getItem('userRole');
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadSortie(id);
    else { this.error = 'ID introuvable'; this.loading = false; }
  }

  // ── Rôles ────────────────────────────────────────────────
  isUser(): boolean {
    const r = this.userRole ?? '';
    return r === 'USER' || r === 'ROLE_USER';
  }

  isAdmin(): boolean {
    const r = this.userRole ?? '';
    return r === 'ADMIN' || r === 'ROLE_ADMIN';
  }

  isOrganisateurRole(): boolean {
    const r = this.userRole ?? '';
    return r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR';
  }

  isConnected(): boolean {
    const t = localStorage.getItem('authToken');
    return !!t && t !== 'null' && t !== 'undefined';
  }

  // ── Getters calculés ─────────────────────────────────────

  /** true si l'utilisateur connecté est l'organisateur de cette sortie */
  get isOrganisateur(): boolean {
    const uid = localStorage.getItem('userId');
    return !!uid && String(this.sortie?.organisateurId) === String(uid);
  }

  /** true si l'utilisateur connecté est inscrit */
  get isParticipant(): boolean {
    if (!this.sortie) return false;
    const uid = localStorage.getItem('userId') ?? '';
    return (this.sortie.participantIds ?? []).map(String).includes(String(uid));
  }

  /** Nombre de participants inscrits */
  get nombreParticipants(): number {
    return this.sortie?.participantIds?.length
      ?? (this.sortie as any)?.participants?.length
      ?? this.sortie?.nombreParticipants
      ?? 0;
  }

  /** Places restantes */
  get placesDisponibles(): number {
    return Math.max(0, (this.sortie?.capaciteMax ?? 0) - this.nombreParticipants);
  }

  /** Pourcentage de remplissage */
  getPlacesPercent(): number {
    if (!this.sortie?.capaciteMax) return 0;
    return Math.min((this.nombreParticipants / this.sortie.capaciteMax) * 100, 100);
  }

  /** Vérifie si la date de la sortie est passée */
  isSortiePassee(): boolean {
    if (!this.sortie?.dateDebut) return false;
    return new Date(this.sortie.dateDebut) < new Date();
  }

  /** Durée entre dateDebut et dateFin */
  getDuree(): string {
    if (!this.sortie?.dateDebut || !this.sortie?.dateFin) return 'Non précisée';
    const diff = new Date(this.sortie.dateFin).getTime()
               - new Date(this.sortie.dateDebut).getTime();
    const h = Math.round(diff / 3_600_000);
    if (h < 24) return `${h}h`;
    const j = Math.floor(h / 24);
    const r = h % 24;
    return r > 0 ? `${j}j ${r}h` : `${j} jour(s)`;
  }

  // ── Chargement ───────────────────────────────────────────
  loadSortie(id: string): void {
    this.loading = true;
    this.error   = null;
    this.sortieService.getSortieById(id).subscribe({
      next: (data) => {
        this.sortie   = data;
        this.equipeNom = (data as any).equipeNom
          || (data as any).equipe?.nom
          || 'Équipe associée';
        this.loading  = false;
      },
      error: (err) => {
        if (err.status === 404) this.error = 'Cette randonnée n\'existe pas.';
        else if (err.status === 0) this.error = 'Serveur inaccessible.';
        else this.error = 'Impossible de charger la randonnée.';
        this.loading = false;
      }
    });
  }

  // ── Actions ──────────────────────────────────────────────

  /** Inscrire l'utilisateur à la sortie */
  inscrire(): void {
    if (!this.sortie) return;

    // Rediriger vers login si non connecté
    if (!this.isConnected()) {
      localStorage.setItem('redirect_after_login', `/sorties/${this.sortie.id}`);
      this.router.navigate(['/login']);
      return;
    }

    // Blocage rôle non USER
    if (this.isOrganisateurRole() || this.isAdmin()) {
      this.showToast('Seuls les utilisateurs peuvent s\'inscrire aux sorties.', 'error');
      return;
    }

    if (this.isParticipant) {
      this.showToast('Vous êtes déjà inscrit à cette sortie.', 'info');
      return;
    }
    if (this.isSortiePassee()) {
      this.showToast('Cette sortie est déjà passée.', 'error');
      return;
    }
    if (this.placesDisponibles === 0) {
      this.showToast('Plus de places disponibles.', 'error');
      return;
    }

    this.inscriptionEnCours = true;

    this.sortieService.inscrire(this.sortie.id).subscribe({
      next: (response: ParticipationDTO | any) => {
        this.inscriptionEnCours = false;
        this.showToast(response?.message || '✅ Inscription réussie !', 'success');

        // ✅ Ajouter automatiquement à l'équipe si equipeId existe
        if (this.sortie?.equipeId) {
          const userId  = localStorage.getItem('userId') ?? '';
          const prenom  = localStorage.getItem('userPrenom') ?? '';
          const nom     = localStorage.getItem('userNom') ?? '';
          const userNom = `${prenom} ${nom}`.trim() || 'Participant';
          this.equipeService.ajouterMembre(
            this.sortie.equipeId, userId, userNom
          ).subscribe();
        }

        // Recharger les données
        this.loadSortie(this.sortie!.id);
      },
      error: (err) => {
        this.inscriptionEnCours = false;
        if (err.status === 401 || err.status === 403) {
          this.router.navigate(['/login']);
          return;
        }
        if (err.status === 409) {
          this.showToast('Vous êtes déjà inscrit à cette sortie.', 'error');
          return;
        }
        this.showToast(err.error?.message || 'Erreur lors de l\'inscription.', 'error');
      }
    });
  }

  /** Ouvre le modal de confirmation désinscription */
  openModal(): void {
    this.showModal = true;
  }

  /** Ferme le modal sans action */
  cancelModal(): void {
    this.showModal = false;
  }

  /** Confirme la désinscription (appelé depuis le modal) */
  confirmDesinscrire(): void {
    this.showModal = false;
    if (!this.sortie) return;

    this.sortieService.desinscrire(this.sortie.id).subscribe({
      next: () => {
        // ✅ Retirer aussi de l'équipe si applicable
        if (this.sortie?.equipeId) {
          const userId = localStorage.getItem('userId') ?? '';
          this.equipeService.retirerMembre(this.sortie.equipeId, userId).subscribe();
        }
        this.showToast('✅ Désinscription effectuée.', 'success');
        this.loadSortie(this.sortie!.id);
      },
      error: (err) => {
        this.showToast(err.error?.message || '❌ Erreur lors de la désinscription.', 'error');
      }
    });
  }

  /** Redirige vers la page de modification */
  modifier(): void {
    if (!this.sortie) return;
    this.router.navigate(['/admin/sorties/edit', this.sortie.id]);
  }

  /** Supprime la sortie après confirmation */
  supprimer(): void {
    if (!this.sortie) return;
    if (!confirm(`Supprimer définitivement "${this.sortie.titre}" ?`)) return;
    this.sortieService.deleteSortie(this.sortie.id).subscribe({
      next: () => {
        this.showToast('Randonnée supprimée.', 'success');
        setTimeout(() => this.router.navigate(['/sorties']), 1200);
      },
      error: () => this.showToast('❌ Erreur lors de la suppression.', 'error')
    });
  }

  // ── Helpers visuels ──────────────────────────────────────

  getDiffLabel(diff: string): string {
    const map: Record<string, string> = {
      FACILE:    '🥾 Facile',
      MOYEN:     '🧗 Modéré',
      DIFFICILE: '⛰️ Difficile',
    };
    return map[diff] ?? diff;
  }

  /** Image hero : Cloudinary en priorité, sinon Unsplash selon difficulté */
  getHeroImage(): string {
    if (this.sortie?.imageUrl?.trim() && this.sortie.imageUrl !== 'null') {
      return this.sortie.imageUrl;
    }
    return this.heroImages[this.sortie?.difficulte ?? 'DEFAULT']
      ?? this.heroImages['DEFAULT'];
  }

  /** Liste des IDs participants (pour les avatars) */
  getParticipantsList(): string[] {
    return (this.sortie?.participantIds ?? []).map(String);
  }

  /** Initiales à partir d'un ID ou nom */
  getInitiales(idOrNom: string): string {
    if (!idOrNom) return '?';
    const clean = idOrNom.replace(/[^a-zA-Z]/g, '').toUpperCase();
    return clean.length >= 2 ? clean.slice(0, 2) : clean || '?';
  }

  /** Couleur avatar déterministe selon l'ID */
  getAvatarColor(id: string): string {
    const idx = (id?.charCodeAt(0) ?? 0) % this.avatarColors.length;
    return this.avatarColors[idx];
  }

  // ── Toast ────────────────────────────────────────────────
  showToast(message: string, type: 'success' | 'error' | 'info' = 'info'): void {
    this.toastMessage = message;
    this.toastType    = type;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer   = setTimeout(() => { this.toastMessage = null; }, 4000);
  }
}