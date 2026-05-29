import { Component, OnInit, OnDestroy, ViewEncapsulation } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { SortieService } from '../../services/sortie.service';
import { SortieResponse } from '../../models/sortie.model';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './home.component.html',
  styleUrl: './home.component.css',
  encapsulation: ViewEncapsulation.None
})
export class HomeComponent implements OnInit, OnDestroy {

  currentSlide = 0;
  private timer: any;

  // ✅ Randonnées dynamiques depuis le backend
  randonneesRecommandees: SortieResponse[] = [];
  loadingRando = true;

  slides = [
    { url: 'https://images.unsplash.com/photo-1504280390367-361c6d9f38f4?w=1200&q=80', label: '🏕️ Forêt · Ain Draham' },
    { url: 'https://images.unsplash.com/photo-1487730116645-74489c95b41b?w=1200&q=80', label: '⛺ Camping en montagne' },
    { url: 'https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?w=1200&q=80', label: '🌲 Nature sauvage' },
    { url: 'https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=1200&q=80', label: '🔥 Soirée autour du feu' },
    { url: 'https://images.unsplash.com/photo-1533873984035-25970ab07461?w=1200&q=80', label: '🌄 Lever de soleil' },
    { url: 'https://images.unsplash.com/photo-1478131143081-80f7f84ca84d?w=1200&q=80', label: '🌌 Nuit étoilée' },
  ];

  services = [
    { icon: '🏕️', title: 'Emplacements', desc: 'Trouvez l\'emplacement idéal parmi nos 240+ sites sélectionnés' },
    { icon: '🎒', title: 'Location matériel', desc: 'Tentes, sacs de couchage et équipements livrés sur place' },
    { icon: '🌄', title: 'Randonnées guidées', desc: 'Explorez la nature avec nos guides expérimentés certifiés', link: '/sorties' },
    { icon: '🔥', title: 'Expériences survie', desc: 'Apprenez les techniques de survie et de vie en plein air' },
    { icon: '👥', title: 'Équipes de randonnée', desc: 'Rejoignez des groupes et partagez vos aventures', link: '/equipes' },
  ];

  destinations = [
    { name: 'Ain Draham', sub: 'Forêt de chênes · Nord', tag: '32 emplacements', gradient: 'linear-gradient(160deg,#072010,#1a4d20)' },
    { name: 'Cap Serrat', sub: 'Bord de mer · Bizerte', tag: '18 emplacements', gradient: 'linear-gradient(135deg,#071a3d,#1f73a3)' },
    { name: 'Jebel Zaghouan', sub: 'Montagne · Centre', tag: '24 emplacements', gradient: 'linear-gradient(135deg,#3d1a00,#f29027)' },
    { name: 'Chott el-Jérid', sub: 'Désert · Sud · Expérience unique', tag: '12 emplacements', gradient: 'linear-gradient(135deg,#0d1f3d,#1b2a4a)' },
  ];

  // Images fallback Tunisie
  private readonly fallbackImages = [
    'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=400&h=240&fit=crop',
    'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=400&h=240&fit=crop',
    'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=400&h=240&fit=crop',
    'https://images.unsplash.com/photo-1501854140801-50d01698950b?w=400&h=240&fit=crop',
  ];

  constructor(private sortieService: SortieService, private router: Router) {}

  ngOnInit(): void {
    this.startSlideshow();
    this.loadRandonnees();
  }

  ngOnDestroy(): void { clearInterval(this.timer); }

  // ✅ Chargement dynamique depuis GET /api/sorties
  loadRandonnees(): void {
    this.loadingRando = true;
    this.sortieService.getAllSorties().subscribe({
      next: (data) => {
        // Prend les 4 prochaines sorties futures non pleines
        const now = new Date();
        const futures = data
          .filter(s => new Date(s.dateDebut) >= now && (s.placesDisponibles ?? 1) > 0)
          .slice(0, 4);
        // Si moins de 4 futures, complète avec les plus récentes
        if (futures.length < 4) {
          const autres = data.filter(s => !futures.includes(s)).slice(0, 4 - futures.length);
          this.randonneesRecommandees = [...futures, ...autres];
        } else {
          this.randonneesRecommandees = futures;
        }
        this.loadingRando = false;
      },
      error: () => {
        this.randonneesRecommandees = [];
        this.loadingRando = false;
      }
    });
  }

  startSlideshow(): void { this.timer = setInterval(() => this.nextSlide(), 5000); }
  goToSlide(i: number): void { this.currentSlide = i; clearInterval(this.timer); this.startSlideshow(); }
  nextSlide(): void { this.currentSlide = (this.currentSlide + 1) % this.slides.length; }
  get currentLabel(): string { return this.slides[this.currentSlide].label; }

  isConnected(): boolean { return !!localStorage.getItem('authToken'); }

  // ✅ CORRIGÉ : isAdminOrOrg retourne false pour USER → montre les bons boutons header
  isAdminOrOrg(): boolean {
    const r = localStorage.getItem('userRole') ?? '';
    return r === 'ADMIN' || r === 'ROLE_ADMIN' || r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR';
  }

  getDashboardLink(): string {
    const r = localStorage.getItem('userRole') ?? '';
    if (r === 'ADMIN' || r === 'ROLE_ADMIN') return '/admin/dashboard';
    if (r === 'ORGANISATEUR' || r === 'ROLE_ORGANISATEUR') return '/admin/organizer';
    return '/dashboard';
  }

  handleEventClick(event: Event): void {
    if (!this.isConnected()) {
      event.preventDefault();
      localStorage.setItem('redirect_after_login', '/events/list');
      this.router.navigate(['/login']);
    }
  }

  // ✅ Clic Participer depuis la home → redirect login si non connecté
  handleParticiper(sortieId: string, event: Event): void {
    if (!this.isConnected()) {
      event.preventDefault();
      localStorage.setItem('redirect_after_login', `/sorties/${sortieId}`);
      this.router.navigate(['/login']);
    } else {
      this.router.navigate(['/sorties', sortieId]);
    }
  }

  getSortieImage(s: SortieResponse): string {
    if (s.imageUrl?.trim()) return s.imageUrl;
    let h = 0;
    const seed = s.id ?? s.titre ?? '';
    for (let i = 0; i < seed.length; i++) h = (h * 31 + seed.charCodeAt(i)) >>> 0;
    return this.fallbackImages[h % this.fallbackImages.length];
  }

  getDiffClass(d: string): string {
    return { FACILE: 'diff-facile', MOYEN: 'diff-moyen', DIFFICILE: 'diff-difficile' }[d] || '';
  }

  getDiffLabel(d: string): string {
    return { FACILE: '🥾 Facile', MOYEN: '🧗 Modéré', DIFFICILE: '⛰️ Difficile' }[d] || d;
  }

  isPleine(s: SortieResponse): boolean {
    return (s.participantIds?.length ?? s.nombreParticipants ?? 0) >= (s.capaciteMax ?? 0);
  }

  formatDate(d: any): string {
    return new Date(d).toLocaleDateString('fr-FR', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  getSkeletons() { return [1,2,3,4]; }
}
