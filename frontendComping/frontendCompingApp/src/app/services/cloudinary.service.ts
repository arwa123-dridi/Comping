import { Injectable } from '@angular/core';

@Injectable({ providedIn: 'root' })
export class CloudinaryService {
  private readonly CLOUD_NAME = 'dyyeyeeb49';

  // Fallbacks Unsplash — toujours disponibles, catégorisés par difficulté
  private readonly UNSPLASH_FALLBACKS: Record<string, string[]> = {
    FACILE: [
      'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=600&h=360&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&h=360&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1500534314209-a25ddb2bd429?w=600&h=360&fit=crop&auto=format',
    ],
    MOYEN: [
      'https://images.unsplash.com/photo-1519681393784-d120267933ba?w=600&h=360&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=600&h=360&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1434725039720-aaad6dd32dfe?w=600&h=360&fit=crop&auto=format',
    ],
    DIFFICILE: [
      'https://images.unsplash.com/photo-1506905925346-21bda4d32df4?w=600&h=360&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1454496522488-7a8e488e8606?w=600&h=360&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1483728642387-6c3bdd6c93e5?w=600&h=360&fit=crop&auto=format',
    ],
    DEFAULT: [
      'https://images.unsplash.com/photo-1551632786-fc0b4cd1235b?w=600&h=360&fit=crop&auto=format',
      'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=600&h=360&fit=crop&auto=format',
    ],
  };

  getImageUrl(imageUrl?: string | null, difficulte?: string, id?: string, titre?: string): string {
    // 1. Image Cloudinary personnalisée → optimisée
    if (imageUrl && imageUrl.trim() && imageUrl !== 'null') {
      return this.optimizeCloudinaryUrl(imageUrl);
    }
    // 2. Fallback Unsplash stable selon difficulté + hash
    const key = difficulte && this.UNSPLASH_FALLBACKS[difficulte] ? difficulte : 'DEFAULT';
    const images = this.UNSPLASH_FALLBACKS[key];
    const index = this.stableIndex(id || titre || '', images.length);
    return images[index];
  }

  private optimizeCloudinaryUrl(url: string): string {
    if (!url.includes('res.cloudinary.com')) return url;
    return url.replace('/upload/', '/upload/f_auto,q_auto,w_600/');
  }

  private stableIndex(str: string, max: number): number {
    let hash = 0;
    for (let i = 0; i < str.length; i++) {
      hash = ((hash << 5) - hash) + str.charCodeAt(i);
      hash |= 0;
    }
    return Math.abs(hash) % max;
  }
}
