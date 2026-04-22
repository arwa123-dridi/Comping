// signin.component.ts 

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
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

  constructor(private signinService: SigninService, private router: Router) {}

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
        console.log('Login réussi', res);

        // Stockage du token
        localStorage.setItem('authToken', res.token);

        //  Récupération des informations utilisateur 
        const userId = res.id || res.userId || '';
        const userEmail = res.email || this.email;
        const userNom = `${res.firstName || ''} ${res.lastName || ''}`.trim() || res.username || 'Utilisateur';
        const userRole = res.role || (res.roles && res.roles[0]) || 'USER';

        localStorage.setItem('userId', userId);
        localStorage.setItem('userEmail', userEmail);
        localStorage.setItem('userNom', userNom);
        localStorage.setItem('userRole', userRole);

        // Redirection en fonction du rôle
        let redirectUrl = '/dashboard'; 
        if (userRole === 'ADMIN') {
          redirectUrl = '/admin/dashboard';
        } else if (userRole === 'ORGANISATEUR') {
          redirectUrl = '/dashboard';
        } else {
          redirectUrl = '/dashboard';
        }

        console.log('Redirection vers :', redirectUrl);
        this.router.navigate([redirectUrl]);

        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg = 'Email ou mot de passe incorrect.';
        console.error('Erreur login', err);
        //
      }
    });
  }
}