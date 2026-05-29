// src/app/admin/dashboard/dashboard.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';
import { UserService } from '../../services/user.service';
import { EventService } from '../../services/event.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {

  // ── Données brutes ───────────────────────────────────────
  sorties: SortieResponse[] = [];
  equipes: EquipeResponse[] = [];
  loading = false;

  // ── KPIs autres modules (dynamiques si services dispo) ──
  totalUsers  = 0;
  totalEvents = 0;

  // ── KPIs sorties ─────────────────────────────────────────
  totalSorties      = 0;
  totalParticipants = 0;
  totalEquipes      = 0;
  sortiesAVenir:    SortieResponse[] = [];
  sortiesCompletes  = 0;
  placesDisponibles = 0;
  moyParticipantsSortie = 0;
  tauxRemplissage   = 0;

  // ── Stats difficultés ────────────────────────────────────
  nbFacile    = 0;
  nbMoyen     = 0;
  nbDifficile = 0;

  // ── Top sorties ──────────────────────────────────────────
  topSorties: { titre: string; nb: number }[] = [];

  // ── User info ────────────────────────────────────────────
  userName    = 'Admin';
  userRole    = '';
  userInitiales = 'A';

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private userService:   UserService,
    private eventService:  EventService
  ) {}

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadData();
  }

  loadUserInfo(): void {
    const nom    = localStorage.getItem('userNom')    ?? '';
    const prenom = localStorage.getItem('userPrenom') ?? '';
    this.userRole = localStorage.getItem('userRole')  ?? '';
    this.userName = (prenom || nom)
      ? `${prenom} ${nom}`.trim()
      : (localStorage.getItem('userEmail')?.split('@')[0] ?? 'Admin');
    this.userInitiales = this.userName
      .split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2) || 'A';
  }

  loadData(): void {
    this.loading = true;

    // ── Sorties ──────────────────────────────────────────
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        const now = new Date();
        this.sorties = data || [];

        this.totalSorties     = this.sorties.length;
        this.totalParticipants = this.sorties.reduce(
          (s, x) => s + this.getNbPart(x), 0
        );
        this.sortiesAVenir = this.sorties
          .filter(s => new Date(s.dateDebut) >= now)
          .sort((a, b) => new Date(a.dateDebut).getTime() - new Date(b.dateDebut).getTime())
          .slice(0, 5);
        this.sortiesCompletes = this.sorties.filter(s =>
          this.getNbPart(s) >= s.capaciteMax
        ).length;
        this.placesDisponibles = this.sorties.reduce(
          (s, x) => s + Math.max(0, x.capaciteMax - this.getNbPart(x)), 0
        );
        this.moyParticipantsSortie = this.totalSorties
          ? Math.round(this.totalParticipants / this.totalSorties) : 0;

        const totalCap = this.sorties.reduce((s, x) => s + (x.capaciteMax || 0), 0);
        this.tauxRemplissage = totalCap
          ? Math.round((this.totalParticipants / totalCap) * 100) : 0;

        // Difficultés
        this.nbFacile    = this.sorties.filter(s => s.difficulte === 'FACILE').length;
        this.nbMoyen     = this.sorties.filter(s => s.difficulte === 'MOYEN').length;
        this.nbDifficile = this.sorties.filter(s => s.difficulte === 'DIFFICILE').length;

        // Top 5
        this.topSorties = [...this.sorties]
          .sort((a, b) => this.getNbPart(b) - this.getNbPart(a))
          .slice(0, 5)
          .map(s => ({ titre: s.titre, nb: this.getNbPart(s) }));

        this.loading = false;
      },
      error: () => { this.loading = false; }
    });

    // ── Équipes ──────────────────────────────────────────
    this.equipeService.getAllEquipes().subscribe({
      next: (data) => {
        this.equipes     = data || [];
        this.totalEquipes = this.equipes.length;
      },
      error: () => {}
    });

    // ── Utilisateurs (module admin global) ──────────────
    this.userService.getTotalUsers().subscribe({
      next: (n: number) => { this.totalUsers = n; },
      error: () => { this.totalUsers = 0; }
    });

    // ── Events (autre module) ────────────────────────────
    this.eventService.getTotalEvents().subscribe({
      next: (n: number) => { this.totalEvents = n; },
      error: () => { this.totalEvents = 0; }
    });
  }

  isAdmin(): boolean {
    return ['ADMIN', 'ROLE_ADMIN'].includes(this.userRole);
  }

  getNbPart(s: SortieResponse): number {
    return s.participantIds?.length ?? s.nombreParticipants ?? 0;
  }

  formatDate(d: any): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('fr-FR', {
      day: '2-digit', month: 'short', year: 'numeric'
    });
  }
}