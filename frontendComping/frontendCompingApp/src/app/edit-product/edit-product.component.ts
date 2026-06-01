import { Component, Input, Output, EventEmitter, OnInit, OnChanges, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { ToastrService } from 'ngx-toastr';

interface Product {
  id: string;
  nomProduit: string;
  descriptionProduit: string;
  prixProduit: number;
  categorieProduit: string | null; // ⭐ STRING now
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
  selector: 'app-edit-product',
  standalone: true,
  imports: [CommonModule, FormsModule, HttpClientModule],
  templateUrl: './edit-product.component.html',
  styleUrls: ['./edit-product.component.css']
})
export class EditProductComponent implements OnInit, OnChanges {

  @Input() productId!: string;
  @Output() close = new EventEmitter<void>();
  @Output() productUpdated = new EventEmitter<void>();

  private baseUrl = 'http://localhost:8087/api/produits';
  private defaultImage = 'assets/default-product.png';

  // ⭐ same categories list as Add component
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
    id: '',
    nomProduit: '',
    descriptionProduit: '',
    prixProduit: 0,
    categorieProduit: null,
    statut: 'Disponible',
    quantiteStock: 0,        // ✅ AJOUT
    seuilAlerteStock: 0,
    imageUrl: '',
    // 🆕 PROMO INIT
    promoPrice: undefined,
    promoStart: undefined,
    promoEnd: undefined
  };

  selectedFile!: File;

  constructor(private http: HttpClient, private toastr: ToastrService) { }

  ngOnInit(): void { }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['productId'] && this.productId) {
      this.loadProduct();
    }
  }

  // 🔵 LOAD PRODUCT
  loadProduct() {
    this.http.get<any>(`${this.baseUrl}/${this.productId}`).subscribe({
      next: (data) => {
        this.product = {
          id: data.id,
          nomProduit: data.nomProduit,
          descriptionProduit: data.descriptionProduit,
          prixProduit: data.prixProduit,
          categorieProduit: data.categorieProduit, // ⭐ already string from backend
          quantiteStock: data.quantiteStock ?? 0,        // ✅ AJOUT
          seuilAlerteStock: data.seuilAlerteStock ?? 0,
          statut: data.statut ?? 'Disponible',
          // 🆕 PROMO
          promoPrice: data.promoPrice,
          promoStart: data.promoStart,
          promoEnd: data.promoEnd,
          imageUrl: data.imageUrl
            ? `http://localhost:8087${data.imageUrl}`
            : this.defaultImage
        };
      },
      error: () => {
        alert('Impossible de charger le produit.');
        this.close.emit();
      }
    });
  }

  // 📷 SELECT IMAGE + PREVIEW
  onFileSelected(event: any) {
    const file = event.target.files[0];
    if (!file) return;

    this.selectedFile = file;

    const reader = new FileReader();
    reader.onload = () => {
      this.product.imageUrl = reader.result as string;
    };
    reader.readAsDataURL(file);
  }

  // 🚀 UPDATE PRODUCT
  saveProduct() {

    if (!this.product.nomProduit || this.product.prixProduit <= 0) {
      this.toastr.warning('Veuillez remplir le nom et le prix correctement.');
      return;
    }

    if (!this.product.categorieProduit) {
      this.toastr.warning('Veuillez choisir une catégorie.');
      return;
    }

    if ((this.product.quantiteStock ?? 0) < 0) {
      this.toastr.warning("La quantité ne peut pas être négative");
      return;
    }

    if ((this.product.seuilAlerteStock ?? 0) < 0) {
      this.toastr.warning("Le seuil ne peut pas être négatif");
      return;
    }

    if (this.product.promoPrice) {
      if (!this.product.promoStart || !this.product.promoEnd) {
        this.toastr.warning("Veuillez remplir les dates de promotion");
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
      statut: this.product.statut,
      // 🆕 PROMOTION
      promoPrice: this.product.promoPrice,
      promoStart: this.product.promoStart,
      promoEnd: this.product.promoEnd
    }));

    if (this.selectedFile) {
      formData.append('image', this.selectedFile);
    }

    this.http.put(`${this.baseUrl}/updateProduct/${this.product.id}`, formData)
      .subscribe({
        next: () => {
          this.productUpdated.emit();
          this.toastr.info('Produit modifié avec succès ✏️');
          this.close.emit();
        },
        error: () => {
          alert('Erreur lors de la mise à jour du produit.');
        }
      });
  }

  cancel() {
    this.close.emit();
  }

  removeImage(event: Event) {
    event.stopPropagation();
    this.product.imageUrl = '';
  }
}
