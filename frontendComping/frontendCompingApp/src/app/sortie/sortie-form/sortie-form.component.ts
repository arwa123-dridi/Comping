// src/app/sortie/sortie-form/sortie-form.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieRequest, SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';

@Component({
  selector: 'app-sortie-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './sortie-form.component.html',
  styleUrls: ['./sortie-form.component.css']
})
export class SortieFormComponent implements OnInit {

  sortieForm:   FormGroup;
  isEdit        = false;
  sortieId:     string | null = null;
  isLoading     = false;
  error:        string | null = null;

  // Équipes
  equipes:        EquipeResponse[] = [];
  equipesLoading  = false;

  // Image
  selectedImage:   File | null = null;
  imageUploading   = false;
  imagePreviewUrl: string | null = null;
  existingImageUrl:string | null = null;

  // Toast
  toastMessage: string | null = null;
  toastType:    'success' | 'error' = 'success';
  private toastTimer: any;

  constructor(
    private fb:           FormBuilder,
    private sortieService:SortieService,
    private equipeService:EquipeService,
    private router:       Router,
    private route:        ActivatedRoute,
    private http:         HttpClient
  ) {
    this.sortieForm = this.fb.group({
      titre:              ['', [Validators.required, Validators.minLength(3)]],
      description:        ['', [Validators.required, Validators.minLength(10)]],
      dateDebut:          ['', Validators.required],
      dateFin:            [''],
      lieuDepart:         ['', Validators.required],
      lieuArrivee:        [''],
      region:             [''],
      difficulte:         ['MOYEN', Validators.required],
      capaciteMax:        [10,  [Validators.required, Validators.min(1), Validators.max(200)]],
      prixParPersonne:    [0,   [Validators.min(0)]],
      equipementRequis:   [''],
      assistanceMedicale: [false],
      equipeId:           ['', Validators.required],
      distanceKm:         [0,   [Validators.min(0)]],
      organisateurId:     [localStorage.getItem('userId') ?? ''],
      organisateurNom:    [
        `${localStorage.getItem('userPrenom') ?? ''} ${localStorage.getItem('userNom') ?? ''}`.trim()
        || 'Organisateur'
      ],
    });
  }

  ngOnInit(): void {
    this.loadEquipes();
    this.sortieId = this.route.snapshot.paramMap.get('id');
    if (this.sortieId) {
      this.isEdit = true;
      this.loadSortie();
    }
  }

  // Raccourci pratique pour les contrôles du formulaire
  get f() { return this.sortieForm.controls; }

  // ── Équipes ──────────────────────────────────────────────

  selectEquipe(equipeId: string): void {
    this.sortieForm.patchValue({ equipeId });
    this.sortieForm.get('equipeId')?.markAsTouched();
  }

  /**
   * Charge les équipes dont l'utilisateur connecté est l'organisateur.
   * ✅ Robuste : gère les champs organisateurId manquants ou nommés différemment.
   */
  loadEquipes(): void {
    this.equipesLoading = true;
    const userId = localStorage.getItem('userId') ?? '';

    this.equipeService.getAllEquipes().subscribe({
      next: (data: any[]) => {
        // ✅ Filtrer par organisateurId (gère plusieurs conventions de nommage)
        this.equipes = (data || []).filter(eq => {
          const orgId = String(
            eq.organisateurId ?? eq.createdBy ?? eq.userId ?? ''
          );
          return orgId === String(userId);
        });

        // Si aucune équipe filtrée, on montre toutes (cas dev/test)
        if (this.equipes.length === 0 && data.length > 0 && !userId) {
          this.equipes = data;
        }

        this.equipesLoading = false;
      },
      error: () => {
        this.equipesLoading = false;
        this.showToast('Erreur lors du chargement des équipes', 'error');
      }
    });
  }

  // ── Chargement sortie (mode édition) ────────────────────

  loadSortie(): void {
    this.isLoading = true;
    this.sortieService.getSortieById(this.sortieId!).subscribe({
      next: (data: SortieResponse) => {
        // Formater les dates pour input datetime-local
        const fmt = (d: any): string =>
          d ? new Date(d).toISOString().slice(0, 16) : '';

        this.sortieForm.patchValue({
          titre:              data.titre,
          description:        data.description,
          dateDebut:          fmt(data.dateDebut),
          dateFin:            fmt(data.dateFin),
          lieuDepart:         data.lieuDepart,
          lieuArrivee:        (data as any).lieuArrivee ?? '',
          region:             data.region ?? '',
          difficulte:         data.difficulte,
          capaciteMax:        data.capaciteMax,
          prixParPersonne:    data.prixParPersonne ?? 0,
          equipementRequis:   data.equipementRequis ?? '',
          assistanceMedicale: data.assistanceMedicale ?? false,
          equipeId:           data.equipeId ?? '',
          distanceKm:         data.distanceKm ?? 0,
          organisateurId:     data.organisateurId,
          organisateurNom:    data.organisateurNom,
        });

        // Image existante
        const imgUrl = (data as any).imageUrl;
        if (imgUrl && imgUrl !== 'null') {
          this.existingImageUrl = imgUrl;
          this.imagePreviewUrl  = imgUrl;
        }

        this.isLoading = false;
      },
      error: (err) => {
        this.error     = 'Impossible de charger la randonnée.';
        this.isLoading = false;
        if (err.status === 401) this.router.navigate(['/login']);
      }
    });
  }

