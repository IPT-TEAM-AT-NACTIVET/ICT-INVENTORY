import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { UsersService } from './users.service';
import { environment } from '../../env';

describe('UsersService', () => {
  let service: UsersService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(UsersService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should list users with a search param', () => {
    service.findAll('ali').subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/users?search=ali`);
    expect(req.request.method).toBe('GET');
    req.flush([]);
  });

  it('should approve a user via POST approve', () => {
    service.approve(5).subscribe();
    const req = httpMock.expectOne(`${environment.apiUrl}/users/5/approve`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });
});
