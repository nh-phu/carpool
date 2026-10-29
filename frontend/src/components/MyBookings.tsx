import { useEffect, useState } from 'react'
import type { Booking } from '../types/Booking'
import type { Trip } from '../types/Trip'
import { API_BASE } from '../api'
import TripDetailsModal from './TripDetailsModal'
import './MyBookings.css'

interface BookingWithTrip {
  booking: Booking
  trip: Trip | null
}

interface MyBookingsProps {
  passengerId: number
}

function formatPrice(price: number) {
  return `${price.toLocaleString('vi-VN')} ₫`
}

function formatTime(value: string) {
  return new Date(value).toLocaleString('en-GB', {
    weekday: 'short',
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

export default function MyBookings({ passengerId }: MyBookingsProps) {
  const [items, setItems] = useState<BookingWithTrip[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [refreshKey, setRefreshKey] = useState(0)
  const [cancellingId, setCancellingId] = useState<number | null>(null)
  const [bookingToCancel, setBookingToCancel] = useState<number | null>(null)
  const [selectedBooking, setSelectedBooking] = useState<{
    booking: Booking
    trip: Trip
  } | null>(null)

  async function handleCancel(bookingId: number) {
    setCancellingId(bookingId)
    setError('')

    try {
      const response = await fetch(`${API_BASE}/bookings/${bookingId}`, {
        method: 'DELETE',
      })

      if (!response.ok) {
        throw new Error('Failed to cancel booking.')
      }

      setBookingToCancel(null)
      setRefreshKey((previous) => previous + 1)
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Something went wrong.',
      )
    } finally {
      setCancellingId(null)
    }
  }

  useEffect(() => {
    const controller = new AbortController()

    async function loadBookings() {
      setLoading(true)
      setError('')

      try {
        const response = await fetch(
          `${API_BASE}/bookings/passenger/${passengerId}`,
          { signal: controller.signal },
        )

        if (!response.ok) {
          throw new Error('Could not load your bookings.')
        }

        const bookings: Booking[] = await response.json()

        const results = await Promise.all(
          bookings.map(async (booking) => {
            try {
              const tripResponse = await fetch(
                `${API_BASE}/trips/${booking.tripId}`,
                { signal: controller.signal },
              )

              if (!tripResponse.ok) {
                return { booking, trip: null }
              }

              const trip: Trip = await tripResponse.json()
              return { booking, trip }
            } catch (err) {
              if (err instanceof Error && err.name === 'AbortError') {
                throw err
              }

              return { booking, trip: null }
            }
          }),
        )

        setItems(results)
      } catch (err) {
        if (err instanceof Error && err.name === 'AbortError') {
          return
        }

        setError(
          err instanceof Error ? err.message : 'Something went wrong.',
        )
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false)
        }
      }
    }

    loadBookings()

    return () => controller.abort()
  }, [passengerId, refreshKey])

  if (loading) {
    return (
      <section className="my-bookings">
        <div className="bookings-heading">
          <p className="bookings-eyebrow">YOUR JOURNEYS</p>
          <h1>My bookings</h1>
          <p>Loading your reserved rides...</p>
        </div>
      </section>
    )
  }

  if (error) {
    return (
      <section className="my-bookings">
        <div className="bookings-heading">
          <p className="bookings-eyebrow">YOUR JOURNEYS</p>
          <h1>My bookings</h1>
        </div>
        <p className="bookings-error">{error}</p>
      </section>
    )
  }

  return (
    <section className="my-bookings">
      <div className="bookings-heading">
      </div>

      {items.length === 0 ? (
        <div className="bookings-empty">
          <div className="empty-icon">↗</div>
          <h2>No bookings yet</h2>
          <p>Find a ride and book your first seat.</p>
        </div>
      ) : (
        <div className="booking-list">
          {items.map(({ booking, trip }) => {
            const totalPrice = trip
              ? trip.priceSeat * booking.seats
              : null

            return (
              <article className="booking-card" key={booking.id}>
                <div className="booking-card-top">
                  <span className="booking-reference">
                    BOOKING #{booking.id}
                  </span>

                  <span
                    className={`booking-status booking-status-${booking.status.toLowerCase()}`}
                  >
                    <span className="status-dot" />
                    {booking.status}
                  </span>
                </div>

                {trip ? (
                  <>
                    <div className="booking-route">
                      <div className="route-marker">
                        <span className="route-dot" />
                        <span className="route-line" />
                        <span className="route-dot route-dot-end" />
                      </div>

                      <div className="route-locations">
                        <div className="route-location">
                          <span className="route-label">START</span>
                          <strong>{trip.start}</strong>
                        </div>

                        <div className="route-location">
                          <span className="route-label">DESTINATION</span>
                          <strong>{trip.dest}</strong>
                        </div>
                      </div>
                    </div>

                    <div className="booking-card-bottom">
                      <div className="booking-meta">
                        <div className="booking-meta-item">
                          <span className="meta-icon">◷</span>
                          <div>
                            <span className="meta-label">Departure</span>
                            <strong>{formatTime(trip.departureTime)}</strong>
                          </div>
                        </div>

                        <div className="booking-meta-item">
                          <span className="meta-icon">♙</span>
                          <div>
                            <span className="meta-label">Seats booked</span>
                            <strong>{booking.seats}</strong>
                          </div>
                        </div>
                      </div>

                      <div className="booking-price">
                        <span className="meta-label">Total price</span>
                        <strong>
                          {totalPrice !== null
                            ? formatPrice(totalPrice)
                            : 'Unavailable'}
                        </strong>
                      </div>
                    </div>
                    <div className="booking-actions">
                      {trip && (
                        <button
                          className="details-button"
                          type="button"
                          onClick={() => setSelectedBooking({ booking, trip })}
                        >
                          View details
                        </button>
                      )}
                      {booking.status === 'CONFIRMED' && (
                        <button
                          className="cancel-booking-button"
                          type="button"
                          onClick={() => setBookingToCancel(booking.id)}
                        >
                          {cancellingId === booking.id
                            ? 'Cancelling...'
                            : 'Cancel booking'}
                        </button>
                      )}
                    </div>
                  </>
                ) : (
                  <p className="booking-unavailable">
                    Trip details are no longer available.
                  </p>
                )}
              </article>
            )
          })}
        </div>
      )}

    {selectedBooking && (
      <TripDetailsModal
        trip={selectedBooking.trip}
        onClose={() => setSelectedBooking(null)}
        actionLabel={
          selectedBooking.booking.status === 'CONFIRMED'
            ? 'Cancel booking'
            : 'Close'
        }
        onAction={
          selectedBooking.booking.status === 'CONFIRMED'
            ? () => {
                setBookingToCancel(selectedBooking.booking.id)
                setSelectedBooking(null)
              }
            : undefined
        }
      />
    )}
    {bookingToCancel !== null && (
      <div
        className="cancel-modal-overlay"
        onMouseDown={(event) => {
          if (event.target === event.currentTarget && cancellingId === null) {
            setBookingToCancel(null)
          }
        }}
      >
        <section
          className="cancel-modal"
          role="dialog"
          aria-modal="true"
          aria-labelledby="cancel-modal-title"
          aria-describedby="cancel-modal-description"
        >
          <div className="cancel-modal-icon" aria-hidden="true">
            !
          </div>

          <h2 id="cancel-modal-title">Cancel this booking?</h2>

          <p id="cancel-modal-description">
            Are you sure you want to cancel your seat? This action cannot
            be undone.
          </p>

          {error && <p className="bookings-error">{error}</p>}

          <div className="cancel-modal-actions">
            <button
              className="keep-booking-button"
              type="button"
              disabled={cancellingId !== null}
              onClick={() => setBookingToCancel(null)}
            >
              Keep booking
            </button>

            <button
              className="confirm-cancel-button"
              type="button"
              disabled={cancellingId !== null}
              onClick={() => handleCancel(bookingToCancel)}
            >
              {cancellingId === bookingToCancel
                ? 'Cancelling...'
                : 'Yes, cancel'}
            </button>
          </div>
        </section>
      </div>
    )}
    </section>
  )
}
