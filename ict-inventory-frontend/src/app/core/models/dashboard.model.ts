export interface RecentAsset {
  assetNumber: string | null;
  deviceName: string;
  deviceType: string | null;
  userOfAsset: string;
  zone: string | null;
  office: string | null;
  registeredBy: string | null;
  registeredAt: string;
}

export interface DashboardResponse {
  totalAssets: number;
  activeAssets: number;
  defectiveAssets: number;
  assetsByDeviceType: Record<string, number>;
  assetsByZone: Record<string, number>;
  assetsByDeviceStatus: Record<string, number>;
  assetsByOwnership: Record<string, number>;
  recentAssets: RecentAsset[];
}

export interface UserDashboardResponse {
  totalAssets: number;
  activeAssets: number;
  defectiveAssets: number;
  assetsByDeviceType: Record<string, number>;
  assetsByZone: Record<string, number>;
  assetsByDeviceStatus: Record<string, number>;
  assetsByOwnership: Record<string, number>;
  recentAssets: RecentAsset[];
}
