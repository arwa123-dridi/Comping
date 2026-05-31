import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { HttpClient, HttpHeaders, HttpClientModule } from '@angular/common/http';
import { CommonModule } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import { SortieService } from '../services/sortie.service';
import { EquipeService } from '../services/equipe.service';
import { SigninService } from '../services/signin.service';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [ReactiveFormsModule, CommonModule, HttpClientModule, RouterModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class ProfileComponent implements OnInit {

  profileForm: FormGroup;
  passwordForm: FormGroup;

  successMessage = '';
  errorMessage = '';
  userId = '';
  userEmail = '';
  userPhoto = 'assets/default-avatar.png';
  userInitiales = 'U';
  userRole = 'USER';
  isLoading = false;

  // Stats réelles
  totalSorties = 0;
  totalEquipes = 0;
  totalSortiesCompletees = 0;
  niveauPrincipal = '—';
  statsLoading = true;

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    private router: Router,
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private signinService: SigninService
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
    this.loadStats();
  }

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value ? null : { mismatch: true };
  }

  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem('authToken')}`,
      'Content-Type': 'application/json'
    });
  }

  loadUserFromToken(): void {
    this.userId    = localStorage.getItem('userId') ?? '';
    this.userEmail = localStorage.getItem('userEmail') ?? '';
    this.userRole  = localStorage.getItem('userRole') ?? 'USER';
    const nom    = localStorage.getItem('userNom') ?? '';
    const prenom = localStorage.getItem('userPrenom') ?? '';
    const fn = prenom || nom.split(' ')[0] || '';
    const ln = nom.split(' ').slice(1).join(' ') || '';
    this.profileForm.patchValue({ firstName: fn, lastName: ln, email: this.userEmail });

    const full = (fn || ln) ? `${fn} ${ln}`.trim() : (this.userEmail.split('@')[0] || 'U');
    this.userInitiales = full.split(' ').map((w: string) => w[0]).join('').toUpperCase().slice(0, 2) || 'U';

    if (!this.userId) return;
    this.http.get<any>(`http://localhost:8087/api/users/${this.userId}`, { headers: this.getHeaders() })
      .subscribe({
        next: (u) => {
          this.profileForm.patchValue({
            firstName: u.prenom || u.firstName || fn,
            lastName:  u.nom || u.lastName || ln,
            email:     u.email || this.userEmail,
            telephone: u.telephone || '',
            address:   u.adresse || u.address || ''
          });
          if (u.photo) this.userPhoto = u.photo;
        },
        error: () => {}
      });
  }

  loadStats(): void {
    this.statsLoading = true;
    const uid = this.userId || localStorage.getItem('userId') || '';
    if (!uid) { this.statsLoading = false; return; }

    // Load sorties
    this.sortieService.getAllSorties().subscribe({
      next: (sorties) => {
        const mySorties = sorties.filter(s =>
          (s.participantIds ?? []).map(String).includes(String(uid))
        );
        this.totalSorties = mySorties.length;
        this.totalSortiesCompletees = mySorties.filter(s => new Date(s.dateDebut) < new Date()).length;

        // Niveau dominant
        const freq: Record<string,number> = {};
        mySorties.forEach(s => freq[s.difficulte] = (freq[s.difficulte] || 0) + 1);
        const top = Object.entries(freq).sort((a, b) => b[1] - a[1])[0];
        this.niveauPrincipal = top ? ({ FACILE: '🥾 Facile', MOYEN: '🧗 Modéré', DIFFICILE: '⛰️ Difficile' }[top[0]] || top[0]) : '—';
        this.statsLoading = false;
      },
      error: () => { this.statsLoading = false; }
    });

    // Load equipes
    this.equipeService.getAllEquipes().subscribe({
      next: (equipes) => {
        this.totalEquipes = equipes.filter(e =>
          e.membres?.some(m => String(m.id) === String(uid)) ||
          String(e.organisateurId) === String(uid)
        ).length;
      },
      error: () => {}
    });
  }

  getRoleLabel(): string {
    const r = this.userRole;
    if (r === 'ADMIN' || r === 'ROLE_ADMIN') return '👑 Administrateur';
    if (r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR') return '🏕️ Organisateur';
    return '🥾 Randonneur';
  }

  onSubmitProfile(): void {
    if (!this.userId) { this.errorMessage = 'Utilisateur non identifié.'; return; }
    this.isLoading = true;
    const v = this.profileForm.value;
    this.http.put(
      `http://localhost:8087/api/users/${this.userId}`,
      { prenom: v.firstName, nom: v.lastName, email: v.email, telephone: v.telephone, adresse: v.address },
      { headers: this.getHeaders(), responseType: 'text' }
    ).subscribe({
      next: (r: string) => {
        this.successMessage = r || 'Profil mis à jour.';
        localStorage.setItem('userNom', `${v.firstName} ${v.lastName}`.trim());
        localStorage.setItem('userPrenom', v.firstName);
        this.errorMessage = '';
        this.isLoading = false;
        this.userInitiales = `${v.firstName?.[0]||''}${v.lastName?.[0]||''}`.toUpperCase() || 'U';
      },
      error: (err) => { this.errorMessage = err.error || `Erreur (${err.status})`; this.isLoading = false; }
    });
  }

  onSubmitPassword(): void {
    if (this.passwordForm.invalid) { this.errorMessage = 'Formulaire invalide.'; return; }
    this.isLoading = true;
    this.http.put(
      `http://localhost:8087/api/users/${this.userId}/password`,
      this.passwordForm.value,
      { headers: this.getHeaders(), responseType: 'text' }
    ).subscribe({
      next: (r: string) => {
        this.successMessage = r || 'Mot de passe modifié.';
        this.passwordForm.reset();
        this.errorMessage = '';
        this.isLoading = false;
      },
      error: (err) => { this.errorMessage = err.error || `Erreur (${err.status})`; this.isLoading = false; }
    });
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) { this.errorMessage = 'Image invalide.'; return; }
    if (file.size > 5 * 1024 * 1024) { this.errorMessage = 'Image trop grande (max 5MB).'; return; }
    const reader = new FileReader();
    reader.onload = () => this.updatePhoto(reader.result as string);
    reader.readAsDataURL(file);
  }

  updatePhoto(photoBase64: string): void {
    this.isLoading = true;
    this.http.put(
      `http://localhost:8087/api/users/${this.userId}/photo`,
      { photo: photoBase64 },
      { headers: this.getHeaders(), responseType: 'text' }
    ).subscribe({
      next: (r: string) => {
        this.successMessage = r || 'Photo mise à jour.';
        this.userPhoto = photoBase64;
        this.isLoading = false;
      },
      error: (err) => { this.errorMessage = err.error || `Erreur (${err.status})`; this.isLoading = false; }
    });
  }

  logout(): void {
    this.signinService.logout();
    this.router.navigate(['/login']);
  }
}