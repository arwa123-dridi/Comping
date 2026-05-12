import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-campinosocialhome',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './campinosocialhome.component.html',
  styleUrls: ['./campinosocialhome.component.css']
})
export class HomeComponent {
  constructor() { }
}