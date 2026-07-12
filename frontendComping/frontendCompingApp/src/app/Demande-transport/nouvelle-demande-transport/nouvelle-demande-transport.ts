import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { ToastrService } from 'ngx-toastr';
import { DemandeTransportService } from '../../services/demande-transport.service';
import { DemandeTransportRequest } from '../../models/demande-transport.model';
import { AddressAutocompleteComponent } from '../../shared/address-autocomplete/address-autocomplete.component';

@Component({
  selector: 'app-nouvelle-demande-transport',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule, AddressAutocompleteComponent],
  templateUrl: './nouvelle-demande-transport.html',
  styleUrls: ['./nouvelle-demande-transport.css']
})
export class NouvelleDemandeTransportComponent implements OnInit {

  demandeForm: FormGroup;
  isEdit = false;
  demandeId: string | null = null;
  loading = false;
  error: string | null = null;

  typesService = [
    { value: 'Navette', label: 'Navette', icon: '🚐' },
    { value: 'Taxi', label: 'Taxi', icon: '🚕' },
    { value: 'Location voiture', label: 'Location voiture', icon: '🚙' },
    { value: 'Location scooter', label: 'Location scooter', icon: '🛵' },
    { value: 'Location moto', label: 'Location moto', icon: '🏍️' },
    { value: 'Location vélo', label: 'Location vélo', icon: '🚲' },
    { value: 'Minibus', label: 'Minibus', icon: '🚌' },
    { value: 'Covoiturage', label: 'Covoiturage', icon: '🚗' },
    { value: 'Transport matériel', label: 'Transport matériel', icon: '📦' },
    { value: 'Transfert aéroport', label: 'Transfert aéroport', icon: '✈️' },
    { value: 'Excursion 4x4', label: 'Excursion 4x4', icon: '🚙' }
  ];

  currentStatut = 'EN_ATTENTE';
  statutLabels: Record<string, string> = {
    EN_ATTENTE: 'En attente',
    PLANIFIEE: 'Planifiée',
    EN_COURS: 'En cours',
    LIVREE: 'Livrée',
    ANNULEE: 'Annulée'
  };

  constructor(
    private fb: FormBuilder,
    private demandeTransportService: DemandeTransportService,
    private router: Router,
    private route: ActivatedRoute,
    private toastr: ToastrService
  ) {
    this.demandeForm = this.fb.group({
      typeService: ['', Validators.required],
      adresseDepart: ['', Validators.required],
      adresseArrivee: ['', Validators.required],
      dateSouhaitee: ['', Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.demandeId = this.route.snapshot.paramMap.get('id');
    if (this.demandeId) {
      this.isEdit = true;
      this.loadDemande();
    }
  }

  loadDemande(): void {
    this.loading = true;
    this.demandeTransportService.getById(this.demandeId!).subscribe({
      next: (data) => {
        this.demandeForm.patchValue({
          typeService: data.typeService,
          adresseDepart: data.adresseDepart,
          adresseArrivee: data.adresseArrivee,
          dateSouhaitee: data.dateSouhaitee,
          description: data.description
        });
        this.currentStatut = data.statut;
        this.loading = false;
      },
      error: () => {
        this.error = 'Erreur lors du chargement.';
        this.loading = false;
      }
    });
  }

  selectType(value: string): void {
    this.demandeForm.patchValue({ typeService: value });
    this.demandeForm.get('typeService')?.markAsTouched();
  }

  onSubmit(): void {
    if (this.demandeForm.invalid) {
      this.error = 'Veuillez corriger les erreurs dans le formulaire.';
      return;
    }

    const dto: DemandeTransportRequest = this.demandeForm.value;
    this.loading = true;

    const request$ = this.isEdit && this.demandeId
      ? this.demandeTransportService.update(this.demandeId, dto)
      : this.demandeTransportService.create(dto);

    request$.subscribe({
      next: () => {
        this.loading = false;
        this.toastr.success(this.isEdit ? 'Demande modifiee' : 'Demande envoyee');
        this.router.navigate(['/dashboard/logistique/transports']);
      },
      error: (err) => {
        this.loading = false;
        this.error = err.error?.error || err.error?.message || 'Erreur lors de l\'enregistrement';
        this.toastr.error(this.error!);
      }
    });
  }
}
