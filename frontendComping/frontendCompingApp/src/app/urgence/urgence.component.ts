import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { UrgenceService } from '../services/urgence.service';

@Component({
  selector: 'app-urgence',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './urgence.component.html',
  styleUrl: './urgence.component.css'
})
export class UrgenceComponent {
  readonly types = ['INCENDIE', 'MEDICALE', 'SECURITE', 'TECHNIQUE', 'AUTRE'];
  loading = false;
  successMessage = '';
  errorMessage = '';

  alerteForm;

  constructor(private fb: FormBuilder, private urgenceService: UrgenceService) {
    this.alerteForm = this.fb.group({
      siteCampingId: ['', Validators.required],
      type: ['MEDICALE', Validators.required],
      titre: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      position: ['', Validators.required]
    });
  }

  submitAlerte(): void {
    if (this.alerteForm.invalid) {
      this.alerteForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    this.successMessage = '';
    this.errorMessage = '';

    this.urgenceService.declencherAlerte(this.alerteForm.getRawValue() as any).subscribe({
      next: () => {
        this.loading = false;
        this.successMessage = 'Alerte envoyée avec succès. Les équipes vont la traiter.';
        this.alerteForm.patchValue({ type: 'MEDICALE' });
        this.alerteForm.reset({ type: 'MEDICALE' });
      },
      error: () => {
        this.loading = false;
        this.errorMessage = 'Impossible d\'envoyer l\'alerte pour le moment.';
      }
    });
  }
}
