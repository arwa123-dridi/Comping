import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CloudinaryService {
  // Remplace par ton vrai cloud name 
  private readonly CLOUD_NAME = 'dyyeyeeb49';

  // Plusieurs images par difficulté pour éviter la duplication visuelle
  private readonly DEFAULT_IMAGES: Record<string, string[]> = {
    FACILE: [
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_facile_1.jpg`,
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_facile_2.jpg`,
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_facile_3.jpg`,
    ],
    MOYEN: [
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_moyen_1.jpg`,
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_moyen_2.jpg`,
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_moyen_3.jpg`,
    ],
    DIFFICILE: [
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_difficile_1.jpg`,
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_difficile_2.jpg`,
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default_difficile_3.jpg`,
    ],
    DEFAULT: [
      `https://res.cloudinary.com/${this.CLOUD_NAME}/image/upload/v1/campino/default.jpg`,
    ],
  };

  /**
   * Retourne l'URL de l'image :
   * - si imageUrl fournie (Cloudinary) → optimisée
   * - sinon → fallback stable (par difficulté) en fonction d'un hash (id + titre)
   */
  getImageUrl(imageUrl?: string | null, difficulte?: string, id?: string, titre?: string): string {
    // 1. Image personnalisée (uploadée par l'organisateur)
    if (imageUrl && imageUrl.trim() && imageUrl !== 'null') {
      return this.optimizeCloudinaryUrl(imageUrl);
    }

    // 2. Fallback : sélection stable par hash pour éviter la duplication
    const key = difficulte && this.DEFAULT_IMAGES[difficulte] ? difficulte : 'DEFAULT';
    const images = this.DEFAULT_IMAGES[key];
    const index = this.getStableIndex(id || titre || '', images.length);
    return images[index];
  }

  /** Ajoute les paramètres d'optimisation Cloudinary (qualité, format) */
  private optimizeCloudinaryUrl(url: string): string {
    if (!url.includes('res.cloudinary.com')) return url;
    // Remplace '/upload/' par '/upload/f_auto,q_auto,w_800/'
    return url.replace('/upload/', '/upload/f_auto,q_auto,w_800/');
  }

  /** Hash simple pour obtenir un index stable (pas de random à chaque refresh) */
  private getStableIndex(str: string, max: number): number {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) - hash) + str.charCodeAt(i);
      hash |= 0;
    }
    return Math.abs(hash) % max;
  }
}