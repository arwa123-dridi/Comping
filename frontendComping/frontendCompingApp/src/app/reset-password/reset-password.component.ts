import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';
import { ResetPasswordService, ResetPasswordResponse } from '../services/reset-password.service';

@Component({
  selector: 'app-reset-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './reset-password.component.html',
  styleUrls: ['./reset-password.component.css']
})
export class ResetPasswordComponent implements OnInit {
  token: string = '';
  newPassword: string = '';
  confirmPassword: string = '';
  loading: boolean = false;
  message: string = '';
  error: string = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute,
    private resetService: ResetPasswordService
  ) {}

  ngOnInit(): void {
    // Extract token from route params or query params
    this.token = this.route.snapshot.paramMap.get('token') || 
                 this.route.snapshot.queryParamMap.get('token') || '';
    if (!this.token) {
      this.error = 'Token de réinitialisation manquant. Vérifiez le lien dans votre email.';
    }
  }

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  onSubmit(): void {
    this.error = '';
    this.message = '';

    if (!this.token) {
      this.error = 'Token invalide.';
      return;
    }

    if (!this.newPassword || this.newPassword.length < 6) {
      this.error = 'Le mot de passe doit contenir au moins 6 caractères.';
      return;
    }

    if (this.newPassword !== this.confirmPassword) {
      this.error = 'Les mots de passe ne correspondent pas.';
      return;
    }

    this.loading = true;

    this.resetService.resetPassword(this.token, this.newPassword).subscribe({
      next: (res: ResetPasswordResponse) => {
        this.loading = false;
        this.message = res.message || 'Mot de passe réinitialisé ! Vous pouvez maintenant vous connecter.';
        this.newPassword = '';
        this.confirmPassword = '';
      },
      error: (err: any) => {
        this.loading = false;
        this.error = err.error?.message || 'Erreur lors de la réinitialisation. Token invalide ou expiré ?';
        console.error('Reset password error:', err);
      }
    });
  }
}

