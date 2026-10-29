import { useEffect, useState } from 'react'
import type { User } from './Login'
import type { Trip } from '../types/Trip'
import { API_BASE } from '../api'
import MyBookings from './MyBookings'
import TripDetailsModal from './TripDetailsModal'
import './MyAccount.css'

interface MyAccountProps {
  user: User
}

type AccountTab = 'bookings' | 'trips'

function formatPrice(price: number) {
  return `${price.toLocaleString('vi-VN')} ₫`
}

function formatTime(value: string) {
  return new Date(value).toLocaleString('en-GB', {
    weekday: 'short',
    day: '2-digit',
    month: 'short',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

export default function MyAccount({ user }: MyAccountProps) {
  const [activeTab, setActiveTab] = useState<AccountTab>('bookings')
  const [trips, setTrips] = useState<Trip[]>([])
  const [loadingTrips, setLoadingTrips] = useState(false)
  const [tripsError, setTripsError] = useState('')
  const [selectedTrip, setSelectedTrip] = useState<Trip | null>(null)

  useEffect(() => {
    if (activeTab !== 'trips') return

    const controller = new AbortController()

    async function loadMyTrips() {
      setLoadingTrips(true)
      setTripsError('')

      try {
        const response = await fetch(`${API_BASE}/trips`, {
          signal: controller.signal,
        })

        if (!response.ok) {
          throw new Error('Could not load your trips.')
        }

        const data: Trip[] = await response.json()

        setTrips(data.filter((trip) => trip.driver?.id === user.id))
      } catch (error) {
        if (error instanceof Error && error.name !== 'AbortError') {
          setTripsError(error.message)
        }
      } finally {
        if (!controller.signal.aborted) {
          setLoadingTrips(false)
        }
      }
    }

    void loadMyTrips()

    return () => controller.abort()
  }, [activeTab, user.id])

  return (
    <section className="my-account">
      <header className="account-profile">
        <div className="account-avatar" aria-hidden="true">
          {user.name.charAt(0).toUpperCase()}
        </div>

        <div className="account-profile-info">
          <p className="account-eyebrow">MY ACCOUNT</p>
          <h1>{user.name}</h1>
          <p className="account-phone">
            {user.phoneNumber || 'No phone number provided'}
          </p>
          <span className="account-member-label">Carpool member</span>
        </div>
      </header>

      <div className="account-tabs" role="tablist" aria-label="Account sections">
        <button
          className={`account-tab ${activeTab === 'bookings' ? 'active' : ''}`}
          id="account-bookings-tab"
          type="button"
          role="tab"
          aria-selected={activeTab === 'bookings'}
          aria-controls="account-tab-panel"
          onClick={() => setActiveTab('bookings')}
        >
          My Bookings
        </button>

        <button
          className={`account-tab ${activeTab === 'trips' ? 'active' : ''}`}
          id="account-trips-tab"
          type="button"
          role="tab"
          aria-selected={activeTab === 'trips'}
          aria-controls="account-tab-panel"
          onClick={() => setActiveTab('trips')}
        >
          My Trips
        </button>
      </div>

      <div
        className="account-tab-panel"
        id="account-tab-panel"
        role="tabpanel"
        aria-labelledby={
          activeTab === 'bookings'
            ? 'account-bookings-tab'
            : 'account-trips-tab'
        }
      >
        {activeTab === 'bookings' && (
          <MyBookings passengerId={user.id} />
        )}

        {activeTab === 'trips' && (
          <section className="account-trips">
            <div className="account-section-heading">
            </div>

            {loadingTrips && <p>Loading your trips...</p>}

            {tripsError && (
              <p className="account-trips-error" role="alert">
                {tripsError}
              </p>
            )}

            {!loadingTrips && !tripsError && trips.length === 0 && (
              <div className="account-trips-empty">
                <h3>No trips yet</h3>
                <p>Offer a ride to share your journey with others.</p>
              </div>
            )}

            {!loadingTrips && !tripsError && trips.length > 0 && (
              <div className="account-trip-list">
              {trips.map((trip) => (
                <article className="account-trip-card" key={trip.id}>
                  <div className="account-trip-card-top">
                    <span className="booking-reference">
                      TRIP #{trip.id}
                    </span>

                    <span
                      className={`booking-status ${
                        trip.availableSeats > 0
                          ? 'booking-status-confirmed'
                          : 'booking-status-full'
                      }`}
                    >
                      <span className="status-dot" />
                      {trip.availableSeats > 0 ? 'AVAILABLE' : 'FULLY BOOKED'}
                    </span>
                  </div>

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
                        <span className="meta-icon" aria-hidden="true">◷</span>
                        <div>
                          <span className="meta-label">Departure</span>
                          <strong>{formatTime(trip.departureTime)}</strong>
                        </div>
                      </div>

                      <div className="booking-meta-item">
                        <span className="meta-icon" aria-hidden="true">♙</span>
                        <div>
                          <span className="meta-label">Available seats</span>
                          <strong>{trip.availableSeats}</strong>
                        </div>
                      </div>
                    </div>

                    <div className="booking-price">
                      <span className="meta-label">Price per seat</span>
                      <strong>{formatPrice(trip.priceSeat)}</strong>
                    </div>
                  </div>

                  <div className="booking-actions">
                    <button
                      className="details-button"
                      type="button"
                      onClick={() => setSelectedTrip(trip)}
                    >
                      View details
                    </button>

                    <button
                      className="cancel-booking-button"
                      type="button"
                      onClick={() => {
                        // We'll implement trip deletion with confirmation next.
                      }}
                    >
                      Delete trip
                    </button>
                  </div>
                </article>
              ))}
              </div>
            )}
          </section>
        )}
      </div>
      {selectedTrip && (
        <TripDetailsModal
          trip={selectedTrip}
          onClose={() => setSelectedTrip(null)}
          actionLabel="Close"
        />
      )}
    </section>
  )
}
