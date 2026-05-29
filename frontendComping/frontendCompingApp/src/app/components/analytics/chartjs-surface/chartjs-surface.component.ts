import { AfterViewInit, Component, ElementRef, Input, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Chart, ChartConfiguration, ChartType } from 'chart.js/auto';

@Component({
  selector: 'app-chartjs-surface',
  standalone: true,
  imports: [CommonModule],
  template: `<canvas #canvas></canvas>`,
  styles: [`canvas{display:block;width:100%;height:240px;}`]
})
export class ChartjsSurfaceComponent implements AfterViewInit, OnChanges {
  @ViewChild('canvas', { static: true }) canvasRef!: ElementRef<HTMLCanvasElement>;

  @Input() chartType: ChartType = 'bar';
  @Input() config!: Omit<ChartConfiguration, 'type'>;
  @Input() plugins: any[] = [];

  private chart?: Chart;

  ngAfterViewInit(): void {
    this.render();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['config'] || changes['chartType'] || changes['plugins']) {
      this.render();
    }
  }

  private render(): void {
    if (!this.canvasRef?.nativeElement) return;
    if (!this.config) return;

    const canvas = this.canvasRef.nativeElement;

    if (this.chart) {
      this.chart.destroy();
      this.chart = undefined;
    }

    this.chart = new Chart(canvas, {
      ...(this.config as any),
      type: this.chartType,
      plugins: this.plugins,
    } as ChartConfiguration);
  }
}

