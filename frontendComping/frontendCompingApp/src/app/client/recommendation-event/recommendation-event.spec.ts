import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecommendationEvent } from './recommendation-event';

describe('RecommendationEvent', () => {
  let component: RecommendationEvent;
  let fixture: ComponentFixture<RecommendationEvent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecommendationEvent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecommendationEvent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
