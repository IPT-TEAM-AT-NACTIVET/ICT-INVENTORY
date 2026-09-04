import { Asset } from './asset.model';

export interface ReportItem {
  id: number | null;
  name: string;
  count: number;
  assets: Asset[];
}

export interface ReportResponse {
  items: ReportItem[];
  reportType: string;
  totalAssets: number;
}

export interface ReportSummary {
  totalAssets: number;
  activeAssets: number;
  defectiveAssets: number;
  officeAssets: number;
  personalAssets: number;
}
