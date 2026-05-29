import { Component, Input, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-chart-host',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="chart-host">
      <canvas [id]="canvasId" width="400" height="220"></canvas>
    </div>
  `,
  styles: [`.chart-host{position:relative;height:240px;} canvas{display:block;}`]
})
export class ChartHostComponent implements OnChanges {
  @Input() canvasId = 'chart';
  ngOnChanges(_: SimpleChanges): void {
    // no-op: charts created by parent using @ViewChild in future refactor
  }
}

