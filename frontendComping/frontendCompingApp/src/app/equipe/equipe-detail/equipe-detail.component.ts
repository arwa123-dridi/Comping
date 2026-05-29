// src/app/equipe/equipe-detail/equipe-detail.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule, DatePipe } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { EquipeService } from '../../services/equipe.service';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-equipe-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, DatePipe],
  templateUrl: './equipe-detail.component.html',
  styleUrls: ['./equipe-detail.component.css']
})
export class EquipeDetailComponent implements OnInit {

  equipe:       EquipeResponse | null = null;
  loading       = true;
  error:        string | null = null;
  showModal     = false;
  joiningEquipe = false;

  userRole: string | null = null;
  userId:   string | null = null;

  toastMessage: string | null = null;
  toastType:    'success' | 'error' | 'info' = 'info';
  private toastTimer: any;

  private readonly heroImages: string[] = [
    'https://images.unsplash.com/photo-1533240332313-0db3b4591e1b?w=1400&q=70',
    'https://images.unsplash.com/photo-1501854140801-50d01698950b?w=1400&q=70',
    'https://images.unsplash.com/photo-1469022563149-aa64dbd37dae?w=1400&q=70',
    'https://images.unsplash.com/photo-1551632811-561732d1e306?w=1400&q=70',
  ];

  private readonly avatarColors = [
    '#16a34a','#1d4ed8','#d97706','#dc2626',
    '#7c3aed','#0891b2','#be185d','#15803d',
  ];

  constructor(
    private route:        ActivatedRoute,
    private router:       Router,
    private equipeService:EquipeService
  ) {}

  ngOnInit(): void {
    this.userRole = localStorage.getItem('userRole');
    this.userId   = localStorage.getItem('userId');
    const id      = this.route.snapshot.paramMap.get('id');
    if (id) this.loadEquipe(id);
    else { this.error = 'ID introuvable'; this.loading = false; }
  }

  // ── Rôles ────────────────────────────────────────────────
  isUser(): boolean {
    const r = this.userRole ?? '';
    return r === 'USER' || r === 'ROLE_USER';
  }

  isAdmin(): boolean {
    const r = this.userRole ?? '';
    return r === 'ADMIN' || r === 'ROLE_ADMIN';
  }

  isOrganisateurRole(): boolean {
    const r = this.userRole ?? '';
    return r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR';
  }

  // ── Getters calculés ─────────────────────────────────────

  get isOrganisateur(): boolean {
    return !!this.userId
      && String(this.equipe?.organisateurId) === String(this.userId);
  }

  get isMembre(): boolean {
    if (!this.userId || !this.equipe?.membres) return false;
    return this.equipe.membres.some(
      m => String(m?.id ?? m) === String(this.userId)
    );
  }

  get nombreMembres(): number {
    return this.equipe?.membres?.length
      ?? this.equipe?.nbMembresActuels
      ?? 0;
  }

  get placesRestantes(): number {
    const max = this.equipe?.nbMembresMax ?? this.equipe?.capaciteMax ?? 10;
    return Math.max(0, max - this.nombreMembres);
  }

  getCapacityPercent(): number {
    const max = this.equipe?.nbMembresMax ?? this.equipe?.capaciteMax ?? 10;
    if (!max) return 0;
    return Math.min((this.nombreMembres / max) * 100, 100);
  }

  // ── Chargement ───────────────────────────────────────────
  loadEquipe(id: string): void {
    this.loading = true;
    this.error   = null;
    this.equipeService.getEquipeById(id).subscribe({
      next: (data) => {
        this.equipe  = data;
        this.loading = false;
      },
      error: (err) => {
        if (err.status === 404) this.error = 'Cette équipe n\'existe pas.';
        else if (err.status === 0) this.error = 'Serveur inaccessible.';
        else this.error = 'Impossible de charger l\'équipe.';
        this.loading = false;
      }
    });
  }

