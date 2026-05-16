import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivityService } from '../../services/activity.service';
import { Activity } from '../../models/activity.model';

@Component({
  selector: 'app-activity',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './activity.html',
  styleUrls: ['./activity.css'],
})
export class ActivityComponent implements OnInit {

  activities: Activity[] = [];
  filteredActivities: Activity[] = [];

  filterNom: string = '';
  filterType: string = '';
  filterSaison: string = '';

  showDeletePopup = false;
  activityToDeleteId?: string;

  constructor(private activityService: ActivityService) {}

  ngOnInit(): void {
    this.loadActivities();
  }

  loadActivities() {
    this.activityService.getAllActivities().subscribe((data: Activity[]) => {
      this.activities = data;
      this.filteredActivities = data;
    });
  }

  applyFilters() {
  const nom = this.filterNom?.toLowerCase().trim() || '';
  const type = this.filterType?.toLowerCase() || '';
  const saison = this.filterSaison?.toLowerCase() || '';

  this.filteredActivities = this.activities.filter(a => {
    return (
      (!nom || (a.nom ?? '').toLowerCase().includes(nom)) &&
      (!type || (a.type ?? '').toLowerCase() === type) &&
      (!saison || (a.saison ?? '').toLowerCase() === saison)
    );
  });
  }

  openDeletePopup(id?: string) {
    this.activityToDeleteId = id;
    this.showDeletePopup = true;
  }

  closePopup() {
    this.showDeletePopup = false;
  }

  confirmDelete() {
    if (!this.activityToDeleteId) return;

    this.activityService.deleteActivity(this.activityToDeleteId).subscribe(() => {
      this.loadActivities();
      this.closePopup();
    });
  }
  resetFilters() {
  this.filterNom = '';
  this.filterType = '';
  this.filterSaison = '';
  this.applyFilters();
}
}