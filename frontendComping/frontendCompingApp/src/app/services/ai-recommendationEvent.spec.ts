import { TestBed } from '@angular/core/testing';

import { AiRecommendation } from './ai-recommendationEvent';

describe('AiRecommendation', () => {
  let service: AiRecommendation;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AiRecommendation);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
