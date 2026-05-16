import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-dashboard-organizer',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
<div class="dashboard-container">
  <header class="dashboard-header">
    <h1>🧗 Dashboard Organisateur</h1>
    <div class="user-info">
      <span>{{ getInitiales() }}</span>
      <span>{{ userName }}</span>
    </div>
  </header>

  <div class="stats-grid">
    <div class="stat-card" routerLink="/sorties">
      <h3>{{ totalSortiesCrees }}</h3>
      <p>Sorties Créées</p>
    </div>
    <div class="stat-card" routerLink="/equipes">
      <h3>{{ totalEquipes }}</h3>
      <p>Mes Équipes</p>
    </div>
  </div>

  <div class="quick-actions">
    <button class="btn-primary" routerLink="/sorties/create">
      ➕ Nouvelle Sortie
    </button>
    <button class="btn-secondary" routerLink="/equipes/create">
      👥 Nouvelle Équipe
    </button>
  </div>

  <!-- Recent -->
  <section *ngIf="mesSortiesCreees.length" class="recent-section">
    <h2>Sorties Récentes</h2>
    <div class="cards-grid">
      <div *ngFor="let s of mesSortiesCreees.slice(0,3)" class="card" routerLink="/sorties/{{s.id}}">
        <h3>{{s.titre}}</h3>
        <p>{{s.dateDebut | date:'short'}}</p>
      </div>
    </div>
  </section>
</div>
  `,
  styleUrls: ['./dashboard-organizer.component.css'] // Inline styles optional
})
export class DashboardOrganizerComponent implements OnInit {
  userName = '';
  userId: string | null = null;
  totalSortiesCrees = 0;
  totalEquipes = 0;
  mesSortiesCreees: SortieResponse[] = [];

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId');
    this.userName = localStorage.getItem('userNom') || 'Organisateur';
    this.loadOrganizerData();
  }

  loadOrganizerData(): void {
    this.sortieService.getAllSorties().subscribe(data => {
      this.mesSortiesCreees = data.filter(s => String(s.organisateurId) === String(this.userId));
      this.totalSortiesCrees = this.mesSortiesCreees.length;
    });
    this.equipeService.getAllEquipes().subscribe(data => {
      this.totalEquipes = data.filter(e => e.organisateurId === this.userId).length;
    });
  }

  getInitiales(): string {
    return this.userName.split(' ').map(w => w[0]).join('').toUpperCase().slice(0,2);
  }
}
