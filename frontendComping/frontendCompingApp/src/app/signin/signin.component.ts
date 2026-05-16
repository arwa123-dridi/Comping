import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule, Router } from '@angular/router';
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
        this.isLoading = false;
        console.log('Login réussi', res);
        this.signinService.saveToken(res.token);
        
        // Redirect the user to the admin dashboard after successfully logging in
        this.router.navigate(['/admin/dashboard']);
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMsg = err?.status === 0
          ? 'Serveur backend indisponible. Démarrez Spring Boot sur http://localhost:8087.'
          : 'Email ou mot de passe incorrect.';
        console.error('Erreur login', err);
      }
    });
  }
}