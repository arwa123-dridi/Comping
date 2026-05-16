import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, ReactiveFormsModule, Validators, FormGroup, FormControl } from '@angular/forms';
import { IncidentResponse, IncidentService } from '../../services/incident.service';

@Component({
  selector: 'app-incidents-admin',
  standalone: true,
  imports: [CommonModule, FormsModule, ReactiveFormsModule],
  templateUrl: './incidents-admin.component.html',
  styleUrl: './incidents-admin.component.css'
})
export class IncidentsAdminComponent implements OnInit {
  incidents: IncidentResponse[] = [];
  loading = false;
  showForm = false;
  editingIncident: IncidentResponse | null = null;
  statutFilter = '';
  
  readonly types = ['ACCIDENT', 'VANDALISME', 'EQUIPEMENT', 'SECURITE', 'PERSONNEL', 'AUTRE'];
  readonly statuts = ['OUVERT', 'EN_COURS', 'RESOLU', 'FERME'];

  incidentForm: FormGroup;

  constructor(
    private incidentService: IncidentService
  ) {
    console.log('IncidentsAdminComponent initialized - v3 - NO CREATEFORM');
    this.incidentForm = new FormGroup({
      type: new FormControl('', Validators.required),
      statut: new FormControl('OUVERT', Validators.required),
      descrition: new FormControl('', [Validators.required, Validators.minLength(10)]),
      resolu: new FormControl(false)
    });
  }

  ngOnInit(): void {
    this.loadIncidents();
  }

  loadIncidents(): void {
    this.loading = true;
    this.incidentService.getIncidents().subscribe({
      next: (data: IncidentResponse[]) => {
        this.incidents = this.statutFilter
          ? data.filter(i => i.statut === this.statutFilter)
          : data;
        this.loading = false;
      },
      error: () => {
        this.incidents = [];
        this.loading = false;
      }
    });
  }

  openForm(): void {
    this.showForm = true;
    this.editingIncident = null;
    this.incidentForm.reset({ statut: 'OUVERT', resolu: false });
  }

  editIncident(incident: IncidentResponse): void {
    this.editingIncident = incident;
    this.showForm = true;
    this.incidentForm.patchValue({
      type: incident.type,
      statut: incident.statut,
      descrition: incident.descrition,
      resolu: incident.resolu
    });
  }

  closeForm(): void {
    this.showForm = false;
    this.editingIncident = null;
    this.incidentForm.reset({ statut: 'OUVERT', resolu: false });
  }

  submitForm(): void {
    if (this.incidentForm.invalid) {
      this.incidentForm.markAllAsTouched();
      return;
    }

    const formValue = this.incidentForm.getRawValue();
    const payload = {
      ...formValue,
      dateDeclaration: new Date()
    } as any;

    if (this.editingIncident) {
      this.incidentService.updateIncident(this.editingIncident.idIncident, payload).subscribe({
        next: () => {
          this.loadIncidents();
          this.closeForm();
        },
        error: (err) => console.error(err)
      });
    } else {
      this.incidentService.createIncident(payload).subscribe({
        next: () => {
          this.loadIncidents();
          this.closeForm();
        },
        error: (err) => console.error(err)
      });
    }
  }

  deleteIncident(incident: IncidentResponse): void {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet incident?')) {
      this.incidentService.deleteIncident(incident.idIncident).subscribe({
        next: () => this.loadIncidents(),
        error: (err) => console.error(err)
      });
    }
  }

  markAsResolved(incident: IncidentResponse): void {
    this.incidentService.resolveIncident(incident.idIncident).subscribe({
      next: () => this.loadIncidents(),
      error: (err) => console.error(err)
    });
  }

  getStatusColor(statut: string): string {
    const colors: { [key: string]: string } = {
      'OUVERT': '#ef4444',
      'EN_COURS': '#f97316',
      'RESOLU': '#22c55e',
      'FERME': '#6b7280'
    };
    return colors[statut] || '#6b7280';
  }
}
