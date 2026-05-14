// src/app/planning-sr/planning-sr.component.ts
import {
  Component, OnInit,
  ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { PlanningService, SortiePlanifieeDTO } from '../services/planning.service';

type ViewMode = 'calendrier' | 'liste';

interface CalendrierJour {
  date:       Date;
  isCurrentMonth: boolean;
  isToday:    boolean;
  sorties:    SortiePlanifieeDTO[];
}

@Component({
  selector: 'app-planning-sr',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './planning-sr.component.html',
  styleUrls: ['./planning-sr.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class PlanningSrComponent implements OnInit {

  // ── State ─────────────────────────────────────────────
  planning:      SortiePlanifieeDTO[] = [];
  filteredMonth: SortiePlanifieeDTO[] = [];
  loading       = false;
  error:         string | null = null;
  serverDown     = false;          // ← Ajouté pour détecter panne serveur
  viewMode: ViewMode           = 'calendrier';
  selectedCard: SortiePlanifieeDTO | null = null;
  validating    = false;
  successMsg:   string | null = null;

  // Calendrier
  currentMonth   = new Date();
  calendarGrid:  CalendrierJour[][] = [];
  readonly JOURS = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim'];
  readonly MOIS  = [
    'Janvier','Février','Mars','Avril','Mai','Juin',
    'Juillet','Août','Septembre','Octobre','Novembre','Décembre'
  ];

  userId = '';

  constructor(
    private planningService: PlanningService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId') ?? '';
    if (this.userId) this.load();
  }

  // ── Chargement ────────────────────────────────────────

  load(): void {
    this.loading = true;
    this.error   = null;
    this.serverDown = false;
    this.cdr.markForCheck();

    this.planningService.getPlanning(this.userId).subscribe({
      next: (data) => {
        this.planning = data;
        this.loading  = false;
        this.buildCalendar();
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loading = false;
        // Détection panne serveur (statut 0 = réseau inaccessible)
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

  // ── Calendrier ────────────────────────────────────────

  buildCalendar(): void {
    const year  = this.currentMonth.getFullYear();
    const month = this.currentMonth.getMonth();

    // Premier jour du mois (lundi = 0)
    const firstDay = new Date(year, month, 1);
    let startDay   = firstDay.getDay() - 1;
    if (startDay < 0) startDay = 6;  // dimanche → 6

    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const today       = new Date();

    // Construire tableau plat de jours
    const jours: CalendrierJour[] = [];

    // Jours du mois précédent pour compléter la semaine
    const prevMonth = new Date(year, month, 0);
    for (let i = startDay - 1; i >= 0; i--) {
      const d = new Date(year, month - 1, prevMonth.getDate() - i);
      jours.push({ date: d, isCurrentMonth: false, isToday: false, sorties: [] });
    }

    // Jours du mois courant
    for (let d = 1; d <= daysInMonth; d++) {
      const date   = new Date(year, month, d);
      const isToday = date.toDateString() === today.toDateString();
      const sorties = this.getSortiesDuJour(date);
      jours.push({ date, isCurrentMonth: true, isToday, sorties });
    }

    // Compléter la dernière semaine
    while (jours.length % 7 !== 0) {
      const last = jours[jours.length - 1].date;
      const next = new Date(last);
      next.setDate(next.getDate() + 1);
      jours.push({ date: next, isCurrentMonth: false, isToday: false, sorties: [] });
    }

    // Découper en semaines
    this.calendarGrid = [];
    for (let i = 0; i < jours.length; i += 7) {
      this.calendarGrid.push(jours.slice(i, i + 7));
    }

    // Filtrer le planning du mois affiché
    const moisStr = `${year}-${String(month + 1).padStart(2, '0')}`;
    this.filteredMonth = this.planning.filter(p =>
      p.dateRecommandee?.startsWith(moisStr)
    );
  }

  private getSortiesDuJour(date: Date): SortiePlanifieeDTO[] {
    const dateStr = this.toDateStr(date);
    return this.planning.filter(p => p.dateRecommandee === dateStr);
  }

  moisPrecedent(): void {
    this.currentMonth = new Date(
      this.currentMonth.getFullYear(),
      this.currentMonth.getMonth() - 1,
      1
    );
    this.buildCalendar();
    this.cdr.markForCheck();
  }

  moisSuivant(): void {
    this.currentMonth = new Date(
      this.currentMonth.getFullYear(),
      this.currentMonth.getMonth() + 1,
      1
    );
    this.buildCalendar();
    this.cdr.markForCheck();
  }

  // ── Sélection + validation ────────────────────────────

  selectCard(card: SortiePlanifieeDTO): void {
    this.selectedCard = card;
    this.successMsg   = null;
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
    this.planningService.validerSortie(this.userId, sortieId).subscribe({
      next: (res) => {
        this.validating  = false;
        this.successMsg  = res.message;
        this.selectedCard = null;
        this.load();   // Rafraîchir
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.validating = false;
        if (err.status === 0) {
          this.serverDown = true;
          this.error = '❌ Serveur inaccessible. Vérifiez que le backend tourne.';
        } else {
          this.error = err.error?.message ?? "Erreur lors de l'inscription.";
        }
        this.cdr.markForCheck();
      }
    });
  }

  // ── Helpers UI ────────────────────────────────────────

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
    if (d === 'MOYEN')     return 'diff--med';
    return 'diff--easy';
  }

  diffLabel(d: string): string {
    if (d === 'DIFFICILE') return 'Difficile';
    if (d === 'MOYEN')     return 'Modéré';
    return 'Facile';
  }

  formatDate(ds: string): string {
    if (!ds) return '—';
    return new Date(ds).toLocaleDateString('fr-FR', {
      weekday: 'long', day: '2-digit', month: 'long'
    });
  }

  monthLabel(): string {
    return `${this.MOIS[this.currentMonth.getMonth()]} ${this.currentMonth.getFullYear()}`;
  }

  private toDateStr(d: Date): string {
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
  }

  imageUrl(sortie: any): string {
    return sortie?.imageUrl && sortie.imageUrl.trim()
      ? sortie.imageUrl
      : `https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=400&h=200&fit=crop&auto=format`;
  }

  readonly skeletons = [1, 2, 3, 4, 5, 6];
}