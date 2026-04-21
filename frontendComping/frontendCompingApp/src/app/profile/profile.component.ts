import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HttpClientModule], // ✅ Bug 3 fix
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
    private http: HttpClient,
    private router: Router
  ) {
    this.profileForm = this.fb.group({
      firstName: ['', [Validators.minLength(2), Validators.maxLength(50)]],
      lastName:  ['', [Validators.minLength(2), Validators.maxLength(50)]],
      email:     ['', [Validators.email]],
      telephone: ['', [Validators.minLength(8), Validators.maxLength(15)]],
      address:   ['']
    });

    this.passwordForm = this.fb.group({
      oldPassword:     ['', [Validators.required, Validators.minLength(6)]],
      newPassword:     ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required, Validators.minLength(6)]]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    this.loadUserFromToken();
  }

  passwordMatchValidator(g: FormGroup) {
    const newPwd = g.get('newPassword')?.value;
    const confirm = g.get('confirmPassword')?.value;
    return newPwd === confirm ? null : { mismatch: true };
  }

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  loadUserFromToken(): void {
    const token = localStorage.getItem('authToken');
    if (!token) {
      this.router.navigate(['/login']); // ✅ Bug 5 fix
      return;
    }

    try {
      const payload = JSON.parse(atob(token.split('.')[1]));
      this.userEmail = payload.sub || payload.email;

      this.http.get<any>(
        `http://localhost:8087/api/users/by-email/${this.userEmail}`,
        { headers: this.getHeaders() }
      ).subscribe({
        next: (user) => {
          this.userId = user.id;
          this.loadUserProfile();
        },
        error: (err) => {
          console.error('Erreur récupération utilisateur', err);
          this.router.navigate(['/login']); // ✅ Bug 5 fix
        }
      });
    } catch (e) {
      console.error('Token invalide', e);
      this.router.navigate(['/login']); // ✅ Bug 5 fix
    }
  }

  loadUserProfile(): void {
    this.isLoading = true;

    this.http.get<any>(
      `http://localhost:8087/api/users/${this.userId}`,
      { headers: this.getHeaders() }
    ).subscribe({
      next: (user) => {
        this.profileForm.patchValue({
          firstName: user.firstName || user.FirstName || '',  
          lastName:  user.lastName  || user.LastName  || '',
          email:     user.email,
          telephone: user.telephone,
          address:   user.address
        });
        this.userPhoto = user.photo || 'assets/default-avatar.png';
        this.isLoading = false;
      },
      error: (err) => {
        this.errorMessage = 'Erreur lors du chargement du profil';
        this.isLoading = false;
        console.error(err);
      }
    });
  }

  onSubmitProfile(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      this.errorMessage = 'Veuillez corriger les erreurs';
      return;
    }

    if (!this.userId) {
      this.errorMessage = 'Utilisateur non identifié. Veuillez vous reconnecter.';
      return;
    }

    this.isLoading = true;

    // ✅ Bug 1 fix — envoie firstName/lastName au lieu de name
    const body = {
      firstName: this.profileForm.get('firstName')?.value,
      lastName:  this.profileForm.get('lastName')?.value,
      email:     this.profileForm.get('email')?.value,
      telephone: this.profileForm.get('telephone')?.value,
      address:   this.profileForm.get('address')?.value
    };

    this.http.put(
      `http://localhost:8087/api/users/${this.userId}/profile`,
      body,
      { headers: this.getHeaders() }
    ).subscribe({
      next: (user: any) => {
        this.successMessage = 'Profil mis à jour avec succès!';
        this.errorMessage = '';
        this.isLoading = false;
        if (user.email) this.userEmail = user.email;
      },
      error: (err: any) => {
        this.errorMessage = err.error?.message || `Erreur (${err.status})`;
        this.successMessage = '';
        this.isLoading = false;
      }
    });
  }

  onSubmitPassword(): void {
    if (this.passwordForm.invalid) {
      this.passwordForm.markAllAsTouched();
      this.errorMessage = 'Veuillez remplir tous les champs correctement';
      return;
    }

    if (!this.userId) {
      this.errorMessage = 'Utilisateur non identifié. Veuillez vous reconnecter.';
      return;
    }

    this.isLoading = true;

    this.http.put(
      `http://localhost:8087/api/users/${this.userId}/password`,
      this.passwordForm.value,
      { headers: this.getHeaders(), responseType: 'text' } 
    ).subscribe({
      next: (response: string) => {
        this.successMessage = response || 'Mot de passe modifié avec succès';
        this.passwordForm.reset();
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = err.error || `Erreur (${err.status})`;
        this.successMessage = '';
        this.isLoading = false;
      }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) return;

    if (!file.type.startsWith('image/')) {
      this.errorMessage = 'Sélectionnez une image valide';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      this.errorMessage = 'Image trop grande (max 5MB)';
      return;
    }

    const reader = new FileReader();
    reader.onload = () => {
      this.updatePhoto(reader.result as string);
    };
    reader.readAsDataURL(file);
  }

  updatePhoto(photoBase64: string): void {
    if (!this.userId) {
      this.errorMessage = 'Utilisateur non identifié. Veuillez vous reconnecter.';
      return;
    }

    this.isLoading = true;

    this.http.put(
      `http://localhost:8087/api/users/${this.userId}/photo`,
      { photo: photoBase64 },
      { headers: this.getHeaders(), responseType: 'text' }  // ✅ Bug 2 fix
    ).subscribe({
      next: (response: string) => {
        this.successMessage = response || 'Photo mise à jour avec succès';
        this.userPhoto = photoBase64;
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: (err: any) => {
        this.errorMessage = err.error || `Erreur (${err.status})`;
        this.successMessage = '';
        this.isLoading = false;
      }
    });
  }

  logout(): void {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userNom');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    this.router.navigate(['/login']); // ✅ Bug 5 fix
  }
}