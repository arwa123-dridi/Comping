import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';

@Component({
  selector: 'app-sortie-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sortie-detail.component.html',
  styleUrls: ['./sortie-detail.component.css']
})
export class SortieDetailComponent implements OnInit {
  sortie: SortieResponse | null = null;
  loading = true;
  error: string | null = null;
  inscriptionEnCours = false;
  equipeNom = '';

  private readonly heroImages = [
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&q=80',
    'https://images.unsplash.com/photo-1551632811-561732d1e306?w=1200&q=80',
    'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1200&q=80',
    'https://images.unsplash.com/photo-1519904981063-b0cf448d479e?w=1200&q=80'
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private sortieService: SortieService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadSortie(id);
    else { this.error = 'ID non trouvé'; this.loading = false; }
  }

  loadSortie(id: string): void {
    this.loading = true;
    this.sortieService.getSortieById(id).subscribe({
      next: (data) => {
        this.sortie = data;
        // Essayer d'obtenir le nom de l'équipe depuis les différents champs possibles
        this.equipeNom = (data as any).equipe?.nom
          || (data as any).equipeNom
          || 'Sans équipe';
        this.loading = false;
      },
      error: () => {
        this.error = 'Impossible de charger la randonnée.';
        this.loading = false;
      }
    });
  }

  // ── Getters ──────────────────────────────────────────────

  get isParticipant(): boolean {
    const userId = localStorage.getItem('userId');
    if (!userId || !this.sortie) return false;
    const ids = (this.sortie as any).participantIds
      || (this.sortie as any).participants?.map((p: any) => p?.id ?? p)
      || [];
    return ids.map(String).includes(String(userId));
  }

  get isOrganisateur(): boolean {
    const userId = localStorage.getItem('userId');
    return !!userId && this.sortie?.organisateurId === userId;
  }

  get nombreParticipants(): number {
    if (!this.sortie) return 0;
    if ((this.sortie as any).participantIds?.length !== undefined)
      return (this.sortie as any).participantIds.length;
    return (this.sortie as any).participants?.length || 0;
  }

  get placesDisponibles(): number {
    return Math.max(0, (this.sortie?.capaciteMax || 0) - this.nombreParticipants);
  }

  // ── Helpers ───────────────────────────────────────────────

  getDuree(): string {
    if (!this.sortie?.dateDebut || !this.sortie?.dateFin) return 'Non spécifiée';
    const diff = new Date(this.sortie.dateFin).getTime()
               - new Date(this.sortie.dateDebut).getTime();
    const heures = Math.round(diff / 3_600_000);
    return heures < 24 ? `${heures}h` : `${Math.round(heures / 24)} jour(s)`;
  }

  getDiffLabel(diff: string): string {
    return { FACILE: '🥾 Facile', MOYEN: '🧗 Modéré', DIFFICILE: '⛰️ Difficile' }[diff] || diff;
  }

  getHeroImage(): string {
    if (!this.sortie) return this.heroImages[0];
    const idx = this.sortie.titre.length % this.heroImages.length;
    return this.heroImages[idx];
  }

  // ── Actions ───────────────────────────────────────────────

  inscrire(): void {
    if (!this.sortie || this.placesDisponibles <= 0) return;
    const token = localStorage.getItem('authToken');
    if (!token) { this.router.navigate(['/login']); return; }

    this.inscriptionEnCours = true;
    this.sortieService.inscrire(this.sortie.id).subscribe({
      next: () => {
        this.loadSortie(this.sortie!.id);
        this.inscriptionEnCours = false;
        alert('✅ Inscription réussie !');
      },
      error: (err) => {
        alert(err.error?.message || 'Erreur lors de l\'inscription.');
        this.inscriptionEnCours = false;
      }
    });
  }

  desinscrire(): void {
    if (!this.sortie || !confirm('Voulez-vous vraiment vous désinscrire ?')) return;
    this.sortieService.desinscrire(this.sortie.id).subscribe({
      next: () => this.loadSortie(this.sortie!.id),
      error: () => alert('Erreur lors de la désinscription.')
    });
  }

  modifier(): void {
    this.router.navigate(['/sorties/edit', this.sortie?.id]);
  }

  supprimer(): void {
    if (!this.sortie || !confirm('⚠️ Supprimer définitivement cette randonnée ?')) return;
    this.sortieService.deleteSortie(this.sortie.id).subscribe({
      next: () => this.router.navigate(['/sorties']),
      error: () => alert('Erreur lors de la suppression.')
    });
  }
}