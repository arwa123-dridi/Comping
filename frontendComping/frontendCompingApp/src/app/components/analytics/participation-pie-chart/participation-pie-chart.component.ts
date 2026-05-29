import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortieResponse } from '../../../models/sortie.model';
import { ChartjsSurfaceComponent } from '../chartjs-surface/chartjs-surface.component';

@Component({
  selector: 'app-participation-pie-chart',
  standalone: true,
  imports: [CommonModule, ChartjsSurfaceComponent],
  template: `
    <div class="chart-block">
      <div class="chart-title">👥 Participation (répartition)</div>
      <div class="chart-sub">Basé sur le ratio inscrits / capacité</div>
      <app-chartjs-surface [chartType]="'pie'" [config]="chartConfig" [plugins]="plugins" />
    </div>
  `,
  styles: [`
    .chart-block { height: 240px; }
    .chart-title { font-weight: 800; font-size: 13px; margin-bottom: 2px; color: #1a1a18; }
    .chart-sub { font-size: 12px; color: #6b6b66; margin-bottom: 8px; }
  `]
})
export class ParticipationPieChartComponent implements OnChanges {
  @Input() sorties: SortieResponse[] = [];

  plugins: any[] = [];
  chartConfig: any = {};

  ngOnChanges(_: SimpleChanges): void {
    const total    = this.sorties.reduce((sum, s) => sum + (s.nombreParticipants ?? 0), 0);
    const totalCap = this.sorties.reduce((sum, s) => sum + (s.capaciteMax ?? 0), 0);
    const available = Math.max(0, totalCap - total);

    this.chartConfig = {
      data: {
        labels: ['Participants', 'Places restantes'],
        datasets: [{
          data: [total, available],
          backgroundColor: ['rgba(29,158,117,0.85)', 'rgba(24,95,165,0.25)'],
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
