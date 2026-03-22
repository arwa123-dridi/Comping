import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ApiService } from '../services/api.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {
  
  profileForm: FormGroup;
  passwordForm: FormGroup;
  
  successMessage: string = '';
  errorMessage: string = '';
  
  userId: string = '';
  userEmail: string = '';
  userPhoto: string = 'assets/default-avatar.png';
  
  isLoading: boolean = false;

  constructor(
    private fb: FormBuilder,
    private api: ApiService,
    private router: Router
  ) {
    // Formulaire d'informations personnelles
    this.profileForm = this.fb.group({
      name: ['', [Validators.minLength(3)]],
      email: ['', [Validators.email]],
      telephone: [''],
      address: ['']
    });

    // Formulaire de changement de mot de passe
    this.passwordForm = this.fb.group({
      oldPassword: ['', [Validators.required, Validators.minLength(6)]],
      newPassword: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.loadUserFromToken();
  }

  passwordMatchValidator(g: FormGroup) {
    const newPassword = g.get('newPassword')?.value;
    const confirmPassword = g.get('confirmPassword')?.value;
    return newPassword === confirmPassword ? null : { mismatch: true };
  }

  loadUserFromToken(): void {
    const token = localStorage.getItem('token');
    if (!token) {
      this.router.navigate(['/login']);
      return;
    }

    try {
      // Décoder le token JWT
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.userEmail = payload.sub;
      
      // Récupérer l'utilisateur par email
      this.api.get(`users/by-email/${this.userEmail}`).subscribe({
        next: (user) => {
          this.userId = user.id;
          this.loadUserProfile();
        },
        error: (err) => {
          console.error('Erreur récupération utilisateur', err);
          this.router.navigate(['/login']);
        }
      });
    } catch (e) {
      console.error('Token invalide', e);
      this.router.navigate(['/login']);
    }
  }

  loadUserProfile(): void {
    this.isLoading = true;
    
    this.api.get(`users/${this.userId}`).subscribe({
      next: (user) => {
        this.profileForm.patchValue({
          name: user.name,
          email: user.email,
          telephone: user.telephone || '',
          address: user.address || ''
        });
        
        this.userPhoto = user.photo || 'assets/default-avatar.png';
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement du profil: ' + err.message;
        this.isLoading = false;
      }
    });
  }

  onSubmitProfile(): void {
    if (this.profileForm.invalid) {
      this.errorMessage = 'Veuillez corriger les erreurs';
      return;
    }

    if (!this.userId) {
      this.errorMessage = 'Utilisateur non identifié. Veuillez vous reconnecter.';
      return;
    }

    this.isLoading = true;
    this.api.put(`users/${this.userId}/profile`, this.profileForm.value)
      .subscribe({
        next: (user: any) => {
          this.successMessage = 'Profil mis à jour avec succès!';
          this.errorMessage = '';
          this.isLoading = false;
          if (user.email) this.userEmail = user.email;
        },
        error: (err) => {
          this.errorMessage = 'Erreur mise à jour profil: ' + err.message;
          this.successMessage = '';
          this.isLoading = false;
        }
      });
  }

  onSubmitPassword(): void {
    if (this.passwordForm.invalid) {
      this.errorMessage = 'Veuillez remplir tous les champs';
      return;
    }

    if (!this.userId) {
      this.errorMessage = 'Utilisateur non identifié. Veuillez vous reconnecter.';
      return;
    }

    this.isLoading = true;
    this.api.put(`users/${this.userId}/password`, this.passwordForm.value)
      .subscribe({
        next: (response: any) => {
          this.successMessage = typeof response === 'string' ? response : 'Mot de passe modifié';
          this.passwordForm.reset();
          this.errorMessage = '';
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Erreur changement mot de passe: ' + err.message;
          this.successMessage = '';
          this.isLoading = false;
        }
      });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      this.errorMessage = 'Sélectionnez une image';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.errorMessage = 'Image trop grande (max 5MB)';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      const base64 = reader.result as string;
      this.updatePhoto(base64);
    };
    reader.readAsDataURL(file);
  }

  updatePhoto(photoBase64: string): void {
    if (!this.userId) {
      this.errorMessage = 'Utilisateur non identifié. Veuillez vous reconnecter.';
      return;
    }
    
    this.isLoading = true;
    
    const body = { 
      photo: photoBase64 
    };
    
    this.api.put(`users/${this.userId}/photo`, body)
      .subscribe({
        next: (response: any) => {
          this.successMessage = 'Photo mise à jour avec succès';
          this.userPhoto = photoBase64;
          this.errorMessage = '';
          this.isLoading = false;
        },
        error: (err) => {
          this.errorMessage = 'Erreur photo: ' + err.message;
          this.successMessage = '';
          this.isLoading = false;
        }
      });
  }

  logout(): void {
    localStorage.removeItem('token');
    this.router.navigate(['/login']);
  }
}