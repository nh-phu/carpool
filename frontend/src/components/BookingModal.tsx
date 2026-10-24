import { useState } from 'react'
import type { FormEvent } from 'react'
import type { Trip } from '../types/Trip'
import './BookingModal.css'

interface BookingModalProps {
  trip: Trip
  passengerId: number
  onClose: () => void
  onBookingSuccess: (seatsBooked: number) => void
}

interface BookingResponse {
  id: number
  passengerId: number
  tripId: number
  seats: number
  status: string
}

export default function BookingModal({
  trip,
  passengerId,
  onClose,
  onBookingSuccess
}: BookingModalProps) {
  const [seats, setSeats] = useState('1')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [booking, setBooking] = useState<BookingResponse | null>(null)

  const formatPrice = (price: number) =>
    new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
      maximumFractionDigits: 0,
    }).format(price)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setError('')

    const parsedSeats = Number(seats)

    if (
      seats.trim() === '' ||
      !Number.isInteger(parsedSeats) ||
      parsedSeats < 1 ||
      parsedSeats > trip.availableSeats
    ) {
      setError(
        `Enter a whole number between 1 and ${trip.availableSeats}.`,
      )
      return
    }

    setLoading(true)

    try {
      const response = await fetch('/api/bookings', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          passengerId,
          tripId: trip.id,
          seats: parsedSeats,
        }),
      })

      if (!response.ok) {
        const body = await response.text()
        let message = `Booking failed (${response.status}).`

        if (body) {
          try {
            const data = JSON.parse(body)
            message = data.message ?? data.error ?? message
          } catch {
            message = body
          }
        }

        throw new Error(message)
      }

      const data: BookingResponse = await response.json()
      setBooking(data)
      onBookingSuccess(parsedSeats)
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Something went wrong.',
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div
      className="booking-backdrop"
      onMouseDown={(event) => {
        if (event.target === event.currentTarget && !loading) onClose()
      }}
    >
      <section
        className="booking-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="booking-title"
      >
        <header className="booking-header">
          <div>
            <p className="booking-eyebrow">RESERVE YOUR SEAT</p>
            <h2 id="booking-title">
              {booking ? 'Booking confirmed' : 'Book a ride'}
            </h2>
          </div>

          <button
            className="booking-close"
            type="button"
            onClick={onClose}
            disabled={loading}
            aria-label="Close booking form"
          >
            ×
          </button>
        </header>

        {booking ? (
          <div className="booking-content">
            <div className="booking-success-icon" aria-hidden="true">
              ✓
            </div>
            <h3>Your seat is reserved!</h3>
            <p>
              Booking reference: <strong>#{booking.id}</strong>
            </p>
            <p>
              {booking.seats} seat{booking.seats === 1 ? '' : 's'} booked
              for {formatPrice(booking.seats * trip.priceSeat)}.
            </p>
            <p className="booking-muted">
              Keep your booking reference for future booking management.
            </p>
            <button
              className="booking-primary"
              type="button"
              onClick={onClose}
            >
              Done
            </button>
          </div>
        ) : (
          <form className="booking-content" onSubmit={handleSubmit}>
            <div className="booking-trip">
              <strong>{trip.start}</strong>
              <span aria-hidden="true">→</span>
              <strong>{trip.dest}</strong>
            </div>

            <div className="booking-summary">
              <div>
                <span>Price per seat</span>
                <strong>{formatPrice(trip.priceSeat)}</strong>
              </div>
              <div>
                <span>Seats available</span>
                <strong>{trip.availableSeats}</strong>
              </div>
            </div>

            <label className="booking-field">
              Number of seats
              <input
                type="number"
                min="1"
                max={trip.availableSeats}
                step="1"
                value={seats}
                onChange={(event) => {
                  setSeats(event.target.value)
                  setError('')
                }}
                placeholder="Enter number of seats"
                required
                disabled={trip.availableSeats < 1}
                aria-invalid={error !== ''}
                aria-describedby={error ? 'booking-seats-error' : undefined}
              />
            </label>

            <div className="booking-total">
              <span>Total</span>
              <strong>{formatPrice((Number(seats) || 0) * trip.priceSeat)}</strong>
            </div>

            {error && (
              <p id="booking-seats-error" className="booking-error" role="alert">
                {error}
              </p>
            )}

            <button
              className="booking-primary"
              type="submit"
              disabled={loading || trip.availableSeats < 1}
            >
              {loading
                ? 'Confirming...'
                : trip.availableSeats < 1
                  ? 'No seats available'
                  : 'Confirm booking'}
            </button>
          </form>
        )}
      </section>
    </div>
  )
}
