import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-star-rating',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './star-rating.component.html',
  styleUrls: ['./star-rating.component.css']
})
export class StarRatingComponent {
  @Input() value = 0;
  @Input() readOnly = false;
  @Output() rate = new EventEmitter<number>();

  hovered = 0;
  readonly stars = [1, 2, 3, 4, 5];

  select(n: number): void {
    if (this.readOnly) return;
    this.value = n;
    this.rate.emit(n);
  }
}
