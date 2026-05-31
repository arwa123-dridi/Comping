// src/app/sortie/sortie-form/sortie-form.component.ts
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { SortieService } from '../../services/sortie.service';
import { EquipeService } from '../../services/equipe.service';
import { SortieRequest, SortieResponse } from '../../models/sortie.model';
import { EquipeResponse } from '../../models/equipe.model';
import { ChecklistService } from '../../services/checklist.service';

@Component({
  selector: 'app-sortie-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, RouterModule],
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
  recommandationsEquipes: EquipeResponse[] = [];

  // Image
  selectedImage:   File | null = null;
  imageUploading   = false;
  imagePreviewUrl: string | null = null;
  existingImageUrl:string | null = null;

  // Toast
  toastMessage: string | null = null;
  toastType:    'success' | 'error' = 'success';
  private toastTimer: any;

  // Modal création équipe
  showCreateEquipeModal = false;
  newEquipeNom = '';
  newEquipeDescription = '';
  newEquipeNiveau = 'Tous niveaux';
  newEquipeNbMembresMax = 10;
  creatingEquipe = false;

  constructor(
    private fb:           FormBuilder,
    private sortieService:SortieService,
    private equipeService:EquipeService,
    private router:       Router,
    private route:        ActivatedRoute,
    private http:         HttpClient,
    private checklistService: ChecklistService 
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
    this.loadRecommandationsEquipes();
    this.sortieId = this.route.snapshot.paramMap.get('id');
    if (this.sortieId) {
      this.isEdit = true;
      this.loadSortie();
    }

    // Auto-suggestion équipement selon difficulté (seulement si champ vide)
    this.sortieForm.get('difficulte')?.valueChanges.subscribe((diff: string) => {
      const equipCtrl = this.sortieForm.get('equipementRequis');
      if (equipCtrl && !equipCtrl.value) {
        equipCtrl.setValue(this.getEquipementSuggestion(diff), { emitEvent: false });
      }
    });

    // ✅ NOUVEAU : appel à la Checklist IA quand la date, le lieu ou la difficulté changent
    this.sortieForm.get('dateDebut')?.valueChanges.subscribe(() => this.autoFillEquipement());
    this.sortieForm.get('lieuDepart')?.valueChanges.subscribe(() => this.autoFillEquipement());
    this.sortieForm.get('difficulte')?.valueChanges.subscribe(() => this.autoFillEquipement());
  }

  getEquipementSuggestion(difficulte: string): string {
    const suggestions: Record<string, string> = {
      'FACILE':    'Chaussures de marche, gourde 1.5L, snack, crème solaire, chapeau',
      'MOYEN':     'Chaussures de randonnée, sac à dos 20L, gourde 2L, bâtons, veste imperméable, trousse premiers secours, carte topographique',
      'DIFFICILE': 'Chaussures de randonnée haute, sac à dos 30L, gourde 3L, bâtons télescopiques, veste imperméable, sac de couchage d\'urgence, lampe frontale, carte + boussole, trousse premiers secours complète, nourriture 2j',
    };
    return suggestions[difficulte] ?? suggestions['MOYEN'];
  }

  get f() { return this.sortieForm.controls; }

  // ── Équipes ──────────────────────────────────────────────

  selectEquipe(equipeId: string): void {
    this.sortieForm.patchValue({ equipeId });
    this.sortieForm.get('equipeId')?.markAsTouched();
  }

  loadEquipes(): void {
    this.equipesLoading = true;
    const userId = localStorage.getItem('userId') ?? '';

    this.equipeService.getAllEquipes().subscribe({
      next: (data: any[]) => {
        this.equipes = (data || []).filter(eq => {
          const orgId = String(eq.organisateurId ?? eq.createdBy ?? eq.userId ?? '');
          return orgId === String(userId);
        });
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

  loadRecommandationsEquipes(): void {
    const userId = localStorage.getItem('userId');
    if (!userId) return;
    this.equipeService.getRecommandationsEquipes(userId).subscribe({
      next: (scores) => {
        this.recommandationsEquipes = scores.map(s => s.equipe).slice(0, 3);
      },
      error: () => {
        // Fallback : équipes avec places disponibles créées par l'organisateur
        this.equipeService.getAllEquipes().subscribe(all => {
          this.recommandationsEquipes = all.filter(e =>
            e.organisateurId === userId && (e.membres?.length || 0) < (e.nbMembresMax || 10)
          ).slice(0, 3);
        });
      }
    });
  }

  // ── Chargement sortie (mode édition) ────────────────────

  loadSortie(): void {
    this.isLoading = true;
    this.sortieService.getSortieById(this.sortieId!).subscribe({
      next: (data: SortieResponse) => {
        const fmt = (d: any): string => d ? new Date(d).toISOString().slice(0, 16) : '';
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
        const imgUrl = (data as any).imageUrl;
        if (imgUrl && imgUrl !== 'null') {
          this.existingImageUrl = imgUrl;
          this.imagePreviewUrl  = imgUrl;
        }
        this.isLoading = false;
      },
      error: (err) => {
        this.error = 'Impossible de charger la randonnée.';
        this.isLoading = false;
        if (err.status === 401) this.router.navigate(['/login']);
      }
    });
  }

  // ── Gestion image ────────────────────────────────────────

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;
    this.selectedImage = input.files[0];
    if (this.imagePreviewUrl && this.imagePreviewUrl.startsWith('blob:')) {
      URL.revokeObjectURL(this.imagePreviewUrl);
    }
    this.imagePreviewUrl = URL.createObjectURL(this.selectedImage);
    this.existingImageUrl = null;
  }

  onImageSelected(event: Event): void {
    this.onFileSelected(event);
  }

  clearImage(): void {
    if (this.imagePreviewUrl?.startsWith('blob:')) {
      URL.revokeObjectURL(this.imagePreviewUrl);
    }
    this.imagePreviewUrl = null;
    this.selectedImage = null;
    this.existingImageUrl = null;
    const fi = document.querySelector('input[type="file"]') as HTMLInputElement;
    if (fi) fi.value = '';
  }

  private uploadImageOptional(): Promise<string> {
    return new Promise((resolve) => {
      if (!this.selectedImage) {
        resolve(this.existingImageUrl ?? '');
        return;
      }
      this.imageUploading = true;
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
      this.http.post<{ url: string }>('http://localhost:8087/api/upload/image', formData, { headers })
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

  // ===================== CORRECTION DES DATES =====================
  private normalizeDateTime(value: any): string | undefined {
    if (!value) return undefined;
    let date: Date;
    if (typeof value === 'string') {
      date = new Date(value);
      if (value.match(/^\d{4}-\d{2}-\d{2}$/)) {
        date.setHours(0, 0, 0, 0);
      }
    } else if (value instanceof Date) {
      date = value;
    } else {
      return undefined;
    }
    if (isNaN(date.getTime())) return undefined;
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}:${seconds}`;
  }

  // ===================== MODAL CRÉATION ÉQUIPE =====================
  openCreateEquipeModal(): void {
    this.showCreateEquipeModal = true;
  }

  closeCreateEquipeModal(): void {
    this.showCreateEquipeModal = false;
    this.newEquipeNom = '';
    this.newEquipeDescription = '';
    this.newEquipeNiveau = 'Tous niveaux';
    this.newEquipeNbMembresMax = 10;
  }

  createEquipeAndSelect(): void {
    if (!this.newEquipeNom.trim()) {
      this.showToast('Le nom de l’équipe est requis', 'error');
      return;
    }
    this.creatingEquipe = true;
    const userId = localStorage.getItem('userId') ?? '';
    const userNom = `${localStorage.getItem('userPrenom') ?? ''} ${localStorage.getItem('userNom') ?? ''}`.trim() || 'Organisateur';
    const equipeData: any = {
      nom: this.newEquipeNom,
      description: this.newEquipeDescription,
      nbMembresMax: this.newEquipeNbMembresMax,
      niveau: this.newEquipeNiveau,
      organisateurId: userId,
      organisateurNom: userNom
    };
    this.equipeService.createEquipe(equipeData).subscribe({
      next: (newEquipe) => {
        this.creatingEquipe = false;
        this.showToast(`Équipe "${newEquipe.nom}" créée avec succès`, 'success');
        this.loadEquipes();
        this.loadRecommandationsEquipes();
        setTimeout(() => {
          this.sortieForm.patchValue({ equipeId: newEquipe.id });
          this.sortieForm.get('equipeId')?.markAsTouched();
        }, 500);
        this.closeCreateEquipeModal();
      },
      error: (err) => {
        this.creatingEquipe = false;
        this.showToast(err.error?.message || 'Erreur lors de la création', 'error');
      }
    });
  }

  /**
 * Appelle la Checklist IA avec la date, le lieu et la difficulté,
 * et remplit automatiquement le champ 'equipementRequis'
 */
private autoFillEquipement(): void {
  const dateDebut = this.sortieForm.get('dateDebut')?.value;
  const lieuDepart = this.sortieForm.get('lieuDepart')?.value;
  const difficulte = this.sortieForm.get('difficulte')?.value as string;

  if (!dateDebut || !lieuDepart || !difficulte) return;

  // Ne pas écraser si l'utilisateur a déjà saisi manuellement
  const currentEquip = this.sortieForm.get('equipementRequis')?.value;
  if (currentEquip && currentEquip.trim().length > 5) return;

  const formattedDate = new Date(dateDebut).toISOString().split('T')[0];
  const niveauMap: Record<string, number> = { FACILE: 2, MOYEN: 3, DIFFICILE: 4 };
  const niveau = niveauMap[difficulte] || 3;

  this.checklistService.recommandationAuto(lieuDepart, formattedDate, niveau)
    .subscribe({
      next: (res) => {
        if (res.success && res.checklist_item) {
          let equipText = res.checklist_item;
          if (res.recommendations?.length) {
            equipText += ' – ' + res.recommendations.slice(0, 4).join(', ');
          }
          this.sortieForm.patchValue({ equipementRequis: equipText }, { emitEvent: false });
        }
      },
      error: () => {
        const fallbackMap: Record<string, string> = {
          FACILE: 'Chaussures de marche, gourde 1.5L, crème solaire',
          MOYEN: 'Chaussures de randonnée, sac à dos 20L, gourde 2L, bâtons',
          DIFFICILE: 'Chaussures de randonnée haute, sac à dos 30L, gourde 3L, veste imperméable'
        };
        const fallback = fallbackMap[difficulte];
        if (fallback && !currentEquip) {
          this.sortieForm.patchValue({ equipementRequis: fallback }, { emitEvent: false });
        }
      }
    });
}

  // ── Soumission ───────────────────────────────────────────

  async onSubmit(): Promise<void> {
    this.sortieForm.markAllAsTouched();
    if (this.sortieForm.invalid) {
      this.error = 'Veuillez corriger les erreurs avant de continuer.';
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    this.isLoading = true;
    this.error = null;
    const imageUrl = await this.uploadImageOptional();
    const v = this.sortieForm.value;
    const sortieData: SortieRequest = {
      titre: v.titre,
      description: v.description,
      dateDebut: this.normalizeDateTime(v.dateDebut) as any,
      dateFin: this.normalizeDateTime(v.dateFin) as any,
      lieuDepart: v.lieuDepart,
      lieuArrivee: v.lieuArrivee || undefined,
      region: v.region || undefined,
      difficulte: v.difficulte,
      capaciteMax: Number(v.capaciteMax),
      prixParPersonne: Number(v.prixParPersonne ?? 0),
      equipementRequis: v.equipementRequis || undefined,
      assistanceMedicale: Boolean(v.assistanceMedicale),
      equipeId: v.equipeId,
      distanceKm: Number(v.distanceKm ?? 0),
      organisateurId: localStorage.getItem('userId') ?? '',
      organisateurNom: `${localStorage.getItem('userPrenom') ?? ''} ${localStorage.getItem('userNom') ?? ''}`.trim() || 'Organisateur',
      imageUrl,
    };
    const timeout = setTimeout(() => {
      this.isLoading = false;
      this.error = '⏱️ Le serveur met trop de temps à répondre. Vérifiez votre connexion.';
    }, 15_000);
    const req$ = this.isEdit && this.sortieId
      ? this.sortieService.updateSortie(this.sortieId, sortieData)
      : this.sortieService.createSortie(sortieData);
    req$.subscribe({
      next: (result) => {
        clearTimeout(timeout);
        this.isLoading = false;
        const id = result.id ?? this.sortieId;
        this.showToast(this.isEdit ? '✅ Randonnée modifiée avec succès !' : '✅ Randonnée créée avec succès !', 'success');
        setTimeout(() => {
          const role = localStorage.getItem('userRole') ?? '';
          if (role === 'ADMIN' || role === 'ROLE_ADMIN' || role === 'ORGANISATEUR' || role === 'ROLE_ORGANISATEUR') {
            this.router.navigate(['/admin/sorties']);
          } else {
            this.router.navigate(['/dashboard/sorties', id]);
          }
        }, 1200);
      },
      error: (err) => {
        clearTimeout(timeout);
        this.isLoading = false;
        if (err.status === 401) { this.router.navigate(['/login']); return; }
        this.error = err.error?.message ?? (this.isEdit ? 'Erreur lors de la mise à jour.' : 'Erreur lors de la création.');
        window.scrollTo({ top: 0, behavior: 'smooth' });
      }
    });
  }

  showToast(msg: string, type: 'success' | 'error' = 'success'): void {
    this.toastMessage = msg;
    this.toastType = type;
    if (this.toastTimer) clearTimeout(this.toastTimer);
    this.toastTimer = setTimeout(() => { this.toastMessage = null; }, 4000);
  }
}