  // ── Actions ──────────────────────────────────────────────
  rejoindre(): void {
    if (!this.userId) {
      localStorage.setItem('redirect_after_login', `/equipes/${this.equipe?.id}`);
      this.router.navigate(['/login']);
      return;
    }
    if (!this.isUser()) {
      this.showToast('Seuls les utilisateurs peuvent rejoindre une équipe.', 'error');
      return;
    }

    this.joiningEquipe = true;
    const prenom  = localStorage.getItem('userPrenom') ?? '';
    const nom     = localStorage.getItem('userNom') ?? '';
    const userNom = `${prenom} ${nom}`.trim() || 'Utilisateur';

    this.equipeService.ajouterMembre(
      String(this.equipe!.id), this.userId, userNom
    ).subscribe({
      next: () => {
        this.joiningEquipe = false;
        this.showToast('✅ Vous avez rejoint l\'équipe !', 'success');
        this.loadEquipe(String(this.equipe!.id));
      },
      error: (err) => {
        this.joiningEquipe = false;
        const msg = err.status === 409 ? 'Vous êtes déjà membre.'
                  : err.status === 400 ? 'Équipe complète.'
                  : err.error?.message ?? 'Erreur lors de l\'inscription.';
        this.showToast(msg, 'error');
      }
    });
  }

  openModal():  void { this.showModal = true; }
  cancelModal():void { this.showModal = false; }

  confirmQuitter(): void {
    this.showModal = false;
    if (!this.userId || !this.equipe) return;

    this.equipeService.retirerMembre(
      String(this.equipe.id), this.userId
    ).subscribe({
      next: () => {
        this.showToast('✅ Vous avez quitté l\'équipe.', 'success');
        this.loadEquipe(String(this.equipe!.id));
      },
      error: (err) => {
        this.showToast(err.error?.message ?? 'Erreur lors de la désinscription.', 'error');
      }
    });
  }

  modifier(): void {
    this.router.navigate(['/admin/equipes/edit', this.equipe?.id]);
  }

  supprimer(): void {
    if (!confirm(`Supprimer définitivement "${this.equipe?.nom}" ?`)) return;
    this.equipeService.deleteEquipe(String(this.equipe!.id)).subscribe({
      next: () => {
        this.showToast('Équipe supprimée.', 'success');
        setTimeout(() => this.router.navigate(['/equipes']), 1200);
      },
      error: () => this.showToast('Erreur lors de la suppression.', 'error')
    });
  }

  // ── Helpers membres ──────────────────────────────────────
  getMembresListe(): any[] {
    return this.equipe?.membres ?? [];
  }

  isMembreOrganisateur(m: any): boolean {
    const mId = String(m?.id ?? m ?? '');
    return mId === String(this.equipe?.organisateurId);
  }

  getMembreNom(m: any): string {
    return m?.nom ?? m?.prenom ?? m?.firstName ?? m?.lastName
      ?? `Membre ${String(m?.id ?? m ?? '?').slice(-4)}`;
  }

  getInitiales(m: any): string {
    const nom = this.getMembreNom(m);
    return nom.split(' ').map((w: string) => w[0] ?? '').join('').toUpperCase().slice(0, 2) || '?';
  }

  getAvatarColor(m: any): string {
    const seed = String(m?.id ?? m ?? '');
    const idx  = (seed.charCodeAt(0) ?? 0) % this.avatarColors.length;
    return this.avatarColors[idx];
  }

  getOrgInitiales(): string {
    const nom = this.equipe?.organisateurNom ?? '';
    return nom.split(' ').map(w => w[0] ?? '').join('').toUpperCase().slice(0, 2) || 'O';
  }

  getHeroImage(): string {
    if ((this.equipe as any)?.imageUrl?.trim()) return (this.equipe as any).imageUrl;
    const seed = String(this.equipe?.id ?? '');
    let hash = 0;
    for (let i = 0; i < seed.length; i++) hash = (hash * 31 + seed.charCodeAt(i)) >>> 0;
    return this.heroImages[hash % this.heroImages.length];
  }

  showToast(msg: string, type: 'success' | 'error' | 'info' = 'info'): void {
    this.toastMessage = msg;
    this.toastType    = type;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer   = setTimeout(() => { this.toastMessage = null; }, 4000);
  }
}