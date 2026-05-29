import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-chart-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="chart-card">
      <div class="chart-card-header">
        <div class="chart-card-title">
          <span class="chart-card-title-icon" aria-hidden="true">{{ icon }}</span>
          {{ title }}
        </div>
        <div class="chart-card-sub" *ngIf="subtitle">{{ subtitle }}</div>
      </div>
      <ng-content></ng-content>
    </div>
  `,
  styles: [`
    .chart-card{background:#fff;border:1px solid rgba(0,0,0,0.08);border-radius:14px;padding:16px;box-shadow:0 1px 4px rgba(0,0,0,0.05);}
    .chart-card-header{display:flex;flex-direction:column;gap:4px;margin-bottom:12px;}
    .chart-card-title{font-weight:800;display:flex;align-items:center;gap:8px;font-size:14px;color:#1a1a18;}
    .chart-card-title-icon{width:28px;height:28px;border-radius:10px;background:#E6F1FB;display:flex;align-items:center;justify-content:center;}
    .chart-card-sub{font-size:12px;color:#6b6b66;}
  `]
})
export class ChartCardComponent {
  @Input() title = '';
  @Input() subtitle = '';
  @Input() icon = '📊';
}

