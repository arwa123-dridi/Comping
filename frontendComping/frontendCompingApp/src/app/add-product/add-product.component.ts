import { Component, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

interface Product {
  nomProduit: string;
  descriptionProduit: string;
  prixProduit: number;
  categorieProduit: string | null; // ⭐ string instead of number
  statut: string;
  quantiteStock?: number;
  seuilAlerteStock?: number;
  imageUrl?: string;
  // 🆕 PROMOTION
  promoPrice?: number;
  promoStart?: string;
  promoEnd?: string;
}

@Component({
  selector: 'app-add-product',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './add-product.component.html',
  styleUrls: ['./add-product.component.css']
})
export class AddProductComponent {

  @Output() close = new EventEmitter<void>();
  @Output() productAdded = new EventEmitter<void>();

  private baseUrl = 'http://localhost:8087/api/produits';

  // ⭐ Categories list for dropdown (label UI + value backend)
  categories = [
    { label: 'Tentes', value: 'TENTES' },
    { label: 'Sacs de couchage', value: 'SACS_DE_COUCHAGE' },
    { label: 'Matelas & tapis de sol', value: 'MATELAS_ET_TAPIS_DE_SOL' },
    { label: 'Cuisine de camping', value: 'CUISINE_DE_CAMPING' },
    { label: 'Glacières', value: 'GLACIERES' },
    { label: 'Stockage eau', value: 'STOCKAGE_EAU' },
    { label: 'Éclairage', value: 'ECLAIRAGE' },
    { label: 'Énergie portable', value: 'ENERGIE_PORTABLE' },
    { label: 'Sacs à dos', value: 'SACS_A_DOS' },
    { label: 'Équipement randonnée', value: 'EQUIPEMENT_DE_RANDONNEE' },
    { label: 'Survie & secours', value: 'SURVIE_ET_SECOURS' },
    { label: 'Mobilier camping', value: 'MOBILIER_DE_CAMPING' },
    { label: 'Abris & tarp', value: 'ABRIS_ET_TARP' },
    { label: 'Vêtements camping', value: 'VETEMENTS_DE_CAMPING' },
    { label: 'Chaussures randonnée', value: 'CHAUSSURES_DE_RANDONNEE' },
    { label: 'Autre', value: 'AUTRE' }
  ];

  product: Product = {
    nomProduit: '',
    descriptionProduit: '',
    prixProduit: 0,
    categorieProduit: null,
    quantiteStock: 0,        // ✅ AJOUT
    seuilAlerteStock: 0,
    statut: 'DISPONIBLE',
    // 🆕 PROMO DEFAULT
    promoPrice: undefined,
    promoStart: undefined,
    promoEnd: undefined,
    imageUrl: undefined
  };

  selectedFile: File | null = null;

  constructor(private http: HttpClient, private toastr: ToastrService) { }

  // 📷 Select image + preview
  onFileSelected(event: Event) {
    const input = event.target as HTMLInputElement;
    if (!input.files || input.files.length === 0) return;

    this.selectedFile = input.files[0];
    this.product.imageUrl = URL.createObjectURL(this.selectedFile);
  }

  // ❌ Remove preview image
  removeImage(event: Event) {
    event.stopPropagation();
    this.selectedFile = null;
    this.product.imageUrl = undefined;
  }

  // 🚀 ADD PRODUCT
  saveProduct() {

    if (!this.product.nomProduit || this.product.prixProduit <= 0) {
      this.toastr.warning('Veuillez remplir le nom et le prix correctement.');
      return;
    }

    if (!this.product.categorieProduit) {
      this.toastr.warning('Veuillez choisir une catégorie.');
      return;
    }

    if (this.product.quantiteStock! < 0) {
      this.toastr.warning("La quantité ne peut pas être négative");
      return;
    }

    if (this.product.seuilAlerteStock! < 0) {
      this.toastr.warning("Le seuil d'alerte ne peut pas être négatif");
      return;
    }

    if (this.product.promoPrice) {
      if (!this.product.promoStart || !this.product.promoEnd) {
        this.toastr.warning("Veuillez compléter les dates de promotion");
        return;
      }

      if (this.product.promoPrice >= this.product.prixProduit) {
        this.toastr.warning("Le prix promo doit être inférieur au prix normal");
        return;
      }
    }

    const formData = new FormData();

    formData.append('produit', JSON.stringify({
      nomProduit: this.product.nomProduit,
      descriptionProduit: this.product.descriptionProduit,
      prixProduit: this.product.prixProduit,
      categorieProduit: this.product.categorieProduit, // ⭐ STRING ENUM
      quantiteStock: this.product.quantiteStock,         // ✅ AJOUT
      seuilAlerteStock: this.product.seuilAlerteStock,
      // 🆕 PROMOTION
      promoPrice: this.product.promoPrice,
      promoStart: this.product.promoStart,
      promoEnd: this.product.promoEnd,
      statut: this.product.statut
    }));

    if (this.selectedFile) {
      formData.append('image', this.selectedFile);
    }

    this.http.post(`${this.baseUrl}/addProduct`, formData)
      .subscribe({
        next: () => {
          this.toastr.success('Produit ajouté avec succès 🎉');
          this.productAdded.emit();
          this.close.emit();
        },
        error: () => {
          this.toastr.error("Erreur lors de l'ajout ❌");
        }
      });
  }

  cancel() {
    this.close.emit();
  }
}