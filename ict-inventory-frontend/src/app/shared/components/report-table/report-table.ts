import { Component, inject, input } from '@angular/core';
import { DatePipe } from '@angular/common';
import { StatusBadge } from '../status-badge/status-badge';
import { TranslationService } from '../../../core/services/translation.service';
import { Asset } from '../../../core/models/asset.model';
import { DEVICE_STATUS_LABELS, OWNERSHIP_TYPE_LABELS, deviceStatusTone } from '../../utils/enum-labels';
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

  readonly deviceStatusLabels = DEVICE_STATUS_LABELS;
  readonly ownershipLabels = OWNERSHIP_TYPE_LABELS;

  protected deviceTone(status: DeviceStatus): 'success' | 'danger' {
    return deviceStatusTone(status);
  }
}
