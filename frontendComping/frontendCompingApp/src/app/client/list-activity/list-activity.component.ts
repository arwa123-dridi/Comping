import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivityService } from '../../services/activity.service';
import { Activity } from '../../models/activity.model';
import { Router } from '@angular/router';

@Component({
  selector: 'app-list-activity',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './list-activity.component.html',
  styleUrls: ['./list-activity.component.css']
})
export class ListActivityComponent implements OnInit {

  activities: Activity[] = [];

  constructor(private activityService: ActivityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.activityService.getAllActivities().subscribe({
      next: (data) => {
        this.activities = data;
      },
      error: (err) => {
        console.error('Erreur chargement activités', err);
      }
    });
  }
  onCreateActivity() {
  this.router.navigate(['/activities/add']);
}
editActivity(activity: any) {
  console.log("Edit activity", activity);
    this.router.navigate(['/activities/edit', activity.idActivity]);
}

deleteActivity(id: string) {
  console.log("Delete activity", id);
  this.activityService.deleteActivity(id).subscribe({
    next: () => {
      // mise à jour locale après suppression
      this.activities = this.activities.filter(a => a.idActivity !== id);
      console.log('Activity supprimée avec succès');
    },
    error: (err) => {
      console.error('Erreur suppression activité', err);
    }
  });
}
}