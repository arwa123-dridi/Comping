// src/app/checklist-ia/checklist-ia.component.ts
import {
  Component, OnInit, OnChanges, Input,
  SimpleChanges, ChangeDetectionStrategy, ChangeDetectorRef
} from '@angular/core';
import { CommonModule }  from '@angular/common';
import { FormsModule }   from '@angular/forms';
import {
  ChecklistService,
  ChecklistRequest,
  ChecklistResponse,
  WeatherDTO
} from '../services/checklist.service';
import { SortieService } from '../services/sortie.service';

// ─── Villes tunisiennes ───────────────────────────────────────────────────────

export const TUNISIAN_CITIES = [
  { value: 'Tunis',      label: 'Tunis',      region: 'Grand Tunis'   },
  { value: 'Zaghouan',   label: 'Zaghouan',   region: 'Centre-Nord'   },
  { value: 'Ain Draham', label: 'Aïn Draham', region: 'Nord-Ouest'    },
  { value: 'Tabarka',    label: 'Tabarka',     region: 'Nord-Ouest'    },
  { value: 'Dougga',     label: 'Dougga',      region: 'Centre-Nord'   },
  { value: 'Beja',       label: 'Béja',        region: 'Nord-Ouest'    },
  { value: 'Jendouba',   label: 'Jendouba',    region: 'Nord-Ouest'    },
  { value: 'Nabeul',     label: 'Nabeul',      region: 'Cap Bon'       },
  { value: 'Bizerte',    label: 'Bizerte',     region: 'Nord'          },
  { value: 'Siliana',    label: 'Siliana',     region: 'Centre-Nord'   },
  { value: 'Kasserine',  label: 'Kasserine',   region: 'Centre-Ouest'  },
  { value: 'Sbeitla',    label: 'Sbeitla',     region: 'Centre-Ouest'  },
  { value: 'Matmata',    label: 'Matmata',     region: 'Sud'           },
];

// ─── Méta alertes ────────────────────────────────────────────────────────────

const ALERT_KEYS: Record<string, string> = {
  VERT: 'VERT', JAUNE: 'JAUNE', ORANGE: 'ORANGE', ROUGE: 'ROUGE'
};

// ─── Icônes et libellés équipements ──────────────────────────────────────────
// Aucune mention d'outil ou de modèle — noms métier uniquement

export const ITEM_ICONS: Record<string, string> = {
  vetement_chaud:             '🧥',
  veste_chaude_imperm:        '🧥',
  protection_solaire:         '🧴',
  protection_solaire_extreme: '☀️',
  impermable_light:           '🌂',
  impermable_complet:         '☔',
  coupe_vent:                 '💨',
  coupe_vent_renforce:        '💨',
  equipement_securite:        '🆘',
  alerte_canicule:            '🌡️',
  alerte_vent_rouge:          '⚠️',
  standard:                   '🎒',
};

export const ITEM_LABELS: Record<string, string> = {
  vetement_chaud:             'Vêtement chaud',
  veste_chaude_imperm:        'Veste imperméable & chaude',
  protection_solaire:         'Protection solaire',
  protection_solaire_extreme: 'Protection solaire renforcée',
  impermable_light:           'Imperméable léger',
  impermable_complet:         'Imperméable complet',
  coupe_vent:                 'Coupe-vent',
  coupe_vent_renforce:        'Coupe-vent renforcé',
  equipement_securite:        'Équipement de sécurité',
  alerte_canicule:            'Alerte canicule',
  alerte_vent_rouge:          'Alerte vent fort',
  standard:                   'Équipement de randonnée standard',
};

type Mode = 'auto' | 'manual';

// ─── Composant ────────────────────────────────────────────────────────────────

