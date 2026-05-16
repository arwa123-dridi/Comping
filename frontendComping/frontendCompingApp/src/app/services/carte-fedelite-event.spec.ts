import { TestBed } from '@angular/core/testing';

import { CarteFedeliteEvent } from './carte-fedelite-event';

describe('CarteFedeliteEvent', () => {
  let service: CarteFedeliteEvent;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(CarteFedeliteEvent);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
