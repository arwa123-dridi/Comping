// src/app/planning-sr/planning-sr.component.ts
import { Component, OnInit, ChangeDetectionStrategy, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { PlanningService, SortiePlanifieeDTO } from '../services/planning.service';
import { SortieService } from '../services/sortie.service';

type ViewMode = 'calendrier' | 'liste';

interface CalendrierJour {
  date: Date;
  isCurrentMonth: boolean;
  isToday: boolean;
  sorties: SortiePlanifieeDTO[];
}

@Component({
  selector: 'app-planning-sr',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './planning-sr.component.html',
  styleUrls: ['./planning-sr.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlanningSrComponent implements OnInit {
  planning: SortiePlanifieeDTO[] = [];
  sortiesPassees: SortiePlanifieeDTO[] = [];
  sortiesAujourdhui: SortiePlanifieeDTO[] = [];
  sortiesFutures: SortiePlanifieeDTO[] = [];
  filteredMonth: SortiePlanifieeDTO[] = [];
  filteredAllPlanning: SortiePlanifieeDTO[] = [];
  loading = false;
  error: string | null = null;
  serverDown = false;
  viewMode: ViewMode = 'calendrier';
  selectedCard: SortiePlanifieeDTO | null = null;
  validating = false;
  successMsg: string | null = null;

  // Filtres
  searchTerm = '';
  filterDifficulte = '';

  // Calendrier
  currentMonth = new Date();
  calendarGrid: CalendrierJour[][] = [];
  readonly JOURS = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  readonly MOIS = ['Janvier','Février','Mars','Avril','Mai','Juin','Juillet','Août','Septembre','Octobre','Novembre','Décembre'];

  userId = '';
  userRole: string | null = null;
  isOrganizer = false;
  isAdmin = false;

  constructor(
    private planningService: PlanningService,
    private sortieService: SortieService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId') ?? '';
    this.userRole = localStorage.getItem('userRole');
    this.isOrganizer = this.userRole === 'ORGANISATEUR' || this.userRole === 'ROLE_ORGANISATEUR';
    this.isAdmin = this.userRole === 'ADMIN' || this.userRole === 'ROLE_ADMIN';
    if (this.userId) this.load();
  }

  load(): void {
    this.loading = true;
    this.error = null;
    this.serverDown = false;
    this.cdr.markForCheck();

    this.planningService.getPlanning(this.userId).subscribe({
      next: (data) => {
        this.planning = data;
        this.trierSorties(data);
        this.loading = false;
        this.refreshFilters();
        this.buildCalendar();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loading = false;
        if (err.status === 0) {
          this.serverDown = true;
          this.error = '❌ Serveur inaccessible. Vérifiez que le backend tourne sur le port 8087.';
        } else {
          this.serverDown = false;
          this.error = 'Impossible de charger le planning. Vérifiez que le serveur tourne.';
        }
        this.cdr.markForCheck();
      }
    });
  }

  refreshFilters(): void {
    let all = this.planning;
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      all = all.filter(p => p.sortie.titre.toLowerCase().includes(term));
    }
    if (this.filterDifficulte) {
      all = all.filter(p => p.sortie.difficulte === this.filterDifficulte);
    }
    this.filteredAllPlanning = all;

    const moisStr = `${this.currentMonth.getFullYear()}-${String(this.currentMonth.getMonth() + 1).padStart(2, '0')}`;
    let month = this.planning.filter(p => p.dateRecommandee?.startsWith(moisStr));
    if (this.searchTerm.trim()) {
      const term = this.searchTerm.toLowerCase();
      month = month.filter(p => p.sortie.titre.toLowerCase().includes(term));
    }
    if (this.filterDifficulte) {
      month = month.filter(p => p.sortie.difficulte === this.filterDifficulte);
    }
    this.filteredMonth = month;
    this.buildCalendar();
    this.cdr.markForCheck();
  }

  onSearchChange(): void {
    this.refreshFilters();
  }

  onDifficulteChange(): void {
    this.refreshFilters();
  }

  buildCalendar(): void {
    const year = this.currentMonth.getFullYear();
    const month = this.currentMonth.getMonth();

    const firstDay = new Date(year, month, 1);
    let startDay = firstDay.getDay() - 1;
    if (startDay < 0) startDay = 6;

    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const today = new Date();

    const jours: CalendrierJour[] = [];

    const prevMonth = new Date(year, month, 0);
    for (let i = startDay - 1; i >= 0; i--) {
      const d = new Date(year, month - 1, prevMonth.getDate() - i);
      jours.push({ date: d, isCurrentMonth: false, isToday: false, sorties: [] });
    }

    for (let d = 1; d <= daysInMonth; d++) {
      const date = new Date(year, month, d);
      const isToday = date.toDateString() === today.toDateString();
      const sorties = this.getSortiesDuJour(date);
      jours.push({ date, isCurrentMonth: true, isToday, sorties });
    }

    while (jours.length % 7 !== 0) {
      const last = jours[jours.length - 1].date;
      const next = new Date(last);
      next.setDate(next.getDate() + 1);
      jours.push({ date: next, isCurrentMonth: false, isToday: false, sorties: [] });
    }

    this.calendarGrid = [];
    for (let i = 0; i < jours.length; i += 7) {
      this.calendarGrid.push(jours.slice(i, i + 7));
    }
  }

  private getSortiesDuJour(date: Date): SortiePlanifieeDTO[] {
    const dateStr = this.toDateStr(date);
    return this.filteredMonth.filter(p => p.dateRecommandee === dateStr);
  }

  moisPrecedent(): void {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() - 1, 1);
    this.refreshFilters();
    this.cdr.markForCheck();
  }

  moisSuivant(): void {
    this.currentMonth = new Date(this.currentMonth.getFullYear(), this.currentMonth.getMonth() + 1, 1);
    this.refreshFilters();
    this.cdr.markForCheck();
  }

  selectCard(card: SortiePlanifieeDTO): void {
    this.selectedCard = card;
    this.successMsg = null;
    this.cdr.markForCheck();
  }

  closePanel(): void {
    this.selectedCard = null;
    this.cdr.markForCheck();
  }

confirmerSortie(sortieId: string): void {
  this.validating = true;
  this.error = null;
  this.serverDown = false;

  this.sortieService.inscrire(sortieId).subscribe({
    next: (res: any) => {
      this.validating = false;
      this.successMsg = res?.message || '✅ Inscription réussie !';
      this.selectedCard = null;
      this.load();  // recharge le planning
      this.cdr.markForCheck();
    },
    error: (err) => {
      this.validating = false;
      if (err.status === 409) {
        this.error = 'ℹ️ Vous êtes déjà inscrit à cette sortie.';
      } else if (err.status === 400) {
        this.error = '🔒 Plus de places disponibles.';
      } else if (err.status === 0) {
        this.serverDown = true;
        this.error = '❌ Serveur inaccessible. Vérifiez que le backend tourne.';
      } else {
        this.error = err.error?.message || "Erreur lors de l'inscription.";
      }
      this.cdr.markForCheck();
    }
  });
}

  // ========== ACTIONS ORGANISATEUR ==========
  editSortie(sortieId: string, event: Event): void {
    event.stopPropagation();
    if (this.isOrganizer || this.isAdmin) {
      this.router.navigate(['/admin/sorties/edit', sortieId]);
    }
  }

  deleteSortie(p: SortiePlanifieeDTO, event: Event): void {
    event.stopPropagation();
    if (!confirm(`Supprimer définitivement "${p.sortie.titre}" ?`)) return;
    this.sortieService.deleteSortie(String(p.sortie.id)).subscribe({
      next: () => {
        this.successMsg = `🗑️ "${p.sortie.titre}" supprimée.`;
        this.load();
      },
      error: () => alert('Erreur lors de la suppression.')
    });
  }

  // ✅ Correction : normalisation de la date pour le backend
  rescheduleSortie(p: SortiePlanifieeDTO, event: Event): void {
    event.stopPropagation();
    const currentDate = p.dateRecommandee ? p.dateRecommandee.slice(0, 16) : '';
    let newDate = prompt("Nouvelle date (YYYY-MM-DDTHH:MM)", currentDate);
    if (newDate && newDate !== currentDate) {
      // Si l'utilisateur n'a saisi que la date (YYYY-MM-DD), on ajoute l'heure par défaut
      if (/^\d{4}-\d{2}-\d{2}$/.test(newDate)) {
        newDate = newDate + "T00:00:00";
      }
      const updated = { ...p.sortie, dateDebut: newDate };
      this.sortieService.updateSortie(String(p.sortie.id), updated).subscribe({
        next: () => {
          this.successMsg = `📅 Date de "${p.sortie.titre}" mise à jour.`;
          this.load();
        },
        error: () => alert('Erreur lors du report.')
      });
    }
  }

  duplicateSortie(p: SortiePlanifieeDTO, event: Event): void {
    event.stopPropagation();
    this.router.navigate(['/admin/sorties/create'], { queryParams: { copyFrom: p.sortie.id } });
  }

  setView(v: ViewMode): void {
    this.viewMode = v;
    this.cdr.markForCheck();
  }

  scoreColor(score: number): string {
    if (score >= 80) return '#3da859';
    if (score >= 60) return '#f59e0b';
    return '#9ca3af';
  }

  scoreDash(score: number): string {
    const c = 2 * Math.PI * 18;
    return `${(score / 100) * c} ${c}`;
  }

  diffClass(d: string): string {
    if (d === 'DIFFICILE') return 'diff--hard';
    if (d === 'MOYEN') return 'diff--med';
    return 'diff--easy';
  }

  diffLabel(d: string): string {
    if (d === 'DIFFICILE') return 'Difficile';
    if (d === 'MOYEN') return 'Modéré';
    return 'Facile';
  }

  formatDate(ds: string): string {
    if (!ds) return '—';
    const date = new Date(ds);
    if (isNaN(date.getTime())) return '—';
    return date.toLocaleDateString('fr-FR', { weekday: 'long', day: '2-digit', month: 'long' });
  }

  monthLabel(): string {
    return `${this.MOIS[this.currentMonth.getMonth()]} ${this.currentMonth.getFullYear()}`;
  }

  private toDateStr(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  private trierSorties(data: SortiePlanifieeDTO[]): void {
    const aujourdhuiStr = this.toDateStr(new Date());
    this.sortiesPassees = data.filter(s => s.dateRecommandee && s.dateRecommandee < aujourdhuiStr);
    this.sortiesAujourdhui = data.filter(s => s.dateRecommandee === aujourdhuiStr);
    this.sortiesFutures = data.filter(s => s.dateRecommandee && s.dateRecommandee > aujourdhuiStr);
  }

  imageUrl(sortie: any): string {
    if (sortie?.imageUrl && sortie.imageUrl.trim() && sortie.imageUrl !== 'null') {
      return sortie.imageUrl.includes('res.cloudinary.com')
        ? sortie.imageUrl.replace('/upload/', '/upload/f_auto,q_auto,w_600/')
        : sortie.imageUrl;
    }
    const fallbacks: Record<string, string[]> = {
      FACILE:    ['https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=600&h=360&fit=crop&auto=format',
                  'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?w=600&h=360&fit=crop&auto=format'],
      MOYEN:     ['https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600&h=360&fit=crop&auto=format',
                  'https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=600&h=360&fit=crop&auto=format'],
      DIFFICILE: ['https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600&h=360&fit=crop&auto=format',
                  'https://images.unsplash.com/photo-1454496522488-7a8e488e8606?w=600&h=360&fit=crop&auto=format'],
    };
    const key = sortie?.difficulte && fallbacks[sortie.difficulte] ? sortie.difficulte : 'FACILE';
    const imgs = fallbacks[key];
    const id = sortie?.id || sortie?.titre || '';
    let hash = 0;
    for (let i = 0; i < id.length; i++) { hash = ((hash << 5) - hash) + id.charCodeAt(i); hash |= 0; }
    return imgs[Math.abs(hash) % imgs.length];
  }

  readonly skeletons = [1, 2, 3, 4, 5, 6];
}
