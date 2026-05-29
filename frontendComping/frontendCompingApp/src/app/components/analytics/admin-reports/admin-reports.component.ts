import { Component, Input } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SortieResponse } from '../../../models/sortie.model';
import { EquipeResponse } from '../../../models/equipe.model';

@Component({
  selector: 'app-admin-reports',
  standalone: true,
  imports: [CommonModule],
  template: `
    <section class="reports" id="reports">
      <div class="reports-header">
        <div>
          <h2>🧾 Rapports</h2>
          <p>Résumé analytics · mise en page imprimable</p>
        </div>
        <div class="reports-actions">
          <button class="btn-print" type="button" (click)="print()">🖨️ Imprimer</button>
        </div>
      </div>

      <div class="report-kpis">
        <div class="rkpi">
          <div class="rkpi-val">{{ stats.totalSorties }}</div>
          <div class="rkpi-lbl">Randonnées</div>
        </div>
        <div class="rkpi">
          <div class="rkpi-val">{{ stats.totalParticipants }}</div>
          <div class="rkpi-lbl">Participants</div>
        </div>
        <div class="rkpi">
          <div class="rkpi-val">{{ stats.totalEquipes }}</div>
          <div class="rkpi-lbl">Équipes</div>
        </div>
        <div class="rkpi">
          <div class="rkpi-val">{{ stats.statusDone }}</div>
          <div class="rkpi-lbl">Terminées</div>
        </div>
      </div>

      <div class="reports-layout">
        <div class="report-card">
          <h3>📅 Répartition par difficulté</h3>
          <div class="diff-lines">
            <div class="diff-line" *ngFor="let row of diffRows">
              <span class="diff-name">{{ row.label }}</span>
              <div class="diff-bar"><div class="diff-fill" [style.width.%]="row.pct"></div></div>
              <span class="diff-meta">{{ row.value }}</span>
            </div>
          </div>
        </div>

        <div class="report-card">
          <h3>🏔️ Top sorties</h3>
          <table class="report-table">
            <thead>
              <tr><th>Sortie</th><th>Date</th><th>Participants</th><th>Statut</th></tr>
            </thead>
            <tbody>
              <tr *ngFor="let s of topSorties">
                <td>{{ s.titre }}</td>
                <td>{{ s.dateDebut | date:'dd/MM/yy' }}</td>
                <td>{{ s.nombreParticipants ?? 0 }}</td>
                <td>{{ getSortieLabel(s) }}</td>
              </tr>
              <tr *ngIf="topSorties.length === 0"><td colspan="4">Aucune donnée</td></tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="report-footer">
        <small>Généré automatiquement · Comping</small>
      </div>
    </section>
  `,
  styles: [`
    .reports{margin-top:16px;background:#fff;border:1px solid rgba(0,0,0,0.08);border-radius:14px;padding:18px;}
    .reports-header{display:flex;justify-content:space-between;align-items:flex-start;gap:12px;flex-wrap:wrap;}
    .reports-header h2{font-size:18px;margin:0 0 6px 0;}
    .reports-header p{font-size:12.5px;color:#6b6b66;margin:0;}
    .reports-actions{display:flex;gap:10px;align-items:center;}
    .btn-print{border:1px solid rgba(0,0,0,0.08);background:#f1f0ec;border-radius:10px;padding:8px 12px;font-weight:700;cursor:pointer;}
    .report-kpis{display:grid;grid-template-columns:repeat(4,minmax(180px,1fr));gap:12px;margin-top:14px;}
    .rkpi{border:1px solid rgba(0,0,0,0.08);background:#fff;border-radius:12px;padding:14px;}
    .rkpi-val{font-size:22px;font-weight:900;color:#1a1a18;}
    .rkpi-lbl{font-size:12px;color:#6b6b66;margin-top:4px;}

    .reports-layout{display:grid;grid-template-columns:1fr 1.2fr;gap:14px;margin-top:14px;}
    .report-card{border:1px solid rgba(0,0,0,0.08);border-radius:12px;padding:14px;}
    .report-card h3{font-size:14px;margin-bottom:10px;}

    .diff-line{display:flex;align-items:center;gap:10px;margin:10px 0;}
    .diff-name{width:120px;font-size:13px;}
    .diff-bar{flex:1;height:8px;background:#f1f0ec;border-radius:10px;overflow:hidden;}
    .diff-fill{height:100%;background:#185FA5;}
    .diff-meta{width:60px;text-align:right;font-size:13px;color:#6b6b66;}

    .report-table{width:100%;border-collapse:collapse;font-size:13px;}
    .report-table th{font-size:11.5px;text-transform:uppercase;color:#6b6b66;text-align:left;border-bottom:1px solid rgba(0,0,0,0.08);padding:10px 0;}
    .report-table td{padding:10px 0;border-bottom:1px solid rgba(0,0,0,0.06);}

    .report-footer{margin-top:14px;color:#6b6b66;}

    @media (max-width: 900px){
      .report-kpis{grid-template-columns:repeat(2,minmax(180px,1fr));}
      .reports-layout{grid-template-columns:1fr;}
    }

    @media print{
      .reports-actions{display:none;}
      .reports{border:none;box-shadow:none;}
      body{background:#fff;}
      @page{margin:12mm;}
    }
  `]
})
export class AdminReportsComponent {
  @Input() sorties: SortieResponse[] = [];
  @Input() equipes: EquipeResponse[] = [];

  get stats() {
    const totalSorties = this.sorties.length;
    const totalParticipants = this.sorties.reduce((sum, s) => sum + (s.nombreParticipants ?? 0), 0);
    const totalEquipes = this.equipes.length;

    const statusDone = this.sorties.filter(s => this.getSortieStatus(s) === 'TERMINEE').length;

    return { totalSorties, totalParticipants, totalEquipes, statusDone };
  }

  get diffRows() {
    const diffs: Array<{ key: 'FACILE' | 'MOYEN' | 'DIFFICILE'; label: string }> = [
      { key: 'FACILE', label: 'Facile' },
      { key: 'MOYEN', label: 'Modéré' },
      { key: 'DIFFICILE', label: 'Difficile' },
    ];
    const max = Math.max(1, ...diffs.map(d => this.sorties.filter(s => s.difficulte === d.key).length));
    return diffs.map(d => {
      const value = this.sorties.filter(s => s.difficulte === d.key).length;
      return { label: d.label, value, pct: (value / max) * 100 };
    });
  }

  get topSorties(): SortieResponse[] {
    return [...this.sorties]
      .sort((a, b) => (b.nombreParticipants ?? 0) - (a.nombreParticipants ?? 0))
      .slice(0, 5);
  }

  getSortieStatus(s: SortieResponse): 'PLANIFIEE' | 'EN_COURS' | 'TERMINEE' {
    const today = new Date();
    const debut = new Date(s.dateDebut);
    const fin = s.dateFin ? new Date(s.dateFin) : null;
    if (debut > today) return 'PLANIFIEE';
    if (fin && fin < today) return 'TERMINEE';
    if (debut <= today && (!fin || fin >= today)) return 'EN_COURS';
    return 'PLANIFIEE';
  }

  getSortieLabel(s: SortieResponse): string {
    const st = this.getSortieStatus(s);
    if (st === 'PLANIFIEE') return 'Planifiée';
    if (st === 'EN_COURS') return 'En cours';
    return 'Terminée';
  }

  print(): void {
    window.print();
  }
}

