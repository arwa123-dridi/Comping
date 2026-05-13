import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SiteService } from '../../services/site';
import { SiteCamping } from '../../models/site-camping.model';

@Component({
  selector: 'app-camping-site',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './camping-site.html',
  styleUrls: ['./camping-site.css']
})
export class CampingSiteComponent implements OnInit {

  sites: SiteCamping[] = [];
  filteredSites: SiteCamping[] = [];

  showModal: boolean = false;
  newSite: any = {};

  showEditModal: boolean = false;
  editSite: any = {};

  // SEARCH
  searchTerm: string = '';

  // PAGINATION
  page: number = 1;
  pageSize: number = 5;

  constructor(private siteService: SiteService) {}

  ngOnInit(): void {
    this.loadSites();
  }

  loadSites() {
    this.siteService.getAll().subscribe(data => {
      this.sites = data;
      this.applyFilters();
    });
  }

  applyFilters() {
    const term = this.searchTerm.toLowerCase();
    this.filteredSites = this.sites.filter(site =>
      (site.nom?.toLowerCase().includes(term) ?? false) ||
      (site.localisation?.toLowerCase().includes(term) ?? false)
    );
    this.page = 1;
  }

  // PAGINATION
  get paginatedSites() {
    const start = (this.page - 1) * this.pageSize;
    return this.filteredSites.slice(start, start + this.pageSize);
  }

  get totalPages() {
    return Math.ceil(this.filteredSites.length / this.pageSize);
  }

  nextPage() {
    if (this.page < this.totalPages) this.page++;
  }

  prevPage() {
    if (this.page > 1) this.page--;
  }

  // DELETE
  deleteSite(id: string) {
    if (confirm('Confirmer la suppression ?')) {
      this.siteService.delete(id).subscribe(() => this.loadSites());
    }
  }

  // BLOCK
  blockSite(id: string) {
    this.siteService.update(id, { statut: 'bloqué' }).subscribe(() => this.loadSites());
  }

  // ADD
  openAddModal() {
    this.newSite = { nom: '', localisation: '', tarifs: null, capacite: null, description: '',disponible: true  };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.newSite = {};
  }

  saveSite() {
    if (!this.newSite.nom || !this.newSite.localisation || !this.newSite.tarifs) {
      alert('Veuillez remplir les champs obligatoires (*)');
      return;
    }
    this.siteService.create(this.newSite).subscribe(() => {
      this.closeModal();
      this.loadSites();
    });
  }

  // EDIT
  openEditModal(site: any) {
    this.editSite = { ...site };
    this.showEditModal = true;
  }

  closeEditModal() {
    this.showEditModal = false;
    this.editSite = {};
  }

  updateSite() {
    if (!this.editSite.nom || !this.editSite.localisation || !this.editSite.tarifs) {
      alert('Veuillez remplir les champs obligatoires (*)');
      return;
    }
    this.siteService.update(this.editSite.id, this.editSite).subscribe(() => {
      this.closeEditModal();
      this.loadSites();
    });
  }
}