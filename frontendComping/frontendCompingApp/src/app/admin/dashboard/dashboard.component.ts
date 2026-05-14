import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  // ── State ───────────────────────────────────────────
  sorties: SortieResponse[] = [];
  equipes: EquipeResponse[] = [];
  loading = false;

  // Stats calculées
  totalSorties = 0;
  totalParticipants = 0;
  totalEquipes = 0;
  totalMembres = 0;
  sortiesAVenir: SortieResponse[] = [];
  sortiesRecentes: SortieResponse[] = [];
  sortiesCompletes = 0;
  placesDisponibles = 0;

  // User infos
  userName = 'Organisateur';
  userRole = '';
  userInitiales = 'O';

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService
  ) {}

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadData();
  }

  loadUserInfo(): void {
    const nom    = localStorage.getItem('userNom')    ?? '';
    const prenom = localStorage.getItem('userPrenom') ?? '';
    this.userRole = localStorage.getItem('userRole') ?? '';
    this.userName = (prenom || nom)
      ? `${prenom} ${nom}`.trim()
      : (localStorage.getItem('userEmail')?.split('@')[0] ?? 'Organisateur');
    this.userInitiales = this.userName.split(' ')
      .map(w => w[0]).join('').toUpperCase().slice(0, 2) || 'O';
  }

  loadData(): void {
    this.loading = true;
    const userId = localStorage.getItem('userId') ?? '';

    // Charger sorties
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        const now = new Date();
        // Si organisateur: filtrer ses sorties; si admin: toutes
        this.sorties = this.isAdmin()
          ? data
          : data.filter(s => String(s.organisateurId) === String(userId));

        this.totalSorties = this.sorties.length;
        this.totalParticipants = this.sorties.reduce(
          (sum, s) => sum + (s.participantIds?.length ?? s.nombreParticipants ?? 0), 0
        );
        this.sortiesAVenir = this.sorties
          .filter(s => new Date(s.dateDebut) >= now)
          .sort((a, b) => new Date(a.dateDebut).getTime() - new Date(b.dateDebut).getTime())
          .slice(0, 5);
        this.sortiesRecentes = this.sorties
          .filter(s => new Date(s.dateDebut) < now)
          .sort((a, b) => new Date(b.dateDebut).getTime() - new Date(a.dateDebut).getTime())
          .slice(0, 4);
        this.sortiesCompletes = this.sorties.filter(s =>
          (s.participantIds?.length ?? s.nombreParticipants ?? 0) >= s.capaciteMax
        ).length;
        this.placesDisponibles = this.sorties.reduce(
          (sum, s) => sum + Math.max(0, s.capaciteMax - (s.participantIds?.length ?? s.nombreParticipants ?? 0)), 0
        );
        this.loading = false;
      },
      error: () => { this.loading = false; }
    });

    // Charger équipes
    this.equipeService.getAllEquipes().subscribe({
      next: (data) => {
        this.equipes = this.isAdmin()
          ? data
          : data.filter(e => String(e.organisateurId) === String(userId));
        this.totalEquipes = this.equipes.length;
        this.totalMembres = this.equipes.reduce(
          (sum, e) => sum + (e.membres?.length ?? e.nbMembresActuels ?? 0), 0
        );
      },
      error: () => {}
    });
  }

  isAdmin(): boolean {
    return ['ADMIN', 'ROLE_ADMIN'].includes(this.userRole);
  }

  isOrga(): boolean {
    return ['ORGANISATEUR', 'ROLE_ORGANISATEUR'].includes(this.userRole) || this.isAdmin();
  }

  getRoleLabel(): string {
    if (this.isAdmin()) return '👑 Administrateur';
    return '🏕️ Organisateur';
  }

  getDifficulteClass(d: string): string {
    return d === 'FACILE' ? 'diff-easy' : d === 'MOYEN' ? 'diff-med' : 'diff-hard';
  }

  getDifficulteLabel(d: string): string {
    return d === 'FACILE' ? '🥾 Facile' : d === 'MOYEN' ? '🧗 Modéré' : '⛰️ Difficile';
  }

  getNbParticipants(s: SortieResponse): number {
    return s.participantIds?.length ?? s.nombreParticipants ?? 0;
  }

  getPlacesPercent(s: SortieResponse): number {
    if (!s.capaciteMax) return 0;
    return Math.min(100, (this.getNbParticipants(s) / s.capaciteMax) * 100);
  }

  formatDate(d: Date | string): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
  }

  getInitiales(nom: string): string {
    return (nom || '?').split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }
}
