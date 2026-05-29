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

  profileForm!:  FormGroup;
  passwordForm!: FormGroup;

  successMessage = '';
  errorMessage   = '';
  userId         = '';
  userEmail      = '';
  userPhoto      = '';
  userInitiales  = 'U';
  userRole       = 'USER';
  isLoading      = false;
  photoLoading   = false;

  // Stats
  totalSorties           = 0;
  totalEquipes           = 0;
  totalSortiesCompletees = 0;
  totalSortiesAVenir     = 0;
  niveauPrincipal        = '—';
  statsLoading           = true;

  private readonly API = 'http://localhost:8087/api/users';

  constructor(
    private fb:            FormBuilder,
    private http:          HttpClient,
    private router:        Router,
    private sortieService: SortieService,
    private equipeService: EquipeService,
    private signinService: SigninService
  ) {}

  ngOnInit(): void {
    this.buildForms();
    this.loadUserFromToken();
    this.loadStats();
  }

  // ─── Forms ──────────────────────────────────────────────────
  buildForms(): void {
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

  passwordMatchValidator(g: FormGroup) {
    return g.get('newPassword')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  // ─── Headers ─────────────────────────────────────────────────
  private getHeaders(): HttpHeaders {
    return new HttpHeaders({
      'Authorization': `Bearer ${localStorage.getItem('authToken') ?? ''}`,
      'Content-Type':  'application/json'
    });
  }

  // ─── Chargement utilisateur ───────────────────────────────────
  loadUserFromToken(): void {
    this.userId    = localStorage.getItem('userId')    ?? '';
    this.userEmail = localStorage.getItem('userEmail') ?? '';
    this.userRole  = localStorage.getItem('userRole')  ?? 'USER';

    const nom    = localStorage.getItem('userNom')    ?? '';
    const prenom = localStorage.getItem('userPrenom') ?? '';
    const fn     = prenom || nom.split(' ')[0] || '';
    const ln     = nom.split(' ').slice(1).join(' ') || '';

    this.profileForm.patchValue({ firstName: fn, lastName: ln, email: this.userEmail });
    this.updateInitiales(fn, ln);

    if (!this.userId) return;

    // Charger le profil complet depuis le backend
    this.http.get<any>(`${this.API}/${this.userId}`, { headers: this.getHeaders() })
      .subscribe({
        next: (u) => {
          const firstName = u.prenom || u.firstName || fn;
          const lastName  = u.nom    || u.lastName  || ln;
          this.profileForm.patchValue({
            firstName,
            lastName,
            email:     u.email     || this.userEmail,
            telephone: u.telephone || '',
            address:   u.adresse   || u.address || ''
          });
          this.updateInitiales(firstName, lastName);
          if (u.photo && u.photo !== 'null' && u.photo.trim() !== '') {
            this.userPhoto = u.photo;
          }
        },
        error: () => {}
      });
  }

  updateInitiales(fn: string, ln: string): void {
    const full = `${fn} ${ln}`.trim() || this.userEmail.split('@')[0] || 'U';
    this.userInitiales = full.split(' ')
      .map(w => w[0] || '')
      .join('')
      .toUpperCase()
      .slice(0, 2) || 'U';
  }

  // ─── Soumettre profil ─────────────────────────────────────────
  onSubmitProfile(): void {
    this.clearMessages();
    if (!this.userId) { this.errorMessage = 'Utilisateur non identifié.'; return; }
    this.isLoading = true;

    const v = this.profileForm.value;
    const body = {
      prenom:    v.firstName,
      nom:       v.lastName,
      firstName: v.firstName,
      lastName:  v.lastName,
      email:     v.email,
      telephone: v.telephone,
      adresse:   v.address,
      address:   v.address
    };

    // ✅ CORRIGÉ : PUT /api/users/{userId} (sans /profile)
    this.http.put(`${this.API}/${this.userId}`, body, {
      headers: this.getHeaders(), responseType: 'text'
    }).subscribe({
      next: (r: string) => {
        this.successMessage = r?.trim() || 'Profil mis à jour avec succès.';
        localStorage.setItem('userNom',    `${v.firstName} ${v.lastName}`.trim());
        localStorage.setItem('userPrenom', v.firstName);
        this.updateInitiales(v.firstName, v.lastName);
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = this.parseError(err);
      }
    });
  }

  // ─── Soumettre mot de passe ───────────────────────────────────
  onSubmitPassword(): void {
    this.clearMessages();
    if (this.passwordForm.invalid) {
      if (this.passwordForm.errors?.['mismatch']) {
        this.errorMessage = 'Les mots de passe ne correspondent pas.';
      } else {
        this.errorMessage = 'Formulaire invalide — vérifiez les champs.';
      }
      return;
    }
    this.isLoading = true;

    const { oldPassword, newPassword, confirmPassword } = this.passwordForm.value;

    // ✅ Fonctionne pour USER, ORGANISATEUR et ADMIN
    this.http.put(`${this.API}/${this.userId}/password`,
      { oldPassword, newPassword, confirmPassword },
      { headers: this.getHeaders(), responseType: 'text' }
    ).subscribe({
      next: (r: string) => {
        this.successMessage = r?.trim() || 'Mot de passe modifié avec succès.';
        this.passwordForm.reset();
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        if (err.status === 400) {
          this.errorMessage = 'Ancien mot de passe incorrect.';
        } else {
          this.errorMessage = this.parseError(err);
        }
      }
    });
  }

  // ─── Photo ────────────────────────────────────────────────────
  onFileSelected(event: any): void {
    this.clearMessages();
    const file: File = event.target.files[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.errorMessage = 'Format invalide — utilisez JPG, PNG ou WebP.'; return;
    }
    if (file.size > 5 * 1024 * 1024) {
      this.errorMessage = 'Image trop grande (max 5 MB).'; return;
    }

    const reader = new FileReader();
    reader.onload = () => this.uploadPhoto(reader.result as string);
    reader.readAsDataURL(file);
  }

  uploadPhoto(photoBase64: string): void {
    this.photoLoading = true;
    this.clearMessages();

    // ✅ CORRIGÉ : canActOn() dans le backend permet ADMIN + propriétaire
    this.http.put(`${this.API}/${this.userId}/photo`,
      { photo: photoBase64 },
      { headers: this.getHeaders(), responseType: 'text' }
    ).subscribe({
      next: (r: string) => {
        this.successMessage = r?.trim() || 'Photo mise à jour.';
        this.userPhoto = photoBase64;
       this.userPhoto = photoBase64;
       localStorage.setItem('userPhoto', photoBase64);           // ✅ sauvegarde
       window.dispatchEvent(new Event('storage'));                // ✅ notifie le header
        this.photoLoading = false;
      },
      error: (err) => {
        this.photoLoading = false;
        if (err.status === 403) {
          this.errorMessage = 'Accès refusé — redémarrez le backend avec la correction SecurityConfig.';
        } else {
          this.errorMessage = this.parseError(err);
        }
      }
    });
  }

  // ─── Stats ────────────────────────────────────────────────────
  loadStats(): void {
    this.statsLoading = true;
    const uid = this.userId || localStorage.getItem('userId') || '';
    if (!uid) { this.statsLoading = false; return; }

    const now = new Date();

    this.sortieService.getAllSorties().subscribe({
      next: (sorties) => {
        const mySorties = sorties.filter(s =>
          (s.participantIds ?? []).map(String).includes(String(uid))
        );
        this.totalSorties           = mySorties.length;
        this.totalSortiesCompletees = mySorties.filter(s => new Date(s.dateDebut) < now).length;
        this.totalSortiesAVenir     = mySorties.filter(s => new Date(s.dateDebut) >= now).length;

        const freq: Record<string, number> = {};
        mySorties.forEach(s => freq[s.difficulte] = (freq[s.difficulte] || 0) + 1);
        const top = Object.entries(freq).sort((a, b) => b[1] - a[1])[0];
        this.niveauPrincipal = top
          ? ({'FACILE':'🥾 Facile','MOYEN':'🧗 Modéré','DIFFICILE':'⛰️ Difficile'} as any)[top[0]] || top[0]
          : '—';
        this.statsLoading = false;
      },
      error: () => { this.statsLoading = false; }
    });

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

  // ─── Helpers ─────────────────────────────────────────────────
  getRoleLabel(): string {
    const r = this.userRole;
    if (r === 'ADMIN'        || r === 'ROLE_ADMIN')        return '👑 Administrateur';
    if (r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR') return '🏕️ Organisateur';
    return '🥾 Randonneur';
  }

  getRoleBadgeClass(): string {
    const r = this.userRole;
    if (r === 'ADMIN'        || r === 'ROLE_ADMIN')        return 'badge-admin';
    if (r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR') return 'badge-orga';
    return 'badge-user';
  }

  hasPhoto(): boolean {
    return !!this.userPhoto && this.userPhoto !== 'assets/default-avatar.png' && this.userPhoto !== 'null';
  }

  getDashboardLink(): string {
    const r = this.userRole;
    if (r === 'ADMIN'        || r === 'ROLE_ADMIN')        return '/admin/dashboard';
    if (r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR') return '/admin/organizer';
    return '/dashboard';
  }

  private clearMessages(): void {
    this.successMessage = '';
    this.errorMessage   = '';
  }

  private parseError(err: any): string {
    if (err.status === 0)   return 'Serveur inaccessible (port 8087).';
    if (err.status === 401) return 'Session expirée — reconnectez-vous.';
    if (err.status === 403) return 'Accès refusé.';
    if (err.status === 404) return 'Utilisateur introuvable.';
    return err.error?.message || err.error || `Erreur serveur (${err.status})`;
  }

  logout(): void {
    this.signinService.logout();
    this.router.navigate(['/login']);
  }
}
