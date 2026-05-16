import { TestBed } from '@angular/core/testing';

import { RecommendationActivity } from './recommendation-activity';

describe('RecommendationActivity', () => {
  let service: RecommendationActivity;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(RecommendationActivity);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
