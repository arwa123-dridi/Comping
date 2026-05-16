import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators} from '@angular/forms';
import { ActivityService } from '../../services/activity.service';
import { BrowserModule } from '@angular/platform-browser';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

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
showPopup = false;
  constructor(
    private fb: FormBuilder,
    private activityService: ActivityService,
     private router: Router 
  ) {}

  ngOnInit(): void {
    this.activityForm = this.fb.group({
      nom: ['', Validators.required],
      description: ['', Validators.required],
      type: ['', Validators.required],
      duree: ['', Validators.required],
      capacite: ['', Validators.required],
        niveauDifficulte: ['', Validators.required],
    trancheAge: ['', Validators.required],
    saison: ['', Validators.required],
    tags: [''] // string à transformer en string[]
    });
  }

  onSubmit(): void {
        const formValue = this.activityForm.value;

    const activity = {
      ...formValue,
      tags: formValue.tags
        ? formValue.tags.split(',').map((t: string) => t.trim())
        : []
    };
    if (this.activityForm.valid) {
      this.activityService.createActivity(activity)
        .subscribe({
          next: (res) => {
            this.successMessage = 'Activité créée avec succès';
              this.showPopup = true;
            this.activityForm.reset();
             setTimeout(() => {
            this.showPopup = false;
             this.router.navigate(['/activities/list']);
          }, 2000);
          },
          error: (err) => {
            console.error(err);
          }
        });
    }
  }



}
