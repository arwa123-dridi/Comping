import { Component, Input, OnChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortieResponse } from '../../../models/sortie.model';
import { ChartjsSurfaceComponent } from '../chartjs-surface/chartjs-surface.component';

@Component({
  selector: 'app-monthly-hikes-chart',
  standalone: true,
  imports: [CommonModule, ChartjsSurfaceComponent],
  template: `
    <div class="chart-block">
      <div class="chart-title">📆 Randonnées par mois</div>
      <app-chartjs-surface [chartType]="'bar'" [config]="chartConfig" [plugins]="plugins" />
    </div>
  `,
  styles: [`
    .chart-block { height: 240px; }
    .chart-title { font-weight: 800; font-size: 13px; margin-bottom: 8px; color: #1a1a18; }
  `]
})
export class MonthlyHikesChartComponent implements OnChanges {
  @Input() sorties: SortieResponse[] = [];

  plugins: any[] = [];
  chartConfig: any = {};

  ngOnChanges(): void {
    const map = new Map<string, number>();
    (this.sorties ?? []).forEach(s => {
      if (!s?.dateDebut) return;
      const d = new Date(s.dateDebut);
      const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
      map.set(key, (map.get(key) ?? 0) + 1);
    });

    const entries = Array.from(map.entries()).sort((a, b) => a[0].localeCompare(b[0])).slice(-6);
    const labels  = entries.map(([key]) => {
      const [y, m] = key.split('-');
      return new Date(Number(y), Number(m) - 1, 1).toLocaleDateString('fr-FR', { month: 'short', year: '2-digit' });
    });
    const values = entries.map(([, v]) => v);

    this.chartConfig = {
      data: {
        labels: labels.length ? labels : ['—'],
        datasets: [{
          label: 'Randonnées',
          data: values.length ? values : [0],
          backgroundColor: 'rgba(24,95,165,0.35)',
          borderColor: 'rgba(24,95,165,1)',
          borderWidth: 1,
          borderRadius: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          x: { grid: { display: false } },
          y: { beginAtZero: true, ticks: { precision: 0 } }
        },
        plugins: { legend: { display: false } }
      }
    };
  }
}
