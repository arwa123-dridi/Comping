// src/app/signin/signin.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
import { SigninService } from '../services/signin.service';

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
  rememberMe   = false;   // ✅ restauré — utilisé par le HTML
  showPassword = false;
  isLoading    = false;
  errorMsg     = '';

  constructor(private signinService: SigninService, private router: Router) {}

  togglePassword(): void { this.showPassword = !this.showPassword; }

  onSubmit(): void {
    this.errorMsg = '';
    if (!this.email || !this.password) {
      this.errorMsg = 'Veuillez remplir tous les champs.';
      return;
    }
    this.isLoading = true;
    this.signinService.login({ email: this.email, password: this.password })
      .subscribe({
        next: () => {
          this.isLoading = false;
          const role = localStorage.getItem('userRole') ?? 'USER';
          if (role === 'ADMIN'        || role === 'ROLE_ADMIN' ||
              role === 'ORGANISATEUR' || role === 'ROLE_ORGANISATEUR') {
            this.router.navigate(['/admin/dashboard']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        },
        error: (err) => {
          this.isLoading = false;
          if (err.status === 401 || err.status === 403) {
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