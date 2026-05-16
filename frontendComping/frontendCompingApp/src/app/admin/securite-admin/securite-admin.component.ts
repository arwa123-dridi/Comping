import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import { SecuriteService, Securite } from '../../services/securite.service';

@Component({
  selector: 'app-securite-admin',
  templateUrl: './securite-admin.component.html',
  styleUrls: ['./securite-admin.component.css'],
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule]
})
export class SecuriteAdminComponent implements OnInit, OnDestroy {
  securites: Securite[] = [];
  allSecurites: Securite[] = [];
  securiteForm: FormGroup;
  selectedSecurite: Securite | null = null;
  showForm = false;
  isEditing = false;
  loading = false;
  errorMessage = '';
  successMessage = '';
  filterStatus = 'TOUS';
  filterRisk = 'TOUS';
  
  statutOptions = ['PLANIFIEE', 'EN_COURS', 'COMPLETEE', 'ANNULEE'];
  niveauSecuriteOptions = ['BASSE', 'MOYENNE', 'HAUTE', 'CRITIQUE'];
  riskLevelOptions = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
  typeMesureOptions = ['SURVEILLANCE', 'CONTROLE_ACCES', 'PATROUILLE', 'INSPECTION', 'AUTRE'];
  monitoringTypeOptions = ['CCTV', 'PERSONNEL', 'SENSOR', 'MANUAL', 'AUTRE'];

  private destroy$ = new Subject<void>();

  constructor(
    private securiteService: SecuriteService,
    private fb: FormBuilder
  ) {
    this.securiteForm = this.createForm();
  }

  ngOnInit(): void {
    this.loadSecurites();
    this.securiteService.securites$
      .pipe(takeUntil(this.destroy$))
      .subscribe(securites => {
        this.allSecurites = securites;
        this.applyFilters();
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  createForm(): FormGroup {
    return this.fb.group({
      titre: ['', [Validators.required, Validators.minLength(3)]],
      description: ['', [Validators.required, Validators.minLength(10)]],
      siteCampingId: ['', Validators.required],
      typeMesure: ['SURVEILLANCE', Validators.required],
      niveauSecurite: ['HAUTE', Validators.required],
      zoneSecurisee: ['', Validators.required],
      responsableId: ['', Validators.required],
      monitoringType: ['CCTV', Validators.required],
      securityScore: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
      riskScore: [5, [Validators.required, Validators.min(1), Validators.max(10)]],
      budgetAlloue: [0],
      equipmentUsed: [''],
      monitoringLocations: [''],
      notes: ['']
    });
  }

  loadSecurites(): void {
    this.loading = true;
    this.securiteService.getAll()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.loading = false,
        error: (err) => {
          this.errorMessage = 'Erreur lors du chargement des mesures';
          this.loading = false;
        }
      });
  }

  applyFilters(): void {
    let filtered = [...this.allSecurites];

    if (this.filterStatus !== 'TOUS') {
      filtered = filtered.filter(s => s.statut === this.filterStatus);
    }

    if (this.filterRisk !== 'TOUS') {
      filtered = filtered.filter(s => s.riskLevel === this.filterRisk);
    }

    this.securites = filtered;
  }

  openForm(): void {
    this.showForm = true;
    this.isEditing = false;
    this.securiteForm.reset();
  }

  editSecurite(securite: Securite): void {
    this.isEditing = true;
    this.selectedSecurite = securite;
    this.securiteForm.patchValue(securite);
    this.showForm = true;
  }

  closeForm(): void {
    this.showForm = false;
    this.securiteForm.reset();
    this.selectedSecurite = null;
  }

  submitForm(): void {
    if (!this.securiteForm.valid) {
      this.errorMessage = 'Formulaire invalide';
      return;
    }

    this.loading = true;

    const formData = this.securiteForm.value;
    if (formData.equipmentUsed && typeof formData.equipmentUsed === 'string') {
      formData.equipmentUsed = formData.equipmentUsed.split(',').map((e: string) => e.trim());
    }
    if (formData.monitoringLocations && typeof formData.monitoringLocations === 'string') {
      formData.monitoringLocations = formData.monitoringLocations.split(',').map((l: string) => l.trim());
    }

    if (this.isEditing && this.selectedSecurite?.id) {
      this.securiteService.update(this.selectedSecurite.id, formData)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.successMessage = 'Mesure mise à jour avec succès';
            this.closeForm();
            this.loading = false;
          },
          error: (err) => {
            this.errorMessage = 'Erreur lors de la mise à jour';
            this.loading = false;
          }
        });
    } else {
      this.securiteService.creer(formData)
        .pipe(takeUntil(this.destroy$))
        .subscribe({
          next: () => {
            this.successMessage = 'Mesure créée avec succès';
            this.closeForm();
            this.loading = false;
          },
          error: (err) => {
            this.errorMessage = 'Erreur lors de la création';
            this.loading = false;
          }
        });
    }

    setTimeout(() => this.successMessage = '', 3000);
  }

  startMonitoring(securite: Securite): void {
    this.securiteService.updateStatut(securite.id!, 'EN_COURS')
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.successMessage = 'Surveillance démarrée',
        error: (err) => this.errorMessage = 'Erreur lors du démarrage'
      });
  }

  completeMonitoring(securite: Securite): void {
    this.securiteService.completeMonitoring(securite.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.successMessage = 'Surveillance complétée',
        error: (err) => this.errorMessage = 'Erreur lors de la complétion'
      });
  }

  delete(securite: Securite): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette mesure?')) return;
    this.securiteService.delete(securite.id!)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => this.successMessage = 'Mesure supprimée',
        error: (err) => this.errorMessage = 'Erreur lors de la suppression'
      });
  }

  getStatusColor(statut?: string): string {
    switch (statut) {
      case 'PLANIFIEE': return 'warning';
      case 'EN_COURS': return 'info';
      case 'COMPLETEE': return 'success';
      case 'ANNULEE': return 'danger';
      default: return 'secondary';
    }
  }

  getRiskColor(risque?: string): string {
    switch (risque) {
      case 'CRITICAL': return 'danger';
      case 'HIGH': return 'warning';
      case 'MEDIUM': return 'info';
      case 'LOW': return 'success';
      default: return 'secondary';
    }
  }
}
