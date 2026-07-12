import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { IncidentService } from '../../services/incident.service';
import { DemandeTransportService } from '../../services/demande-transport.service';
import { IncidentRequest } from '../../models/incident.model';
import { DemandeTransportResponse } from '../../models/demande-transport.model';

@Component({
  selector: 'app-signaler-incident',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './signaler-incident.html',
  styleUrls: ['./signaler-incident.css']
})
export class SignalerIncidentComponent implements OnInit {

  incidentForm: FormGroup;
  isEdit = false;
  incidentId: string | null = null;
  loading = false;
  error: string | null = null;

  types = ['Securite', 'Panne technique', 'Retard', 'Autre'];
  mesTransports: DemandeTransportResponse[] = [];

  constructor(
    private fb: FormBuilder,
    private incidentService: IncidentService,
    private demandeTransportService: DemandeTransportService,
    private router: Router,
    private route: ActivatedRoute,
    private toastr: ToastrService
  ) {
    this.incidentForm = this.fb.group({
      type: ['', Validators.required],
      description: ['', [Validators.required, Validators.minLength(10)]],
      demandeTransportId: ['']
    });
  }

  ngOnInit(): void {
    this.demandeTransportService.getMine().subscribe({
      next: (data) => this.mesTransports = data,
      error: () => {}
    });

    this.incidentId = this.route.snapshot.paramMap.get('id');
    if (this.incidentId) {
      this.isEdit = true;
      this.loadIncident();
    }
  }

  loadIncident(): void {
    this.loading = true;
    this.incidentService.getById(this.incidentId!).subscribe({
      next: (data) => {
        this.incidentForm.patchValue({
          type: data.type,
          description: data.description,
          demandeTransportId: data.demandeTransportId || ''
        });
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement.';
        this.loading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.incidentForm.invalid) {
      this.error = 'Veuillez corriger les erreurs dans le formulaire.';
      return;
    }

    const dto: IncidentRequest = this.incidentForm.value;
    if (!dto.demandeTransportId) delete (dto as any).demandeTransportId;

    this.loading = true;
    const request$ = this.isEdit && this.incidentId
      ? this.incidentService.update(this.incidentId, dto)
      : this.incidentService.create(dto);

    request$.subscribe({
      next: () => {
        this.loading = false;
        this.toastr.success(this.isEdit ? 'Incident modifie' : 'Incident signale');
        this.router.navigate(['/dashboard/logistique/incidents']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.error || err.error?.message || 'Erreur lors de l\'enregistrement';
        this.toastr.error(this.error!);
      }
    });
  }
}
