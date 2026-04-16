import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivityService } from '../../services/activity.service';
import { Activity } from '../../models/activity.model';

@Component({
  selector: 'app-edit-activity',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './edit-activity.component.html',
  styleUrls: ['./edit-activity.component.css']
})
export class EditActivityComponent implements OnInit {

  id!: string;

  activity: Activity = {
  idActivity: '',
  nom: '',
  description: '',
  type: '',
  duree: '0',
  capacite: '0'
  };

  loading = true;

  constructor(
    private route: ActivatedRoute,
    private activityService: ActivityService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.id = this.route.snapshot.paramMap.get('id')!;

    this.activityService.getActivityById(this.id).subscribe({
      next: (data) => {
        this.activity = data;
        this.loading = false;
      },
      error: (err) => {
        console.error('Erreur chargement activité', err);
        this.loading = false;
      }
    });
  }

  updateActivity(): void {
    this.activityService.updateActivity(this.id, this.activity).subscribe({
      next: () => {
        alert('Activité modifiée avec succès');
        this.router.navigate(['/activities']);
      },
      error: (err) => {
        console.error('Erreur update', err);
      }
    });
  }

  cancel(): void {
    this.router.navigate(['/activities']);
  }
}