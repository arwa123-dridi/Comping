import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { SigninService, LoginDTORequest, LoginDTOResponse } from '../services/signin.service';

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


  constructor(private signinService: SigninService
  ) {}

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

      this.signinService.saveToken(res.token);

      console.log("TOKEN IN LOCALSTORAGE:", localStorage.getItem('authToken'));
    },

   error: (err) => {
  this.isLoading = false;

  console.log("FULL ERROR:", err);

  const message = err?.error?.message;

  if (err.status === 403 || message === "ACCOUNT_DISABLED") {
    this.errorMsg = "Votre compte est désactivé";
  } 
  else if (err.status === 401 || message === "INVALID_PASSWORD") {
    this.errorMsg = "Email ou mot de passe incorrect.";
  } 
  else {
    this.errorMsg = "Erreur serveur. Veuillez réessayer.";
  }
    }
  });
}

}