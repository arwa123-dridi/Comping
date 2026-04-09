import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-event',
  imports: [CommonModule, FormsModule],
  templateUrl: './add-event.component.html',
  styleUrl: './add-event.component.css'
})
export class AddEventComponent {
 event = {
    titre: '',
    description: '',
    prix: 0,
    capacite: 0,
    statut: 'VALIDE'
  };

  constructor(
    private eventService: EventService,
    private router: Router
  ) {}

  onSubmit(): void {
    this.eventService.createEvent(this.event).subscribe({
      next: () => {
        alert('Événement ajouté avec succès ✅');
      },
      error: (err) => console.error(err)
    });
  }
}
