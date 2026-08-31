export interface DashboardResponse {
  totalUsers: number;
  activeStaff: number;
  disabledStaff: number;
  totalAssets: number;
  pendingAssets: number;
  verifiedAssets: number;
  rejectedAssets: number;
  activeAssets: number;
  defectiveAssets: number;
  assetsByDeviceType: Record<string, number>;
  assetsByDirectorate: Record<string, number>;
  assetsBySection: Record<string, number>;
  assetsByZone: Record<string, number>;
  assetsByVerificationStatus: Record<string, number>;
  assetsByDeviceStatus: Record<string, number>;
}

export interface StaffDashboardResponse {
  totalAssets: number;
  pendingAssets: number;
  verifiedAssets: number;
  rejectedAssets: number;
  activeAssets: number;
  defectiveAssets: number;
}