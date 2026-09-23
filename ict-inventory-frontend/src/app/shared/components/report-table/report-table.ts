import { Component, inject, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { StatusBadge } from '../status-badge/status-badge';
import { TranslationService } from '../../../core/services/translation.service';
import { Asset } from '../../../core/models/asset.model';
import { deviceStatusTone } from '../../utils/enum-labels';
import { DeviceStatus } from '../../../core/models/enums';

@Component({
  selector: 'app-report-table',
  imports: [StatusBadge, DatePipe],
  templateUrl: './report-table.html',
  styleUrl: './report-table.css',
})
export class ReportTable {
  readonly assets = input.required<Asset[]>();
  private readonly translation = inject(TranslationService);

  t = (k: string) => this.translation.t(k);

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }

  protected deviceStatusLabel(status: DeviceStatus): string {
    return status === 'WORKING' ? this.t('deviceStatus.working') : this.t('deviceStatus.notWorking');
  }
}
