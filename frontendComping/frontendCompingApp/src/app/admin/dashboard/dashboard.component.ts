import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';
import { DemandeTransportService } from '../../services/demande-transport.service';
import { IncidentService } from '../../services/incident.service';
import { DemandeTransportResponse } from '../../models/demande-transport.model';
import { IncidentResponse } from '../../models/incident.model';

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
  sortiesFaciles = 0;
  sortiesMoyennes = 0;
  sortiesDifficiles = 0;
  tauxRemplissage = 0;

  // User infos
  userName = 'Organisateur';
  userRole = '';
  userInitiales = 'O';

  // Logistique (donnees reelles)
  demandesTransport: DemandeTransportResponse[] = [];
  incidents: IncidentResponse[] = [];
  totalDemandesEnAttente = 0;
  totalIncidentsOuverts = 0;
  totalIncidentsCritiques = 0;

  // Fil d'activité (calculé à partir des vraies données ci-dessus)
  activitesRecentes: { iconClass: string; icon: 'sortie' | 'transport' | 'incident' | 'equipe'; text: string; date: Date }[] = [];

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private demandeTransportService: DemandeTransportService,
    private incidentService: IncidentService
  ) {}

  ngOnInit(): void {
    this.loadUserInfo();
    this.loadData();
    this.loadLogistique();
  }

  loadLogistique(): void {
    this.demandeTransportService.getAll().subscribe({
      next: (data) => {
        this.demandesTransport = data;
        this.totalDemandesEnAttente = data.filter(d => d.statut === 'EN_ATTENTE').length;
        this.updateActivitesRecentes();
      },
      error: () => {}
    });
    this.incidentService.getAll().subscribe({
      next: (data) => {
        this.incidents = data;
        this.totalIncidentsOuverts = data.filter(i => i.statut === 'OUVERT' || i.statut === 'EN_COURS').length;
        this.totalIncidentsCritiques = data.filter(i => i.priorite === 'CRITIQUE' && (i.statut === 'OUVERT' || i.statut === 'EN_COURS')).length;
        this.updateActivitesRecentes();
      },
      error: () => {}
    });
  }

  updateActivitesRecentes(): void {
    const items: { iconClass: string; icon: 'sortie' | 'transport' | 'incident' | 'equipe'; text: string; date: Date }[] = [];

    this.sorties.forEach(s => items.push({
      icon: 'sortie', iconClass: 'green',
      text: `Sortie "${s.titre}" créée`,
      date: new Date(s.dateCreation)
    }));
    this.equipes.forEach(e => items.push({
      icon: 'equipe', iconClass: 'blue',
      text: `Équipe "${e.nom}" créée`,
      date: new Date(e.dateCreation)
    }));
    this.demandesTransport.forEach(d => items.push({
      icon: 'transport', iconClass: 'blue',
      text: `Demande de transport (${d.typeService}) — ${d.statut === 'EN_ATTENTE' ? 'en attente' : d.statut.toLowerCase()}`,
      date: new Date(d.dateCreation)
    }));
    this.incidents.forEach(i => items.push({
      icon: 'incident', iconClass: i.priorite === 'CRITIQUE' ? 'red' : 'amber',
      text: `Incident "${i.type}" déclaré`,
      date: new Date(i.dateDeclaration)
    }));

    this.activitesRecentes = items
      .filter(it => !isNaN(it.date.getTime()))
      .sort((a, b) => b.date.getTime() - a.date.getTime())
      .slice(0, 6);
  }

  timeAgo(date: Date): string {
    const diffMs = Date.now() - date.getTime();
    const minutes = Math.floor(diffMs / 60000);
    if (minutes < 1) return "à l'instant";
    if (minutes < 60) return `il y a ${minutes} min`;
    const hours = Math.floor(minutes / 60);
    if (hours < 24) return `il y a ${hours}h`;
    const days = Math.floor(hours / 24);
    return `il y a ${days}j`;
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
        this.sortiesFaciles = this.sorties.filter(s => s.difficulte === 'FACILE').length;
        this.sortiesMoyennes = this.sorties.filter(s => s.difficulte === 'MOYEN').length;
        this.sortiesDifficiles = this.sorties.filter(s => s.difficulte === 'DIFFICILE').length;
        const capaciteTotale = this.totalParticipants + this.placesDisponibles;
        this.tauxRemplissage = capaciteTotale > 0 ? Math.round((this.totalParticipants / capaciteTotale) * 100) : 0;
        this.loading = false;
        this.updateActivitesRecentes();
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
        this.updateActivitesRecentes();
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
