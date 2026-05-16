import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ReservationsCamping } from './reservations-camping';

describe('ReservationsCamping', () => {
  let component: ReservationsCamping;
  let fixture: ComponentFixture<ReservationsCamping>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ReservationsCamping]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ReservationsCamping);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