@Component({
  selector: 'app-checklist-ia',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checklist-ia.component.html',
  styleUrls: ['./checklist-ia.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ChecklistIaComponent implements OnInit, OnChanges {

  // @Input — préremplissage depuis sortie-detail
  @Input() sortieVille?:      string;
  @Input() sortieDateDebut?:  string;
  @Input() sortieDifficulte?: number;

  // Données
  mode: Mode = 'auto';
  selectedCity  = '';
  villeDepart:  string = '';   // lieu de rassemblement
  villeArrivee: string | null = null; // ville visitée (météo principale)
  selectedDate  = '';
  difficulte    = 2;
  cities        = TUNISIAN_CITIES;
  userId: string | null = null;
  sorties_participees: any[] = [];
  sortie_selectionnee: any = null;

  manualReq: ChecklistRequest = {
    temperature: 24, precipitation: 0, wind_speed: 10, humidity: 60, difficulte: 2
  };

  // Météo
  weather:        WeatherDTO | null    = null;
  weatherLoading  = false;
  weatherError:   string | null        = null;

  // Résultat
  response:       ChecklistResponse | null = null;
  loading         = false;
  errorMessage:   string | null            = null;
  confidenceDisplay = 0;

  // Expose au template (sans trace d'outil tiers)
  readonly itemIcons  = ITEM_ICONS;
  readonly itemLabels = ITEM_LABELS;

  constructor(
    private checklistService: ChecklistService,
    private sortieService: SortieService,
    private cdr: ChangeDetectorRef
  ) {}

  // ── Lifecycle ─────────────────────────────────────────────────────────────

  ngOnInit(): void {
    this.userId = localStorage.getItem('userId');

    // Charger les sorties passées de l'utilisateur
    if (this.userId) {
      this.loadSortiesParticipees();
    }

    if (this.sortieVille)      { this.selectedCity  = this.sortieVille;               this.mode = 'auto'; }
    if (this.sortieDateDebut)  { this.selectedDate  = this.sortieDateDebut.slice(0, 10); }
    if (this.sortieDifficulte) { this.difficulte    = this.sortieDifficulte; }

    if (!this.selectedDate) {
      this.selectedDate = new Date().toISOString().split('T')[0];
    }

    if (this.selectedCity && this.selectedDate) {
      this.fetchWeatherPreview();
    }
  }

  // ── Charger sorties passées de l'utilisateur
  loadSortiesParticipees(): void {
    this.sortieService.getAllSorties().subscribe({
      next: (sorties) => {
        this.sorties_participees = sorties.filter(s => {
          const isParticipant = (s.participantIds || []).map(String).includes(String(this.userId));
          const isPassed = new Date(s.dateDebut) < new Date();
          return isParticipant && isPassed;
        });
        this.cdr.markForCheck();
      },
      error: () => {
        this.sorties_participees = [];
        this.cdr.markForCheck();
      }
    });
  }

  selectSortieForChecklist(sortie: any): void {
    this.sortie_selectionnee = sortie;
    this.selectedCity = sortie.lieuDepart;
    this.selectedDate = new Date(sortie.dateDebut).toISOString().split('T')[0];
    this.mode = 'auto';
    this.fetchWeatherPreview();
    this.cdr.markForCheck();
  }

  ngOnChanges(ch: SimpleChanges): void {
    if (ch['sortieVille']?.currentValue)     { this.selectedCity = ch['sortieVille'].currentValue; this.fetchWeatherPreview(); }
    if (ch['sortieDateDebut']?.currentValue) { this.selectedDate = ch['sortieDateDebut'].currentValue?.slice(0,10); this.fetchWeatherPreview(); }
    if (ch['sortieDifficulte']?.currentValue){ this.difficulte   = ch['sortieDifficulte'].currentValue; }
  }

  // ── Mode ──────────────────────────────────────────────────────────────────

  setMode(m: Mode): void {
    this.mode        = m;
    this.response    = null;
    this.errorMessage = null;
    this.cdr.markForCheck();
  }

  // ── Météo ─────────────────────────────────────────────────────────────────

  onCityOrDateChange(): void {
    if (this.selectedCity && this.selectedDate) this.fetchWeatherPreview();
  }

  fetchWeatherPreview(): void {
    if (!this.selectedCity || !this.selectedDate) return;
    this.weatherLoading = true;
    this.weatherError   = null;
    this.weather        = null;
    this.cdr.markForCheck();

    this.checklistService.getWeather(this.selectedCity, this.selectedDate).subscribe({
      next: (w) => {
        this.weather        = w;
        this.weatherLoading = false;
        this.manualReq = {
          temperature:   w.temperature,
          precipitation: w.precipitation,
          wind_speed:    w.windSpeed,
          humidity:      w.humidity,
          difficulte:    this.difficulte
        };
        this.cdr.markForCheck();
      },
      error: () => {
        this.weatherLoading = false;
        this.weatherError   = 'Météo indisponible pour cette ville / date.';
        this.cdr.markForCheck();
      }
    });
  }

  // ── Génération ────────────────────────────────────────────────────────────

  canGenerate(): boolean {
    return this.mode === 'auto' ? (!!this.selectedCity && !!this.selectedDate) : true;
  }

  generate(): void {
    this.loading      = true;
    this.response     = null;
    this.errorMessage = null;
    this.cdr.markForCheck();

    const obs$ = this.mode === 'auto'
      ? this.checklistService.recommandationAuto(this.selectedCity, this.selectedDate, this.difficulte)
      : this.checklistService.predict({ ...this.manualReq, difficulte: this.difficulte });

    obs$.subscribe({
      next: (res) => {
        this.loading = false;
        if (res.success) {
          this.response = res;
          this.animateConfidence(res.confidence);
        } else {
          this.errorMessage = res.error ?? 'Analyse impossible.';
        }
        this.cdr.markForCheck();
      },
      error: (err) => {
        this.loading      = false;
        this.errorMessage = err.status === 503
          ? 'Serveur d\'analyse indisponible — vérifiez que le service tourne sur le port 5000.'
          : 'Connexion au serveur impossible (port 8087).';
        this.cdr.markForCheck();
      }
    });
  }

  private animateConfidence(target: number): void {
    this.confidenceDisplay = 0;
    const steps = 40;
    const step  = target / steps;
    let cur     = 0;
    const iv = setInterval(() => {
      cur = Math.min(cur + step, target);
      this.confidenceDisplay = cur;
      this.cdr.markForCheck();
      if (cur >= target) clearInterval(iv);
    }, 600 / steps);
  }

  // ── Helpers template ──────────────────────────────────────────────────────

  getAlertKey(lvl: string): string {
    if (!lvl) return 'VERT';
    if (lvl.includes('ROUGE'))  return 'ROUGE';
    if (lvl.includes('ORANGE')) return 'ORANGE';
    if (lvl.includes('JAUNE'))  return 'JAUNE';
    return 'VERT';
  }

  diffLabel(d: number): string {
    return ['', 'Très facile', 'Facile', 'Modéré', 'Difficile', 'Extrême'][d] ?? '';
  }

  weatherIcon(t: number, p: number, w: number): string {
    if (p >= 15) return '🌧️';
    if (p >= 5)  return '🌦️';
    if (w >= 55) return '🌪️';
    if (w >= 35) return '💨';
    if (t >= 35) return '🌞';
    if (t <= 5)  return '🌨️';
    return '⛅';
  }

  confidencePct(): number {
    return Math.round(this.confidenceDisplay * 100);
  }

  /** Résout le label même si le backend retourne un nom légèrement différent */
  resolveLabel(key: string): string {
    if (!key) return 'Équipement standard';
    if (ITEM_LABELS[key]) return ITEM_LABELS[key];
    const alt = key.replace(/-/g, '_');
    if (ITEM_LABELS[alt]) return ITEM_LABELS[alt];
    return key.replace(/[_-]/g, ' ').replace(/^\w/, c => c.toUpperCase());
  }

  resolveIcon(key: string): string {
    if (!key) return '🎒';
    if (ITEM_ICONS[key]) return ITEM_ICONS[key];
    return ITEM_ICONS[key.replace(/-/g, '_')] ?? '🎒';
  }

  /** Génère une liste d'équipements détaillée selon les conditions météo réelles */
  getWeatherEquipment(): { icon: string; label: string; reason: string; required: boolean }[] {
    const w = this.weather;
    const m = this.manualReq;
    const t   = w ? w.temperature   : m.temperature;
    const p   = w ? w.precipitation : m.precipitation;
    const wnd = w ? (w as any).windSpeed ?? (w as any).wind_speed ?? 0 : m.wind_speed;
    const h   = w ? w.humidity      : m.humidity;
    const d   = this.difficulte;

    const eq: { icon: string; label: string; reason: string; required: boolean }[] = [];

    // Eau
    const L = d >= 4 ? 3 : d >= 3 ? 2 : 1.5;
    eq.push({ icon: '💧', label: `Eau : ${L} L minimum`,             reason: `Effort niveau ${d}/5`,              required: true });

    // Solaire
    if (t >= 20 && p < 5) {
      const spf = t >= 32 ? 'SPF 50+' : 'SPF 30+';
      eq.push({ icon: '🧴', label: `Crème solaire ${spf}`,            reason: `${t}°C — exposition solaire`,        required: t >= 28 });
      eq.push({ icon: '🕶️', label: 'Lunettes de soleil',              reason: 'Protection UV',                     required: t >= 28 });
      if (t >= 28)
        eq.push({ icon: '🧢', label: 'Chapeau à large bord',           reason: `${t}°C — chaleur intense`,          required: true });
    }

    // Pluie légère
    if (p >= 5 && p < 15) {
      eq.push({ icon: '🌂', label: 'Cape de pluie légère',            reason: `Précipitations : ${p} mm`,          required: true });
      eq.push({ icon: '👟', label: 'Chaussures imperméables',          reason: 'Terrain potentiellement glissant',  required: false });
    }
    // Forte pluie
    if (p >= 15) {
      eq.push({ icon: '☔', label: 'Imperméable intégral',             reason: `Fortes pluies : ${p} mm`,          required: true });
      eq.push({ icon: '🥾', label: 'Guêtres de randonnée',             reason: 'Protection terrain trempé',        required: true });
      eq.push({ icon: '🎒', label: 'Housse de sac imperméable',        reason: 'Protection matériel',              required: true });
    }

    // Vent
    if (wnd >= 25 && wnd < 45)
      eq.push({ icon: '💨', label: 'Coupe-vent',                      reason: `Vent : ${wnd} km/h`,               required: true });
    if (wnd >= 45) {
      eq.push({ icon: '🧥', label: 'Coupe-vent renforcé',              reason: `Vent fort : ${wnd} km/h`,          required: true });
      if (d >= 3)
        eq.push({ icon: '🪢', label: 'Bâtons de marche',               reason: 'Stabilité face au vent',           required: true });
    }

    // Froid
    if (t < 15)
      eq.push({ icon: '🧣', label: 'Sous-couche thermique',            reason: `${t}°C — risque de refroidissement`, required: true });
    if (t < 8) {
      eq.push({ icon: '🧤', label: 'Gants de randonnée',               reason: `${t}°C — froid extrême`,           required: true });
      eq.push({ icon: '🎿', label: 'Bonnet',                           reason: `Perte de chaleur crânienne`,       required: true });
    }

    // Humidité élevée + chaleur
    if (h >= 75 && t >= 25)
      eq.push({ icon: '🩹', label: 'Anti-frottements / talc',          reason: `Humidité ${h}% + ${t}°C`,         required: false });

    // Sécurité selon difficulté
    eq.push({ icon: '🩺', label: 'Trousse premiers secours',           reason: 'Sécurité — obligatoire',           required: true });
    if (d >= 3)
      eq.push({ icon: '📍', label: 'GPS ou carte topographique',        reason: `Niveau ${this.diffLabel(d)}`,      required: d >= 4 });
    if (d >= 4)
      eq.push({ icon: '📡', label: 'Téléphone chargé + batterie externe', reason: 'Terrain isolé',                required: true });

    // Alimentation
    const snack = d >= 4 ? 'Barres énergie × 4 + repas complet'
                : d >= 3 ? 'Barres énergie × 2 + snacks'
                :           'Snacks légers';
    eq.push({ icon: '🍫', label: snack,                                reason: `Apport calorique niveau ${d}/5`,   required: true });

    return eq;
  }

  // ── Export PDF ────────────────────────────────────────────────────────────

  async exportPDF(): Promise<void> {
    if (!this.response) return;
    const { jsPDF } = await import('jspdf');

    const doc  = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    const G: [number,number,number]  = [45, 168, 89];
    const N: [number,number,number]  = [25, 34, 53];
    const MU: [number,number,number] = [107, 114, 128];
    const W: [number,number,number]  = [255, 255, 255];

    // En-tête vert forêt
    doc.setFillColor(26, 58, 10);
    doc.rect(0, 0, 210, 30, 'F');
    doc.setTextColor(...W);
    doc.setFontSize(20); doc.setFont('helvetica','bold');
    doc.text('Campino — Checklist Sécurité', 14, 14);
    doc.setFontSize(9); doc.setFont('helvetica','normal');
    doc.text(`Générée le ${new Date().toLocaleDateString('fr-FR', { dateStyle: 'long' })}`, 14, 24);

    let y = 42;

    // Sortie info
    if (this.mode === 'auto' && this.selectedCity) {
      doc.setTextColor(...N);
      doc.setFontSize(11); doc.setFont('helvetica','bold');
      doc.text(`Sortie : ${this.selectedCity}  ·  ${new Date(this.selectedDate).toLocaleDateString('fr-FR',{dateStyle:'long'})}`, 14, y);
      y += 9;
    }

    // Météo
    if (this.weather) {
      doc.setFillColor(237, 247, 241);
      doc.roundedRect(14, y, 182, 20, 3, 3, 'F');
      doc.setTextColor(...N); doc.setFontSize(9); doc.setFont('helvetica','bold');
      doc.text('Conditions météo', 18, y + 7);
      doc.setFont('helvetica','normal'); doc.setTextColor(...MU);
      doc.text([
        `Température : ${this.weather.temperature}°C`,
        `Précipitations : ${this.weather.precipitation} mm`,
        `Vent : ${this.weather.windSpeed} km/h`,
        `Humidité : ${this.weather.humidity}%`,
      ].join('   ·   '), 18, y + 14);
      y += 28;
    }

    // Alerte
    const ak  = this.getAlertKey(this.response.alert_level);
    const bgMap: Record<string, [number,number,number]> = {
      VERT: [220,252,231], JAUNE: [254,249,195], ORANGE: [255,237,213], ROUGE: [239,68,68]
    };
    const fgMap: Record<string, [number,number,number]> = {
      VERT: [20,83,45], JAUNE: [113,63,18], ORANGE: [124,45,18], ROUGE: [255,255,255]
    };
    doc.setFillColor(...(bgMap[ak] ?? bgMap['VERT']));
    doc.roundedRect(14, y, 182, 13, 3, 3, 'F');
    doc.setTextColor(...(fgMap[ak] ?? fgMap['VERT']));
    doc.setFontSize(11); doc.setFont('helvetica','bold');
    doc.text(this.response.alert_level, 105, y + 9, { align: 'center' });
    y += 21;

    // Équipement
    doc.setTextColor(...N); doc.setFontSize(13); doc.setFont('helvetica','bold');
    doc.text('Équipement recommandé', 14, y);  y += 8;
    doc.setFillColor(...G);
    doc.roundedRect(14, y, 182, 12, 3, 3, 'F');
    doc.setTextColor(...W); doc.setFontSize(11);
    const eqLabel = ITEM_LABELS[this.response.checklist_item] ?? this.response.checklist_item;
    doc.text(eqLabel, 105, y + 8, { align: 'center' });
    y += 20;

    // Fiabilité
    const pct = Math.round(this.response.confidence * 100);
    doc.setTextColor(...N); doc.setFontSize(10); doc.setFont('helvetica','normal');
    doc.text(`Fiabilité : ${pct}%`, 14, y);
    doc.setFillColor(228, 228, 228); doc.roundedRect(55, y-5, 100, 6, 2, 2, 'F');
    doc.setFillColor(...G);          doc.roundedRect(55, y-5, pct, 6, 2, 2, 'F');
    y += 12;

    // Détails
    doc.setFontSize(9); doc.setTextColor(...MU);
    const dl = doc.splitTextToSize(this.response.details, 180);
    doc.text(dl, 14, y); y += dl.length * 5 + 6;

    // Difficulté
    doc.setTextColor(...N); doc.setFontSize(10); doc.setFont('helvetica','bold');
    doc.text(`Niveau : ${this.diffLabel(this.difficulte)} (${this.difficulte}/5)`, 14, y);
    y += 10;

    // Recommandations
    if (this.response.recommendations?.length) {
      doc.setFillColor(244, 241, 236);
      doc.roundedRect(14, y, 182, 8 + this.response.recommendations.length * 8, 3, 3, 'F');
      y += 6;
      doc.setFontSize(10); doc.setFont('helvetica','bold'); doc.setTextColor(...N);
      doc.text('À emporter', 18, y); y += 7;
      doc.setFont('helvetica','normal'); doc.setFontSize(9); doc.setTextColor(...MU);
      for (const r of this.response.recommendations) {
        doc.text(`• ${r}`, 20, y); y += 7;
      }
    }

    // Pied de page
    doc.setFillColor(26, 58, 10); doc.rect(0, 282, 210, 15, 'F');
    doc.setTextColor(...W); doc.setFontSize(8); doc.setFont('helvetica','normal');
    doc.text('Campino — Randonnée en Tunisie', 105, 290, { align: 'center' });

    doc.save(`checklist-campino-${this.selectedCity || 'manual'}-${this.selectedDate}.pdf`);
  }
}
