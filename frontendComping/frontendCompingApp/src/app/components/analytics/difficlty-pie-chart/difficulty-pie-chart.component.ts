import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ChartjsSurfaceComponent } from '../chartjs-surface/chartjs-surface.component';
import { SortieResponse } from '../../../models/sortie.model';

@Component({
  selector: 'app-difficulty-pie-chart',
  standalone: true,
  imports: [CommonModule, ChartjsSurfaceComponent],
  template: `
    <div class="chart-block">
      <div class="chart-title">🎯 Répartition par difficulté</div>
      <div class="chart-sub">Facile / Moyen / Difficile</div>
      <app-chartjs-surface [chartType]="'pie'" [config]="chartConfig" />
    </div>
  `,
  styles: [`
    .chart-block { height: 240px; }
    .chart-title { font-weight: 800; font-size: 13px; margin-bottom: 2px; color: #1a1a18; }
    .chart-sub { font-size: 12px; color: #6b6b66; margin-bottom: 8px; }
  `]
})
export class DifficultyPieChartComponent implements OnChanges {
  @Input() sorties: SortieResponse[] = [];
  chartConfig: any = {};

  ngOnChanges(): void {
    const facile    = this.sorties.filter(s => s.difficulte === 'FACILE').length;
    const moyen     = this.sorties.filter(s => s.difficulte === 'MOYEN').length;
    const difficile = this.sorties.filter(s => s.difficulte === 'DIFFICILE').length;

    this.chartConfig = {
      data: {
        labels: ['Facile', 'Moyen', 'Difficile'],
        datasets: [{
          data: [facile, moyen, difficile],
          backgroundColor: ['#2e7d32', '#ff7043', '#d32f2f'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } }
      }
    };
  }
}
