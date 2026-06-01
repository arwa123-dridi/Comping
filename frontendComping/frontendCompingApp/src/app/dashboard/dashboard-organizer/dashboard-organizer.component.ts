import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { CloudinaryService } from '../../services/cloudinary.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-dashboard-organizer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard-organizer.component.html',
  styleUrls: ['./dashboard-organizer.component.css']
})
export class DashboardOrganizerComponent implements OnInit {
  userName = '';
  userId: string | null = null;
  loading = true;

  totalSortiesCrees = 0;
  totalEquipes = 0;
  totalParticipants = 0;
  totalMembres = 0;
  sortiesAVenir = 0;
  sortiesPassees = 0;
  tauxRemplissage = 0;

  mesSortiesCreees: SortieResponse[] = [];
  sortiesAVenirList: SortieResponse[] = [];
  mesEquipes: EquipeResponse[] = [];
  topFiveSorties: SortieResponse[] = [];

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private cloudinaryService: CloudinaryService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId');
    this.userName = localStorage.getItem('userNom') || localStorage.getItem('userEmail')?.split('@')[0] || 'Organisateur';
    this.loadData();
  }

  loadData(): void {
    this.loading = true;

    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        this.mesSortiesCreees = data.filter(s => String(s.organisateurId) === String(this.userId));
        this.totalSortiesCrees = this.mesSortiesCreees.length;

        this.sortiesAVenirList = this.mesSortiesCreees
          .filter(s => !this.isPastDate(s.dateDebut))
          .sort((a, b) => new Date(a.dateDebut).getTime() - new Date(b.dateDebut).getTime());

        this.sortiesAVenir = this.sortiesAVenirList.length;
        this.sortiesPassees = this.mesSortiesCreees.filter(s => this.isPastDate(s.dateDebut)).length;

        this.topFiveSorties = this.mesSortiesCreees
          .slice()
          .sort((a, b) => this.getParticipantsCount(b) - this.getParticipantsCount(a))
          .slice(0, 5);

        this.totalParticipants = this.mesSortiesCreees.reduce(
          (acc, s) => acc + (s.participantIds ? s.participantIds.length : (s.nombreParticipants ?? 0)),
          0
        );

        const capaciteTotale = this.mesSortiesCreees.reduce((acc, s) => acc + (s.capaciteMax ?? 0), 0);
        this.tauxRemplissage = capaciteTotale > 0 ? Math.round((this.totalParticipants / capaciteTotale) * 100) : 0;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });

    this.equipeService.getAllEquipes().subscribe({
      next: (data) => {
        this.mesEquipes = data.filter(e => String(e.organisateurId) === String(this.userId));
        this.totalEquipes = this.mesEquipes.length;
        this.totalMembres = this.mesEquipes.reduce(
          (acc, e) => acc + (e.membres?.length ?? e.nbMembresActuels ?? 0),
          0
        );
      },
      error: () => {}
    });
  }

  deleteSortie(s: SortieResponse, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Supprimer d�finitivement "${s.titre}" ?`)) return;
    this.sortieService.deleteSortie(String(s.id)).subscribe({
      next: () => this.loadData(),
      error: () => alert('Erreur lors de la suppression')
    });
  }

  getParticipantsCount(s: SortieResponse): number {
    return s.participantIds?.length ?? s.nombreParticipants ?? 0;
  }

  getCoverImageUrl(s: SortieResponse): string {
    return this.cloudinaryService.getImageUrl(s.imageUrl, s.difficulte, s.id, s.titre);
  }

  getInitiales(): string {
    return (this.userName || '?')
      .split(' ')
      .map(w => w[0])
      .join('')
      .toUpperCase()
      .slice(0, 2);
  }

  getDiffClass(d: string): string {
    const map: Record<string, string> = {
      FACILE: 'diff-facile',
      MOYEN: 'diff-moyen',
      DIFFICILE: 'diff-difficile'
    };
    return map[d] || '';
  }

  getStatusLabel(s: SortieResponse): string {
    if (s.statut?.toString().toLowerCase().includes('en')) {
      return 'En cours';
    }
    if (s.statut?.toString().toLowerCase().includes('term')) {
      return 'Terminé';
    }
    return this.isPastDate(s.dateDebut) ? 'Terminée' : 'À venir';
  }

  getStatusClass(s: SortieResponse): string {
    if (s.statut?.toString().toLowerCase().includes('en')) {
      return 'status-ongoing';
    }
    if (s.statut?.toString().toLowerCase().includes('term')) {
      return 'status-past';
    }
    return this.isPastDate(s.dateDebut) ? 'status-past' : 'status-upcoming';
  }

  formatDate(d: any): string {
    return new Date(d).toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  isPastDate(date: string | Date): boolean {
    return new Date(date) < new Date();
  }
}
