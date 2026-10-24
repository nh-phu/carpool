import type { User } from './User.ts'
export interface Trip {
  id: number
  driver: User
  start: string
  startLatitude: number | null
  startLongitude: number | null
  dest: string
  destLatitude: number | null
  destLongitude: number | null
  departureTime: string
  availableSeats: number
  priceSeat: number
}
