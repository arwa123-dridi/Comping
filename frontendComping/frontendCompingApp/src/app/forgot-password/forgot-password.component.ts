import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { ForgotPasswordService, ForgotPasswordResponse } from '../services/forgot-password.service';

@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterModule],
  templateUrl: './forgot-password.component.html',
  styleUrls: ['./forgot-password.component.css']
})
export class ForgotPasswordComponent {
  email: string = '';
  loading: boolean = false;
  message: string = '';
  error: string = '';

  constructor(
    private router: Router, 
    private forgotService: ForgotPasswordService
  ) {}

  goToLogin(): void {
    this.router.navigate(['/login']);
  }

  onSubmit(): void {
    this.error = '';
    this.message = '';

    if (!this.email) {
      this.error = 'Veuillez entrer votre adresse email.';
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!emailRegex.test(this.email)) {
      this.error = 'Veuillez entrer une adresse email valide.';
      return;
    }

    this.loading = true;

    this.forgotService.forgotPassword(this.email).subscribe({
      next: (res: ForgotPasswordResponse) => {
        this.loading = false;
        this.message = res.message || 'Email de réinitialisation envoyé ! Vérifiez votre boîte de réception.';
        this.email = '';
      },
      error: (err: any) => {
        this.loading = false;
        this.error = err.error?.message || `Erreur lors de l'envoi. Vérifiez votre email.`;
        console.error('Forgot password error:', err);
      }
    });
  }
}
