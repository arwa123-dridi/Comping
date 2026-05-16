import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivityService } from '../../services/activity.service';
import { Activity } from '../../models/activity.model';
import { Router } from '@angular/router';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-list-activity',
  standalone: true,
  imports: [CommonModule,FormsModule],
  templateUrl: './list-activity.component.html',
  styleUrls: ['./list-activity.component.css']
})
export class ListActivityComponent implements OnInit {

  activities: Activity[] = [];
searchText: string = '';
selectedType: string = '';
selectedDifficulty: string = '';
uniqueTypes: string[] = [];
filteredActivities: Activity[] = [];
 userRole: string = '';
  constructor(private activityService: ActivityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.activityService.getAllActivities().subscribe({
      next: (data) => {
        this.activities = data;
        this.uniqueTypes = [...new Set(this.activities.map(a => a.type))];
        this.filteredActivities = data;
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
goToEvents() {
  this.router.navigate(['/events/list']);
}
applyFilters(): void {
  this.filteredActivities = this.activities.filter(act => {

    const matchName =
      act.nom.toLowerCase().includes(this.searchText.toLowerCase());

    const matchType =
      this.selectedType ? act.type === this.selectedType : true;

    const matchDifficulty =
      this.selectedDifficulty
        ? act.niveauDifficulte === this.selectedDifficulty
        : true;

    return matchName && matchType && matchDifficulty;
  });
}
}