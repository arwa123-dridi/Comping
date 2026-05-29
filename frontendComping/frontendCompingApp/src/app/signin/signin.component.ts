// src/app/signin/signin.component.ts
// ✅ CORRIGÉ : suppression du double redirect — le service gère tout
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { SigninService, LoginDTOResponse } from '../services/signin.service';

@Component({
  selector: 'app-signin',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './signin.component.html',
  styleUrls: ['./signin.component.css']
})
export class SigninComponent {
  email        = '';
  password     = '';
  rememberMe   = false;
  showPassword = false;
  isLoading    = false;
  errorMsg     = '';

  constructor(private signinService: SigninService) {}

  togglePassword(): void { this.showPassword = !this.showPassword; }

  onSubmit(): void {
    this.errorMsg = '';
    if (!this.email || !this.password) {
      this.errorMsg = 'Veuillez remplir tous les champs.';
      return;
    }
    this.isLoading = true;

    // ✅ Le service login() gère saveSession() + handleRedirectAfterLogin()
    // Ne pas rediriger ici — le service le fait via tap()
    this.signinService.login({ email: this.email, password: this.password }).subscribe({
      next: (_res: LoginDTOResponse) => {
        this.isLoading = false;
        // Redirect géré dans signin.service.ts → handleRedirectAfterLogin()
      },
      error: (err) => {
        this.isLoading = false;
        const message = err?.error?.message;
        if (err.status === 403 || message === 'ACCOUNT_DISABLED') {
          this.errorMsg = 'Votre compte est désactivé.';
        } else if (err.status === 401 || message === 'INVALID_PASSWORD') {
          this.errorMsg = 'Email ou mot de passe incorrect.';
        } else if (err.status === 0) {
          this.errorMsg = 'Serveur inaccessible (port 8087).';
        } else {
          this.errorMsg = err.error?.message ?? 'Erreur de connexion.';
        }
      }
    });
  }
}
