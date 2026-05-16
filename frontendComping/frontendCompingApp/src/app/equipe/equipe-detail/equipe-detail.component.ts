import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse, MembreDTO } from '../../models/equipe.model';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';

@Component({
  selector: 'app-equipe-detail',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './equipe-detail.component.html',
  styleUrls: ['./equipe-detail.component.css']
})
export class EquipeDetailComponent implements OnInit {
  equipe: EquipeResponse | null = null;
  sorties: SortieResponse[] = [];
  loading = false;
  error: string | null = null;
  actionLoading = false;
  membreActionId: string | null = null;  // ID du membre en cours de retrait

  private readonly heroImages = [
    'https://images.unsplash.com/photo-1533240332313-0db3b4591e1b?w=1200&q=80',
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=1200&q=80',
    'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=1200&q=80',
    'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=1200&q=80',
  ];

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private equipeService: EquipeService,
    private sortieService: SortieService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) this.loadEquipe(id);
    else this.router.navigate(['/equipes']);
  }

  loadEquipe(id: string): void {
    this.loading = true;
    this.equipeService.getEquipeById(id).subscribe({
      next: (data) => { this.equipe = data; this.loadSorties(id); this.loading = false; },
      error: () => { this.error = "Impossible de charger l'équipe."; this.loading = false; }
    });
  }

  loadSorties(equipeId: string): void {
    this.sortieService.getAllSorties().subscribe({
      next: (data) => { this.sorties = data.filter(s => s.equipeId === equipeId); },
      error: () => {}
    });
  }

  // ── Getters utilisateur ────────────────────────────────────
  get userId(): string { return localStorage.getItem('userId') ?? ''; }
  get userRole(): string { return localStorage.getItem('userRole') ?? ''; }

  isAdminRole(): boolean {
    return ['ADMIN', 'ROLE_ADMIN'].includes(this.userRole);
  }

  isOrganisateur(): boolean {
    return !!this.userId && (
      String(this.equipe?.organisateurId) === String(this.userId) || this.isAdminRole()
    );
  }

  isMembre(): boolean {
    return !!this.userId && !!this.equipe?.membres?.some(m => String(m.id) === String(this.userId));
  }

  get placesLibres(): number {
    if (!this.equipe) return 0;
    return Math.max(0, this.equipe.nbMembresMax - (this.equipe.membres?.length ?? 0));
  }

  // ── Helpers ─────────────────────────────────────────────────
  getHeroImage(): string {
    if (!this.equipe) return this.heroImages[0];
    const seed = this.equipe.nom.length;
    return this.heroImages[seed % this.heroImages.length];
  }

  getInitiales(nom: string): string {
    return (nom || '?').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  getAvatarColor(nom: string): string {
    const colors = ['#3da859','#1f73a3','#7c3aed','#f29027','#dc2626','#0891b2','#059669'];
    let hash = 0;
    for (let i = 0; i < nom.length; i++) hash = (hash * 31 + nom.charCodeAt(i)) >>> 0;
    return colors[hash % colors.length];
  }

  // ── REJOINDRE ────────────────────────────────────────────────
  rejoindre(): void {
    if (!this.equipe || this.actionLoading) return;
    if (!this.userId) { this.router.navigate(['/login']); return; }
    if (this.placesLibres <= 0) { alert('⛔ Équipe complète.'); return; }

    this.actionLoading = true;
    const userNom = localStorage.getItem('userNom') || 'Utilisateur';
    this.equipeService.ajouterMembre(this.equipe.id, this.userId, userNom).subscribe({
      next: () => { this.actionLoading = false; this.loadEquipe(this.equipe!.id); },
      error: (err) => {
        this.actionLoading = false;
        alert(err.status === 409 ? 'Vous êtes déjà membre.'
            : err.status === 400 ? 'Équipe complète.'
            : err.error?.message || "Erreur lors de l'adhésion.");
      }
    });
  }

  // ── QUITTER ───────────────────────────────────────────────────
  quitter(): void {
    if (!this.equipe || this.actionLoading) return;
    if (this.isOrganisateur()) {
      alert("⛔ L'organisateur ne peut pas quitter sa propre équipe.");
      return;
    }
    if (!confirm('Voulez-vous vraiment quitter cette équipe ?')) return;

    this.actionLoading = true;
    this.equipeService.retirerMembre(this.equipe.id, this.userId).subscribe({
      next: () => { this.actionLoading = false; this.router.navigate(['/equipes']); },
      error: (err) => {
        this.actionLoading = false;
        alert(err.status === 403 ? 'Action non autorisée.'
            : err.error?.message || 'Erreur lors de la désinscription.');
      }
    });
  }

  // ── RETIRER UN MEMBRE (organisateur seulement) ─────────────────
  retirerMembre(membre: MembreDTO): void {
    if (!this.equipe || !this.isOrganisateur()) return;
    if (String(membre.id) === String(this.userId)) {
      alert("⛔ Vous ne pouvez pas vous retirer vous-même.");
      return;
    }
    if (!confirm(`⚠️ Retirer ${membre.nom} de l'équipe ?`)) return;

    this.membreActionId = membre.id;
    this.equipeService.retirerMembre(this.equipe.id, membre.id).subscribe({
      next: () => {
        this.membreActionId = null;
        this.loadEquipe(this.equipe!.id);
      },
      error: (err) => {
        this.membreActionId = null;
        alert(err.error?.message || 'Erreur lors du retrait du membre.');
      }
    });
  }

  // ── MODIFIER équipe ───────────────────────────────────────────
  modifier(): void {
    if (!this.equipe) return;
    this.router.navigate(['/admin/equipes/edit', this.equipe.id]);
  }

  // ── SUPPRIMER équipe ──────────────────────────────────────────
  supprimer(): void {
    if (!this.equipe || !confirm('⚠️ Supprimer définitivement cette équipe ?')) return;
    this.equipeService.deleteEquipe(this.equipe.id).subscribe({
      next: () => this.router.navigate(['/admin/equipes']),
      error: () => alert('Erreur lors de la suppression.')
    });
  }
}
