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
 event = {
    titre: '',
    description: '',
    prix: 0,
    capacite: 0,
    statut: 'VALIDE',
    activityIds: [] as string[]
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
    this.eventService.createEvent(this.event).subscribe({
      next: () => {
        alert('Événement ajouté avec succès ✅');
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
