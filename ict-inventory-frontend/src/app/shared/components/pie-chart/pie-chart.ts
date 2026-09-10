import { Component, computed, input } from '@angular/core';

export interface PieSlice {
  label: string;
  value: number;
  color?: string;
}

const PALETTE = [
  '#1d4ed8',
  '#16a34a',
  '#dc2626',
  '#f59e0b',
  '#7c3aed',
  '#0ea5e9',
  '#ec4899',
  '#14b8a6',
  '#f97316',
  '#64748b',
];

const RADIUS = 42;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

@Component({
  selector: 'app-pie-chart',
  imports: [],
  templateUrl: './pie-chart.html',
  styleUrl: './pie-chart.css',
})
export class PieChart {
  readonly slices = input<PieSlice[]>([]);
  readonly totalLabel = input('Total');

  readonly total = computed(() =>
    this.slices().reduce((sum, slice) => sum + (Number(slice.value) || 0), 0),
  );

  protected readonly segments = computed(() => {
    const slices = this.slices();
    const total = Math.max(0, this.total());
    let cumulative = 0;
    return slices.map((slice, i) => {
      const value = Number(slice.value) || 0;
      const fraction = total > 0 ? value / total : 0;
      const start = cumulative;
      cumulative += fraction;
      return {
        label: slice.label,
        value,
        color: slice.color ?? PALETTE[i % PALETTE.length],
        percent: total > 0 ? (value / total) * 100 : 0,
        dashArray: `${Math.max(0, fraction * CIRCUMFERENCE)} ${CIRCUMFERENCE}`,
        dashOffset: total > 0 ? -start * CIRCUMFERENCE : -CIRCUMFERENCE,
      };
    });
  });

  protected formatPercent(percent: number): string {
    return `${percent.toFixed(1)}%`;
  }
}