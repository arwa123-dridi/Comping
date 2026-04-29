import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { ActivityService } from '../../services/activity.service';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-activity-create',
  imports: [ CommonModule,
    ReactiveFormsModule],
  templateUrl: './activity-create.component.html',
  styleUrl: './activity-create.component.css'
})
export class ActivityCreateComponent {

  activityForm!: FormGroup;
  successMessage = '';

  constructor(
    private fb: FormBuilder,
    private activityService: ActivityService
  ) {}

  ngOnInit(): void {
    this.activityForm = this.fb.group({
      nom: ['', Validators.required],
      description: ['', Validators.required],
      type: ['', Validators.required],
      duree: ['', Validators.required],
      capacite: ['', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.activityForm.valid) {
      this.activityService.createActivity(this.activityForm.value)
        .subscribe({
          next: (res) => {
            this.successMessage = 'Activité créée avec succès';
            this.activityForm.reset();
          },
          error: (err) => {
            console.error(err);
          }
        });
    }
  }



}
