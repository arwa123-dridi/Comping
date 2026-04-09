import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-dashboard-user',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './dashboard-user.component.html',
  styleUrls: ['./dashboard-user.component.css']
})
export class DashboardUserComponent implements OnInit {

  // ── User info ─────────────────────────────────────────────
  userName  = '';
  userId: string | null = null;
  userRole  = 'USER';

  // ── Stats ──────────────────────────────────────────────────
  totalSortiesInscrites = 0;
  totalEquipesMembre    = 0;
  totalSortiesCreees    = 0;

  // ── Data ───────────────────────────────────────────────────
  mesSortiesInscrites: SortieResponse[] = [];
  mesEquipes: EquipeResponse[]          = [];
  mesSortiesCreees: SortieResponse[]    = [];

  loading = { sorties: false, equipes: false, stats: false };

  constructor(
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.userId   = localStorage.getItem('userId');
    this.userName = localStorage.getItem('userNom')
                 || localStorage.getItem('userPrenom')
                 || 'Campeur';
    this.userRole = localStorage.getItem('userRole') || 'USER';
    this.loadData();
  }

  loadData(): void {
    this.loadMesSorties();
    this.loadMesEquipes();
    this.loadMesSortiesCreees();
  }

  // ── Sorties inscrites ──────────────────────────────────────
  loadMesSorties(): void {
    this.loading.sorties = true;
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        this.mesSortiesInscrites = data.filter(s => {
          const ids = (s as any).participantIds
                   || (s as any).participants?.map((p: any) => p?.id ?? p)
                   || [];
          return ids.map(String).includes(String(this.userId));
        });
        this.totalSortiesInscrites = this.mesSortiesInscrites.length;
        this.loading.sorties = false;
      },
      error: () => { this.loading.sorties = false; }
    });
  }

  // ── Équipes ────────────────────────────────────────────────
  loadMesEquipes(): void {
    this.loading.equipes = true;
    this.equipeService.getAllEquipes().subscribe({
      next: (data) => {
        this.mesEquipes = data.filter(e =>
          e.membres?.some(m => String(m?.id) === String(this.userId))
        );
        this.totalEquipesMembre = this.mesEquipes.length;
        this.loading.equipes = false;
      },
      error: () => { this.loading.equipes = false; }
    });
  }

  // ── Sorties créées ─────────────────────────────────────────
  loadMesSortiesCreees(): void {
    this.loading.stats = true;
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        this.mesSortiesCreees = data.filter(s =>
          String(s.organisateurId) === String(this.userId)
        );
        this.totalSortiesCreees = this.mesSortiesCreees.length;
        this.loading.stats = false;
      },
      error: () => { this.loading.stats = false; }
    });
  }

  // ── Helpers ───────────────────────────────────────────────
  getInitiales(): string {
    if (!this.userName) return '?';
    return this.userName.split(' ')
      .map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  getInitialesEquipe(nom: string): string {
    if (!nom) return '?';
    return nom.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2);
  }

  // ── Navigation ────────────────────────────────────────────
  voirSortie(id: string): void { this.router.navigate(['/sorties', id]); }
  voirEquipe(id: string): void { this.router.navigate(['/equipes', id]); }
  creerSortie(): void          { this.router.navigate(['/sorties/create']); }
}