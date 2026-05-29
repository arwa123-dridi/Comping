import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortieResponse } from '../../../models/sortie.model';
import { ChartjsSurfaceComponent } from '../chartjs-surface/chartjs-surface.component';

@Component({
  selector: 'app-status-hikes-chart',
  standalone: true,
  imports: [CommonModule, ChartjsSurfaceComponent],
  template: `
    <div class="chart-block">
      <div class="chart-title">✅ Statut des randonnées</div>
      <div class="chart-sub">Terminées vs Actives (calcul date)</div>
      <app-chartjs-surface [chartType]="'doughnut'" [config]="chartConfig" [plugins]="plugins" />
    </div>
  `,
  styles: [`
    .chart-block { height: 240px; }
    .chart-title { font-weight: 800; font-size: 13px; margin-bottom: 2px; color: #1a1a18; }
    .chart-sub { font-size: 12px; color: #6b6b66; margin-bottom: 8px; }
  `]
})
export class StatusHikesChartComponent implements OnChanges {
  @Input() sorties: SortieResponse[] = [];

  plugins: any[] = [];
  chartConfig: any = {};

  ngOnChanges(_: SimpleChanges): void {
    const today = new Date();
    const active = this.sorties.filter(s => {
      const debut = new Date(s.dateDebut);
      const fin   = s.dateFin ? new Date(s.dateFin) : null;
      return debut > today || (debut <= today && (!fin || fin >= today));
    }).length;
    const done = this.sorties.length - active;

    this.chartConfig = {
      data: {
        labels: ['Actives', 'Terminées'],
        datasets: [{
          data: [active, done],
          backgroundColor: ['rgba(24,95,165,0.35)', 'rgba(95,94,90,0.35)'],
          borderColor:     ['rgba(24,95,165,1)',    'rgba(95,94,90,1)'],
          borderWidth: 1
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
