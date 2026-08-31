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