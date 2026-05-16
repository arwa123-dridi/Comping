import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { CommunityService } from '../services/community.service';

export const adminCommunityGuard: CanActivateFn = () => {
  const community = inject(CommunityService);
  const router = inject(Router);

  if (community.isAdmin()) {
    return true;
  }

  void router.navigate(['/social-home']);
  return false;
};
