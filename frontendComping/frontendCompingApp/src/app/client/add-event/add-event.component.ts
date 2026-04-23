import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { EventService } from '../../services/event.service';
import { ActivityService } from '../../services/activity.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-add-event',
  imports: [CommonModule, FormsModule],
  templateUrl: './add-event.component.html',
  styleUrl: './add-event.component.css'
})
export class AddEventComponent {
showSuccess = false;
  tagsInput: string = '';
  event = {
    titre: '',
    description: '',
    prix: 0,
    capacite: 0,

    dateDebut: '',
    dateFin: '',

    statut: 'VALIDE',
    lieu: '',

    organisateurId: '',
    participantIds: [] as string[],

    imageUrl: '',
    categorie: '',

    createdAt: '',

    activityIds: [] as string[],

    // Attributs IA
    tags: [] as string[],
    niveauDifficulte: '',
    trancheAge: '',

    latitude: 0,
    longitude: 0,

    saison: '',
    dureeEnHeures: 0
  };

activities: any[] = [];
selectedActivities: string[] = [];
  constructor(
    private eventService: EventService,
     private activityService: ActivityService,
    private router: Router
  ) {}
ngOnInit(): void {
  this.loadActivities();
}

loadActivities(): void {
  this.activityService.getAllActivities().subscribe({
    next: (data) => {
      console.log("ACTIVITIES FROM API =>", data);
      this.activities = data;
    },
    error: (err) => console.error(err)
  });
}
  onSubmit(): void {
     // 🔥 conversion tagsInput -> tags[]
  this.event.tags = this.tagsInput
    .split(',')
    .map(tag => tag.trim())
    .filter(tag => tag !== '');

  // 🔥 date création automatique
  this.event.createdAt = new Date().toISOString();
  // 🔥 debug important
  console.log("EVENT SENT =>", this.event);
    this.eventService.createEvent(this.event).subscribe({
    
        next: () => {
  this.showSuccess = true;

  setTimeout(() => {
    this.showSuccess = false;
  }, 2000);
},
      
      error: (err) => console.error(err)
    });
  }
 onActivityChange(event: any): void {
  const id = event.target.value; // string

  if (event.target.checked) {
    if (!this.selectedActivities.includes(id)) {
      this.selectedActivities.push(id);
    }
  } else {
    this.selectedActivities = this.selectedActivities.filter(x => x !== id);
  }

  this.event.activityIds = [...this.selectedActivities];
}

}