  // ── Gestion image ────────────────────────────────────────

  /** Appelé par (change) sur l'input file */
  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    this.selectedImage = input.files[0];

    // Révoquer l'ancienne URL objet si ce n'était pas une URL Cloudinary
    if (this.imagePreviewUrl && this.imagePreviewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.imagePreviewUrl);
    }

    this.imagePreviewUrl  = URL.createObjectURL(this.selectedImage);
    this.existingImageUrl = null; // L'image existante est remplacée
  }

  /** Alias pour compatibilité avec ancien nom HTML */
  onImageSelected(event: Event): void {
    this.onFileSelected(event);
  }

  /** Supprimer la sélection d'image */
  clearImage(): void {
    if (this.imagePreviewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(this.imagePreviewUrl);
    }
    this.imagePreviewUrl  = null;
    this.selectedImage    = null;
    this.existingImageUrl = null;

    // Reset l'input file
    const fi = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fi) fi.value = '';
  }

  /**
   * ✅ Upload image vers Cloudinary via le backend.
   * Retourne l'URL ou l'URL existante en cas d'échec (non bloquant).
   */
  private uploadImageOptional(): Promise<string> {
    return new Promise((resolve) => {
      // Pas de nouvelle image sélectionnée
      if (!this.selectedImage) {
        resolve(this.existingImageUrl ?? '');
        return;
      }

      this.imageUploading = true;

      // Timeout sécurité 10s
      const timeout = setTimeout(() => {
        this.imageUploading = false;
        console.warn('[Campino] Upload image timeout — sortie sauvegardée sans image');
        resolve(this.existingImageUrl ?? '');
      }, 10_000);

      const formData = new FormData();
      formData.append('file', this.selectedImage);

      const headers = new HttpHeaders({
        'Authorization': `Bearer ${localStorage.getItem('authToken') ?? ''}`
      });

      this.http
        .post<{ url: string }>('http://localhost:8087/api/upload/image', formData, { headers })
        .subscribe({
          next: (res) => {
            clearTimeout(timeout);
            this.imageUploading = false;
            resolve(res.url ?? '');
          },
          error: (err) => {
            clearTimeout(timeout);
            this.imageUploading = false;
            console.warn('[Campino] Upload image échoué:', err.message);
            resolve(this.existingImageUrl ?? '');
          }
        });
    });
  }

  // ── Soumission ───────────────────────────────────────────

  async onSubmit(): Promise<void> {
    // Marquer tous les champs comme touchés pour afficher les erreurs
    this.sortieForm.markAllAsTouched();

    if (this.sortieForm.invalid) {
      this.error = 'Veuillez corriger les erreurs avant de continuer.';
      // Scroller vers le haut
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    this.isLoading = true;
    this.error     = null;

    // 1. Upload image (non bloquant)
    const imageUrl = await this.uploadImageOptional();

    // 2. Construire l'objet sortie
    const v = this.sortieForm.value;
    const sortieData: SortieRequest = {
      titre:              v.titre,
      description:        v.description,
      dateDebut:          new Date(v.dateDebut),
      dateFin:            v.dateFin ? new Date(v.dateFin) : undefined,
      lieuDepart:         v.lieuDepart,
      lieuArrivee:        v.lieuArrivee || undefined,
      region:             v.region || undefined,
      difficulte:         v.difficulte,
      capaciteMax:        Number(v.capaciteMax),
      prixParPersonne:    Number(v.prixParPersonne ?? 0),
      equipementRequis:   v.equipementRequis || undefined,
      assistanceMedicale: Boolean(v.assistanceMedicale),
      equipeId:           v.equipeId,
      distanceKm:         Number(v.distanceKm ?? 0),
      organisateurId:     localStorage.getItem('userId') ?? '',
      organisateurNom:    `${localStorage.getItem('userPrenom') ?? ''} ${localStorage.getItem('userNom') ?? ''}`.trim()
                          || 'Organisateur',
      imageUrl,
    };

    // 3. Appel API
    const timeout = setTimeout(() => {
      this.isLoading = false;
      this.error     = '⏱️ Le serveur met trop de temps à répondre. Vérifiez votre connexion.';
    }, 15_000);

    const req$ = this.isEdit && this.sortieId
      ? this.sortieService.updateSortie(this.sortieId, sortieData)
      : this.sortieService.createSortie(sortieData);

    req$.subscribe({
      next: (result) => {
        clearTimeout(timeout);
        this.isLoading = false;
        const id = result.id ?? this.sortieId;
        this.showToast(
          this.isEdit ? '✅ Randonnée modifiée avec succès !' : '✅ Randonnée créée avec succès !',
          'success'
        );
        setTimeout(() => this.router.navigate(['/sorties', id]), 1200);
      },
      error: (err) => {
        clearTimeout(timeout);
        this.isLoading = false;
        if (err.status === 401) { this.router.navigate(['/login']); return; }
        this.error = err.error?.message
          ?? (this.isEdit ? 'Erreur lors de la mise à jour.' : 'Erreur lors de la création.');
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  // ── Toast ────────────────────────────────────────────────
  showToast(msg: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = msg;
    this.toastType    = type;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer   = setTimeout(() => { this.toastMessage = null; }, 4000);
  }
}