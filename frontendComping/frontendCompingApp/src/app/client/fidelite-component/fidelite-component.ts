import { Component, OnInit } from '@angular/core';
import { CarteFedeliteEvent} from '../../services/carte-fedelite-event';
import { CommonModule } from '@angular/common';

@Component({
  standalone: true,
  imports: [CommonModule],
  selector: 'app-fidelite-component',
  templateUrl: './fidelite-component.html',
  styleUrl: './fidelite-component.css',
})
export class FideliteComponent implements OnInit {

  message: string = '';
  userId: string = '';

  constructor(private carteService: CarteFedeliteEvent) {}

  ngOnInit() {
    this.carteService.getMessage(this.userId)
      .subscribe((res: any) => {
        this.message = res;
      });
  }
}