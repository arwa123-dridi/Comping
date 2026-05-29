import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';

import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

import { ParticipationPieChartComponent } from '../../components/analytics/participation-pie-chart/participation-pie-chart.component';
import { StatusHikesChartComponent }      from '../../components/analytics/status-hikes-chart/status-hikes-chart.component';
import { MonthlyHikesChartComponent }     from '../../components/analytics/monthly-hikes-chart/monthly-hikes-chart.component';
import { DifficultyPieChartComponent } from '../../components/analytics/difficlty-pie-chart/difficulty-pie-chart.component';

@Component({
  selector: 'app-admin-statistics',
  standalone: true,
  imports: [
    CommonModule, FormsModule, RouterModule,
    DifficultyPieChartComponent,
    ParticipationPieChartComponent,
    StatusHikesChartComponent,
    MonthlyHikesChartComponent
  ],
  templateUrl: './admin-statistics.component.html',
  styleUrls: ['./admin-statistics.component.css']
})
export class AdminStatisticsComponent implements OnInit {
  loading = false;
  sorties: SortieResponse[] = [];
  equipes: EquipeResponse[] = [];

  // KPI
  totalSorties = 0;      sortiesAVenir = 0;    sortiesPassees = 0;
  totalEquipes = 0;      equipesDispo = 0;      equipesCompletes = 0;
  totalParticipants = 0; moyParticipants = 0;
  tauxRemplissage = 0;   totalPlaces = 0;
  topSorties: { titre: string; nb: number }[] = [];

  // Table
  filteredSorties: SortieResponse[] = [];
  searchTerm = '';
  diffFilter = '';

  // Toast
  toastMsg = ''; toastErr = false;

  constructor(private sortieService: SortieService, private equipeService: EquipeService) {}

  ngOnInit(): void { this.loadAll(); }

  loadAll(): void {
    this.loading = true;
    let done = 0;
    const check = () => { if (++done === 2) { this.loading = false; this.compute(); } };
    this.sortieService.getAllSorties().subscribe({
      next: d => { this.sorties = d || []; check(); },
      error: ()  => { this.sorties = [];   check(); }
    });
    this.equipeService.getAllEquipes().subscribe({
      next: d => { this.equipes = d || []; check(); },
      error: ()  => { this.equipes = [];   check(); }
    });
  }

  compute(): void {
    const now = new Date();
    this.totalSorties      = this.sorties.length;
    this.sortiesAVenir     = this.sorties.filter(s => new Date(s.dateDebut) > now).length;
    this.sortiesPassees    = this.sorties.filter(s => new Date(s.dateDebut) <= now).length;
    this.totalEquipes      = this.equipes.length;
    this.equipesDispo      = this.equipes.filter(e => (e.membres?.length ?? 0) < (e.capaciteMax ?? 0)).length;
    this.equipesCompletes  = this.equipes.filter(e => (e.membres?.length ?? 0) >= (e.capaciteMax ?? 0)).length;
    this.totalParticipants = this.sorties.reduce((acc, s) => acc + this.getNb(s), 0);
    this.moyParticipants   = this.totalSorties ? Math.round(this.totalParticipants / this.totalSorties) : 0;
    this.totalPlaces       = this.sorties.reduce((acc, s) => acc + (s.capaciteMax ?? 0), 0);
    const tauxList         = this.sorties.filter(s => s.capaciteMax).map(s => (this.getNb(s) / s.capaciteMax) * 100);
    this.tauxRemplissage   = tauxList.length ? Math.round(tauxList.reduce((a, b) => a + b, 0) / tauxList.length) : 0;
    this.topSorties        = [...this.sorties]
      .sort((a, b) => this.getNb(b) - this.getNb(a))
      .slice(0, 5)
      .map(s => ({ titre: s.titre, nb: this.getNb(s) }));
    this.filteredSorties   = [...this.sorties];
  }

  applyFilter(): void {
    this.filteredSorties = this.sorties.filter(s => {
      const matchSearch = !this.searchTerm || s.titre?.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchDiff   = !this.diffFilter  || s.difficulte === this.diffFilter;
      return matchSearch && matchDiff;
    });
  }

  // ✅ dateDebut est de type Date dans SortieResponse — on accepte Date | string
  getNb(s: SortieResponse): number { return s.participantIds?.length ?? s.nombreParticipants ?? 0; }
  getTaux(s: SortieResponse): number { return s.capaciteMax ? Math.round((this.getNb(s) / s.capaciteMax) * 100) : 0; }
  formatDate(d: Date | string): string { return d ? new Date(d).toLocaleDateString('fr-FR') : '—'; }

  exportCSV(): void {
    const rows = [
      ['Titre', 'Difficulté', 'Date', 'Lieu', 'Participants', 'Capacité', 'Taux'],
      ...this.sorties.map(s => [
        s.titre, s.difficulte, this.formatDate(s.dateDebut),
        s.lieuDepart ?? '', this.getNb(s), s.capaciteMax ?? '', this.getTaux(s) + '%'
      ])
    ];
    const csv = rows.map(r => r.join(',')).join('\n');
    const a = document.createElement('a');
    a.href = 'data:text/csv;charset=utf-8,' + encodeURIComponent(csv);
    a.download = 'stats-randonnees.csv';
    a.click();
    this.showToast('Export réussi ✓');
  }

  showToast(msg: string, err = false): void {
    this.toastMsg = msg; this.toastErr = err;
    setTimeout(() => this.toastMsg = '', 3000);
  }
}
