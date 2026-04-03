import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';

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
    private http: HttpClient,
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

  //  Méthode utilitaire pour les headers avec token
  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('authToken');
    return new HttpHeaders({
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    });
  }

  // ✅ CORRECTION 1: Utiliser 'authToken' au lieu de 'token'
  loadUserFromToken(): void {
    const token = localStorage.getItem('authToken');  // ← CHANGÉ: token → authToken
    if (!token) {
      this.router.navigate(['/signup']);  // ← CHANGÉ: /login → /signup
      return;
    }

    try {
      // Décoder le token JWT
      const payload = JSON.parse(atob(token.split('.')[1]));
      // ✅ Adapte selon ton backend (peut être payload.sub, payload.email, etc.)
      this.userEmail = payload.sub || payload.email;
      
      // Récupérer l'utilisateur par email AVEC headers
      this.http.get<any>(`http://localhost:8087/api/users/by-email/${this.userEmail}`, {
        headers: this.getHeaders()
      }).subscribe({
        next: (user) => {
          this.userId = user.id;
          this.loadUserProfile();
        },
        error: (err) => {
          console.error('Erreur récupération utilisateur', err);
          this.router.navigate(['/signup']);  // ← CHANGÉ: /login → /signup
        }
      });
    } catch (e) {
      console.error('Token invalide', e);
      this.router.navigate(['/signup']);  // ← CHANGÉ: /login → /signup
    }
  }

  // ✅ CORRECTION 2: Ajouter headers dans loadUserProfile
  loadUserProfile(): void {
    this.isLoading = true;
    
    this.http.get<any>(`http://localhost:8087/api/users/${this.userId}`, {
      headers: this.getHeaders()
    }).subscribe({
      next: (user) => {
        console.log('Utilisateur reçu:', user);
        
        this.profileForm.patchValue({
          name: user.name,
          email: user.email,
          telephone: user.telephone,
          address: user.address
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

  // ✅ CORRECTION 3: Ajouter headers dans onSubmitProfile
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
    this.http.put(`http://localhost:8087/api/users/${this.userId}/profile`, this.profileForm.value, {
      headers: this.getHeaders()
    }).subscribe({
      next: (user: any) => {
        this.successMessage = 'Profil mis à jour avec succès!';
        this.errorMessage = '';
        this.isLoading = false;
        if (user.email) this.userEmail = user.email;
        
        // ✅ Mettre à jour localStorage si le nom a changé
        if (user.name) {
          localStorage.setItem('userNom', user.name);
        }
        if (user.email) {
          localStorage.setItem('userEmail', user.email);
        }
      },
      error: (err: any) => {
        console.error('Erreur mise à jour profil:', err);
        
        let errorMsg = 'Erreur de mise à jour du profil';
        if (err.status === 0) {
          errorMsg = 'Serveur indisponible. Vérifiez que le backend est démarré.';
        } else if (err.status === 401) {
          errorMsg = 'Session expirée. Veuillez vous reconnecter.';
          this.router.navigate(['/signup']);
        } else if (err.status) {
          errorMsg += ` (Status: ${err.status})`;
        }
        if (err.error && typeof err.error === 'string') {
          errorMsg = err.error;
        } else if (err.error && err.error.message) {
          errorMsg = err.error.message;
        }
        
        this.errorMessage = errorMsg;
        this.successMessage = '';
        this.isLoading = false;
      }
    });
  }

  // ✅ CORRECTION 4: Ajouter headers dans onSubmitPassword
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
    this.http.put(`http://localhost:8087/api/users/${this.userId}/password`, this.passwordForm.value, {
      headers: this.getHeaders()
    }).subscribe({
      next: (response: any) => {
        this.successMessage = typeof response === 'string' ? response : 'Mot de passe modifié';
        this.passwordForm.reset();
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('Erreur changement mot de passe:', err);
        
        let errorMsg = 'Erreur de changement de mot de passe';
        if (err.status === 0) {
          errorMsg = 'Serveur indisponible. Vérifiez que le backend est démarré.';
        } else if (err.status === 401) {
          errorMsg = 'Ancien mot de passe incorrect ou session expirée.';
        } else if (err.status) {
          errorMsg += ` (Status: ${err.status})`;
        }
        if (err.error && typeof err.error === 'string') {
          errorMsg = err.error;
        } else if (err.error && err.error.message) {
          errorMsg = err.error.message;
        }
        
        this.errorMessage = errorMsg;
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

  // ✅ CORRECTION 5: Ajouter headers dans updatePhoto
  updatePhoto(photoBase64: string): void {
    if (!this.userId) {
      this.errorMessage = 'Utilisateur non identifié. Veuillez vous reconnecter.';
      return;
    }
    
    this.isLoading = true;
    
    const body = { 
      photo: photoBase64 
    };
    
    this.http.put(`http://localhost:8087/api/users/${this.userId}/photo`, body, {
      headers: this.getHeaders()
    }).subscribe({
      next: (response: any) => {
        this.successMessage = 'Photo mise à jour avec succès';
        this.userPhoto = photoBase64;
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: (err: any) => {
        console.error('ERREUR COMPLÈTE:', err);
        console.error('STATUS:', err.status);
        console.error('MESSAGE:', err.message);
        console.error('ERROR BODY:', err.error);
        
        let errorMsg = 'Erreur lors de la mise à jour de la photo';
        if (err.status === 0) {
          errorMsg = 'Serveur indisponible. Vérifiez que le backend est démarré sur le port 8087.';
        } else if (err.status === 401) {
          errorMsg = 'Session expirée. Veuillez vous reconnecter.';
          this.router.navigate(['/signup']);
        } else if (err.status) {
          errorMsg += ` (Status: ${err.status})`;
        }
        if (err.error && typeof err.error === 'string') {
          errorMsg = err.error;
        } else if (err.error && err.error.message) {
          errorMsg = err.error.message;
        } else if (err.message) {
          errorMsg = err.message;
        }
        
        this.errorMessage = errorMsg;
        this.successMessage = '';
        this.isLoading = false;
      }
    });
  }

  // ✅ CORRECTION 6: Nettoyer TOUTES les clés localStorage
  logout(): void {
    // Supprimer toutes les clés utilisées par l'auth
    localStorage.removeItem('authToken');
    localStorage.removeItem('userId');
    localStorage.removeItem('userNom');
    localStorage.removeItem('userEmail');
    localStorage.removeItem('userRole');
    this.router.navigate(['/signup']);  // ← CHANGÉ: /login → /signup
  }
}