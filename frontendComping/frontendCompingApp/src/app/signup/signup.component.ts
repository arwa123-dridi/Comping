import { Component } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './signup.component.html',
  styleUrls: ['./signup.component.css']
})
export class SignupComponent {
  signupForm: FormGroup;
  successMessage: string = '';
  errorMessage: string = '';
  
  constructor(private fb: FormBuilder, private api: ApiService) {
    this.signupForm = this.fb.group({
      name: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      telephone: [''],
      address: [''],
      role: ['USER', Validators.required]
    });
  }

  onSubmit(): void {
    if (this.signupForm.valid) {
      const signupData = this.signupForm.value;
      console.log('Signup Data:', signupData);

      this.api.post('signup', signupData)
        .subscribe({
          next: () => {
            this.successMessage = 'Inscription réussie! Vous pouvez maintenant vous connecter.';
            this.signupForm.reset({ role: 'USER' });
          },
          error: (err: any) => {
            this.successMessage = '';
            this.errorMessage = err.message || 'Erreur inscription';
            console.error('Signup failed', err);
          }
        });
    } else {
      this.errorMessage = 'Veuillez corriger les erreurs du formulaire.';
    }
  }
}
