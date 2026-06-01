import { TestBed } from '@angular/core/testing';


import { AiRecommendationEvent} from './ai-recommendationEvent';

describe('AiRecommendationEvent', () => {


  let service: AiRecommendationEvent;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AiRecommendationEvent);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
