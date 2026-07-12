import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ToastrService } from 'ngx-toastr';
import { CreneauLivraisonService } from '../../services/creneau-livraison.service';
import { CreneauLivraison } from '../../models/creneau-livraison.model';

@Component({
  selector: 'app-admin-livraisons',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-livraisons.html',
  styleUrls: ['./admin-livraisons.css']
})
export class AdminLivraisonsComponent implements OnInit {

  creneaux: CreneauLivraison[] = [];
  loading = false;

  formOpen = false;
  editingId: string | null = null;
  heureDebut = '';
  heureFin = '';
  disponible = true;

  constructor(private creneauService: CreneauLivraisonService, private toastr: ToastrService) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.creneauService.getAll().subscribe({
      next: (data) => {
        this.creneaux = data;
        this.loading = false;
      },
      error: () => {
        this.toastr.error('Erreur chargement creneaux');
        this.loading = false;
      }
    });
  }

  openCreate(): void {
    this.editingId = null;
    this.heureDebut = '';
    this.heureFin = '';
    this.disponible = true;
    this.formOpen = true;
  }

  openEdit(c: CreneauLivraison): void {
    this.editingId = c.idCreneauLivraison;
    this.heureDebut = c.heureDebut;
    this.heureFin = c.heureFin;
    this.disponible = c.disponible;
    this.formOpen = true;
  }

  closeForm(): void {
    this.formOpen = false;
  }

  save(): void {
    if (!this.heureDebut || !this.heureFin) {
      this.toastr.warning('Heures de debut et de fin requises');
      return;
    }
    const dto = { heureDebut: this.heureDebut, heureFin: this.heureFin, disponible: this.disponible };
    const request$ = this.editingId
      ? this.creneauService.update(this.editingId, dto)
      : this.creneauService.create(dto);

    request$.subscribe({
      next: () => {
        this.toastr.success(this.editingId ? 'Creneau modifie' : 'Creneau cree');
        this.closeForm();
        this.load();
      },
      error: () => this.toastr.error('Erreur lors de l\'enregistrement')
    });
  }

  supprimer(c: CreneauLivraison): void {
    if (!confirm('Supprimer ce creneau ?')) return;
    this.creneauService.delete(c.idCreneauLivraison).subscribe({
      next: () => {
        this.toastr.success('Creneau supprime');
        this.load();
      },
      error: () => this.toastr.error('Erreur lors de la suppression')
    });
  }
}
