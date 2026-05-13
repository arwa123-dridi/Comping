import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CampingSite } from './camping-site';

describe('CampingSite', () => {
  let component: CampingSite;
  let fixture: ComponentFixture<CampingSite>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CampingSite]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CampingSite);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
