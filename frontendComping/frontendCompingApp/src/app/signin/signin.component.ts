// src/app/signin/signin.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
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

  private returnUrl = '/Campino';

  constructor(
    private signinService: SigninService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.returnUrl = this.route.snapshot.queryParamMap.get('returnUrl') ?? '/Campino';
  }

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.errorMsg = '';

    if (!this.email || !this.password) {
      this.errorMsg = 'Veuillez remplir tous les champs.';
      return;
    }

    this.isLoading = true;

    const dto = { email: this.email, password: this.password };

    this.signinService.login(dto).subscribe({
      next: (res: LoginDTOResponse) => {
        this.isLoading = false;
        console.log('Login réussi', res);

        const role = localStorage.getItem('userRole') ?? 'USER';
        const allowedRoles = ['ADMIN', 'ROLE_ADMIN', 'USER', 'ROLE_USER', 'ORGANIZER', 'ROLE_ORGANIZER'];

        if (this.returnUrl) {
          if (allowedRoles.includes(role)) {
            this.router.navigateByUrl(this.returnUrl);
          } else {
            this.isLoading = false;
            this.errorMsg = 'Accès réservé aux administrateurs, utilisateurs et organisateurs.';
          }
        } else if (role === 'ADMIN' || role === 'ROLE_ADMIN') {
          this.router.navigate(['/admin/dashboard']);
        } else {
          this.router.navigate(['/Campino']);
        }
      },
      error: (err) => {
        this.isLoading = false;
        console.log("FULL ERROR:", err);

        const message = err?.error?.message;

        // Gestion d'erreur enrichie (version de ta collègue)
        if (err.status === 403 || message === "ACCOUNT_DISABLED") {
          this.errorMsg = "Votre compte est désactivé";
        } 
        else if (err.status === 401 || message === "INVALID_PASSWORD") {
          this.errorMsg = "Email ou mot de passe incorrect.";
        } 
        else if (err.status === 0) {
          this.errorMsg = "Serveur inaccessible (port 8087).";
        } 
        else {
          this.errorMsg = err.error?.message ?? "Erreur de connexion.";
        }
      }
    });
  }
}