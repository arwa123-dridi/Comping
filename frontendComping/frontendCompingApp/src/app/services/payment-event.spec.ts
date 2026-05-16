import { TestBed } from '@angular/core/testing';

import { PaymentEvent } from './payment-event';

describe('PaymentEvent', () => {
  let service: PaymentEvent;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PaymentEvent);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
