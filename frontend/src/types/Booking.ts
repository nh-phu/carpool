export interface Booking {
  id: number
  passengerId: number
  tripId: number
  seats: number
  status: 'CONFIRMED' | 'CANCELLED'
}
