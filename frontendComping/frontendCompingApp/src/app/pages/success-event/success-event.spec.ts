import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SuccessEvent } from './success-event';

describe('SuccessEvent', () => {
  let component: SuccessEvent;
  let fixture: ComponentFixture<SuccessEvent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SuccessEvent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SuccessEvent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
