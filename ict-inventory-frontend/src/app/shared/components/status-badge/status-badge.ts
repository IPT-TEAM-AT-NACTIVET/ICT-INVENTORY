import { Component, input } from '@angular/core';

@Component({
  selector: 'app-status-badge',
  imports: [],
  templateUrl: './status-badge.html',
  styleUrl: './status-badge.css',
})
export class StatusBadge {
  readonly label = input.required<string>();
  readonly tone = input<'neutral' | 'info' | 'success' | 'warning' | 'danger'>('neutral');
}