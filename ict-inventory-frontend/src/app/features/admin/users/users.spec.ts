import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { of } from 'rxjs';
import { Users } from './users';
import { UsersService } from '../../../core/services/users.service';
import { AuthService } from '../../../core/services/auth.service';
import { UserAccount } from '../../../core/models/users.model';

const pendingUser: UserAccount = {
  id: 1,
  employeeId: 'NCT-EMP-001',
  fullName: 'John Pending',
  email: 'john@nactvet.go.tz',
  phoneNumber: '0712345678',
  setupCompleted: false,
  role: 'ADMIN',
  enabled: false,
  directorateId: null,
  directorateName: null,
  sectionId: null,
  sectionName: null,
  unitId: null,
  unitName: null,
};

const activeUser: UserAccount = {
  id: 2,
  employeeId: 'NCT-EMP-002',
  fullName: 'Jane Active',
  email: 'jane@nactvet.go.tz',
  phoneNumber: '0712987654',
  setupCompleted: true,
  role: 'ADMIN',
  enabled: true,
  directorateId: null,
  directorateName: null,
  sectionId: null,
  sectionName: null,
  unitId: null,
  unitName: null,
};

describe('Users', () => {
  let service: {
    findAll: ReturnType<typeof vi.fn>;
    approve: ReturnType<typeof vi.fn>;
    activate: ReturnType<typeof vi.fn>;
    deactivate: ReturnType<typeof vi.fn>;
    delete: ReturnType<typeof vi.fn>;
  };
  let authService: {
    user: ReturnType<typeof vi.fn>;
  };

  beforeEach(async () => {
    service = {
      findAll: vi.fn(() => of([pendingUser, activeUser])),
      approve: vi.fn(() => of(activeUser)),
      activate: vi.fn(() => of()),
      deactivate: vi.fn(() => of()),
      delete: vi.fn(() => of()),
    };
    authService = {
      user: vi.fn(() => ({ id: 99 })),
    };
    await TestBed.configureTestingModule({
      imports: [Users],
      providers: [
        provideHttpClient(),
        { provide: UsersService, useValue: service },
        { provide: AuthService, useValue: authService },
      ],
    }).compileComponents();
  });

  it('should create the component', () => {
    const fixture = TestBed.createComponent(Users);
    expect(fixture.componentInstance).toBeTruthy();
  });

  it('should separate pending from active users', () => {
    const fixture = TestBed.createComponent(Users);
    const component = fixture.componentInstance;
    component.ngOnInit();
    expect(component.pending().map((u) => u.id)).toEqual([1]);
    expect(component.activeUsers().map((u) => u.id)).toEqual([2]);
  });

  it('should approve a pending user', () => {
    const fixture = TestBed.createComponent(Users);
    const component = fixture.componentInstance;
    component.approve(pendingUser);
    expect(service.approve).toHaveBeenCalledWith(1);
  });
});